package com.retrotv.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import com.retrotv.app.ui.theme.TvAccentGreen

@Composable
fun RetroButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    accentColor: Color = TvAccentGreen,
    content: @Composable RowScope.() -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) accentColor else Color(0xFF161616),
        animationSpec = tween(160),
        label = "retroButtonBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF0A0A0A) else Color(0xFFD8D8D8),
        animationSpec = tween(160),
        label = "retroButtonContent"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.03f else 1f,
        animationSpec = tween(160),
        label = "retroButtonScale"
    )

    var finalModifier = modifier
        .scale(scale)
        .onFocusChanged { isFocused = it.isFocused }

    if (focusRequester != null) {
        finalModifier = finalModifier.focusRequester(focusRequester)
    }

    Button(
        onClick = onClick,
        modifier = finalModifier,
        colors = ButtonDefaults.colors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            focusedContainerColor = backgroundColor,
            focusedContentColor = contentColor
        ),
        content = content
    )
}
