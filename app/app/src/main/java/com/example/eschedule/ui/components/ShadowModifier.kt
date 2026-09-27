package com.example.eschedule.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Figma-accurate soft shadow:
 *   X: 0  Y: 0  Blur: 10dp  Spread: -5dp  Color: #000 @ 10%
 *
 * Spread is simulated by shrinking the shadow rect inward by [spread].
 * Works on all API levels (no BlurMaskFilter hardware restrictions apply here
 * because we use setShadowLayer on the framework paint).
 */
fun Modifier.appShadow(
    blur: Dp = 10.dp,
    spread: Dp = (-5).dp,
    color: Color = Color.Black.copy(alpha = 0.10f),
    cornerRadius: Dp = 12.dp,
): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val fp = paint.asFrameworkPaint()
        fp.color = android.graphics.Color.TRANSPARENT
        fp.setShadowLayer(
            blur.toPx(),
            0f,
            0f,
            color.toArgb(),
        )
        val inset = -spread.toPx()   // spread=-5dp → inset=+5px
        val r = cornerRadius.toPx()
        canvas.drawRoundRect(
            left   = inset,
            top    = inset,
            right  = size.width  - inset,
            bottom = size.height - inset,
            radiusX = r,
            radiusY = r,
            paint   = paint,
        )
    }
}
