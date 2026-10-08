package com.example.cyberpunkandroid.effects

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.asComposeRenderEffect
import org.intellij.lang.annotations.Language

/**
 * AGSL sources for the Cyber runtime-shader effects. Compiled and bound by [cyberShaderEffect];
 * every shader declares `uniform float2 resolution` and `uniform shader contents`.
 */
internal object CyberShaders {

    // Samples each square at its center so the whole screen resolves from coarse pixels to sharp content.
    @Language("AGSL")
    const val PixelateShader = """
        uniform float2 resolution;
        uniform float pixelSize;
        uniform shader contents;

        half4 main(float2 fragCoord) {
            float2 center = (floor(fragCoord / pixelSize) + 0.5) * pixelSize;
            return contents.eval(min(center, resolution - 0.5));
        }
    """

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createPixelateShader() = RuntimeShader(PixelateShader)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun pixelateEffect(shader: RuntimeShader, width: Float, height: Float, pixelSize: Float): androidx.compose.ui.graphics.RenderEffect {
        shader.setFloatUniform("resolution", width, height)
        shader.setFloatUniform("pixelSize", pixelSize)
        return android.graphics.RenderEffect.createRuntimeShaderEffect(shader, "contents").asComposeRenderEffect()
    }

    /**
     * Names of the uniforms that [PixelFrontierShader] declares. Kotlin code sets uniforms only through
     * these names, and a unit test checks that the shader source still declares every one of them.
     * (Setting a uniform that the shader does not declare throws at runtime.)
     */
    internal object PixelFrontierUniforms {
        const val Resolution = "resolution"
        const val Contents = "contents"
        const val Axis = "axis"
        const val Frontier = "frontier"
        const val RevealBehind = "revealBehind"
        const val BlockSize = "blockSize"
        const val BandWidth = "bandWidth"
        const val EdgeWidth = "edgeWidth"
        const val EdgeThreshold = "edgeThreshold"
        const val SplitOffset = "splitOffset"
        const val Jitter = "jitter"
        const val EdgeColor = "edgeColor"

        /** Every uniform name, used by the source-consistency test. */
        val All = listOf(
            Resolution, Contents, Axis, Frontier, RevealBehind, BlockSize, BandWidth,
            EdgeWidth, EdgeThreshold, SplitOffset, Jitter, EdgeColor,
        )
    }

    /**
     * Visual: a frontier line sweeps across one screen layer. On the side where this layer is hidden, the
     * shader returns transparent pixels. On the side where it is shown, pixels near the frontier are
     * grouped into square blocks. Blocks are largest at the frontier and halve in size in steps
     * (three steps, set by `LevelCount` in the source) away from it, so the picture sharpens with distance. Each block is shown
     * or hidden as a whole. Each column of largest blocks moves the frontier by its own random amount
     * (up to `jitter`), so the frontier is a ragged, stepped edge, not a straight line.
     *
     * Edge effects, only inside the band: (1) a neon outline on a block side when the neighbor block
     * differs in brightness, (2) the same outline on a block side that touches the frontier, and
     * (3) a red/blue color split along the sweep axis, strongest at the frontier.
     *
     * Two layers (old screen and new screen) use the same shader with opposite [revealBehind] values.
     * Both layers compute the same blocks, so their visible areas never overlap and never leave a gap.
     *
     * Cost per pixel: one sample outside the band. Inside the band, three samples for the block color,
     * plus up to four neighbor samples, and only for pixels within `edgeWidth` of a block side.
     */
    @Language("AGSL")
    const val PixelFrontierShader = """
        uniform float2 resolution;
        uniform shader contents;
        uniform float2 axis;            // (1,0) sweeps left to right, (0,1) sweeps top to bottom
        uniform float frontier;         // frontier position along the axis, in pixels
        uniform float revealBehind;     // 1.0: show pixels the frontier has passed (new screen). 0.0: show pixels ahead of it (old screen)
        uniform float blockSize;        // edge length of the largest block, in pixels
        uniform float bandWidth;        // distance from the frontier over which blocks exist, in pixels
        uniform float edgeWidth;        // outline thickness, in pixels
        uniform float edgeThreshold;    // smallest brightness difference (0..1) that draws an outline
        uniform float splitOffset;      // peak red/blue shift along the axis, in pixels
        uniform float jitter;           // total random shift of the frontier per block column, in pixels (0 = straight line)
        layout(color) uniform half4 edgeColor;

        // Number of block sizes in the band. Each step halves the block size.
        const int LevelCount = 3;
        // Rec. 709 weights that turn a color into a brightness value.
        const half3 LumaWeights = half3(0.2126, 0.7152, 0.0722);

        half luma(half3 rgb) {
            return dot(rgb, LumaWeights);
        }

        // Pseudo-random value from 0 to 1 for a whole number. The two constants are the widely used
        // "sine hash" values. Any pair that scrambles the sine output works.
        float hash(float n) {
            return fract(sin(n * 12.9898) * 43758.5453);
        }

        // Frontier position for the block column that contains a point. Each column of the largest blocks
        // gets its own shift of up to half of jitter in each direction, so the frontier is ragged.
        // The value depends only on the point, so the old and new screen layers agree.
        float frontierAt(float2 point) {
            float2 across = float2(axis.y, axis.x); // direction across the sweep
            float column = floor(dot(point, across) / blockSize);
            return frontier + (hash(column) - 0.5) * jitter;
        }

        // Reads the content at a point, kept half a pixel inside the layer so the read is always valid.
        half4 sampleInside(float2 point) {
            return contents.eval(clamp(point, float2(0.5), resolution - 0.5));
        }

        // Returns 1.0 when this pixel lies within edgeWidth of a block side that needs an outline.
        // A side needs an outline when the neighbor block is on the other side of the frontier,
        // or when the neighbor block differs in brightness by at least edgeThreshold.
        float sideEdge(float2 neighborCenter, half centerLuma, float distanceToSide, bool ahead) {
            if (distanceToSide > edgeWidth) {
                return 0.0;
            }
            bool neighborAhead = dot(neighborCenter, axis) - frontierAt(neighborCenter) >= 0.0;
            if (neighborAhead != ahead) {
                return 1.0;
            }
            half neighborLuma = luma(sampleInside(neighborCenter).rgb);
            return abs(centerLuma - neighborLuma) >= edgeThreshold ? 1.0 : 0.0;
        }

        half4 main(float2 fragCoord) {
            // Pick the block for this pixel. The loop runs from the finest level to the coarsest,
            // so the coarsest level whose reach covers the block wins.
            float2 samplePoint = fragCoord;
            float cell = 0.0; // 0.0 means the pixel is outside the band
            for (int i = 0; i < LevelCount; i++) {
                int level = LevelCount - 1 - i;
                float size = blockSize / exp2(float(level));
                float2 center = (floor(fragCoord / size) + 0.5) * size;
                float reach = bandWidth * float(level + 1) / float(LevelCount);
                if (abs(dot(center, axis) - frontierAt(center)) < reach) {
                    samplePoint = center;
                    cell = size;
                }
            }

            // The whole block is shown or hidden, based on the block center.
            bool ahead = dot(samplePoint, axis) - frontierAt(samplePoint) >= 0.0;
            bool visible = revealBehind > 0.5 ? !ahead : ahead;
            if (!visible) {
                return half4(0.0);
            }
            if (cell == 0.0) {
                return contents.eval(fragCoord);
            }

            // Closeness to the frontier: 1.0 on the frontier, 0.0 at the edge of the band.
            float proximity = 1.0 - clamp(abs(dot(samplePoint, axis) - frontierAt(samplePoint)) / bandWidth, 0.0, 1.0);

            // Block color with a red/blue split along the axis.
            float2 split = axis * splitOffset * proximity;
            half4 middle = sampleInside(samplePoint);
            half4 color = half4(sampleInside(samplePoint + split).r, middle.g, sampleInside(samplePoint - split).b, middle.a);

            // Outlines on the four block sides.
            float2 local = fragCoord - (samplePoint - 0.5 * cell); // pixel position inside the block, 0..cell
            half centerLuma = luma(middle.rgb);
            float edge = 0.0;
            edge = max(edge, sideEdge(samplePoint + float2(-cell, 0.0), centerLuma, local.x, ahead));
            edge = max(edge, sideEdge(samplePoint + float2(cell, 0.0), centerLuma, cell - local.x, ahead));
            edge = max(edge, sideEdge(samplePoint + float2(0.0, -cell), centerLuma, local.y, ahead));
            edge = max(edge, sideEdge(samplePoint + float2(0.0, cell), centerLuma, cell - local.y, ahead));

            half strength = half(edge * proximity * edgeColor.a);
            color.rgb = mix(color.rgb, edgeColor.rgb * color.a, strength);
            return color;
        }
    """

    /** Settings for one frame of [PixelFrontierShader]. All distances are in pixels. */
    internal data class PixelFrontierSpec(
        /** Layer width. */
        val width: Float,
        /** Layer height. */
        val height: Float,
        /** True when the frontier sweeps top to bottom. False when it sweeps left to right. */
        val vertical: Boolean,
        /** Frontier position along the sweep axis. */
        val frontier: Float,
        /** True for the new screen (shown behind the frontier). False for the old screen. */
        val revealBehind: Boolean,
        /** Edge length of the largest block. */
        val blockSize: Float,
        /** Width of the band in which blocks exist. */
        val bandWidth: Float,
        /** Outline thickness. */
        val edgeWidth: Float,
        /** Smallest brightness difference (0..1) that draws an outline. */
        val edgeThreshold: Float,
        /** Peak red/blue shift along the sweep axis. */
        val splitOffset: Float,
        /** Total random shift of the frontier per block column. 0 gives a straight frontier. */
        val jitter: Float,
        /** Outline color as a packed ARGB integer. */
        val edgeColorArgb: Int,
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createPixelFrontierShader() = RuntimeShader(PixelFrontierShader)

    /**
     * Writes [spec] into [shader] and wraps it as a render effect for one screen layer.
     *
     * @param shader A shader made by [createPixelFrontierShader]. Use one instance per screen layer.
     * @param spec Settings for this frame.
     * @return A render effect that applies the shader to the layer content.
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    internal fun pixelFrontierEffect(shader: RuntimeShader, spec: PixelFrontierSpec): androidx.compose.ui.graphics.RenderEffect {
        shader.setFloatUniform(PixelFrontierUniforms.Resolution, spec.width, spec.height)
        shader.setFloatUniform(
            PixelFrontierUniforms.Axis,
            if (spec.vertical) 0f else 1f,
            if (spec.vertical) 1f else 0f,
        )
        shader.setFloatUniform(PixelFrontierUniforms.Frontier, spec.frontier)
        shader.setFloatUniform(PixelFrontierUniforms.RevealBehind, if (spec.revealBehind) 1f else 0f)
        shader.setFloatUniform(PixelFrontierUniforms.BlockSize, spec.blockSize)
        shader.setFloatUniform(PixelFrontierUniforms.BandWidth, spec.bandWidth)
        shader.setFloatUniform(PixelFrontierUniforms.EdgeWidth, spec.edgeWidth)
        shader.setFloatUniform(PixelFrontierUniforms.EdgeThreshold, spec.edgeThreshold)
        shader.setFloatUniform(PixelFrontierUniforms.SplitOffset, spec.splitOffset)
        shader.setFloatUniform(PixelFrontierUniforms.Jitter, spec.jitter)
        shader.setColorUniform(PixelFrontierUniforms.EdgeColor, spec.edgeColorArgb)
        return android.graphics.RenderEffect
            .createRuntimeShaderEffect(shader, PixelFrontierUniforms.Contents)
            .asComposeRenderEffect()
    }


    @Language("AGSL")
    const val GlowShader = """
        uniform float2 resolution;
        uniform float intensity;
        uniform float dropoffPower;
        layout(color) uniform half4 glowColor;
        uniform shader contents;

        half4 main(float2 fragCoord) {
            half4 blurred = contents.eval(fragCoord);
            float alpha = blurred.a;

            // Non-linear amplification for a "hot core" neon effect
            float boostedAlpha = clamp(alpha * intensity, 0.0, 1.0);

            // Core becomes white-hot when density is extremely high
            float coreGlow = pow(alpha, dropoffPower) * intensity * 0.4;
            half3 finalColor = mix(glowColor.rgb, half3(1.0), clamp(coreGlow, 0.0, 1.0));

            return half4(finalColor * boostedAlpha, boostedAlpha);
        }
    """
    /** Upper bound on sparks per [SparkShader]; AGSL loops need a constant bound. */
    const val MaxSparks: Int = 64

    @Language("AGSL")
    const val CrtShader = """
        uniform float2 resolution;
        uniform float time;
        uniform shader contents;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / resolution;

            // Convert to -1 to 1 space
            float2 crtUV = uv * 2.0 - 1.0;

            // Barrel distortion (fisheye)
            float r2 = dot(crtUV, crtUV);
            crtUV *= 1.0 + r2 * 0.20;

            // Convert back to 0 to 1 space
            uv = crtUV * 0.5 + 0.5;

            if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {
                return half4(0.0, 0.0, 0.0, 1.0);
            }

            // Chromatic aberration at edges
            float aberration = r2 * 0.015;

            half r = contents.eval(float2(uv.x - aberration, uv.y) * resolution).r;
            half g = contents.eval(uv * resolution).g;
            half b = contents.eval(float2(uv.x + aberration, uv.y) * resolution).b;
            half a = contents.eval(uv * resolution).a;

            float vignette = smoothstep(1.6, 0.3, length(crtUV));

            return half4(r * vignette, g * vignette, b * vignette, a);
        }
    """

    @Language("AGSL")
    const val OverloadShader = """
        uniform float2 resolution;
        uniform float time;
        uniform float intensity;
        uniform shader contents;

        float random(float2 st) {
            return fract(sin(dot(st.xy, float2(12.9898, 78.233))) * 43758.5453123);
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / resolution;

            // Generate horizontal jitter
            float jitter = random(float2(uv.y, time)) * 2.0 - 1.0;
            float jitterOffset = jitter * 0.08 * intensity;

            // RGB splitting
            float rOffset = 0.04 * intensity + jitterOffset;
            float bOffset = -0.04 * intensity + jitterOffset;

            float2 rCoords = float2(fragCoord.x + rOffset * resolution.x, fragCoord.y);
            float2 bCoords = float2(fragCoord.x + bOffset * resolution.x, fragCoord.y);
            float2 gCoords = float2(fragCoord.x + jitterOffset * resolution.x, fragCoord.y);

            half4 rColor = contents.eval(rCoords);
            half4 gColor = contents.eval(gCoords);
            half4 bColor = contents.eval(bCoords);
            half4 baseColor = contents.eval(fragCoord);

            half alpha = max(baseColor.a, max(rColor.a, max(gColor.a, bColor.a)));

            return half4(rColor.r, gColor.g, bColor.b, alpha);
        }
    """

    @Language("AGSL")
    const val ScanlinesShader = """
        uniform float2 resolution;
        uniform float time;
        uniform float scanlineOpacity;
        uniform float spacing;
        layout(color) uniform half4 scanlineColor;
        uniform shader contents;

        half4 main(float2 fragCoord) {
            half4 color = contents.eval(fragCoord);

            float scan = sin((fragCoord.y - time * 30.0) * (6.2831853 / spacing)) * 0.5 + 0.5;
            float lineIntensity = scan * scanlineOpacity;

            if (scanlineColor.a == 0.0) {
                // Darken scanline bands over the content
                color.rgb *= (1.0 - lineIntensity * 0.7);
            } else {
                color.rgb = mix(color.rgb, scanlineColor.rgb, lineIntensity);
            }

            return color;
        }
    """

    @Language("AGSL")
    const val NoiseShader = """
        uniform float2 resolution;
        uniform float time;
        uniform float intensity;
        uniform shader contents;

        float random(float2 st) {
            return fract(sin(dot(st.xy, float2(12.9898, 78.233))) * 43758.5453123);
        }

        half4 main(float2 fragCoord) {
            half4 color = contents.eval(fragCoord);
            float2 uv = fragCoord / resolution;

            float noise = random(uv + time) * 2.0 - 1.0;
            color.rgb += noise * intensity;

            return color;
        }
    """

    @Language("AGSL")
    const val SparkShader = """
        uniform float2 resolution;
        uniform float time;
        uniform float intensity;
        uniform float speed;
        layout(color) uniform half4 primaryColor;
        layout(color) uniform half4 secondaryColor;
        layout(color) uniform half4 warningColor;
        uniform shader contents;

        float sparkHash(float n) {
            return fract(sin(n * 127.1) * 43758.5453123);
        }

        // LinearInSlowOut easing curve
        float linearInSlowOut(float t) {
            return t * t * (3.0 - 2.0 * t);
        }

        half4 main(float2 fragCoord) {
            half4 color = contents.eval(fragCoord);
            float2 uv = (fragCoord - 0.5 * resolution) / min(resolution.x, resolution.y);

            float containerHeightScale = 4.0;
            float totalSparkGlow = 0.0;
            half3 accumColor = half3(0.0);

            for (float i = 0.0; i < 32.0; i += 1.0) {
                // Popcorn burst cycle timing driven by speed
                float burstCycle = fract(time * speed * (0.8 + sparkHash(i * 3.1) * 1.2) + sparkHash(i * 7.7));
                float t = burstCycle;
                float easedT = linearInSlowOut(t);

                // Initial random X velocity. ±1.6 (after the 0.25 scale below) lets sparks
                // travel up to ~40% of the short side from center, so the burst fans out wide
                float vx = (sparkHash(i * 13.5) * 2.0 - 1.0) * 1.6;

                // Initial Y velocity: Always starts going UP (-vy) with 10% randomness
                float vyBase = -1.2;
                float vyRandom = 1.0 + (sparkHash(i * 19.3) * 0.2 - 0.1);
                float vy = vyBase * vyRandom;

                // Gravity downward acceleration
                float gravity = 1.8;

                // Position in 4x height space
                float2 sparkPos = float2(
                    vx * easedT,
                    (vy * easedT + 0.5 * gravity * easedT * easedT) * containerHeightScale
                ) * 0.25;

                float d = length(uv - sparkPos);

                // Fine pinpoint ember. 0.00025 keeps the saturated (white-hot) core to ~1.6% of the
                // short side at birth. Quadratic (1-t)^2 decay shrinks that core to nothing over the lifetime
                float baseRadius = 0.00025 * intensity;
                float life = 1.0 - t;
                float currentRadius = baseRadius * life * life;

                // Stochastic flicker over time
                float timeStep = floor(time * 30.0);
                float flicker = 0.85 + 0.3 * sparkHash(i * 43.2 + timeStep);

                // Pinpoint spark intensity
                float sparkIntensity = (currentRadius * flicker) / (d * d + 0.000005);

                // Plasma colorscale interpolating Material 3 & Cyber semantic keywords
                half3 plasmaColor;
                if (t < 0.25) {
                    float localT = t / 0.25;
                    plasmaColor = mix(half3(1.0, 1.0, 1.0), warningColor.rgb, localT);
                } else if (t < 0.60) {
                    float localT = (t - 0.25) / 0.35;
                    plasmaColor = mix(primaryColor.rgb, secondaryColor.rgb, localT);
                } else {
                    float localT = (t - 0.60) / 0.40;
                    plasmaColor = mix(secondaryColor.rgb, warningColor.rgb * 0.5, localT);
                }

                // Rapid cooling alpha decay
                float alphaDecay = (1.0 - t);

                accumColor += plasmaColor * sparkIntensity * alphaDecay;
            }

            color.rgb += accumColor;
            return color;
        }
    """

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createOverloadShader(): RuntimeShader = RuntimeShader(OverloadShader)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun overloadEffect(
        shader: RuntimeShader,
        width: Float,
        height: Float,
        time: Float,
        intensity: Float
    ): androidx.compose.ui.graphics.RenderEffect {
        shader.setFloatUniform("resolution", width, height)
        shader.setFloatUniform("time", time)
        shader.setFloatUniform("intensity", intensity)
        return android.graphics.RenderEffect.createRuntimeShaderEffect(
            shader, "contents"
        ).asComposeRenderEffect()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createScanlinesShader(): RuntimeShader = RuntimeShader(ScanlinesShader)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun scanlinesEffect(
        shader: RuntimeShader,
        width: Float,
        height: Float,
        time: Float,
        opacity: Float,
        spacing: Float,
        colorArgb: Int
    ): androidx.compose.ui.graphics.RenderEffect {
        shader.setFloatUniform("resolution", width, height)
        shader.setFloatUniform("time", time)
        shader.setFloatUniform("scanlineOpacity", opacity)
        shader.setFloatUniform("spacing", spacing)
        shader.setColorUniform("scanlineColor", colorArgb)
        return android.graphics.RenderEffect.createRuntimeShaderEffect(
            shader, "contents"
        ).asComposeRenderEffect()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createNoiseShader(): RuntimeShader = RuntimeShader(NoiseShader)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun noiseEffect(
        shader: RuntimeShader,
        width: Float,
        height: Float,
        time: Float,
        intensity: Float
    ): androidx.compose.ui.graphics.RenderEffect {
        shader.setFloatUniform("resolution", width, height)
        shader.setFloatUniform("time", time)
        shader.setFloatUniform("intensity", intensity)
        return android.graphics.RenderEffect.createRuntimeShaderEffect(
            shader, "contents"
        ).asComposeRenderEffect()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createSparkShader(): RuntimeShader = RuntimeShader(SparkShader)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun sparkEffect(
        shader: RuntimeShader,
        width: Float,
        height: Float,
        time: Float,
        intensity: Float,
        speed: Float,
        primaryColorArgb: Int,
        secondaryColorArgb: Int,
        warningColorArgb: Int
    ): androidx.compose.ui.graphics.RenderEffect {
        shader.setFloatUniform("resolution", width, height)
        shader.setFloatUniform("time", time)
        shader.setFloatUniform("intensity", intensity)
        shader.setFloatUniform("speed", speed)
        shader.setColorUniform("primaryColor", primaryColorArgb)
        shader.setColorUniform("secondaryColor", secondaryColorArgb)
        shader.setColorUniform("warningColor", warningColorArgb)
        return android.graphics.RenderEffect.createRuntimeShaderEffect(
            shader, "contents"
        ).asComposeRenderEffect()
    }
}
