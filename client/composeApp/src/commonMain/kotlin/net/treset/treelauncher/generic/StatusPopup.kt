package net.treset.treelauncher.generic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.backend.util.Status

@Composable
fun StatusPopup(
    status: Status
) {
    PopupOverlay(
        titleRow = { Text(status.step) },
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                status.progress?.let {
                    LinearProgressIndicator(
                        progress = { it },
                        modifier = Modifier.width(250.dp),
                    )
                }
                Text(status.details)
            }
        }
    )
}