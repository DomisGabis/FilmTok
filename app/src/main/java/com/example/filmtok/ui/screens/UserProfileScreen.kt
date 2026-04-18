package com.example.filmtok.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.filmtok.model.Achievement
import com.example.filmtok.model.User
import com.example.filmtok.viewmodel.ProfileViewModel

@Composable
fun UserProfileScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val scrollState = rememberScrollState()

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
                
                ProfileHeader(currentUser)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                StatsGrid(currentUser)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                SectionHeader(title = "Ulubione gatunki", icon = Icons.Default.Menu)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentUser.favoriteGenres) { genre ->
                        GenreChip(genre)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                SectionHeader(title = "Osiągnięcia", icon = Icons.Default.Star)
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
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ProfileHeader(user: User) {
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
        ) {
            AsyncImage(
                model = user.profileImageUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = user.username,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = user.name, // Używamy 'name' zamiast 'email' jeśli w modelu nie ma email
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
        StatCard(Modifier.weight(1f), Icons.Default.Favorite, user.stats.moviesInBase.toString(), "Filmy")
        StatCard(Modifier.weight(1f), Icons.Default.PlayArrow, user.stats.moviesWatched.toString(), "Obejrzane")
        StatCard(Modifier.weight(1f), Icons.Default.Star, user.stats.averageRating.toString(), "Ocena")
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
fun GenreChip(genre: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Text(
            text = genre,
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
                    text = achievement.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = achievement.description,
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
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00BFFF))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Język", color = MaterialTheme.colorScheme.onSurface)
                }
                Surface(color = Color.Black.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        Text("PL", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                        Text("EN", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
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
                    Text(text = "Dark Mode", color = MaterialTheme.colorScheme.onSurface)
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
            Text(text = "Wyloguj się", color = Color(0xFFFF2D55), fontWeight = FontWeight.Bold)
        }
    }
}
