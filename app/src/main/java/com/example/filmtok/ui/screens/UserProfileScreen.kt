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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.filmtok.R
import com.example.filmtok.model.Achievement
import com.example.filmtok.model.User
import com.example.filmtok.viewmodel.ProfileViewModel
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import android.net.Uri

@Composable
fun UserProfileScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val scrollState = rememberScrollState()
    var showEditUsernameDialog by remember { mutableStateOf(false) }

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
                Spacer(modifier = Modifier.height(24.dp))
                
                ProfileHeader(
                    user = currentUser,
                    isUploading = isUploading,
                    onImageClick = {
                        photoPickerLauncher.launch(arrayOf("image/*"))
                    },
                    onEditUsernameClick = { showEditUsernameDialog = true }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                StatsGrid(currentUser)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                SectionHeader(title = stringResource(R.string.profile_favorite_genres), icon = Icons.Default.Menu)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentUser.favoriteGenres) { genreKey ->
                        GenreChip(genreKey)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                SectionHeader(title = stringResource(R.string.profile_achievements), icon = Icons.Default.Star)
                Spacer(modifier = Modifier.height(12.dp))
                AchievementsList(currentUser.achievements)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                SettingsSection(
                    isDarkMode = isDarkMode,
                    onDarkModeChange = onDarkModeChange,
                    onLogout = onLogout
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (showEditUsernameDialog) {
                EditUsernameDialog(
                    currentUsername = currentUser.username,
                    onDismiss = { showEditUsernameDialog = false },
                    onConfirm = { newUsername ->
                        viewModel.updateUsername(newUsername)
                        showEditUsernameDialog = false
                    }
                )
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun EditUsernameDialog(
    currentUsername: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(currentUsername) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_username_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.username_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ProfileHeader(
    user: User,
    isUploading: Boolean,
    onImageClick: () -> Unit,
    onEditUsernameClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFF2D55), Color(0xFF00F2EA))
                    )
                )
                .padding(4.dp)
                .clickable { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = user.profileImageUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = user.username,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onEditUsernameClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Username",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Text(
            text = user.name,
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun StatsGrid(user: User) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCard(Modifier.weight(1f), Icons.Default.Favorite, user.stats.moviesInBase.toString(), stringResource(R.string.profile_stats_movies))
        StatCard(Modifier.weight(1f), Icons.Default.PlayArrow, user.stats.moviesWatched.toString(), stringResource(R.string.profile_stats_watched))
        StatCard(Modifier.weight(1f), Icons.Default.Star, user.stats.averageRating.toString(), stringResource(R.string.profile_stats_rating))
    }
}

@Composable
fun StatCard(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFFFF2D55), modifier = Modifier.size(24.dp))
        Text(text = value, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
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

@Composable
fun AchievementsList(achievements: List<Achievement>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        achievements.forEach { achievement ->
            AchievementItem(
                modifier = Modifier.fillMaxWidth(),
                achievement = achievement
            )
        }
    }
}

@Composable
fun AchievementItem(modifier: Modifier, achievement: Achievement) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.isUnlocked) Color(0xFFFFD700).copy(alpha = 0.2f)
                        else Color.Gray.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = if (achievement.isUnlocked) Color(0xFFFFD700) else Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = stringResource(achievement.titleRes),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(achievement.descriptionRes),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
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
    
    // Debug log to see the current locale
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
