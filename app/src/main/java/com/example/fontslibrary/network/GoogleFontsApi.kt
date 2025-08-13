package com.example.fontslibrary.network

import com.example.fontslibrary.data.GoogleFontsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleFontsApi {
    
    @GET("webfonts")
    suspend fun getFonts(
        @Query("key") apiKey: String,
        @Query("sort") sort: String? = null,
        @Query("family") family: String? = null,
        @Query("category") category: String? = null,
        @Query("subset") subset: String? = null,
        @Query("capability") capability: String? = null
    ): GoogleFontsResponse
    
    companion object {
        const val BASE_URL = "https://www.googleapis.com/webfonts/v1/"
    }
}