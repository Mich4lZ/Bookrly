package com.example.bookrly

import android.app.Application
import com.example.bookrly.data.api.OpenLibraryApi
import com.example.bookrly.data.local.FavoritesManager
import com.example.bookrly.data.repository.BookRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(context: android.content.Context) {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://openlibrary.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(OpenLibraryApi::class.java)
    
    private val favoritesManager = FavoritesManager(context)

    val bookRepository = BookRepository(api, favoritesManager)
}
