package net.treset.treelauncher.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import net.treset.treelauncher.backend.auth.userAuth
import net.treset.treelauncher.generic.TitledCheckBox
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

private enum class State(val actionAllowed: Boolean) {
    NOT_LOGGED_IN(true),
    AUTHENTICATING(false),
    LOGGED_IN(false),
    FAILED(true)
}

@Composable
fun LoginScreen(
    content: @Composable () -> Unit
) {
    var keepLoggedIn by remember { mutableStateOf(true) }
    var loginState by remember { mutableStateOf(State.NOT_LOGGED_IN) }
    var browserUrl: String? by remember { mutableStateOf(null) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if(userAuth().hasFile()) {
            startLogin(true,
                {
                    loginState = it
                    if(it == State.LOGGED_IN) {
                        showContent = true
                    }
                },
                { browserUrl = it }
            )
        }
    }

    if(showContent) {
        content()
        return
    }

    browserUrl?.let {
        LoginBrowserWindow(it) {url ->
            if(
                userAuth().checkUserUrl(url, keepLoggedIn) {
                    loginState = if (it) {
                        State.LOGGED_IN
                    } else {
                        State.FAILED
                    }
                }
            ) {
                browserUrl = null
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                Button(
                    onClick = {
                        startLogin(
                            keepLoggedIn,
                            { loginState = it },
                            { browserUrl = it }
                        )
                    },
                    enabled = loginState.actionAllowed
                ) {
                    Text(strings().login.button())
                }
                TitledCheckBox(
                    text = strings().login.keepLoggedIn(),
                    checked = keepLoggedIn,
                    onCheckedChange = { keepLoggedIn = it },
                    enabled = loginState.actionAllowed
                )
            }

            Text(
                text = when (loginState) {
                    State.NOT_LOGGED_IN -> ""
                    State.AUTHENTICATING -> strings().login.label.authenticating()
                    State.LOGGED_IN -> strings().login.label.success(userAuth().minecraftUser?.name)
                    State.FAILED -> strings().login.label.failure()
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxSize().padding(10.dp)
        ) {
            if(loginState == State.LOGGED_IN) {
                FloatingActionButton(
                    onClick = { showContent = true },
                    shape = MaterialTheme.shapes.small,
                ) {
                    Icon(
                        icons().PlayArrow,
                        "Continue"
                    )
                }
            }
        }
    }

}

@Composable
private fun LoginBrowserWindow(
    url: String,
    onUrl: (String?) -> Unit
) {
    val webViewState = rememberWebViewState(url)

    LaunchedEffect(Unit) {
        webViewState.webSettings.apply {
            isJavaScriptEnabled = true
        }
    }

    webViewState.lastLoadedUrl?.let {
        onUrl(webViewState.lastLoadedUrl)
    }

    Window(
        onCloseRequest = { onUrl(null) },
        title = strings().login.browserTitle(webViewState)
    ) {
        Column(Modifier.fillMaxSize()) {
            WebView(
                state = webViewState,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun startLogin(
    keepLoggedIn: Boolean,
    onState: (State) -> Unit,
    onUrl: (String) -> Unit
) {
    onState(State.AUTHENTICATING)
    Thread {
        val url = userAuth().startAuthentication(keepLoggedIn) {
            onState(
                if (it) {
                    State.LOGGED_IN
                } else {
                    State.FAILED
                }
            )
        }
        url?.let { onUrl(it) }
    }.start()
}