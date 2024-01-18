package net.treset.treelauncher.generic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

enum class PopupType(val accent: @Composable () -> Color) {
    NONE({ MaterialTheme.colorScheme.secondary }),
    SUCCESS({ MaterialTheme.colorScheme.primary }),
    WARNING({ MaterialTheme.colorScheme.inversePrimary }),
    ERROR({ MaterialTheme.colorScheme.error })
}

@Composable
fun PopupOverlay(
    type: PopupType = PopupType.NONE,
    titleRow: @Composable RowScope.() -> Unit = {},
    buttonRow: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Popup(
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(6.dp),
                        spotColor = MaterialTheme.colorScheme.secondary
                    )
                    //.clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    //.border(2.dp, type.accent(), RoundedCornerShape(6.dp))
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
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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