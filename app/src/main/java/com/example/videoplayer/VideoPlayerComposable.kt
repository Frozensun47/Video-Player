package com.example.videoplayer

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay


@Composable
fun VideoPlayerWithScrubber(uri: Uri) {
    val context = LocalContext.current

    @OptIn(androidx.media3.common.util.UnstableApi::class)
    val exoPlayer = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            isScrubbingModeEnabled = true
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }


    var sliderPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var isDragging by remember { mutableStateOf(false) }
    var wasPlayingBeforeDrag by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    duration = exoPlayer.duration.coerceAtLeast(0L)
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    // Update slider when not dragging
    LaunchedEffect(Unit) {
        while (true) {
            if (!isDragging) {
                sliderPosition = exoPlayer.currentPosition
            }
            delay(50)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // 🔥 video fills remaining height
        )

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = sliderPosition.coerceAtMost(duration).toFloat(),
            onValueChange = {
                if (!isDragging) {
                    isDragging = true
                    wasPlayingBeforeDrag = exoPlayer.isPlaying
                    exoPlayer.playWhenReady = false
                }
                sliderPosition = it.toLong()
                exoPlayer.seekTo(sliderPosition)
            },
            onValueChangeFinished = {
                isDragging = false
                if (wasPlayingBeforeDrag) {
                    exoPlayer.playWhenReady = true
                }
            },
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Time: ${sliderPosition / 1000}s / ${duration / 1000}s",
            modifier = Modifier.align(alignment = androidx.compose.ui.Alignment.CenterHorizontally)
        )
    }

}

