package com.example.fontslibrary.screens

import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import com.example.fontslibrary.data.FontUI
import java.io.File

data class FontsUiState(
    val isLoading: Boolean = false,
    val allFonts: List<FontUI> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val sortOrder: String = "popularity",
    val previewText: String = "The quick brown fox jumps over the lazy dog",
    val selectedFont: FontUI? = null,
    val selectedFontFile: File? = null,
    val selectedTypeface: Typeface? = null,
    val selectedFontFamily: FontFamily? = null,
    val error: String? = null
) {
    val filteredFonts: List<FontUI>
        get() {
            val query = searchQuery.trim().lowercase()
            return allFonts
                .filter { font ->
                    if (selectedCategory == "All") true 
                    else font.category.equals(selectedCategory, ignoreCase = true)
                }
                .filter { font ->
                    if (query.isBlank()) true 
                    else font.family.lowercase().contains(query)
                }
        }
}