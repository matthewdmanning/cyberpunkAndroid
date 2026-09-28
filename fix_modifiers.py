import re
import sys

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. cyberDatastream 100000L comment
content = content.replace('value = (frameTime % 100000L) / 1000f', 
'// 100000L prevents Float precision loss over long uptimes while keeping loop smooth\n                value = (frameTime % 100000L) / 1000f')

# 2. cyberOverload RenderEffect
old_overload = '''    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createOverloadShader() }
        graphicsLayer {
            if (activeIntensity == 0f && bounceAmount.toPx() == 0f) return@graphicsLayer
            if (activeIntensity > 0f) {
                renderEffect = CyberShaders.overloadEffect(
                    shader = shader,
                    width = size.width,
                    height = size.height,
                    time = time * timeScale,
                    intensity = activeIntensity
                )
            }
            if (bounceAmount.toPx() > 0f) {
                translationY = -bounceAmount.toPx() * kotlin.math.abs(kotlin.math.sin(time * 10f)).toFloat()
            }
        }'''
new_overload = '''    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createOverloadShader() }
        val effect = remember(shader) {
            android.graphics.RenderEffect.createRuntimeShaderEffect(shader, "contents").asComposeRenderEffect()
        }
        graphicsLayer {
            if (activeIntensity == 0f && bounceAmount.toPx() == 0f) return@graphicsLayer
            if (activeIntensity > 0f) {
                shader.setFloatUniform("resolution", size.width, size.height)
                shader.setFloatUniform("time", time * timeScale)
                shader.setFloatUniform("intensity", activeIntensity)
                renderEffect = effect
            }
            if (bounceAmount.toPx() > 0f) {
                translationY = -bounceAmount.toPx() * kotlin.math.abs(kotlin.math.sin(time * 10f)).toFloat()
            }
        }'''
content = content.replace(old_overload, new_overload)

# 3. cyberScanlines RenderEffect
old_scanlines = '''    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createScanlinesShader() }
        graphicsLayer {
            if (activeOpacity == 0f) return@graphicsLayer
            renderEffect = CyberShaders.scanlinesEffect(
                shader = shader,
                width = size.width,
                height = size.height,
                time = time * speed,
                opacity = activeOpacity,
                spacing = spacing.toPx().coerceAtLeast(1f),
                colorArgb = scanlineColor.toArgb()
            )
        }'''
new_scanlines = '''    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createScanlinesShader() }
        val effect = remember(shader) {
            android.graphics.RenderEffect.createRuntimeShaderEffect(shader, "contents").asComposeRenderEffect()
        }
        graphicsLayer {
            if (activeOpacity == 0f) return@graphicsLayer
            shader.setFloatUniform("resolution", size.width, size.height)
            shader.setFloatUniform("time", time * speed)
            shader.setFloatUniform("scanlineOpacity", activeOpacity)
            shader.setFloatUniform("spacing", spacing.toPx().coerceAtLeast(1f))
            shader.setColorUniform("scanlineColor", scanlineColor.toArgb())
            renderEffect = effect
        }'''
content = content.replace(old_scanlines, new_scanlines)

# 4. cyberNoise RenderEffect
old_noise = '''    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createNoiseShader() }
        graphicsLayer {
            if (activeOpacity == 0f) return@graphicsLayer
            renderEffect = CyberShaders.noiseEffect(
                shader = shader,
                width = size.width,
                height = size.height,
                time = time,
                intensity = activeOpacity
            )
        }'''
new_noise = '''    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createNoiseShader() }
        val effect = remember(shader) {
            android.graphics.RenderEffect.createRuntimeShaderEffect(shader, "contents").asComposeRenderEffect()
        }
        graphicsLayer {
            if (activeOpacity == 0f) return@graphicsLayer
            shader.setFloatUniform("resolution", size.width, size.height)
            shader.setFloatUniform("time", time)
            shader.setFloatUniform("intensity", activeOpacity)
            renderEffect = effect
        }'''
content = content.replace(old_noise, new_noise)

# 5. cyberCrt RenderEffect
old_crt = '''    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createCrtShader() }
        graphicsLayer {
            if (!isActive) return@graphicsLayer
            renderEffect = CyberShaders.crtEffect(shader, size.width, size.height, time)
            clip = true
        }'''
new_crt = '''    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { CyberShaders.createCrtShader() }
        val effect = remember(shader) {
            android.graphics.RenderEffect.createRuntimeShaderEffect(shader, "contents").asComposeRenderEffect()
        }
        graphicsLayer {
            if (!isActive) return@graphicsLayer
            shader.setFloatUniform("resolution", size.width, size.height)
            shader.setFloatUniform("time", time)
            renderEffect = effect
            clip = true
        }'''
content = content.replace(old_crt, new_crt)


# 6. cyberNeonBorderFlow Object allocations
old_border_flow = '''    drawWithContent {
        drawContent()
        if (activeGlowRadius == 0f && progress.value == 0f) return@drawWithContent

        val strokeWidthPx = strokeWidth.toPx()
        val glowWidthPx = glowWidth?.toPx() ?: (strokeWidthPx + (activeGlowRadius * density))
        val outline = shape.createOutline(size, layoutDirection, this)

        val glowBrush = cyberSweepGradient(
            center = Offset(size.width / 2f, size.height / 2f),
            colors = colors.map { it.copy(alpha = 0.35f * (activeGlowRadius / glowRadius.value.coerceAtLeast(0.1f))) },
            rotation = progress.value
        )
        val sharpBrush = cyberSweepGradient(
            center = Offset(size.width / 2f, size.height / 2f),
            colors = colors,
            rotation = progress.value
        )

        drawNeonBorderFlow(
            outline = outline,
            sharpBrush = sharpBrush,
            glowBrush = glowBrush,
            strokeWidthPx = strokeWidthPx,
            glowWidthPx = glowWidthPx
        )
    }'''
new_border_flow = '''    val mappedColors = remember(colors, activeGlowRadius, glowRadius.value) {
        colors.map { it.copy(alpha = 0.35f * (activeGlowRadius / glowRadius.value.coerceAtLeast(0.1f))) }
    }
    val glowBrush = remember(mappedColors) {
        com.example.cyberpunkandroid.utils.CyberSweepGradientBrush(mappedColors)
    }
    val sharpBrush = remember(colors) {
        com.example.cyberpunkandroid.utils.CyberSweepGradientBrush(colors)
    }

    drawWithContent {
        drawContent()
        if (activeGlowRadius == 0f && progress.value == 0f) return@drawWithContent

        val strokeWidthPx = strokeWidth.toPx()
        val glowWidthPx = glowWidth?.toPx() ?: (strokeWidthPx + (activeGlowRadius * density))
        val outline = shape.createOutline(size, layoutDirection, this)

        glowBrush.rotation = progress.value
        sharpBrush.rotation = progress.value

        drawNeonBorderFlow(
            outline = outline,
            sharpBrush = sharpBrush,
            glowBrush = glowBrush,
            strokeWidthPx = strokeWidthPx,
            glowWidthPx = glowWidthPx
        )
    }'''
content = content.replace(old_border_flow, new_border_flow)

# 7. cyberAtmosphericGlow Misuse of drawWithCache
old_atmospheric = '''fun Modifier.cyberAtmosphericGlow(
    color: Color = Color.Cyan,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberAtmosphericGlow", appendedA11y, customA11y).drawWithCache {
    onDrawWithContent {
        // Draw 3 layers of native blur
        val paint20 = Paint().apply {
            colorFilter = ColorFilter.tint(color)
            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(20f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        val paint40 = Paint().apply {
            colorFilter = ColorFilter.tint(color.copy(alpha = 0.5f))
            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(40f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        val paint80 = Paint().apply {
            colorFilter = ColorFilter.tint(color.copy(alpha = 0.25f))
            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(80f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
        
        drawIntoCanvas { canvas ->
            canvas.drawRect(Rect(Offset.Zero, size), paint80)
            canvas.drawRect(Rect(Offset.Zero, size), paint40)
            canvas.drawRect(Rect(Offset.Zero, size), paint20)
        }
        
        drawContent()
    }
}'''
new_atmospheric = '''fun Modifier.cyberAtmosphericGlow(
    color: Color = Color.Cyan,
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberAtmosphericGlow", appendedA11y, customA11y).drawWithCache {
    val paint20 = Paint().apply {
        colorFilter = ColorFilter.tint(color)
        asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(20f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    val paint40 = Paint().apply {
        colorFilter = ColorFilter.tint(color.copy(alpha = 0.5f))
        asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(40f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    val paint80 = Paint().apply {
        colorFilter = ColorFilter.tint(color.copy(alpha = 0.25f))
        asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(80f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }

    onDrawWithContent {
        drawIntoCanvas { canvas ->
            canvas.drawRect(Rect(Offset.Zero, size), paint80)
            canvas.drawRect(Rect(Offset.Zero, size), paint40)
            canvas.drawRect(Rect(Offset.Zero, size), paint20)
        }
        
        drawContent()
    }
}'''
content = content.replace(old_atmospheric, new_atmospheric)

# 8. cyberInnerGlow misuse
old_inner_glow = '''fun Modifier.cyberInnerGlow(
    color: Color = Color.Cyan.copy(alpha = 0.3f),
    radius: Dp = 8.dp,
    shape: Shape = CutCornerShape(12.dp),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberInnerGlow", appendedA11y, customA11y).drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    onDrawWithContent {
        drawContent()
        
        val radiusPx = radius.toPx()
        if (radiusPx > 0) {
            val paint = Paint().apply {
                this.color = color
                asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(radiusPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
            
            drawContext.canvas.save()
                        val path = androidx.compose.ui.graphics.Path()
            when (outline) {
                is androidx.compose.ui.graphics.Outline.Rectangle -> path.addRect(outline.rect)
                is androidx.compose.ui.graphics.Outline.Rounded -> path.addRoundRect(outline.roundRect)
                is androidx.compose.ui.graphics.Outline.Generic -> path.addPath(outline.path)
            }
            drawContext.canvas.clipPath(path)
            
            // Draw a stroke that is blurred inwards
                        drawPath(
                path = path,
                color = color,
                style = Stroke(width = radiusPx * 2)
            )
            drawContext.canvas.restore()
        }
    }
}'''
new_inner_glow = '''fun Modifier.cyberInnerGlow(
    color: Color = Color.Cyan.copy(alpha = 0.3f),
    radius: Dp = 8.dp,
    shape: Shape = CutCornerShape(12.dp),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberInnerGlow", appendedA11y, customA11y).drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val radiusPx = radius.toPx()
    val path = androidx.compose.ui.graphics.Path()
    when (outline) {
        is androidx.compose.ui.graphics.Outline.Rectangle -> path.addRect(outline.rect)
        is androidx.compose.ui.graphics.Outline.Rounded -> path.addRoundRect(outline.roundRect)
        is androidx.compose.ui.graphics.Outline.Generic -> path.addPath(outline.path)
    }

    val paint = if (radiusPx > 0) Paint().apply {
        this.color = color
        this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
        this.strokeWidth = radiusPx * 2
        asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(radiusPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
    } else null

    onDrawWithContent {
        drawContent()
        
        if (paint != null) {
            drawContext.canvas.save()
            drawContext.canvas.clipPath(path)
            
            // Draw a stroke that is blurred inwards
            drawIntoCanvas { canvas ->
                canvas.drawPath(path, paint)
            }
            drawContext.canvas.restore()
        }
    }
}'''
content = content.replace(old_inner_glow, new_inner_glow)


# 9. cyberBorder Path alloc
old_border = '''fun Modifier.cyberBorder(
    width: Dp = 1.dp,
    color: Color = Color.Cyan,
    shape: Shape = CutCornerShape(12.dp),
    pathEffect: PathEffect? = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberBorder", appendedA11y, customA11y).drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    onDrawWithContent {
        drawContent()
                val path = androidx.compose.ui.graphics.Path()
        when (outline) {
            is androidx.compose.ui.graphics.Outline.Rectangle -> path.addRect(outline.rect)
            is androidx.compose.ui.graphics.Outline.Rounded -> path.addRoundRect(outline.roundRect)
            is androidx.compose.ui.graphics.Outline.Generic -> path.addPath(outline.path)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = width.toPx(), pathEffect = pathEffect)
        )
    }
}'''
new_border = '''fun Modifier.cyberBorder(
    width: Dp = 1.dp,
    color: Color = Color.Cyan,
    shape: Shape = CutCornerShape(12.dp),
    pathEffect: PathEffect? = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
    appendedA11y: String? = null,
    customA11y: String? = null
): Modifier = this.cyberSemantics("CyberBorder", appendedA11y, customA11y).drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val path = androidx.compose.ui.graphics.Path()
    when (outline) {
        is androidx.compose.ui.graphics.Outline.Rectangle -> path.addRect(outline.rect)
        is androidx.compose.ui.graphics.Outline.Rounded -> path.addRoundRect(outline.roundRect)
        is androidx.compose.ui.graphics.Outline.Generic -> path.addPath(outline.path)
    }

    onDrawWithContent {
        drawContent()
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = width.toPx(), pathEffect = pathEffect)
        )
    }
}'''
content = content.replace(old_border, new_border)

with open('cyberpunkandroid/src/main/java/com/example/cyberpunkandroid/effects/CyberModifiers.kt', 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated CyberModifiers.kt")
