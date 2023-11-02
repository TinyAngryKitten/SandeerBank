package search

import kotlinx.serialization.Serializable

@Serializable
data class ImageSearchResult(
    val contentUrl: String,
    val imageId: String,
    val encodingFormat: String,
    val height: Int,
    val width: Int,
    val thumbnailUrl: String,
)