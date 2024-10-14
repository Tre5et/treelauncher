package net.treset.treelauncher.settings

import androidx.compose.runtime.Composable
import net.treset.treelauncher.AppContext
import net.treset.treelauncher.generic.Button
import net.treset.treelauncher.generic.Text
import net.treset.treelauncher.localization.strings

@Composable
fun WindowReset() {
    Button(
        onClick = {
            AppContext.resetWindowSize()
        }
    ) {
        Text(strings().settings.resetWindow())
    }
}