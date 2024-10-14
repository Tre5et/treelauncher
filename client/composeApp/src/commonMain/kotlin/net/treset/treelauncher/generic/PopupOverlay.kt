package net.treset.treelauncher.generic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.treset.treelauncher.style.warning

enum class PopupType(val accent: @Composable () -> Color) {
    NONE({ MaterialTheme.colorScheme.secondary }),
    SUCCESS({ MaterialTheme.colorScheme.primary }),
    WARNING({ MaterialTheme.colorScheme.warning }),
    ERROR({ MaterialTheme.colorScheme.error })
}

@Composable
fun PopupOverlay(
    type: PopupType = PopupType.NONE,
    titleRow: @Composable RowScope.() -> Unit = {},
    buttonRow: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Dialog(
        onDismissRequest = {},
        //properties = PopupProperties(focusable = true),
        properties = DialogProperties(),
        //alignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .border(1.dp, type.accent(), RoundedCornerShape(6.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    ProvideTextStyle(
                        MaterialTheme.typography.titleMedium
                    ) {
                        titleRow()
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .weight(1f, false)
                        .verticalScroll(rememberScrollState())
                ) {
                    content()
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    buttonRow()
                }
            }
        }
    }
}

@Composable
fun PopupOverlay(data: PopupData) = PopupOverlay(
    type = data.type,
    titleRow = data.titleRow,
    buttonRow = data.buttonRow,
    content = data.content
)

data class PopupData(
    val type: PopupType = PopupType.NONE,
    val titleRow: @Composable RowScope.() -> Unit = {},
    val buttonRow: @Composable RowScope.() -> Unit = {},
    val content: @Composable ColumnScope.() -> Unit = {}
)