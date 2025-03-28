package com.example.botchat.converstation.ui.dialog.widget

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun AnimatedCircle(
    modifier: Modifier = Modifier,
    isRecording: Boolean,
    isBotSpeaking: Boolean,
    sizeDp: Int = 200
) {
    var isBlack by remember { mutableStateOf(false) }

    LaunchedEffect(isRecording, isBotSpeaking) {
        if (!isRecording && !isBotSpeaking) {
            delay(10000)
            if (!isRecording && !isBotSpeaking) {
                isBlack = true
            }
        } else {
            isBlack = false
        }
    }

    val expandAnimation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        expandAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    // Animation phóng to thu nhỏ liên tục khi thu âm
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isRecording) 1.2f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    // Tính scale cuối cùng
    val baseScale = expandAnimation.value * (if (isBlack) 2f/3f else 1f) // Khi đen thì scale = 2/3
    val finalScale = if (isRecording) baseScale * pulseScale else baseScale

    if (isBlack) {
        Box(
            modifier = modifier
                .size(sizeDp.dp)
                .graphicsLayer {
                    scaleX = finalScale
                    scaleY = finalScale
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val circleRadius = min(size.width, size.height) / 2f
                drawCircle(
                    color = Color.Black,
                    center = center,
                    radius = circleRadius
                )
            }
        }
    } else {
        val colorShift1 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )

        val colorShift2 by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )

        val waveOffsetRaw by infiniteTransition.animateFloat(
            initialValue = -0.15f,
            targetValue = 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )
        val waveOffset = if (isBotSpeaking) waveOffsetRaw else 0f

        val colorA = lerp(Color(0xFF29DD9D), Color(0xFFE6E3C5), colorShift1)
        val colorB = lerp(Color(0xFF1592D6), Color(0xFFAEF2F8), colorShift2)
        val colorC = lerp(Color(0xFF0066FF), Color(0xFF00FFAE), colorShift1)
        val gradientColors = listOf(colorA, colorB, colorC, Color(0xFFDBDBDB))

        Box(
            modifier = modifier
                .size(sizeDp.dp)
                .graphicsLayer {
                    scaleX = finalScale
                    scaleY = finalScale
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val circleRadius = min(size.width, size.height) / 2f
                val centerShifted = center.copy(x = center.x + waveOffset * circleRadius)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = gradientColors,
                        center = centerShifted,
                        radius = circleRadius * 1.2f
                    ),
                    center = center,
                    radius = circleRadius
                )
            }
        }
    }
}