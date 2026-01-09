package com.example.bookrly.data.api

import com.example.bookrly.data.model.SearchResponse
import com.example.bookrly.data.model.SubjectResponse
import com.example.bookrly.data.model.WorkDetailDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("subjects/fiction.json")
    suspend fun getFictionBooks(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): SubjectResponse

    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): SearchResponse

    @GET("{workPath}.json")
    suspend fun getBookDetails(@Path("workPath", encoded = true) workPath: String): WorkDetailDto
}
