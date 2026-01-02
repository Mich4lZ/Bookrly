package com.example.bookrly.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookrly.App
import com.example.bookrly.data.model.Book
import com.example.bookrly.data.repository.BookRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class BookViewModel(private val repository: BookRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BookUiState())
    val uiState: StateFlow<BookUiState> = _uiState.asStateFlow()

    private val _favorites = MutableStateFlow<List<Book>>(emptyList())
    val favorites: StateFlow<List<Book>> = _favorites.asStateFlow()
    
    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()
    
    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getFictionBooks().collect { result ->
                result.onSuccess { books ->
                    _uiState.update { it.copy(books = books, isLoading = false) }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
            }
        }
    }
    
    fun loadBookDetails(book: Book) {
        _selectedBook.value = book
        _isLoadingDetails.value = true
        
        viewModelScope.launch {
            val detailedBook = repository.getBookDetails(book)
            _selectedBook.value = detailedBook
            _isLoadingDetails.value = false
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            val ids = repository.getFavoriteIds()
            if (ids.isEmpty()) {
                _favorites.value = emptyList()
                return@launch
            }

            val books = ids.map { id ->
                async { repository.getBookDetailsById(id) }
            }.awaitAll()

            val successBooks = books.mapNotNull { it.getOrNull() }
            _favorites.value = successBooks
        }
    }

    fun addToFavorites(book: Book) {
        repository.addToFavorites(book.id)
        val current = _favorites.value.toMutableList()
        if (current.none { it.id == book.id }) {
            current.add(book)
            _favorites.value = current
        }
    }

    fun removeFromFavorites(bookId: String) {
        repository.removeFromFavorites(bookId)
        _favorites.value = _favorites.value.filter { it.id != bookId }
    }

    fun isFavorite(bookId: String): Boolean {
        return repository.isFavorite(bookId)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App)
                val repository = application.container.bookRepository
                BookViewModel(repository)
            }
        }
    }
}
