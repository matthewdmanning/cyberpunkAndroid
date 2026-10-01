package com.example.cyberpunkandroid.effects

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.asComposeRenderEffect
import org.intellij.lang.annotations.Language

object CyberShaders {
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

    /** Use this function to prepare the API 33 pixelation shader for a screen transition.
     * Inputs: None. Dependencies: [PixelateShader].
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createPixelateShader() = RuntimeShader(PixelateShader)

    /** Use this function to pixelate a composable layer during a screen transition.
     * Inputs: shader is the cached AGSL program; width and height are the layer dimensions;
     * pixelSize is the animated square edge length in pixels. Dependencies: [PixelateShader].
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun pixelateEffect(shader: RuntimeShader, width: Float, height: Float, pixelSize: Float): androidx.compose.ui.graphics.RenderEffect {
        shader.setFloatUniform("resolution", width, height)
        shader.setFloatUniform("pixelSize", pixelSize)
        return android.graphics.RenderEffect.createRuntimeShaderEffect(shader, "contents").asComposeRenderEffect()
    }

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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createCrtShader() = RuntimeShader(CrtShader)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun crtEffect(
        shader: RuntimeShader,
        width: Float,
        height: Float,
        time: Float
    ): androidx.compose.ui.graphics.RenderEffect {
        shader.setFloatUniform("resolution", width, height)
        shader.setFloatUniform("time", time)
        return android.graphics.RenderEffect.createRuntimeShaderEffect(
            shader,
            "contents"
        ).asComposeRenderEffect()
    }

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
