package com.example.fontslibrary.data

data class FontUI(
    val family: String,
    val category: String,
    val regularUrl: String?,
    val variants: Map<String, String>,
    val subsets: List<String> = emptyList(),
    val lastModified: String? = null
)