package net.treset.treelauncher.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.treset.treelauncher.generic.TitledColumn
import net.treset.treelauncher.localization.strings


@Composable
fun Settings() {
    TitledColumn(
        title = strings().settings.title(),
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(550.dp)
        ) {
            Appearance()

            Directory()

            Discord()

            //TODO: Sync()

            UpdateUrl()

            User()

            Cleanup()

            Update()
        }
    }
}

