package dev.treset.treelauncher.util

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import dev.treset.treelauncher.AppContext
import dev.treset.treelauncher.backend.config.AppSettings
import dev.treset.treelauncher.backend.news.News
import dev.treset.treelauncher.backend.news.news
import dev.treset.treelauncher.generic.Button
import dev.treset.treelauncher.generic.NotificationData
import dev.treset.treelauncher.generic.PopupOverlay
import dev.treset.treelauncher.generic.Text
import dev.treset.treelauncher.localization.Strings
import dev.treset.treelauncher.style.info
import java.io.IOException

@Composable
fun News() {
    var newOnly: Boolean by remember { mutableStateOf(false) }

    var popupVisible by remember { mutableStateOf(false) }
    LaunchedEffect(AppContext.newsIndex) {
        if (AppContext.newsIndex > 0) {
            newOnly = false
            popupVisible = true
        }
    }

    var notification: NotificationData? by remember { mutableStateOf(null) }

    var currentNews: News? by remember { mutableStateOf(null) }

    val notificationColor = MaterialTheme.colorScheme.info
    LaunchedEffect(Unit) {
        try {
            currentNews = news().also { nws ->
                if (nws.important?.map { it.id }?.allContainedIn(AppSettings.acknowledgedNews) == false) {
                    notification = NotificationData(
                        onClick = {
                            newOnly = false
                            popupVisible = true
                        },
                        color = notificationColor,
                        content = {
                            Text(Strings.news.notification())
                        },
                    ).also { AppContext.addNotification(it) }
                }
            }
        } catch (e: IOException) {
            AppContext.errorIfOnline(IOException("Unable to load News", e))
        }
    }

    if(popupVisible) {
        PopupOverlay(
            content = {
                val important = remember(currentNews) {
                    if(newOnly) {
                        currentNews?.important?.filter { !AppSettings.acknowledgedNews.contains(it.id) }
                    } else {
                        currentNews?.important
                    }
                }

                val other = remember(currentNews) {
                    if(newOnly) {
                        null
                    } else {
                        currentNews?.other
                    }
                }

                    if (!important.isNullOrEmpty()) {
                        Text(
                            Strings.news.important(),
                            style = MaterialTheme.typography.titleMedium,
                        )

                        val content = remember {
                            val sb = StringBuilder("<hr/>")
                            important.forEach {
                                sb.append("<h3>${it.title}</h3>${it.content}<hr/>")
                            }

                            htmlToAnnotatedString(sb.toString())
                        }

                        Text(
                            content,
                            softWrap = true,
                            modifier = Modifier.widthIn(0.dp, 800.dp)
                        )
                    }

                    if (!other.isNullOrEmpty()) {
                        Text(
                            Strings.news.other(),
                            style = MaterialTheme.typography.titleMedium
                        )

                        val content = remember {
                            val sb = StringBuilder()
                            other.forEach {
                                sb.append("<h3>${it.title}</h3>${it.content}<hr/>")
                            }

                            htmlToAnnotatedString(sb.toString())
                        }

                        Text(
                            content,
                            softWrap = true,
                            modifier = Modifier.widthIn(0.dp, 800.dp)
                        )
                    }

                    if(other.isNullOrEmpty() && important.isNullOrEmpty()) {
                        Text(
                            Strings.news.none(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
            },
            buttonRow  = {
                Button(
                    onClick = {
                        popupVisible = false
                        notification?.let { AppContext.dismissNotification(it) }
                        currentNews?.important?.forEach {
                            if(!AppSettings.acknowledgedNews.contains(it.id)) {
                                AppSettings.acknowledgedNews.add(it.id)
                            }
                        }
                    }
                ) {
                    Text(Strings.news.close())
                }
            },
        )
    }
}

fun <T> List<T>.allContainedIn(other: List<T>): Boolean {
    return this.all { other.contains(it) }
}