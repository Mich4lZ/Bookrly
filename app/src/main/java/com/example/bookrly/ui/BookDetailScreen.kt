package com.example.bookrly.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bookrly.data.model.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    viewModel: BookViewModel,
    onBack: () -> Unit
) {
    val book by viewModel.selectedBook.collectAsState()
    val isLoading by viewModel.isLoadingDetails.collectAsState()
    val currentBook = book ?: return
    var isFavorite by remember { mutableStateOf(viewModel.isFavorite(currentBook.id)) }
    
    LaunchedEffect(currentBook.id) {
        isFavorite = viewModel.isFavorite(currentBook.id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentBook.title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                 CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentBook.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .height(350.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = currentBook.title, 
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Author: ${currentBook.author}", 
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (currentBook.publishYear != null) {
                     Spacer(modifier = Modifier.height(4.dp))
                     Text(
                        text = "Published: ${currentBook.publishYear}",
                        style = MaterialTheme.typography.bodyMedium
                     )
                }

                if (currentBook.pages != null) {
                     Spacer(modifier = Modifier.height(4.dp))
                     Text(
                        text = "Pages: ${currentBook.pages}",
                        style = MaterialTheme.typography.bodyMedium
                     )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isFavorite) {
                            viewModel.removeFromFavorites(currentBook.id)
                        } else {
                            viewModel.addToFavorites(currentBook)
                        }
                        isFavorite = !isFavorite
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = if (isFavorite) "Remove from Favorites" else "Add to Favorites")
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentBook.description ?: "No description available.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }
    }
}
