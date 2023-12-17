package net.treset.treelauncher.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import dev.datlag.kcef.KCEF
import net.treset.treelauncher.backend.auth.userAuth
import net.treset.treelauncher.generic.TextCheckBox
import net.treset.treelauncher.localization.strings

enum class LoginState(val actionAllowed: Boolean) {
    NOT_LOGGED_IN(true),
    AUTHENTICATING(false),
    LOGGED_IN(false),
    FAILED(true)
}

@Composable
fun LoginScreen(
    onLogin: () -> Unit
) {
    var keepLoggedIn by remember { mutableStateOf(true) }
    var loginState by remember { mutableStateOf(LoginState.NOT_LOGGED_IN) }
    var browserUrl: String? by remember { mutableStateOf(null) }

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

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().fillMaxHeight()
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Button(
                onClick = {
                    loginState = LoginState.AUTHENTICATING
                    val url = userAuth().startAuthentication(keepLoggedIn) {
                        loginState = if (it) {
                            LoginState.LOGGED_IN
                        } else {
                            LoginState.FAILED
                        }
                    }
                    url?.let{ browserUrl = it }
                },
                enabled = loginState.actionAllowed
            ) {
                Text(strings().login.button())
            }
            TextCheckBox(
                text = strings().login.keepLoggedIn(),
                checked = keepLoggedIn,
                onCheckedChange = { keepLoggedIn = it },
                enabled = loginState.actionAllowed
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = when(loginState) {
                    LoginState.NOT_LOGGED_IN -> ""
                    LoginState.AUTHENTICATING -> strings().login.label.authenticating()
                    LoginState.LOGGED_IN -> strings().login.label.success(userAuth().minecraftUser?.name ?: "Anonymous User")
                    LoginState.FAILED -> strings().login.label.failure()
                }
            )
        }
    }

}

@Composable
fun LoginBrowserWindow(
    url: String,
    onUrl: (String?) -> Unit
) {
    val webViewState = rememberWebViewState("https://google.com")
    LaunchedEffect(Unit) {
        webViewState.webSettings.apply {
            isJavaScriptEnabled = true
        }
    }

    webViewState.lastLoadedUrl?.let {
        onUrl(webViewState.lastLoadedUrl)
    }


    Column(Modifier.fillMaxSize()) {
        val text =
            webViewState.let {
                "${it.pageTitle ?: ""} ${it.loadingState} ${it.lastLoadedUrl ?: ""}"
            }
        Text(text)
        IconButton(
            onClick = { onUrl(null) }
        ) {
            Icon(Icons.Default.ArrowBack, "Back")
        }

        //TODO: This is somehow very broken
        WebView(
            state = webViewState,
            modifier = Modifier.fillMaxSize(),
        )
    }
}