package com.example.fontslibrary.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FontItem(
    @Json(name = "kind") val kind: String? = null,
    @Json(name = "family") val family: String,
    @Json(name = "category") val category: String, // serif, sans-serif, display, handwriting, monospace
    @Json(name = "variants") val variants: List<String> = emptyList(),
    @Json(name = "subsets") val subsets: List<String> = emptyList(),
    @Json(name = "version") val version: String? = null,
    @Json(name = "lastModified") val lastModified: String? = null,
    @Json(name = "files") val files: Map<String, String> = emptyMap(), // variant -> url
    @Json(name = "menu") val menu: String? = null
)