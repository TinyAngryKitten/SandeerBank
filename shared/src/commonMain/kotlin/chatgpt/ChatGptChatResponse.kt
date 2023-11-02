package chatgpt

import kotlinx.serialization.Serializable

@Serializable
data class ChatGptChatResponse(
    val id: String,
    val created: String,
    val model: String,
    val usage: ChatGptUsage,
    val choices: List<ChatGptChatChoice>,
)

@Serializable
data class ChatGptChatChoice(
    val message: ChatGptChatMessage,
    val finish_reason: String,
    val index: Int
)

@Serializable
data class ChatGptUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int,
)