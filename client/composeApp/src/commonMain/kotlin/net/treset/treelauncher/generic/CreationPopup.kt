package net.treset.treelauncher.generic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.backend.util.CreationStatus
import net.treset.treelauncher.localization.strings

@Composable
fun CreationPopup(
    status: CreationStatus
) {
    PopupOverlay(
        titleRow = { Text(status.currentStep.message()) },
        content = {
            status.downloadStatus?.let {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = it.currentAmount.toFloat() / it.totalAmount,
                        modifier = Modifier.width(250.dp)
                    )
                    Text(strings().settings.update.downloadingMessage(it.currentFile, it.currentAmount, it.totalAmount))
                }
            }
        }
    )
}