package io.github.neronguyenvn.nerochat.chat.api.websocket

import io.github.neronguyenvn.nerochat.chat.api.model.asDto
import io.github.neronguyenvn.nerochat.chat.api.model.websocket.IncomingWsMessage
import io.github.neronguyenvn.nerochat.chat.api.model.websocket.OutgoingWsMessage
import io.github.neronguyenvn.nerochat.chat.domain.event.InternalChatEvent
import io.github.neronguyenvn.nerochat.chat.service.ChatMessageService
import io.github.neronguyenvn.nerochat.chat.service.ChatRoomService
import io.github.neronguyenvn.nerochat.domain.type.ChatRoomId
import io.github.neronguyenvn.nerochat.domain.type.UserId
import io.github.neronguyenvn.nerochat.service.JwtService
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Component
class ChatWebSocketHandler(
    private val chatRoomService: ChatRoomService,
    private val chatMessageService: ChatMessageService,
    private val jwtService: JwtService,
    private val json: Json = defaultJson
) : TextWebSocketHandler() {

    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession,
        var lastPongTimestamp: AtomicLong = AtomicLong(System.currentTimeMillis())
    )

    private val logger = LoggerFactory.getLogger(javaClass)

    private val sessionsById = ConcurrentHashMap<String, UserSession>()
    private val sessionIdsByUserId = ConcurrentHashMap<UserId, MutableSet<String>>()
    private val chatRoomIdsByUserId = ConcurrentHashMap<UserId, MutableSet<ChatRoomId>>()
    private val sessionIdsByChatRoomId = ConcurrentHashMap<ChatRoomId, MutableSet<String>>()


    override fun afterConnectionEstablished(session: WebSocketSession) {
        val authHeader = session
            .handshakeHeaders
            .getFirst(HttpHeaders.AUTHORIZATION)

        if (authHeader == null) {
            logger.warn("Session ${session.id} was closed due to missing Authorization header")
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Authentication failed"))
            return
        }

        if (!jwtService.validateAccessToken(token = authHeader)) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Authentication failed"))
            return
        }

        val userId = jwtService.getUserIdFromToken(token = authHeader)

        // TODO: Wrap WebSocketSession with ConcurrentWebSocketSessionDecorator in afterConnectionEstablished
        // to prevent IllegalStateException from concurrent writes across ping scheduler, broadcast threads, and event listeners.
        val userSession = UserSession(
            userId = userId,
            session = session
        )

        sessionsById[session.id] = userSession

        sessionIdsByUserId
            .computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }
            .add(session.id)

        val chatRoomIds = chatRoomIdsByUserId.computeIfAbsent(userId) {
            val userChatRooms = chatRoomService.findChatRoomsByUser(userId = userId).map { it.id }
            ConcurrentHashMap.newKeySet<ChatRoomId>().apply { addAll(userChatRooms) }
        }

        chatRoomIds.forEach { chatRoomId ->
            sessionIdsByChatRoomId
                .computeIfAbsent(chatRoomId) { ConcurrentHashMap.newKeySet() }
                .add(session.id)
        }

        logger.info("Websocket connection established for user $userId")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val userSession = sessionsById.remove(session.id) ?: return
        val userId = userSession.userId

        chatRoomIdsByUserId[userId]?.forEach { chatRoomId ->
            sessionIdsByChatRoomId[chatRoomId]?.remove(session.id)
        }

        sessionIdsByUserId[userId]?.let { sessions ->
            sessions.remove(session.id)
            if (sessions.isEmpty()) {
                sessionIdsByUserId.remove(userId)
                chatRoomIdsByUserId.remove(userId)
            }
        }

        logger.info("WebSocket closed: user={}, session={}, status={}", userId, session.id, status)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("Transport error on session {}", session.id, exception)
        if (!session.isOpen) return

        runCatching {
            session.close(CloseStatus.SERVER_ERROR.withReason("Transport error"))
        }
    }

    @Scheduled(fixedDelay = PING_INTERVAL_MS)
    private fun pingClients() {
        val currentTime = System.currentTimeMillis()
        val sessionsToClose = mutableListOf<String>()

        sessionsById.entries.forEach { (sessionId, userSession) ->
            try {
                if (userSession.session.isOpen) {
                    val lastPong = userSession.lastPongTimestamp.get()
                    if (currentTime - lastPong > PONG_TIMEOUT_MS) {
                        logger.warn("Session $sessionId has timed out, closing connection.")
                        sessionsToClose.add(sessionId)
                        return@forEach
                    }

                    userSession.session.sendMessage(PingMessage())
                    logger.debug("Sent ping to {}", userSession.userId)
                }
            } catch (e: Exception) {
                logger.error("Could not ping session $sessionId", e)
                sessionsToClose.add(sessionId)
            }
        }

        sessionsToClose.forEach { sessionId ->
            sessionsById[sessionId]?.session?.let { session ->
                try {
                    session.close(CloseStatus.SESSION_NOT_RELIABLE.withReason("Ping timeout"))
                } catch (e: Exception) {
                    logger.error("Couldn't close sessions for session ${session.id}", e)
                }
            }
        }
    }

    override fun handlePongMessage(session: WebSocketSession, message: PongMessage) {
        sessionsById[session.id]?.lastPongTimestamp?.set(System.currentTimeMillis())
        logger.debug("Received pong from ${session.id}")
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
            // TODO: Handle specific errors
        } catch (e: Exception) {
            logger.warn("Payload decode error from session ${session.id}: ${e.message}")
            val outgoing = OutgoingWsMessage.Error(
                code = "INVALID_JSON",
                message = "Malformed payload"
            )
            val payload = TextMessage(json.encodeToString(outgoing))
            userSession.sendMessage(payload)
        }
    }

    private fun handleIncomingNewMessage(
        senderId: UserId,
        incoming: IncomingWsMessage.NewMessage,
    ) {
        val allowedChatRooms = chatRoomIdsByUserId[senderId] ?: return
        if (incoming.chatRoomId !in allowedChatRooms) return

        val savedMessage = chatMessageService.sendMessage(
            chatRoomId = incoming.chatRoomId,
            senderId = senderId,
            content = incoming.content,
            messageId = incoming.messageId
        )

        broadcastToChatRoom(
            chatRoomId = incoming.chatRoomId,
            outgoing = OutgoingWsMessage.NewMessage(savedMessage.asDto())
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    private fun onDeleteMessage(event: InternalChatEvent.MessageDeletedEvent) {
        broadcastToChatRoom(
            chatRoomId = event.chatRoomId,
            outgoing = OutgoingWsMessage.MessageDeleted(
                chatRoomId = event.chatRoomId,
                messageId = event.messageId
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    private fun onJoinChatRoom(event: InternalChatEvent.ChatParticipantJoinedEvent) {
        event.newUsers.forEach { user ->
            chatRoomIdsByUserId
                .computeIfAbsent(user.userId) { ConcurrentHashMap.newKeySet() }
                .add(event.chatRoomId)

            sessionIdsByUserId[user.userId]?.let { sessions ->
                sessionIdsByChatRoomId
                    .computeIfAbsent(event.chatRoomId) { ConcurrentHashMap.newKeySet() }
                    .addAll(sessions)
            }
        }

        broadcastToChatRoom(
            chatRoomId = event.chatRoomId,
            outgoing = OutgoingWsMessage.ParticipantJoined(
                chatRoomId = event.chatRoomId,
                newUsers = event.newUsers.map { it.asDto() },
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    private fun onLeftChatRoom(event: InternalChatEvent.ChatParticipantLeftEvent) {
        broadcastToChatRoom(
            chatRoomId = event.chatRoomId,
            outgoing = OutgoingWsMessage.ParticipantLeft(
                chatRoomId = event.chatRoomId,
                leftUser = event.leftUser.asDto(),
            )
        )

        val leftUserId = event.leftUser.userId
        chatRoomIdsByUserId[leftUserId]?.remove(event.chatRoomId)
        sessionIdsByUserId[leftUserId]?.forEach { sessionId ->
            sessionIdsByChatRoomId[event.chatRoomId]?.remove(sessionId)
        }
    }

    private fun broadcastToChatRoom(
        chatRoomId: ChatRoomId,
        outgoing: OutgoingWsMessage
    ) {
        val sessionIds = sessionIdsByChatRoomId[chatRoomId] ?: return
        val payload = TextMessage(json.encodeToString(outgoing))

        sessionIds.forEach { sessionId ->
            val session = sessionsById[sessionId] ?: return@forEach
            val chatRooms = chatRoomIdsByUserId[session.userId]
            if (chatRooms == null || chatRoomId !in chatRooms) {
                sessionIds.remove(sessionId)
                return@forEach
            }

            session.sendMessage(message = payload)
        }
    }

    private fun UserSession.sendMessage(message: TextMessage) {
        if (!session.isOpen) return

        try {
            session.sendMessage(message)
            logger.debug("Sent message to user {}: {}", userId, message)
        } catch (e: Exception) {
            logger.error("Error while sending message to $userId", e)
        }
    }

    companion object {
        private const val PING_INTERVAL_MS = 30_000L
        private const val PONG_TIMEOUT_MS = 30_000L

        private const val EVENT_TYPE = "type"
        private val defaultJson = Json {
            classDiscriminator = EVENT_TYPE
        }
    }
}
