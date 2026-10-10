package io.github.neronguyenvn.nerochat.chat.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ChatRoomType {
    DIRECT,
    GROUP
}
