package com.example.fontslibrary.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoogleFontsResponse(
    @Json(name = "items") val items: List<FontItem>,
    @Json(name = "kind") val kind: String
)