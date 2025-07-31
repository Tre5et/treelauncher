package dev.treset.treelauncher.backend.debug

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.treset.treelauncher.backend.util.SimpleStringProvider
import dev.treset.treelauncher.backend.util.Status
import dev.treset.treelauncher.backend.util.StatusProvider
import dev.treset.treelauncher.backend.util.StatusReceiver
import dev.treset.treelauncher.generic.Button
import dev.treset.treelauncher.generic.PopupOverlay
import dev.treset.treelauncher.generic.PopupType
import dev.treset.treelauncher.generic.StatusPopup
import java.lang.Thread.sleep

@Composable
fun DebugPopup(
    close: () -> Unit,
) {
    var index: Int by remember { mutableStateOf(0) }
    var type: PopupType by remember { mutableStateOf(PopupType.entries[index]) }

    var status: List<Status> by remember { mutableStateOf(emptyList()) }

    if(status.isEmpty()) {
        PopupOverlay(
            type = type,
            titleRow = {
                Text("Debug Popup!")
            },
            buttonRow = {
                Button(
                    onClick = {
                        index = (index + 1) % PopupType.entries.size
                        type = PopupType.entries[index]
                    }
                ) { Text("Cycle Type") }

                Button(
                    onClick = {
                        Thread {
                            debugStatusSequence { status = it }
                            status = emptyList()
                        }.start()
                    }
                ) {
                    Text("Start status sequence")
                }

                Button(
                    onClick = { close() }
                ) {
                    Text("Close")
                }
            },
        ) {
            Text("This is a test popup.")
            Text("This is a reeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeealy loooooooooooooooooooooooooooooooooooong liiiiiiiiiiiiiiiiiiiiiiine!")
        }
    } else {
        StatusPopup(status)
    }
}

fun debugStatusSequence(
    onStatus: StatusReceiver,
) {
    val p = StatusProvider(SimpleStringProvider("Base step"), -1, onStatus)
    p.unknown("We KNOW nothing!")
    sleep(1000)
    p.total = 10
    p.next("Now we know the value")

    val p1 = p.subStep(SimpleStringProvider("Step 1"), 3)
    p1.next("1")
    sleep(500)
    p1.next("2")
    sleep(500)
    p1.next("3")
    sleep(1000)
    p1.next("Unexpected 4")
    sleep(1000)
    p1.finish()

    p.next("This is a very long text to see how line wrapping hopefully works correctly yay this should be long enough now :D")
    sleep(500)

    val p2 = p.subStep(SimpleStringProvider("Step 2"), 2)
    p2.next("1")
    sleep(1000)

    val p21 = p2.subStep(SimpleStringProvider("Step 2.1"), 4)
    sleep(1000)
    p21.next("1")
    sleep(500)
    p21.next("2")
    sleep(500)
    p21.finish("Early finish")
    sleep(1000)

    p2.next("2")
    sleep(500)
    p2.finish()
    sleep(1000)

    p.next("Almost done!")
    sleep(1500)
    p.next("Last...")
    sleep(100)
    p.finish()
    sleep(1000)
}