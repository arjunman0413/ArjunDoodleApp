package com.example.doodleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BrushPattern {
    SOLID,
    DOTTED
}

data class DrawingStroke(
    val path: Path,
    val color: Color,
    val width: Float,
    val pattern: BrushPattern = BrushPattern.SOLID
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DoodleApp()
            }
        }
    }
}

@Composable
fun DoodleApp() {
    var brushSize by remember { mutableFloatStateOf(10f) }
    var brushColor by remember { mutableStateOf(Color(0xFF6C63FF)) }
    var brushPattern by remember { mutableStateOf(BrushPattern.SOLID) }
    val strokes = remember { mutableStateListOf<DrawingStroke>() }
    val undoStack = remember { mutableStateListOf<DrawingStroke>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Toolbar
            TopToolbar(
                brushSize = brushSize,
                brushColor = brushColor,
                brushPattern = brushPattern,
                onBrushSizeChange = { brushSize = it },
                onBrushColorChange = { brushColor = it },
                onBrushPatternChange = { brushPattern = it },
                onClear = {
                    undoStack.addAll(strokes)
                    strokes.clear()
                },
                onUndo = {
                    if (strokes.isNotEmpty()) {
                        val lastStroke = strokes.removeLast()
                        undoStack.add(lastStroke)
                    }
                },
                canUndo = strokes.isNotEmpty()
            )

            // Canvas Area
            DrawingCanvas(
                strokes = strokes,
                brushColor = brushColor,
                brushSize = brushSize,
                brushPattern = brushPattern,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun TopToolbar(
    brushSize: Float,
    brushColor: Color,
    brushPattern: BrushPattern,
    onBrushSizeChange: (Float) -> Unit,
    onBrushColorChange: (Color) -> Unit,
    onBrushPatternChange: (BrushPattern) -> Unit,
    onClear: () -> Unit,
    onUndo: () -> Unit,
    canUndo: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F3460).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✨ Doodle",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontSize = 28.sp
                )

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onUndo,
                        enabled = canUndo,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF),
                            disabledContainerColor = Color(0xFF2A2A4E)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("↶ Undo", fontSize = 16.sp)
                    }

                    Button(
                        onClick = onClear,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE94560)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🗑 Clear All", fontSize = 16.sp)
                    }
                }
            }

            // Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brush Size Section
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Brush Size",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB8B8D1),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = brushSize,
                            onValueChange = onBrushSizeChange,
                            valueRange = 5f..50f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF6C63FF),
                                activeTrackColor = Color(0xFF6C63FF),
                                inactiveTrackColor = Color(0xFF2A2A4E)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${brushSize.toInt()}px",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }

                // Pattern Section
                Column {
                    Text(
                        text = "Brush Style",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB8B8D1),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PatternSelector(
                        selectedPattern = brushPattern,
                        onPatternSelected = onBrushPatternChange
                    )
                }
            }

            // Color Picker Section
            Column {
                Text(
                    text = "Color Palette",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB8B8D1),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                ColorPickerGrid(
                    selectedColor = brushColor,
                    onColorSelected = onBrushColorChange
                )
            }
        }
    }
}

@Composable
fun ColorPickerGrid(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color(0xFF6C63FF), Color(0xFFE94560), Color(0xFFFF6B9D),
        Color(0xFFFFA500), Color(0xFFFFC93C), Color(0xFF00D9FF),
        Color(0xFF06FFA5), Color(0xFF4ECDC4), Color(0xFF9B59B6),
        Color(0xFFFF1493), Color(0xFF2D3561), Color.White
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        colors.chunked(4).forEach { rowColors ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (color == selectedColor) {
                                    Modifier.border(
                                        3.dp,
                                        Color.White,
                                        CircleShape
                                    )
                                } else {
                                    Modifier.border(
                                        1.dp,
                                        Color(0xFF2A2A4E),
                                        CircleShape
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { onColorSelected(color) },
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
fun PatternSelector(
    selectedPattern: BrushPattern,
    onPatternSelected: (BrushPattern) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(BrushPattern.SOLID, BrushPattern.DOTTED).forEach { pattern ->
            Button(
                onClick = { onPatternSelected(pattern) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedPattern == pattern)
                        Color(0xFF6C63FF)
                    else Color(0xFF2A2A4E)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = if (pattern == BrushPattern.SOLID) "━━" else "- - -",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun DrawingCanvas(
    strokes: MutableList<DrawingStroke>,
    brushColor: Color,
    brushSize: Float,
    brushPattern: BrushPattern,
    modifier: Modifier = Modifier
) {
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentStroke by remember { mutableStateOf<DrawingStroke?>(null) }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .pointerInput(brushColor, brushSize) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val newPath = Path().apply {
                                moveTo(offset.x, offset.y)
                            }
                            currentPath = newPath
                            currentStroke = DrawingStroke(newPath, brushColor, brushSize, brushPattern)
                        },
                        onDrag = { change, _ ->
                            currentPath?.lineTo(change.position.x, change.position.y)
                            change.consume()
                        },
                        onDragEnd = {
                            currentStroke?.let { strokes.add(it) }
                            currentPath = null
                            currentStroke = null
                        }
                    )
                }
        ) {
            strokes.forEach { stroke ->
                drawPath(
                    path = stroke.path,
                    color = stroke.color,
                    style = Stroke(
                        width = stroke.width,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = if (stroke.pattern == BrushPattern.DOTTED)
                            androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(stroke.width, stroke.width)
                            )
                        else null
                    )
                )
            }

            currentStroke?.let { stroke ->
                drawPath(
                    path = stroke.path,
                    color = stroke.color,
                    style = Stroke(
                        width = stroke.width,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = if (stroke.pattern == BrushPattern.DOTTED)
                            androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(stroke.width, stroke.width)
                            )
                        else null
                    )
                )
            }
        }
    }
}