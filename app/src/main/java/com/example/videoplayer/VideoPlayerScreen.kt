package com.example.videoplayer

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

@Composable
fun VideoPlayerScreen() {
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> videoUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        videoUri = it.data?.data
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { galleryLauncher.launch("video/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pick from Gallery")
                }
                Button(
                    onClick = {
                        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                        cameraLauncher.launch(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Record Video")
                }
            }

            videoUri?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Playing: ${it.lastPathSegment}",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        VideoPlayerWithScrubber(uri = it)
                    }
                }
            } ?: Text("No video selected", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
