package com.example.filmtok.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.filmtok.R
import com.example.filmtok.model.User
import com.example.filmtok.model.Movie
import com.example.filmtok.viewmodel.ProfileViewModel
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

@Composable
fun UserProfileScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onMovieClick: (String) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val favoriteMovies by viewModel.favoriteMovies.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val scrollState = rememberScrollState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { viewModel.updateProfilePicture(it) }
        }
    )

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        user?.let { currentUser ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // 1. ZDJĘCIE PROFILOWE
                ProfileImageSection(
                    user = currentUser,
                    isUploading = isUploading,
                    onImageClick = { photoPickerLauncher.launch(arrayOf("image/*")) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. ADRES E-MAIL
                Text(
                    text = currentUser.email,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 3. LICZBA POLUBIONYCH FILMÓW
                FavoriteMoviesCounter(count = currentUser.favoriteMovies.size)

                Spacer(modifier = Modifier.height(12.dp))

                // 4. KARUZELA POLUBIONYCH FILMÓW
                if (favoriteMovies.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    ) {
                        items(favoriteMovies) { movie ->
                            FavoriteMovieCard(
                                movieImageUrl = movie.posterUrl,
                                onClick = { onMovieClick(movie.id) }
                            )
                        }
                    }
                } else if (currentUser.favoriteMovies.isNotEmpty()) {
                    // Wyświetl loader jeśli lista IDs nie jest pusta, ale filmy jeszcze się ładują
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else {
                    Text(
                        text = stringResource(R.string.no_favorite_movies),
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 5. ULUBIONE GATUNKI
                SectionHeader(title = stringResource(R.string.profile_favorite_genres), icon = Icons.Default.Favorite)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentUser.favoriteGenres) { genreKey ->
                        GenreChip(genreKey)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // USTAWIENIA (Język, Dark Mode, Log out)
                SettingsSection(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    onLogout = onLogout
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ProfileImageSection(user: User, isUploading: Boolean, onImageClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(colors = listOf(Color(0xFFFF2D55), Color(0xFF00F2EA))))
            .padding(4.dp)
            .clickable { onImageClick() },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = user.profileImageUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier.fillMaxSize().clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        if (isUploading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Composable
fun FavoriteMoviesCounter(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFFFF2D55), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Polubione filmy: $count",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FavoriteMovieCard(movieImageUrl: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(120.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        AsyncImage(
            model = movieImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color(0xFF00F2EA), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsSection(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val currentLocale = configuration.locales[0].language
    LaunchedEffect(currentLocale) {
        android.util.Log.d("UserProfileScreen", "Current configuration locale: $currentLocale")
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF00BFFF))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = stringResource(R.string.profile_settings_language), color = MaterialTheme.colorScheme.onSurface)
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        val isPl = currentLocale.startsWith("pl")
                        val isEn = currentLocale.startsWith("en")
                        Surface(
                            color = if (isPl) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .clickable {
                                    android.util.Log.d("UserProfileScreen", "Setting locale to PL")
                                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("pl")
                                    AppCompatDelegate.setApplicationLocales(appLocale)
                                }
                        ) {
                            Text(
                                text = "PL",
                                color = if (isPl) Color.White else Color.Gray,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontWeight = if (isPl) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        Surface(
                            color = if (isEn) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .clickable {
                                    android.util.Log.d("UserProfileScreen", "Setting locale to EN")
                                    val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags("en")
                                    AppCompatDelegate.setApplicationLocales(appLocale)
                                }
                        ) {
                            Text(
                                text = "EN",
                                color = if (isEn) Color.White else Color.Gray,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontWeight = if (isEn) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFA020F0))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = stringResource(R.string.profile_settings_dark_mode), color = MaterialTheme.colorScheme.onSurface)
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onDarkModeChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF2D55)
                    )
                )
            }
        }

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFFF2D55))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = stringResource(R.string.profile_logout), color = Color(0xFFFF2D55), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GenreChip(genreKey: String) {
    val genreName = when (genreKey) {
        "Action" -> stringResource(R.string.genre_action)
        "Adventure" -> stringResource(R.string.genre_adventure)
        "Animation / Animated film" -> stringResource(R.string.genre_animation)
        "Biopic / Biographical film" -> stringResource(R.string.genre_biopic)
        "Comedy" -> stringResource(R.string.genre_comedy)
        "Crime" -> stringResource(R.string.genre_crime)
        "Disaster movie" -> stringResource(R.string.genre_disaster)
        "Documentary" -> stringResource(R.string.genre_documentary)
        "Drama" -> stringResource(R.string.genre_drama)
        "Fantasy" -> stringResource(R.string.genre_fantasy)
        "Horror" -> stringResource(R.string.genre_horror)
        "Musical" -> stringResource(R.string.genre_musical)
        "Mystery" -> stringResource(R.string.genre_mystery)
        "Romance" -> stringResource(R.string.genre_romance)
        "Romantic Comedy (Romcom)" -> stringResource(R.string.genre_romcom)
        "Science Fiction (Sci-fi)" -> stringResource(R.string.genre_sci_fi)
        "Superhero movie" -> stringResource(R.string.genre_superhero)
        "Thriller" -> stringResource(R.string.genre_thriller)
        "War film" -> stringResource(R.string.genre_war)
        "Western" -> stringResource(R.string.genre_western)
        else -> genreKey
    }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Text(
            text = genreName,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp
        )
    }
}
