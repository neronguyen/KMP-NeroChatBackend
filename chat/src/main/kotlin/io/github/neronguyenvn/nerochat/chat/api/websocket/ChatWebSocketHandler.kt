package io.github.neronguyenvn.nerochat.chat.api.websocket

import io.github.neronguyenvn.nerochat.chat.service.ChatService
import io.github.neronguyenvn.nerochat.domain.type.ChatId
import io.github.neronguyenvn.nerochat.domain.type.UserId
import io.github.neronguyenvn.nerochat.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class ChatWebSocketHandler(
    private val chatService: ChatService,
    private val jwtService: JwtService
) : TextWebSocketHandler() {

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


    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession
    )
}
