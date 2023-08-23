package com.github.jing332.tts_server_android.compose.nav

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.ui.AppLog
import com.github.jing332.tts_server_android.utils.ClipboardUtils
import com.github.jing332.tts_server_android.utils.toast
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogScreen(
    modifier: Modifier,
    list: List<AppLog>,
    lazyListState: LazyListState = rememberLazyListState()
) {
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val context = LocalContext.current
    Box(Modifier.fillMaxSize()) {
        val isAtBottom by remember {
            derivedStateOf {
                val layoutInfo = lazyListState.layoutInfo
                val visibleItemsInfo = layoutInfo.visibleItemsInfo
                if (layoutInfo.totalItemsCount <= 0) {
                    false
                } else {
                    val lastVisibleItem = visibleItemsInfo.last()
                    lastVisibleItem.index > layoutInfo.totalItemsCount - 5
                }
            }
        }

        LaunchedEffect(list.size) {
            if (isAtBottom && list.isNotEmpty())
                scope.launch {
                    lazyListState.animateScrollToItem(list.size - 1)
                }
        }

        LazyColumn(modifier, state = lazyListState) {
            itemsIndexed(list, key = { index, _ -> index }) { _, item ->
                val style = MaterialTheme.typography.bodyMedium
                val spanned = remember {
                    HtmlCompat.fromHtml(item.msg, HtmlCompat.FROM_HTML_MODE_COMPACT)
                        .toAnnotatedString()
                }

                Text(
                    text = spanned,
                    style = style.copy(color = MaterialTheme.colorScheme.onBackground),
                    lineHeight = style.lineHeight * 0.75f,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {
                                println("onClick")
                            },
                            onLongClick = {
                                view.isHapticFeedbackEnabled = true
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                ClipboardUtils.copyText("tts-server-log", spanned.text)
                                context.toast(R.string.copied)
                            }
                        )
                        .padding(horizontal = 4.dp)
                )
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp), visible = !isAtBottom
        ) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        kotlin.runCatching {
                            lazyListState.scrollToItem(list.size - 1)
                        }
                    }
                }) {
                Icon(
                    Icons.Default.KeyboardDoubleArrowDown,
                    stringResource(id = R.string.move_to_bottom)
                )
            }
        }
    }
}

fun Spanned.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    val spanned = this@toAnnotatedString
    append(spanned.toString())
    getSpans(0, spanned.length, Any::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)
        when (span) {
            is StyleSpan -> when (span.style) {
                Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                Typeface.BOLD_ITALIC -> addStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    ), start, end
                )
            }

            is UnderlineSpan -> addStyle(
                SpanStyle(textDecoration = TextDecoration.Underline),
                start,
                end
            )

            is ForegroundColorSpan -> addStyle(
                SpanStyle(color = Color(span.foregroundColor)),
                start,
                end
            )
        }
    }
}