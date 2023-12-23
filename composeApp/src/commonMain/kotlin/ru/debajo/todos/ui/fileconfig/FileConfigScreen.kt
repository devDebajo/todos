package ru.debajo.todos.ui.fileconfig

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun FileConfigScreen(viewModel: FileConfigViewModel) {
    val state by viewModel.state.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = state.currentFileUri ?: "No file selected",
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.size(12.dp))
            if (state.loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    enabled = state.openListButtonEnabled,
                    onClick = { viewModel.openList() }
                ) {
                    Text("Open list")
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp),
        ) {
            Button(onClick = { viewModel.createNewFile() }) {
                Text("Create file")
            }
            Spacer(modifier = Modifier.size(40.dp))
            Button(onClick = { viewModel.selectFile() }) {
                Text("Select file")
            }
        }
    }
}
