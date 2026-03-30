package com.locapin.mobile.feature.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.locapin.mobile.domain.model.MapArea
import com.locapin.mobile.domain.model.MapAttraction

@Composable
fun SegmentedSanJuanMap(
    areas: List<MapArea>,
    selectedAreaId: String?,
    visibleAttractions: List<MapAttraction>,
    selectedAttractionId: String?,
    onAreaTapped: (String) -> Unit,
    onPinTapped: (String) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .pointerInput(areas, visibleAttractions, selectedAttractionId) {
                detectTapGesturesWithHitTest(
                    areas = areas,
                    visibleAttractions = visibleAttractions,
                    onAreaTapped = onAreaTapped,
                    onPinTapped = onPinTapped
                )
            }
    ) {
        areas.forEach { area ->
            val path = Path().apply {
                area.polygon.firstOrNull()?.let { first -> moveTo(first.x * size.width, first.y * size.height) }
                area.polygon.drop(1).forEach { p -> lineTo(p.x * size.width, p.y * size.height) }
                close()
            }
            val selected = area.id == selectedAreaId
            drawPath(
                path = path,
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.32f) else MaterialTheme.colorScheme.surfaceVariant,
                style = Fill
            )
            drawPath(
                path = path,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                style = Stroke(width = if (selected) 6f else 3f, cap = StrokeCap.Round)
            )
        }

        visibleAttractions.forEach { attraction ->
            val point = Offset(attraction.mapPoint.x * size.width, attraction.mapPoint.y * size.height)
            val selectedPin = attraction.id == selectedAttractionId
            drawCircle(
                color = if (selectedPin) MaterialTheme.colorScheme.tertiary else Color(0xFF1C1B1F),
                center = point,
                radius = if (selectedPin) 14f else 11f
            )
            drawCircle(color = Color.White, center = point, radius = 5f)
        }
    }
}

private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectTapGesturesWithHitTest(
    areas: List<MapArea>,
    visibleAttractions: List<MapAttraction>,
    onAreaTapped: (String) -> Unit,
    onPinTapped: (String) -> Unit
) {
    androidx.compose.foundation.gestures.detectTapGestures { tap ->
        val width = size.width
        val height = size.height
        val normalizedX = tap.x / width
        val normalizedY = tap.y / height

        val pinHit = visibleAttractions.firstOrNull { attraction ->
            val dx = (normalizedX - attraction.mapPoint.x) * width
            val dy = (normalizedY - attraction.mapPoint.y) * height
            (dx * dx + dy * dy) <= (18f * 18f)
        }
        if (pinHit != null) {
            onPinTapped(pinHit.id)
        } else {
            areas.firstOrNull { area -> area.contains(normalizedX, normalizedY) }?.let { onAreaTapped(it.id) }
        }
    }
}

private fun MapArea.contains(px: Float, py: Float): Boolean {
    var intersections = 0
    polygon.indices.forEach { i ->
        val a = polygon[i]
        val b = polygon[(i + 1) % polygon.size]
        val cond1 = (a.y > py) != (b.y > py)
        if (cond1) {
            val xinters = (py - a.y) * (b.x - a.x) / ((b.y - a.y).takeIf { it != 0f } ?: 0.0001f) + a.x
            if (px < xinters) intersections++
        }
    }
    return intersections % 2 == 1
}

@Composable
fun MapInstruction() {
    Text(
        text = "Tap an area to explore attractions",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
