package com.example.bookrly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bookrly.ui.BookDetailScreen
import com.example.bookrly.ui.BookViewModel
import com.example.bookrly.ui.FavoritesScreen
import com.example.bookrly.ui.HomeScreen
import com.example.bookrly.ui.theme.BookrlyTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            BookrlyTheme(darkTheme = isDarkTheme) {
                BookrlyApp(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
fun BookrlyApp(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel: BookViewModel = viewModel(factory = BookViewModel.Factory)

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onBookClick = { book ->
                    viewModel.loadBookDetails(book)
                    navController.navigate("detail")
                },
                onFavoritesClick = {
                    navController.navigate("favorites")
                },
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        }
        composable("detail") {
            BookDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("favorites") {
            FavoritesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onBookClick = { book ->
                    viewModel.loadBookDetails(book)
                    navController.navigate("detail")
                }
            )
        }
    }
}