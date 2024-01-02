package net.treset.treelauncher.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import net.treset.mc_version_loader.saves.Save
import net.treset.treelauncher.generic.SelectorButton

@Composable
fun SaveButton(
    save: Save,
    selected: Boolean,
    onClick: () -> Unit
) {
    SelectorButton(
        selected = selected,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(LocalContentColor.current)
                    .padding(4.dp)
            ) {
                Image(
                    save.image.toComposeImageBitmap(),
                    "World Icon",
                    modifier = Modifier.size(72.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    save.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    save.fileName
                )
            }
        }
    }
}