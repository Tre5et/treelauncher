package net.treset.treelauncher.style

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

@Composable
fun icons() = Icons()

class Icons {
    val instances = Icons.Rounded.Dashboard
    val saves = Icons.Rounded.Save
    val resourcePacks = Icons.Rounded.Inventory2
    val options = Icons.Rounded.Tune
    val mods = Icons.Rounded.Code
    val settings = Icons.Rounded.Settings
    val add = Icons.Rounded.AddCircle
    val time = Icons.Rounded.Timer
    val version = Icons.Rounded.SwapHoriz
    val comboBox = Icons.Rounded.ArrowDropDown
    val updateHint = Icons.Rounded.DownloadForOffline
    val clear = Icons.Rounded.Close
    val start = Icons.Rounded.PlayArrow
    val logout = Icons.Rounded.Logout
    val update = Icons.Rounded.Download
    val gitHub = @Composable { painterResource("icons/github.svg") }
    val sort = Icons.Rounded.SwapVert
    val play = @Composable { painterResource("icons/play_arrow_scaled.svg") }
    val rename = Icons.Rounded.Edit
    val folder = Icons.Rounded.Folder
    val file = Icons.Rounded.InsertDriveFile
    val delete = Icons.Rounded.Delete
    val change = Icons.Rounded.Sync
    val back = Icons.Rounded.ArrowBack
}