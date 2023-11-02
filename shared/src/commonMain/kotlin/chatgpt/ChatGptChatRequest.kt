package chatgpt

import kotlinx.serialization.Serializable

@Serializable
data class ChatGptChatRequest(
    val messages: List<ChatGptChatMessage>,
    val model: String
)

@Serializable
enum class ChatGptChatRole {
    user,
    assistant
}

@Serializable
data class ChatGptChatMessage(
    val role: ChatGptChatRole,
    val content: String
)