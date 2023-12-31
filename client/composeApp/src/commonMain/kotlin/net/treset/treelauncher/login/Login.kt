package net.treset.treelauncher.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import net.treset.treelauncher.backend.auth.UserAuth
import net.treset.treelauncher.backend.auth.userAuth
import net.treset.treelauncher.generic.TitledCheckBox
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.icons

enum class LoginState(val actionAllowed: Boolean) {
    NOT_LOGGED_IN(true),
    AUTHENTICATING(false),
    LOGGED_IN(false),
    FAILED(true)
}

@Composable
fun LoginScreen(
    content: @Composable (LoginContext) -> Unit
) {
    var keepLoggedIn by remember { mutableStateOf(true) }
    var loginState by remember { mutableStateOf(LoginState.NOT_LOGGED_IN) }
    var browserUrl: String? by remember { mutableStateOf(null) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if(userAuth().hasFile()) {
            startLogin(true,
                {
                    loginState = it
                    if(it == LoginState.LOGGED_IN) {
                        showContent = true
                    }
                },
                { browserUrl = it }
            )
        }
    }

    if(showContent) {
        content(
            LoginContext(
                loginState,
                userAuth()
            ) {
                loginState = LoginState.NOT_LOGGED_IN
                showContent = false
                userAuth().logout()
            }
        )
        return
    }

    browserUrl?.let {
        LoginBrowserWindow(it) {url ->
            if(
                userAuth().checkUserUrl(url, keepLoggedIn) {
                    loginState = if (it) {
                        LoginState.LOGGED_IN
                    } else {
                        LoginState.FAILED
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
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(0.25f)
            ) {
                Button(
                    onClick = {
                        startLogin(
                            keepLoggedIn,
                            { loginState = it },
                            { browserUrl = it }
                        )
                    },
                    enabled = loginState.actionAllowed,
                    modifier = Modifier.fillMaxWidth()
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
                    LoginState.NOT_LOGGED_IN -> ""
                    LoginState.AUTHENTICATING -> strings().login.label.authenticating()
                    LoginState.LOGGED_IN -> strings().login.label.success(userAuth().minecraftUser?.name)
                    LoginState.FAILED -> strings().login.label.failure()
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
            if(loginState == LoginState.LOGGED_IN) {
                FloatingActionButton(
                    onClick = { showContent = true },
                    shape = MaterialTheme.shapes.small,
                ) {
                    Icon(
                        icons().start,
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
    onState: (LoginState) -> Unit,
    onUrl: (String) -> Unit
) {
    onState(LoginState.AUTHENTICATING)
    Thread {
        val url = userAuth().startAuthentication(keepLoggedIn) {
            onState(
                if (it) {
                    LoginState.LOGGED_IN
                } else {
                    LoginState.FAILED
                }
            )
        }
        url?.let { onUrl(it) }
    }.start()
}

data class LoginContext(
    val loginState: LoginState,
    val userAuth: UserAuth,
    val logout: () -> Unit
)