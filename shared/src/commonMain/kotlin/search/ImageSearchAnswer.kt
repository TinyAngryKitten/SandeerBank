package search

import kotlinx.serialization.Serializable

@Serializable
data class ImageSearchAnswer(
    val value: List<ImageSearchResult>,
    val readLink: String,
    val webSearchUrl: String
)