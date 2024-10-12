package dev.treset.treelauncher.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.treset.mcdl.auth.AuthenticationStep
import dev.treset.mcdl.auth.InteractiveData
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.auth.UserAuth
import dev.treset.treelauncher.backend.auth.userAuth
import dev.treset.treelauncher.backend.util.string.copyToClipboard
import dev.treset.treelauncher.backend.util.string.openInBrowser
import dev.treset.treelauncher.generic.*
import dev.treset.treelauncher.localization.Language
import dev.treset.treelauncher.localization.language
import dev.treset.treelauncher.localization.strings
import dev.treset.treelauncher.style.disabledContent
import dev.treset.treelauncher.style.icons
import dev.treset.treelauncher.style.info
import dev.treset.treelauncher.util.checkUpdateOnStart

enum class LoginState(val actionAllowed: Boolean) {
    NOT_LOGGED_IN(true),
    AUTHENTICATING(false),
    LOGGED_IN(false),
    OFFLINE(false),
    FAILED(true)
}

data class LoginContextData(
    val loginState: LoginState,
    val userAuth: UserAuth,
    val logout: () -> Unit,
) {
    fun isOffline() = loginState == LoginState.OFFLINE
    fun isLoggedIn() = loginState == LoginState.LOGGED_IN
}

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
    var interactiveData by remember { mutableStateOf<InteractiveData?>(null) }
    var loginStep by remember { mutableStateOf<AuthenticationStep?>(null) }
    var showContent by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf(language().appLanguage) }

    var updateChecked by remember { mutableStateOf(false) }

    var popupData: PopupData? by remember { mutableStateOf(null) }

    val notificationColor = MaterialTheme.colorScheme.info
    val offlineNotification = remember {
        NotificationData(
            color = notificationColor,
            content = {
                Text(
                    strings().login.offlineNotification(),
                    softWrap = true
                )
            }
        )
    }

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
                    userAuth().cancelAuthentication()

                },
                {
                    loginState = it
                    if(it == LoginState.LOGGED_IN && updateChecked) {
                        showContent = true
                    }
                },
                { loginStep = it }
            )
        }
    }

    LaunchedEffect(showContent) {
        if(!showContent) {
            AppContext.dismissNotification(offlineNotification)
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

    interactiveData?.let {
        LoginPopup(it) {
            interactiveData = null
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
                            { interactiveData = it },
                            {
                                loginState = it
                                interactiveData = null
                            },
                            {
                                loginStep = it
                                if(it != null && it != AuthenticationStep.MICROSOFT) {
                                    interactiveData = null
                                }
                            }
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

                Text(
                    strings().login.offline(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f).let { if (loginState.actionAllowed) it else it.disabledContent() },
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clickable(
                            enabled = loginState.actionAllowed,
                        ) {
                            loginState = LoginState.OFFLINE
                            AppContext.addNotification(offlineNotification)
                            showContent = true
                        }
                        .pointerHoverIcon(PointerIcon.Hand)
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
                            LoginState.AUTHENTICATING -> strings().login.label.authenticating(loginStep)
                            LoginState.LOGGED_IN -> strings().login.label.success(userAuth().minecraftUser?.username)
                            LoginState.OFFLINE -> strings().login.label.offline()
                            LoginState.FAILED -> strings().login.label.failure()
                        },
                        style = MaterialTheme.typography.titleMedium,
                    )

                    if(loginState == LoginState.AUTHENTICATING) {
                        Text(
                            text = strings().login.label.authenticatingSub(loginStep),
                            style = MaterialTheme.typography.labelMedium,
                        )

                        Button(
                            onClick = {
                                loginState = LoginState.FAILED
                                userAuth().cancelAuthentication()
                                showContent = false
                            },
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(strings().login.cancel())
                        }
                    }

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
private fun LoginPopup(
    data: InteractiveData,
    close: () -> Unit
) {
    PopupOverlay(
        type = PopupType.NONE,
        titleRow = { Text(strings().login.popup.title() ) },
        buttonRow = {
            Button(
                onClick = close,
                color = MaterialTheme.colorScheme.error
            ) {
                Text(strings().login.popup.close())
            }
            Button(
                onClick = {
                    data.url.openInBrowser()
                    data.userCode.copyToClipboard()
                }
            ) {
                Text(strings().login.popup.open())
            }
        }
    ) {
        strings().login.popup.content().let {
            Text(it.first)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    data.url,
                    style = MaterialTheme.typography.titleSmall,
                )
                IconButton(
                    onClick = { data.url.copyToClipboard() },
                    icon = icons().copy,
                    tooltip = strings().login.popup.copyContent(),
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Text(it.second)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    data.userCode,
                    style = MaterialTheme.typography.titleSmall,
                )
                IconButton(
                    onClick = { data.userCode.copyToClipboard() },
                    icon = icons().copy,
                    tooltip = strings().login.popup.copyContent(),
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

private fun startLogin(
    keepLoggedIn: Boolean,
    onInteractiveData: (InteractiveData) -> Unit,
    onState: (LoginState) -> Unit,
    onStep: (AuthenticationStep?) -> Unit
) {
    onState(LoginState.AUTHENTICATING)
    userAuth().authenticate(
        keepLoggedIn,
        onInteractiveData,
        onStep
    ) {
        it?.let {
            onStep(null)
            onState(LoginState.FAILED)
            AppContext.error(it)
        } ?: run {
            onStep(null)
            onState(LoginState.LOGGED_IN)
        }
    }
}