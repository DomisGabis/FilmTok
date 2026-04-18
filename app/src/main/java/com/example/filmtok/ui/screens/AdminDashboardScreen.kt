package com.example.filmtok.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.filmtok.model.Movie
import com.example.filmtok.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit,
    onNavigateToAddMovie: () -> Unit,
    onNavigateToEditMovie: (String) -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val movies by viewModel.movies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Pobieramy dane tylko gdy wchodzimy na ten ekran
    LaunchedEffect(Unit) {
        viewModel.loadMovies()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("FilmTok Admin", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "TRYB ADMINISTRATORA",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Wyloguj", tint = Color.Gray)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddMovie,
                containerColor = Color(0xFFFF2D55),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj film")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFFFF2D55)
                    )
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "Wystąpił błąd",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 14.sp
                    )
                }
                movies.isEmpty() -> {
                    Text(
                        text = "Brak filmów w bazie",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 14.sp
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(movies) { movie ->
                            MovieAdminItem(
                                movie = movie,
                                onEdit = { onNavigateToEditMovie(movie.id) },
                                onDelete = { viewModel.deleteMovie(movie.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieAdminItem(
    movie: Movie,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(movie.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(movie.director, color = Color.Gray, fontSize = 12.sp)
                Text("${movie.year} | ${movie.rating}⭐", color = Color.Gray, fontSize = 12.sp)
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edytuj", tint = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
