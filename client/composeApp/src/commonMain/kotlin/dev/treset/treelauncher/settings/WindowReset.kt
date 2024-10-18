package dev.treset.treelauncher.settings

import androidx.compose.runtime.Composable
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.generic.Button
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings

@Composable
fun WindowReset() {
    Button(
        onClick = {
            AppContext.resetWindowSize()
        }
    ) {
        Text(Strings.settings.resetWindow())
    }
}