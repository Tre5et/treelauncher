package dev.treset.treelauncher.generic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.treset.treelauncher.backend.util.MutableStateList
import dev.treset.treelauncher.backend.util.Status

@Composable
fun StatusPopup(
    statusList: List<Status>
) {
    if (statusList.isEmpty()) {
        return
    }

    PopupOverlay(
        titleRow = { Text(statusList.first().step) },
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(450.dp)
                    .heightIn(min = 200.dp)
            ) {
                for (status in statusList) {
                    StatusCard(
                        status,
                        includeTitle = status != statusList.first(),
                    )
                }
            }
        }
    )
}

@Composable
fun StatusCard(
    status: Status,
    includeTitle: Boolean = true,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(450.dp)
    ) {
        if(includeTitle) {
            Text(
              status.step,
              style = MaterialTheme.typography.titleSmall,
              modifier = Modifier.widthIn(max = 400.dp)
            )
        }
        status.progress?.let {
            if(it < 0) {
                LinearProgressIndicator(
                    modifier = Modifier.width(350.dp),
                    trackColor = MaterialTheme.colorScheme.secondary
                )
            } else {
                LinearProgressIndicator(
                    progress = {it},
                    modifier = Modifier.width(350.dp),
                    trackColor = MaterialTheme.colorScheme.secondary,
                    drawStopIndicator = {}
                )
            }
        }
        Text(
            status.details,
            modifier = Modifier.widthIn(max = 400.dp)
        )
    }
}