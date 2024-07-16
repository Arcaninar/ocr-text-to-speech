package com.ocrtts.type

import kotlinx.serialization.Serializable

@Serializable
data class OnlineOCRRequest(
    val requests: List<ImageRequest>
)

@Serializable
data class ImageRequest(
    val image: ImageBase64,
    val features: List<AnalysisFeature>
)

@Serializable
data class ImageBase64(
    val content: String
)

@Serializable
data class AnalysisFeature(
    val type: String,
    val maxResults: Int
)