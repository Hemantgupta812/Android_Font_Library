package com.example.fontslibrary.screens

import android.app.Application
import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fontslibrary.data.FontUI
import com.example.fontslibrary.repo.FontsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FontsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = FontsRepository(application)
    
    private val _uiState = MutableStateFlow(FontsUiState())
    val uiState: StateFlow<FontsUiState> = _uiState.asStateFlow()
    
    fun loadFonts(apiKey: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val fonts = repository.fetchFonts(
                    apiKey = apiKey,
                    sort = _uiState.value.sortOrder,
                    category = _uiState.value.selectedCategory
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allFonts = fonts,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load fonts: ${e.message}"
                )
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    fun updateCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
    
    fun updateSortOrder(sort: String, apiKey: String) {
        _uiState.value = _uiState.value.copy(sortOrder = sort)
        loadFonts(apiKey) // Reload with new sort order
    }
    
    fun updatePreviewText(text: String) {
        _uiState.value = _uiState.value.copy(previewText = text)
    }
    
    fun downloadAndApplyFont(apiKey: String, font: FontUI, variant: String = "regular") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = font.variants[variant] ?: font.regularUrl ?: return@launch
                
                val fontFile = repository.downloadFontToCache(
                    family = font.family,
                    variant = variant,
                    url = url
                )
                
                // Create typeface from file
                val typeface = Typeface.createFromFile(fontFile)
                val fontFamily = FontFamily(typeface)
                
                _uiState.value = _uiState.value.copy(
                    selectedFont = font,
                    selectedFontFile = fontFile,
                    selectedTypeface = typeface,
                    selectedFontFamily = fontFamily
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to download font: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearCache() {
        repository.clearCache()
        _uiState.value = _uiState.value.copy(
            selectedFont = null,
            selectedFontFile = null,
            selectedTypeface = null,
            selectedFontFamily = null
        )
    }
}