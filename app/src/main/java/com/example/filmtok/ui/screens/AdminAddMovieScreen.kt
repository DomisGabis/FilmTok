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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.filmtok.model.CastMember
import com.example.filmtok.viewmodel.AdminMovieFormState
import androidx.compose.ui.res.stringResource
import com.example.filmtok.R
import com.example.filmtok.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddMovieScreen(
    onBackClick: () -> Unit,
    movieId: String? = null,
    viewModel: AdminViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val uploadProgress by viewModel.uploadProgress.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val posterLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        viewModel.onPosterUriChange(uri)
    }
    val backdropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        viewModel.onBackdropUriChange(uri)
    }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        viewModel.onVideoUriChange(uri)
    }

    LaunchedEffect(movieId) {
        if (movieId != null) {
            viewModel.loadMovie(movieId)
        } else {
            viewModel.clearMovieToEdit()
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSuccess()
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            AdminTopBar(
                isEditMode = movieId != null,
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

    BasicInfoSection(uiState = uiState, viewModel = viewModel)

                ImagesSection(
                    uiState = uiState,
                    onPosterClick = { posterLauncher.launch(arrayOf("image/*")) },
                    onBackdropClick = { backdropLauncher.launch(arrayOf("image/*")) },
                    onVideoClick = { videoLauncher.launch(arrayOf("video/*")) }
                )

                CastSection(uiState = uiState, viewModel = viewModel)

                Spacer(modifier = Modifier.height(80.dp))
            }

            SaveButton(
                isLoading = isLoading,
                uploadProgress = uploadProgress,
                onClick = { viewModel.saveMovie(movieId) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar(isEditMode: Boolean, onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = if (isEditMode) stringResource(R.string.admin_edit_movie_title) else stringResource(R.string.admin_add_movie_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(stringResource(R.string.admin_form_subtitle), color = Color.Gray, fontSize = 12.sp)
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
fun BasicInfoSection(uiState: AdminMovieFormState, viewModel: AdminViewModel) {
    AdminSection(title = stringResource(R.string.admin_section_basic), icon = Icons.Default.Info) {
        AdminTextField(
            value = uiState.title,
            onValueChange = viewModel::onTitleChange,
            label = "Tytuł *",
            placeholder = "Np. Matrix"
        )
        AdminTextField(
            value = uiState.director,
            onValueChange = viewModel::onDirectorChange,
            label = "Reżyser *",
            placeholder = "Lana Wachowski"
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AdminTextField(
                modifier = Modifier.weight(1f),
                value = uiState.year,
                onValueChange = viewModel::onYearChange,
                label = "Rok produkcji",
                placeholder = "2024"
            )
            AdminTextField(
                modifier = Modifier.weight(1f),
                value = uiState.duration,
                onValueChange = viewModel::onDurationChange,
                label = "Czas trwania (min)",
                placeholder = "120"
            )
        }
        
        AdminTextField(
            value = uiState.rating,
            onValueChange = viewModel::onRatingChange,
            label = "Ocena (0-5)",
            placeholder = "5"
        )
        AdminTextField(
            value = uiState.genre,
            onValueChange = viewModel::onGenreChange,
            label = "Gatunek",
            placeholder = "Sci-Fi, Akcja"
        )
        AdminTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            label = "Opis",
            placeholder = "Krótki opis filmu...",
            minLines = 3
        )
    }
}

@Composable
fun ImagesSection(
    uiState: AdminMovieFormState,
    onPosterClick: () -> Unit,
    onBackdropClick: () -> Unit,
    onVideoClick: () -> Unit
) {
    AdminSection(title = stringResource(R.string.admin_section_media), icon = Icons.Default.ThumbUp) {
        Text("Plakat główny", color = Color.Gray, fontSize = 14.sp)
        ImagePickerBox(
            uri = uiState.posterUri, 
            existingUrl = uiState.existingPosterUrl,
            onClick = onPosterClick, 
            label = "Kliknij aby dodać plakat"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text("Tło (backdrop)", color = Color.Gray, fontSize = 14.sp)
        ImagePickerBox(
            uri = uiState.backdropUri, 
            existingUrl = uiState.existingBackdropUrl,
            onClick = onBackdropClick, 
            label = "Kliknij aby dodać tło", 
            height = 150.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Wideo (reels)", color = Color.Gray, fontSize = 14.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (uiState.videoUri != null || uiState.existingVideoUrl.isNotEmpty()) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surface)
                .clickable { onVideoClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (uiState.videoUri != null || uiState.existingVideoUrl.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (uiState.videoUri != null || uiState.existingVideoUrl.isNotEmpty()) Color.White else Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.videoUri != null || uiState.existingVideoUrl.isNotEmpty()) "Wideo wybrane" else "Kliknij aby dodać wideo",
                    color = if (uiState.videoUri != null || uiState.existingVideoUrl.isNotEmpty()) Color.White else Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CastSection(uiState: AdminMovieFormState, viewModel: AdminViewModel) {
    AdminSection(title = stringResource(R.string.admin_section_cast), icon = Icons.Default.Person) {
        AdminTextField(
            value = uiState.actorName,
            onValueChange = viewModel::onActorNameChange,
            label = "Imię i nazwisko aktora"
        )
        AdminTextField(
            value = uiState.actorRole,
            onValueChange = viewModel::onActorRoleChange,
            label = "Rola w filmie"
        )
        AdminTextField(
            value = uiState.actorImageUrl,
            onValueChange = viewModel::onActorImageUrlChange,
            label = "URL zdjęcia aktora (opcjonalnie)"
        )
        
        Button(
            onClick = viewModel::addCastMember,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dodaj aktora")
        }

        uiState.castMembers.forEach { member ->
            CastMemberItem(member = member, onRemove = { viewModel.removeCastMember(member) })
        }
    }
}

@Composable
fun CastMemberItem(member: CastMember, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${member.name} jako ${member.character}", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SaveButton(
    isLoading: Boolean, 
    uploadProgress: Float,
    onClick: () -> Unit, 
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLoading && uploadProgress > 0f) {
            Text(
                text = "Wysyłanie plików: ${(uploadProgress * 100).toInt()}%",
                color = Color.White,
                fontSize = 12.sp
            )
            LinearProgressIndicator(
                progress = { uploadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFFF2D55),
                trackColor = Color.White.copy(alpha = 0.1f),
            )
        }

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55)),
            shape = RoundedCornerShape(28.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(stringResource(R.string.admin_save), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background
            ),
            minLines = minLines,
            singleLine = minLines == 1
        )
    }
}

@Composable
fun ImagePickerBox(
    uri: Uri?, 
    existingUrl: String = "", 
    onClick: () -> Unit, 
    label: String, 
    height: Dp = 200.dp
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (uri != null || existingUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(uri ?: existingUrl)
                    .crossfade(true)
                    .build(),
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
