package com.example.fontslibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fontslibrary.data.FontUI
import com.example.fontslibrary.screens.FontsViewModel
import com.example.fontslibrary.ui.theme.FontsLibraryTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FontsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FontsLibraryTheme {
                FontsApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontsApp(viewModel: FontsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Replace with your actual API key
    val apiKey = "AIzaSyCUPAf_lzuijwG_0SLDQy8XfYAxPJcDLYM"

    LaunchedEffect(Unit) {
        if (uiState.allFonts.isEmpty()) {
            viewModel.loadFonts(apiKey)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::updateSearchQuery,
            previewText = uiState.previewText,
            onPreviewTextChange = viewModel::updatePreviewText
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filters Row
        FiltersRow(
            selectedCategory = uiState.selectedCategory,
            onCategoryChange = viewModel::updateCategory,
            sortOrder = uiState.sortOrder,
            onSortChange = { sort -> viewModel.updateSortOrder(sort, apiKey) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Preview Card
        PreviewCard(
            previewText = uiState.previewText,
            fontFamily = uiState.selectedFontFamily,
            selectedFont = uiState.selectedFont
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Loading indicator
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Error message
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Fonts List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.filteredFonts) { font ->
                FontCard(
                    font = font,
                    onClick = {
                        viewModel.downloadAndApplyFont(apiKey, font)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    previewText: String,
    onPreviewTextChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search font families...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = previewText,
            onValueChange = onPreviewTextChange,
            label = { Text("Preview text") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun FiltersRow(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    sortOrder: String,
    onSortChange: (String) -> Unit
) {
    val categories = listOf("All", "serif", "sans-serif", "display", "handwriting", "monospace")
    val sortOptions = listOf("popularity", "alpha", "date", "trending")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Chips
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categories.take(3).forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategoryChange(category) },
                    label = { Text(category, fontSize = 12.sp) }
                )
            }
        }

        // Sort Dropdown
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(
                onClick = { expanded = true }
            ) {
                Text("Sort: $sortOrder", fontSize = 12.sp)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                sortOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSortChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PreviewCard(
    previewText: String,
    fontFamily: FontFamily?,
    selectedFont: FontUI?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = previewText,
                style = TextStyle(
                    fontFamily = fontFamily ?: FontFamily.Default,
                    fontSize = 20.sp,
                    lineHeight = 28.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            selectedFont?.let { font ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Font: ${font.family} (${font.category})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FontCard(
    font: FontUI,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = font.family,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = font.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (font.variants.size > 1) {
                    Text(
                        text = "${font.variants.size} variants",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            OutlinedButton(onClick = onClick) {
                Text("Apply", fontSize = 12.sp)
            }
        }
    }
}

