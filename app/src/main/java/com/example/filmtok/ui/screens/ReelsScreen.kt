package com.example.filmtok.ui.screens

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.filmtok.model.Movie
import com.example.filmtok.viewmodel.ReelsViewModel

@Composable
fun ReelsScreen(
    onSeeDetailsClick: (String) -> Unit,
    viewModel: ReelsViewModel = viewModel()
) {
    val reels by viewModel.reels.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFF2D55))
        }
    } else {
        val pagerState = rememberPagerState(pageCount = { reels.size })

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            ReelItem(
                movie = reels[page],
                onSeeDetailsClick = onSeeDetailsClick
            )
        }
    }
}

@Composable
fun ReelItem(movie: Movie, onSeeDetailsClick: (String) -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(movie.videoUrl)
            setMediaItem(mediaItem)
            prepare()
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
        }
    }

    var isPlayerReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    isPlayerReady = true
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!isPlayerReady) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF2D55))
            }
        }

        // Overlay do przyciemnienia tła pod napisy
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                        startY = 500f
                    )
                )
        )

        // Środkowa ikona Play (statyczna dla placeholdera)
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.Center)
                .background(Color.Black.copy(alpha = 0.2f), CircleShape),
            tint = Color.White.copy(alpha = 0.5f)
        )

        // Prawy panel z akcjami
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ReelActionItem(Icons.Default.Favorite, movie.likesCount.toString())
            Spacer(modifier = Modifier.height(20.dp))
            // Używamy Ikony z tekstem jako placeholder dla komentarza
            ReelActionItem(Icons.Default.PlayArrow, movie.commentsCount.toString(), "Koment.") 
            Spacer(modifier = Modifier.height(20.dp))
            ReelActionItem(Icons.Default.Share, "Udost.")
        }

        // Dolny panel z opisem
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 40.dp, end = 100.dp)
        ) {
            Text(
                text = movie.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = movie.description,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSeeDetailsClick(movie.id) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Zobacz szczegóły", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ReelActionItem(icon: ImageVector, label: String, description: String = "") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { /* Action */ }) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
