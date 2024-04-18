package net.treset.treelauncher.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import net.treset.treelauncher.backend.auth.UserAuth
import net.treset.treelauncher.backend.auth.userAuth
import net.treset.treelauncher.generic.*
import net.treset.treelauncher.localization.Language
import net.treset.treelauncher.localization.language
import net.treset.treelauncher.localization.strings
import net.treset.treelauncher.style.disabledContent
import net.treset.treelauncher.style.icons
import net.treset.treelauncher.util.checkUpdateOnStart

enum class LoginState(val actionAllowed: Boolean) {
    NOT_LOGGED_IN(true),
    AUTHENTICATING(false),
    LOGGED_IN(false),
    FAILED(true)
}

data class LoginContextData(
    val loginState: LoginState,
    val userAuth: UserAuth,
    val logout: () -> Unit
)

lateinit var LoginContext: LoginContextData

val LocalLoginContext = staticCompositionLocalOf<LoginContextData> {
    error("No LoginState provided")
}

@Composable
fun LoginScreen(
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var keepLoggedIn by remember { mutableStateOf(true) }
    var loginState by remember { mutableStateOf(LoginState.NOT_LOGGED_IN) }
    var browserUrl: String? by remember { mutableStateOf(null) }
    var showContent by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf(language().appLanguage) }

    var updateChecked by remember { mutableStateOf(false) }

    var popupData: PopupData? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        checkUpdateOnStart(
            coroutineScope,
            { popupData = it },
            { updateChecked = true }
        )
    }

    LaunchedEffect(Unit) {
        if(userAuth().hasFile()) {
            startLogin(true,
                {
                    loginState = it
                    if(it == LoginState.LOGGED_IN && updateChecked) {
                        showContent = true
                    }
                },
                { browserUrl = it }
            )
        }
    }

    LoginContext = remember(loginState, userAuth().isLoggedIn) {
        LoginContextData(
            loginState,
            userAuth()
        ) {
            loginState = LoginState.NOT_LOGGED_IN
            showContent = false
            userAuth().logout()
        }
    }


    if(showContent) {
        CompositionLocalProvider(
            LocalLoginContext provides LoginContext
        ) {
            content()
        }
        return
    }

    browserUrl?.let {
        LoginBrowserWindow(it) { url ->
            if(
                userAuth().checkUserUrl(url, keepLoggedIn) { success ->
                    loginState = if (success) {
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
                    key(language) {
                        Text(strings().login.button())
                    }
                }
                TitledCheckBox(
                    title = strings().login.keepLoggedIn(),
                    checked = keepLoggedIn,
                    onCheckedChange = { keepLoggedIn = it },
                    enabled = loginState.actionAllowed
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when (loginState) {
                            LoginState.NOT_LOGGED_IN -> ""
                            LoginState.AUTHENTICATING -> strings().login.label.authenticating()
                            LoginState.LOGGED_IN -> strings().login.label.success(userAuth().minecraftUser?.name)
                            LoginState.FAILED -> strings().login.label.failure()
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )

                    if (loginState == LoginState.FAILED) {
                        Button(
                            onClick = {
                                LoginContext.logout()
                            },
                            color = MaterialTheme.colorScheme.error,
                        ) {
                            Text(strings().login.logout())
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icons().language,
                "Select Language"
            )
            ComboBox(
                Language.entries,
                onSelected = {
                    language = it
                    language().appLanguage = it
                },
                selected = language,
                toDisplayString = { displayName() },
                decorated = false
            )
        }

        val tip = remember { strings().login.tip() }
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 6.dp),
        ) {
            Text(
                tip,
                color = MaterialTheme.colorScheme.onBackground.disabledContent(),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
        ) {
            if (loginState == LoginState.LOGGED_IN && updateChecked) {
                FloatingActionButton(
                    onClick = { showContent = true },
                    shape = MaterialTheme.shapes.small,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        icons().start,
                        "Continue"
                    )
                }
            }
        }
    }

    popupData?.let {
        PopupOverlay(it)
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