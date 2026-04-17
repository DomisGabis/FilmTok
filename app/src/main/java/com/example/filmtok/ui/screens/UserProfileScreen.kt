package com.example.filmtok.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.filmtok.model.Achievement
import com.example.filmtok.model.User
import com.example.filmtok.ui.viewmodel.ProfileViewModel

@Composable
fun UserProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val scrollState = rememberScrollState()

    user?.let { currentUser ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
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
            
            SettingsSection(onLogout = onLogout)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileHeader(user: User) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            AsyncImage(
                model = user.profileImageUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFFF2D55), CircleShape),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(Color(0xFFFF2D55), CircleShape)
                    .padding(6.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = user.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Surface(
            color = Color.DarkGray,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = "USER",
                color = Color.LightGray,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Udostępnij profil")
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = { },
                modifier = Modifier.background(Color.DarkGray.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun StatsGrid(user: User) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.List, value = user.stats.moviesInBase.toString(), label = "Filmów w bazie")
            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Star, value = user.stats.averageRating.toString(), label = "Średnia ocena")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.PlayArrow, value = "${user.stats.watchTimeHours}h", label = "Czas oglądania")
            StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.FavoriteBorder, value = user.stats.moviesWatched.toString(), label = "Obejrzane")
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, icon: ImageVector, value: String, label: String) {
    Surface(
        modifier = modifier,
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFFFF2D55), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, tint = Color(0xFF00BFFF), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GenreChip(genre: String) {
    val backgroundColor = when(genre) {
        "Sci-Fi" -> Color(0xFFFF2D55)
        "Action" -> Color(0xFF00BFFF)
        "Drama" -> Color(0xFFFFD700)
        else -> Color.DarkGray
    }
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = genre,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun AchievementsList(achievements: List<Achievement>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        achievements.forEach { achievement ->
            AchievementItem(modifier = Modifier.weight(1f), achievement = achievement)
        }
    }
}

@Composable
fun AchievementItem(modifier: Modifier = Modifier, achievement: Achievement) {
    val icon = when(achievement.iconName) {
        "Movie" -> Icons.Default.PlayArrow
        "Star" -> Icons.Default.Star
        "Timer" -> Icons.Default.Build // Placeholder for Timer
        else -> Icons.Default.Info
    }

    Surface(
        modifier = modifier,
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .alpha(if (achievement.isUnlocked) 1f else 0.4f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(achievement.color).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(achievement.color),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = achievement.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SettingsSection(onLogout: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            color = Color(0xFF1E1E1E),
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
                    Text(text = "Język", color = Color.White)
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
            color = Color(0xFF1E1E1E),
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
                    Text(text = "Dark Mode", color = Color.White)
                }
                Switch(
                    checked = true,
                    onCheckedChange = { },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFF2D55))
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
