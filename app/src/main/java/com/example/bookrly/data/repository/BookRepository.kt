package com.example.bookrly.data.repository

import android.util.Log
import com.example.bookrly.data.api.OpenLibraryApi
import com.example.bookrly.data.local.FavoritesManager
import com.example.bookrly.data.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BookRepository(
    private val api: OpenLibraryApi,
    private val favoritesManager: FavoritesManager
) {
    fun getFictionBooks(limit: Int = 20, offset: Int = 0): Flow<Result<List<Book>>> = flow {
        try {
            val response = api.getFictionBooks(limit = limit, offset = offset)
            val books = response.works.map { work ->
                Book(
                    id = work.key,
                    title = work.title,
                    author = work.authors?.firstOrNull()?.name ?: "Unknown Author",
                    coverUrl = work.coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
                )
            }
            emit(Result.success(books))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun searchBooks(query: String, limit: Int = 20, offset: Int = 0): Flow<Result<List<Book>>> = flow {
        try {
            val response = api.searchBooks(query = query, limit = limit, offset = offset)
            val books = response.docs.map { doc ->
                Book(
                    id = doc.key,
                    title = doc.title,
                    author = doc.authorNames?.firstOrNull() ?: "Unknown Author",
                    coverUrl = doc.coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
                )
            }
            emit(Result.success(books))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getBookDetails(book: Book): Book {
        return try {
            val path = if (book.id.startsWith("/")) book.id.substring(1) else book.id
            Log.d("BookRepository", "Fetching details for: $path")
            val details = api.getBookDetails(path)
            
            Log.d("BookRepository", "Details received: pages=${details.numberOfPages}, year=${details.firstPublishDate}")
            
            book.copy(
                description = details.description,
                publishYear = details.firstPublishDate,
                pages = details.numberOfPages
            )
        } catch (e: Exception) {
            e.printStackTrace()
            book
        }
    }
    
    suspend fun getBookDetailsById(id: String): Result<Book> {
        return try {
            val path = if (id.startsWith("/")) id.substring(1) else id
            val details = api.getBookDetails(path)
            
            val coverId = details.covers?.firstOrNull()
            val coverUrl = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }

            val book = Book(
                id = id,
                title = details.title ?: "Unknown Title",
                author = "Unknown Author", 
                coverUrl = coverUrl,
                description = details.description,
                publishYear = details.firstPublishDate,
                pages = details.numberOfPages
            )
            Result.success(book)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFavoriteIds(): Set<String> = favoritesManager.getFavorites()

    fun addToFavorites(bookId: String) = favoritesManager.addFavorite(bookId)

    fun removeFromFavorites(bookId: String) = favoritesManager.removeFavorite(bookId)

    fun isFavorite(bookId: String): Boolean = favoritesManager.isFavorite(bookId)
}
