package io.github.neronguyenvn.nerochat.chat.api.websocket

import io.github.neronguyenvn.nerochat.chat.api.model.asDto
import io.github.neronguyenvn.nerochat.chat.api.model.websocket.IncomingWsMessage
import io.github.neronguyenvn.nerochat.chat.api.model.websocket.OutgoingWsMessage
import io.github.neronguyenvn.nerochat.chat.domain.event.InternalChatEvent
import io.github.neronguyenvn.nerochat.chat.service.ChatMessageService
import io.github.neronguyenvn.nerochat.chat.service.ChatService
import io.github.neronguyenvn.nerochat.domain.type.ChatId
import io.github.neronguyenvn.nerochat.domain.type.UserId
import io.github.neronguyenvn.nerochat.service.JwtService
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class ChatWebSocketHandler(
    private val chatService: ChatService,
    private val chatMessageService: ChatMessageService,
    private val jwtService: JwtService,
    private val json: Json = defaultJson
) : TextWebSocketHandler() {

    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession
    )

    private val logger = LoggerFactory.getLogger(javaClass)

    private val sessionsById = ConcurrentHashMap<String, UserSession>()
    private val sessionIdsByUserId = ConcurrentHashMap<UserId, MutableSet<String>>()
    private val chatIdsByUserId = ConcurrentHashMap<UserId, MutableSet<ChatId>>()
    private val sessionIdsByChatId = ConcurrentHashMap<ChatId, MutableSet<String>>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val authHeader = session
            .handshakeHeaders
            .getFirst(HttpHeaders.AUTHORIZATION)

        if (authHeader == null) {
            logger.warn("Session ${session.id} was closed due to missing Authorization header")
            session.close(CloseStatus.SERVER_ERROR.withReason("Authentication failed"))
            return
        }

        val userId = jwtService.getUserIdFromToken(token = authHeader)
        val userSession = UserSession(
            userId = userId,
            session = session
        )

        sessionsById[session.id] = userSession

        sessionIdsByUserId
            .computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }
            .add(session.id)

        val chatIds = chatIdsByUserId.computeIfAbsent(userId) {
            val userChats = chatService.findChatsByUser(userId = userId).map { it.id }
            ConcurrentHashMap.newKeySet<ChatId>().apply { addAll(userChats) }
        }

        chatIds.forEach { chatId ->
            sessionIdsByChatId
                .computeIfAbsent(chatId) { ConcurrentHashMap.newKeySet() }
                .add(session.id)
        }

        logger.info("Websocket connection established for user $userId")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.debug("Received message ${message.payload}")
        val userSession = sessionsById[session.id] ?: return

        try {
            when (val incoming = json.decodeFromString<IncomingWsMessage>(message.payload)) {
                is IncomingWsMessage.NewMessage -> {
                    handleIncomingNewMessage(
                        senderId = userSession.userId,
                        incoming = incoming
                    )
                }
            }
        } catch (e: Exception) {
            logger.warn("Payload decode error from session ${session.id}: ${e.message}")
            userSession.sendMessage(
                outgoing = OutgoingWsMessage.Error(
                    code = "INVALID_JSON",
                    message = "Malformed payload"
                )
            )
        }
    }

    private fun handleIncomingNewMessage(
        senderId: UserId,
        incoming: IncomingWsMessage.NewMessage,
    ) {
        val allowedChats = chatIdsByUserId[senderId] ?: return
        if (incoming.chatId !in allowedChats) return

        val savedMessage = chatMessageService.sendMessage(
            chatId = incoming.chatId,
            senderId = senderId,
            content = incoming.content,
            messageId = incoming.messageId
        )

        broadcastToChat(
            chatId = incoming.chatId,
            outgoing = OutgoingWsMessage.NewMessage(savedMessage.asDto())
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    private fun onDeleteMessage(event: InternalChatEvent.MessageDeletedEvent) {
        broadcastToChat(
            chatId = event.chatId,
            outgoing = OutgoingWsMessage.MessageDeleted(
                chatId = event.chatId,
                messageId = event.messageId
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    private fun onJoinChat(event: InternalChatEvent.ChatParticipantJoinedEvent) {
        event.newUsers.forEach { user ->
            chatIdsByUserId
                .computeIfAbsent(user.userId) { ConcurrentHashMap.newKeySet() }
                .add(event.chatId)

            sessionIdsByUserId[user.userId]?.let { sessions ->
                sessionIdsByChatId
                    .computeIfAbsent(event.chatId) { ConcurrentHashMap.newKeySet() }
                    .addAll(sessions)
            }
        }

        broadcastToChat(
            chatId = event.chatId,
            outgoing = OutgoingWsMessage.ParticipantJoined(
                chatId = event.chatId,
                newUsers = event.newUsers.map { it.asDto() },
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    private fun onLeftChat(event: InternalChatEvent.ChatParticipantLeftEvent) {
        broadcastToChat(
            chatId = event.chatId,
            outgoing = OutgoingWsMessage.ParticipantLeft(
                chatId = event.chatId,
                leftUser = event.leftUser.asDto(),
            )
        )

        val leftUserId = event.leftUser.userId
        chatIdsByUserId[leftUserId]?.remove(event.chatId)
        sessionIdsByUserId[leftUserId]?.forEach { sessionId ->
            sessionIdsByChatId[event.chatId]?.remove(sessionId)
        }
    }

    private fun broadcastToChat(
        chatId: ChatId,
        outgoing: OutgoingWsMessage
    ) {
        val sessionIds = sessionIdsByChatId[chatId] ?: return
        sessionIds.forEach {
            sessionsById[it]?.sendMessage(outgoing = outgoing)
        }
    }

    private fun UserSession.sendMessage(outgoing: OutgoingWsMessage) {
        if (session.isOpen) {
            try {
                val payload = TextMessage(json.encodeToString(outgoing))
                session.sendMessage(payload)
                logger.debug("Sent message to user {}: {}", userId, payload)
            } catch (e: Exception) {
                logger.error("Error while sending message to $userId", e)

            }
        }
    }

    private companion object {
        const val EVENT_TYPE = "type"

        val defaultJson = Json {
            classDiscriminator = EVENT_TYPE
        }
    }
}
