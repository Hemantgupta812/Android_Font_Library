package com.example.fontslibrary.repo

import android.content.Context
import android.util.Log
import com.example.fontslibrary.data.FontItem
import com.example.fontslibrary.data.FontUI
import com.example.fontslibrary.di.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

class FontsRepository(private val context: Context) {
    
    private val api = NetworkModule.api
    private val httpClient = OkHttpClient()
    
    companion object {
        private const val TAG = "FontsRepository"
    }
    
    suspend fun fetchFonts(
        apiKey: String, 
        sort: String? = null,
        category: String? = null
    ): List<FontUI> = withContext(Dispatchers.IO) {
        try {
            val response = api.getFonts(
                apiKey = apiKey,
                sort = sort,
                category = if (category == "All") null else category
            )
            response.items.map { it.toUI() }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching fonts", e)
            emptyList()
        }
    }
    
    private fun FontItem.toUI(): FontUI {
        return FontUI(
            family = family,
            category = category,
            regularUrl = files["regular"] ?: files.values.firstOrNull(),
            variants = files,
            subsets = subsets,
            lastModified = lastModified
        )
    }
    
    /**
     * Downloads a font file to the app's cache directory
     * @param family Font family name
     * @param variant Font variant (e.g., "regular", "700", "italic")
     * @param url Direct URL to the font file
     * @return File object pointing to the cached font file
     */
    suspend fun downloadFontToCache(
        family: String, 
        variant: String, 
        url: String
    ): File = withContext(Dispatchers.IO) {
        
        // Create fonts directory in cache
        val fontsDir = File(context.cacheDir, "fonts").apply { 
            if (!exists()) mkdirs() 
        }
        
        // Create safe filename
        val safeFamily = family.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val safeVariant = variant.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val targetFile = File(fontsDir, "${safeFamily}_${safeVariant}.ttf")
        
        // Return existing file if it exists and is not empty
        if (targetFile.exists() && targetFile.length() > 0) {
            Log.d(TAG, "Font already cached: ${targetFile.name}")
            return@withContext targetFile
        }
        
        try {
            // Download the font
            val request = Request.Builder()
                .url(url)
                .build()
            
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP error: ${response.code}")
                }
                
                response.body?.let { body ->
                    targetFile.outputStream().use { fileOut ->
                        body.byteStream().copyTo(fileOut)
                    }
                    Log.d(TAG, "Font downloaded: ${targetFile.name} (${targetFile.length()} bytes)")
                } ?: throw IOException("Response body is null")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading font: $family-$variant", e)
            // Clean up partial file
            if (targetFile.exists()) targetFile.delete()
            throw e
        }
        
        targetFile
    }
    
    fun getCachedFonts(): List<File> {
        val fontsDir = File(context.cacheDir, "fonts")
        return if (fontsDir.exists()) {
            fontsDir.listFiles()?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    fun clearCache() {
        val fontsDir = File(context.cacheDir, "fonts")
        if (fontsDir.exists()) {
            fontsDir.deleteRecursively()
        }
    }
}