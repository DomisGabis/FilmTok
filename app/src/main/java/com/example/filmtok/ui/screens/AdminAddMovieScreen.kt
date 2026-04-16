package com.example.filmtok.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.filmtok.model.CastMember
import com.example.filmtok.model.Movie
import com.example.filmtok.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddMovieScreen(
    onBackClick: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var director by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    var posterUri by remember { mutableStateOf<Uri?>(null) }
    var backdropUri by remember { mutableStateOf<Uri?>(null) }
    
    // Obsada
    var actorName by remember { mutableStateOf("") }
    var actorRole by remember { mutableStateOf("") }
    var actorImageUrl by remember { mutableStateOf("") }
    val castMembers = remember { mutableStateListOf<CastMember>() }

    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    val posterLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        posterUri = uri
    }
    val backdropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        backdropUri = uri
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSuccess()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dodaj Film", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Wypełnij wszystkie pola", color = Color.Gray, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Sekcja: Podstawowe informacje
                AdminSection(title = "Podstawowe informacje", icon = Icons.Default.Info) {
                    AdminTextField(value = title, onValueChange = { title = it }, label = "Tytuł *", placeholder = "Np. Matrix")
                    AdminTextField(value = director, onValueChange = { director = it }, label = "Reżyser *", placeholder = "Lana Wachowski")
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminTextField(modifier = Modifier.weight(1f), value = year, onValueChange = { year = it }, label = "Rok produkcji", placeholder = "2024")
                        AdminTextField(modifier = Modifier.weight(1f), value = duration, onValueChange = { duration = it }, label = "Czas trwania (min)", placeholder = "120")
                    }
                    
                    AdminTextField(value = rating, onValueChange = { rating = it }, label = "Ocena (0-5)", placeholder = "5")
                    AdminTextField(value = genre, onValueChange = { genre = it }, label = "Gatunek", placeholder = "Sci-Fi, Akcja")
                    AdminTextField(value = description, onValueChange = { description = it }, label = "Opis", placeholder = "Krótki opis filmu...", minLines = 3)
                }

                // Sekcja: Plakaty i zdjęcia
                AdminSection(title = "Plakaty i zdjęcia", icon = Icons.Default.ThumbUp) { // ThumbUp as placeholder for Plakaty
                    Text("Plakat główny", color = Color.Gray, fontSize = 14.sp)
                    ImagePickerBox(uri = posterUri, onClick = { posterLauncher.launch("image/*") }, label = "Kliknij aby dodać plakat")
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Tło (backdrop)", color = Color.Gray, fontSize = 14.sp)
                    ImagePickerBox(uri = backdropUri, onClick = { backdropLauncher.launch("image/*") }, label = "Kliknij aby dodać tło", height = 150.dp)
                }

                // Sekcja: Obsada
                AdminSection(title = "Obsada", icon = Icons.Default.Person) {
                    AdminTextField(value = actorName, onValueChange = { actorName = it }, label = "Imię i nazwisko aktora")
                    AdminTextField(value = actorRole, onValueChange = { actorRole = it }, label = "Rola w filmie")
                    AdminTextField(value = actorImageUrl, onValueChange = { actorImageUrl = it }, label = "URL zdjęcia aktora (opcjonalnie)")
                    
                    Button(
                        onClick = {
                            if (actorName.isNotBlank() && actorRole.isNotBlank()) {
                                castMembers.add(CastMember(name = actorName, character = actorRole, imageUrl = actorImageUrl))
                                actorName = ""
                                actorRole = ""
                                actorImageUrl = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dodaj aktora")
                    }

                    // Lista dodanych aktorów
                    castMembers.forEach { member ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${member.name} jako ${member.character}", color = Color.White, fontSize = 14.sp)
                            IconButton(onClick = { castMembers.remove(member) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }

            // Przycisk Zapisz
            Button(
                onClick = {
                    if (title.isNotBlank() && director.isNotBlank()) {
                        val movie = Movie(
                            title = title,
                            director = director,
                            year = year.toIntOrNull() ?: 2024,
                            duration = duration,
                            rating = rating.toDoubleOrNull() ?: 0.0,
                            genres = genre.split(",").map { it.trim() },
                            description = description,
                            posterUrl = posterUri?.toString() ?: "",
                            backdropUrl = backdropUri?.toString() ?: "",
                            cast = castMembers.toList()
                        )
                        viewModel.saveMovie(movie)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55)),
                shape = RoundedCornerShape(28.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Zapisz", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = Color(0xFF121212),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF00BFFF), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
fun AdminTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    minLines: Int = 1
) {
    Column(modifier = modifier) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.DarkGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFFF2D55),
                unfocusedBorderColor = Color(0xFF333333),
                focusedContainerColor = Color(0xFF1E1E1E),
                unfocusedContainerColor = Color(0xFF1E1E1E)
            ),
            minLines = minLines,
            singleLine = minLines == 1
        )
    }
}

@Composable
fun ImagePickerBox(uri: Uri?, onClick: () -> Unit, label: String, height: androidx.compose.ui.unit.Dp = 200.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(label, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
