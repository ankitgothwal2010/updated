package com.ankit.familytools.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * AnkitG Family Tools
 * Developed by Ankit Gothwal
 * Copyright ©️ 2026 Ankit Gothwal. All rights reserved.
 *
 * Smooth finger-drawing canvas for capturing electronic signatures.
 */

data class DrawingPath(
    val path: androidx.compose.ui.graphics.Path,
    val color: Color = Color.Black,
    val strokeWidth: Float = 6f
)

@Composable
fun SignatureDrawingCanvas(
    modifier: Modifier = Modifier,
    strokeColor: Color = Color(0xFF1E3A8A), // Clean dark blue ink
    strokeWidth: Float = 7f,
    onPathsChanged: (Boolean) -> Unit = {},
    exportCallback: (()-> Bitmap)? = null,
    onExportReady: ((Bitmap) -> Unit)? = null
) {
    val paths = remember { mutableStateListOf<DrawingPath>() }
    var currentPath by remember { mutableStateOf<androidx.compose.ui.graphics.Path?>(null) }
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize(800, 400)) }

    fun captureBitmap(): Bitmap {
        val w = canvasSize.width.coerceAtLeast(200)
        val h = canvasSize.height.coerceAtLeast(100)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        paths.forEach { dp ->
            paint.color = android.graphics.Color.argb(
                (dp.color.alpha * 255).toInt(),
                (dp.color.red * 255).toInt(),
                (dp.color.green * 255).toInt(),
                (dp.color.blue * 255).toInt()
            )
            paint.strokeWidth = dp.strokeWidth
            canvas.drawPath(dp.path.asAndroidPath(), paint)
        }

        return bitmap
    }

    LaunchedEffect(paths.size) {
        onPathsChanged(paths.isNotEmpty())
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val newPath = androidx.compose.ui.graphics.Path().apply {
                                moveTo(offset.x, offset.y)
                            }
                            currentPath = newPath
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPath?.lineTo(change.position.x, change.position.y)
                        },
                        onDragEnd = {
                            currentPath?.let {
                                paths.add(DrawingPath(it, strokeColor, strokeWidth))
                                currentPath = null
                            }
                        },
                        onDragCancel = {
                            currentPath = null
                        }
                    )
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                canvasSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())

                paths.forEach { dp ->
                    drawPath(
                        path = dp.path,
                        color = dp.color,
                        style = Stroke(
                            width = dp.strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                currentPath?.let {
                    drawPath(
                        path = it,
                        color = strokeColor,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            if (paths.isEmpty() && currentPath == null) {
                Text(
                    text = "Sign here with your finger\nयहाँ अपनी उंगली से दस्तखत करें",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                OutlinedButton(
                    onClick = {
                        if (paths.isNotEmpty()) {
                            paths.removeAt(paths.size - 1)
                        }
                    },
                    enabled = paths.isNotEmpty(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Undo")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = {
                        paths.clear()
                    },
                    enabled = paths.isNotEmpty(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear / साफ़ करें")
                }
            }

            Button(
                onClick = {
                    if (paths.isNotEmpty() && onExportReady != null) {
                        onExportReady(captureBitmap())
                    }
                },
                enabled = paths.isNotEmpty(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Use Signature / हस्ताक्षर लगाएं")
            }
        }
    }
}
