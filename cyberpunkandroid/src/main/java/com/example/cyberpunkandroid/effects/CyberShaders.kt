package com.example.cyberpunkandroid.effects

import org.intellij.lang.annotations.Language

/**
 * AGSL sources for the Cyber runtime-shader effects. Compiled and bound by [cyberShaderEffect];
 * every shader declares `uniform float2 resolution` and `uniform shader contents`.
 */
internal object CyberShaders {

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

}
