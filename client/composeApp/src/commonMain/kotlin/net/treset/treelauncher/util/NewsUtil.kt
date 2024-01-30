package net.treset.treelauncher.util

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import net.treset.treelauncher.app
import net.treset.treelauncher.backend.config.appSettings
import net.treset.treelauncher.backend.news.News
import net.treset.treelauncher.backend.news.news
import net.treset.treelauncher.generic.Button
import net.treset.treelauncher.generic.PopupData
import net.treset.treelauncher.localization.strings
import java.io.IOException

fun getNewsPopup(
    close: () -> Unit,
    displayOther: Boolean = true,
    acknowledgeImportant: Boolean = true,
    displayAcknowledged: Boolean = true
): PopupData = PopupData(
        content = {
            var currentNews: News? by remember { mutableStateOf(null) }

            LaunchedEffect(Unit) {
                try {
                    currentNews = news().let { nws ->
                        nws.apply {
                            if(!displayAcknowledged) {
                                important = important?.filter { !appSettings().acknowledgedNews.contains(it.id) }
                            }
                            if(!displayOther) {
                                other = null
                            }
                        }
                    }.also { nws ->
                        if(acknowledgeImportant) {
                            nws.important?.forEach {
                                if(!appSettings().acknowledgedNews.contains(it.id)) {
                                    appSettings().acknowledgedNews.add(it.id)
                                }
                            }
                        }
                    }
                } catch(e: IOException) {
                    app().error(e)
                }
            }

            currentNews?.let {nws ->
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!nws.important.isNullOrEmpty()) {
                        Text(
                            strings().news.important(),
                            style = MaterialTheme.typography.titleMedium,
                        )

                        val content = remember {
                            val sb = StringBuilder("<hr/>")
                            nws.important!!.forEach {
                                sb.append("<h3>${it.title}</h3>${it.content}<hr/>")
                            }

                            print(sb.toString())
                            htmlToAnnotatedString(sb.toString())
                        }

                        Text(
                            content,
                            softWrap = true,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(0.dp, 800.dp)
                        )
                    }

                    if (!nws.other.isNullOrEmpty()) {
                        Text(
                            strings().news.other(),
                            style = MaterialTheme.typography.titleMedium
                        )

                        val content = remember {
                            val sb = StringBuilder()
                            nws.other!!.forEach {
                                sb.append("<h3>${it.title}</h3>${it.content}<hr/>")
                            }

                            htmlToAnnotatedString(sb.toString())
                        }

                        Text(
                            content,
                            softWrap = true,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(0.dp, 800.dp)
                        )
                    }
                }
            } ?: Text(strings().news.loading())
        },
        buttonRow  = {
            Button(
                onClick = {
                    close()
                }
            ) {
                Text(strings().news.close())
            }
        },
    )

fun <T> List<T>.allContainedIn(other: List<T>): Boolean {
    return this.all { other.contains(it) }
}