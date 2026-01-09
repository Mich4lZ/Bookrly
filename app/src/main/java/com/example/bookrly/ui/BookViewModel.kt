package com.example.bookrly.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookrly.App
import com.example.bookrly.data.model.Book
import com.example.bookrly.data.repository.BookRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val isPaginationLoading: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val searchQuery: String = ""
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

    private var currentOffset = 0
    private val pageSize = 20
    private var searchJob: Job? = null

    init {
        loadBooks()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            loadBooks()
        }
    }

    fun loadBooks() {
        if (_uiState.value.isLoading) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, books = emptyList(), endReached = false) }
            currentOffset = 0
            val query = _uiState.value.searchQuery
            
            val flow = if (query.isEmpty()) {
                repository.getFictionBooks(limit = pageSize, offset = currentOffset)
            } else {
                repository.searchBooks(query = query, limit = pageSize, offset = currentOffset)
            }

            flow.collect { result ->
                result.onSuccess { books ->
                    currentOffset += books.size
                    _uiState.update { it.copy(
                        books = books, 
                        isLoading = false,
                        endReached = books.size < pageSize
                    ) }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
            }
        }
    }

    fun loadNextPage() {
        if (_uiState.value.isPaginationLoading || _uiState.value.endReached || _uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPaginationLoading = true) }
            val query = _uiState.value.searchQuery
            
            val flow = if (query.isEmpty()) {
                repository.getFictionBooks(limit = pageSize, offset = currentOffset)
            } else {
                repository.searchBooks(query = query, limit = pageSize, offset = currentOffset)
            }

            flow.collect { result ->
                result.onSuccess { newBooks ->
                    if (newBooks.isEmpty()) {
                        _uiState.update { it.copy(isPaginationLoading = false, endReached = true) }
                    } else {
                        currentOffset += newBooks.size
                        _uiState.update { it.copy(
                            books = it.books + newBooks,
                            isPaginationLoading = false,
                            endReached = newBooks.size < pageSize
                        ) }
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(isPaginationLoading = false, error = "Failed to load more books") }
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
