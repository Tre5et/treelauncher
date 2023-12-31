package net.treset.treelauncher.generic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TitledColumn(
    modifier: Modifier = Modifier,
    headModifier: Modifier = Modifier,
    parentModifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    headerContent: @Composable BoxScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = parentModifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProvideTextStyle(
                value = MaterialTheme.typography.titleMedium
            ) {
                Box(
                    modifier = headModifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    headerContent()
                }
            }
            Divider(
                color = MaterialTheme.colorScheme.primary,
                thickness = 2.dp
            )
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment
        ) {
            this.content()
        }
    }
}

@Composable
fun TitledColumn(
    title: String,
    modifier: Modifier = Modifier,
    parentModifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) = TitledColumn(
    modifier = modifier,
    headModifier = Modifier,
    parentModifier = parentModifier,
    verticalArrangement = verticalArrangement,
    horizontalAlignment = horizontalAlignment,
    headerContent = { Text(title) },
    content = content
)
