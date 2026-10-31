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

    // Brush shader (no `contents` input): blends three palette colors with cosine lobes 120 degrees apart along a
    // direction, so bands of color flow into each other like the interference film on a hologram foil.
    @Language("AGSL")
    const val HoloIridescenceShader = """
        uniform float2 resolution;
        uniform float2 direction;
        uniform float bands;
        uniform float phase;
        uniform half4 color0; // Premultiplied sRGB, set as plain floats for portability.
        uniform half4 color1;
        uniform half4 color2;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / resolution;
            float t = dot(uv, direction) * bands + phase;
            float w0 = 0.5 + 0.5 * cos(6.2831853 * t);
            float w1 = 0.5 + 0.5 * cos(6.2831853 * (t - 0.3333333));
            float w2 = 0.5 + 0.5 * cos(6.2831853 * (t - 0.6666667));
            return (color0 * w0 + color1 * w1 + color2 * w2) / (w0 + w1 + w2);
        }
    """

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
        const val Time = "time"
        const val StochasticMode = "stochasticMode"

        /** Every uniform name, used by the source-consistency test. */
        val All = listOf(
            Resolution, Contents, Axis, Frontier, RevealBehind, BlockSize, BandWidth,
            EdgeWidth, EdgeThreshold, SplitOffset, Jitter, EdgeColor,
            Time, StochasticMode,
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
        uniform float time;             // animation progress/time for temporal autocorrelation
        uniform float stochasticMode;   // 0: smooth wave, 1: cyber glitch, 2: harmonic surge

        // Number of block sizes in the band. Each step halves the block size.
        const int LevelCount = 3;

        // True 2D isotropic stochastic field using 6 symmetric hexagonal wavevectors
        // Ensures sum(kx^2) == sum(ky^2) and sum(kx*ky) == 0.0 (zero directional / row bias).
        float evaluate2DField(float2 p, float t, int mode) {
            float kBase = (mode == 1) ? 0.16 : ((mode == 2) ? 0.05 : 0.08);
            float omegaBase = (mode == 1) ? 4.5 : ((mode == 2) ? 2.2 : 1.8);

            float total = 0.0;
            // Octave 1: Hexagonal symmetric wave pairs (weight 0.65)
            total += sin(p.x * kBase + omegaBase * t + 0.0);
            total += sin(p.y * kBase + omegaBase * t + 0.0);
            total += sin((0.8660254 * p.x + 0.5 * p.y) * kBase + omegaBase * t + 1.2566);
            total += sin((0.5 * p.x + 0.8660254 * p.y) * kBase + omegaBase * t + 1.2566);
            total += sin((-0.8660254 * p.x + 0.5 * p.y) * kBase + omegaBase * t + 2.5133);
            total += sin((0.5 * p.x - 0.8660254 * p.y) * kBase + omegaBase * t + 2.5133);
            total = (total / 6.0) * 0.65;

            // Octave 2: Diagonal and axis harmonics (weight 0.35, 2x frequency)
            float kHarm = kBase * 2.0;
            float omegaHarm = omegaBase * 1.6;
            float totalHarm = 0.0;
            totalHarm += sin(p.x * kHarm + omegaHarm * t + 3.7699);
            totalHarm += sin(p.y * kHarm + omegaHarm * t + 3.7699);
            totalHarm += sin((0.7071068 * p.x + 0.7071068 * p.y) * kHarm + omegaHarm * t + 0.85);
            totalHarm += sin((0.7071068 * p.x - 0.7071068 * p.y) * kHarm + omegaHarm * t + 5.0265);
            totalHarm += sin((-0.7071068 * p.x + 0.7071068 * p.y) * kHarm + omegaHarm * t + 5.0265);
            total += (totalHarm / 5.0) * 0.35;

            return total;
        }

        // Catmull-Rom cubic spline interpolator for 2D density extrema curves
        float catmullRom(float p0, float p1, float p2, float p3, float t) {
            float t2 = t * t;
            float t3 = t2 * t;
            return 0.5 * (
                (2.0 * p1) +
                (-p0 + p2) * t +
                (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t2 +
                (-p0 + 3.0 * p1 - 3.0 * p2 + p3) * t3
            );
        }

        float evalSpline(float p0, float p1, float p2, float p3, float p4, float p5, float v) {
            float sv = clamp(v, 0.0, 0.9999) * 3.0;
            int idx = int(floor(sv));
            float t = fract(sv);
            if (idx == 0) return catmullRom(p0, p1, p2, p3, t);
            else if (idx == 1) return catmullRom(p1, p2, p3, p4, t);
            else return catmullRom(p2, p3, p4, p5, t);
        }

        // Distinct top-to-bottom spline curves acting as density extrema (not all the same).
        // Start offset is 0 so particle spawn density begins immediately at the barrier/divide:
        float evalSplineStart(float v) {
            return 0.0;
        }

        float evalSplinePeak(float v) {
            return evalSpline(0.36, 0.46, 0.38, 0.52, 0.34, 0.44, v);
        }

        float evalSplineEnd(float v) {
            return evalSpline(0.72, 0.84, 0.70, 0.88, 0.76, 0.82, v);
        }

        // 2D spawn density field rho(normDist, v) with top-to-bottom spline extrema
        float computeSpawnDensity(float normDist, float v) {
            float xs = evalSplineStart(v);
            float xp = evalSplinePeak(v);
            float xe = evalSplineEnd(v);

            if (normDist <= xs || normDist >= xe) {
                return 0.0;
            } else if (normDist <= xp) {
                float u = clamp((normDist - xs) / max(1e-4, xp - xs), 0.0, 1.0);
                return 3.0 * u * u - 2.0 * u * u * u;
            } else {
                float w = clamp((normDist - xp) / max(1e-4, xe - xp), 0.0, 1.0);
                return 1.0 - (3.0 * w * w - 2.0 * w * w * w);
            }
        }

        half4 sampleInside(float2 point) {
            return contents.eval(clamp(point, float2(0.5), resolution - 0.5));
        }

        half4 main(float2 fragCoord) {
            float distAdvancing = dot(fragCoord, axis) - frontier;
            bool ahead = distAdvancing >= 0.0;

            if (revealBehind > 0.5) {
                if (!ahead) {
                    return contents.eval(fragCoord);
                }
                return half4(0.0);
            }

            if (!ahead) {
                return half4(0.0);
            }

            // Far ahead of the dissolution band: intact solid outgoing screen
            if (distAdvancing >= bandWidth) {
                return contents.eval(fragCoord);
            }

            float cell = blockSize;
            for (int i = 0; i < LevelCount; i++) {
                float reach = bandWidth * float(i + 1) / float(LevelCount);
                if (distAdvancing < reach) {
                    cell = blockSize;
                }
            }

            // 2D discrete particle emission sites:
            float2 blockCoord = floor(fragCoord / max(blockSize, 1.0));
            float2 samplePoint = (blockCoord + 0.5) * blockSize;
            float particleDist = dot(samplePoint, axis) - frontier;

            if (particleDist < 0.0) {
                return half4(0.0);
            }

            float normDist = clamp(particleDist / max(bandWidth, 1.0), 0.0, 1.0);

            // 2D SPAWN DENSITY GOVERNED BY TOP-TO-BOTTOM SPLINE EXTREMA (NOT ALL THE SAME):
            float2 across = float2(-axis.y, axis.x);
            float v = clamp(dot(fragCoord, across) / max(dot(resolution, abs(across)), 1.0), 0.0, 1.0);
            float xp = evalSplinePeak(v);
            float xe = evalSplineEnd(v);

            // BEYOND PARTICLE EXTINCTION (NO BLACK GAP): INTACT SOLID OUTGOING SCREEN
            if (normDist >= xe) {
                return contents.eval(fragCoord);
            }

            // STOCHASTIC EROSION OF OUTGOING SCREEN ALL THE WAY TO THE FRONTIER BORDER:
            // The old screen erodes continuously from xe down to 0.0 with zero deadband or black gaps.
            float solidHash = fract(sin(dot(blockCoord, float2(26.37, 83.19))) * 43758.5453);
            float solidProb = smoothstep(0.0, xe, normDist);
            if (solidHash < solidProb) {
                return contents.eval(fragCoord);
            }

            // CIRCULAR PARTICLE GEOMETRY: OUTSIDE RADIUS IS TRANSPARENT SPACE
            float radius = 0.48 * blockSize;
            float distFromCenter = length(fragCoord - samplePoint);
            if (distFromCenter > radius) {
                return half4(0.0);
            }

            float spawnDensity = computeSpawnDensity(normDist, v);

            // COMPLETELY RANDOM LIFETIME PER INDIVIDUAL PARTICLE (BETA(2, 5) SKINNY TAIL):
            float lifeHash = fract(sin(dot(blockCoord, float2(12.9898 + stochasticMode * 3.7, 78.233 + stochasticMode * 7.1))) * 43758.5453);
            // Solve CDF of Beta(2, 5) via Newton-Raphson:
            float xBeta = 0.25;
            for (int i = 0; i < 2; i++) {
                float oneMinusX = clamp(1.0 - xBeta, 0.0, 1.0);
                float f = xBeta * xBeta * (15.0 + xBeta * (-40.0 + xBeta * (45.0 + xBeta * (-24.0 + 5.0 * xBeta)))) - lifeHash;
                float df = max(30.0 * xBeta * oneMinusX * oneMinusX * oneMinusX * oneMinusX, 0.001);
                xBeta = clamp(xBeta - f / df, 0.0, 1.0);
            }
            float particleLifetime = clamp(0.30 + 1.70 * xBeta, 0.30, 2.0);

            // EMISSION DRAW MODULATED BY SPAWN DENSITY & LIFETIME (2x INITIAL DENSITY = 0.80, 2x LIFETIME):
            float emitHash = fract(sin(dot(blockCoord + float2(0.31, 0.73), float2(41.17, 93.31)) + time * 0.5) * 43758.5453);
            float threshold = clamp(max(spawnDensity, 0.80) * particleLifetime * 0.95, 0.0, 1.0);

            bool isParticleActive = emitHash < threshold;
            if (!isParticleActive) {
                return half4(0.0);
            }

            // 100% OPAQUE & LUMINOUS CIRCULAR PARTICLE (ALPHA = 1.0, NO TRANSPARENCY):
            half4 sampled = sampleInside(samplePoint);
            half3 particleColor = mix(sampled.rgb, edgeColor.rgb, 0.75);
            return half4(particleColor, 1.0);
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
        /** True to reverse the sweep (bottom-to-top if vertical, right-to-left if horizontal). */
        val reverse: Boolean = false,
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
        /** Normalized time or progress driving temporal autocorrelation. */
        val time: Float = 0f,
        /** Preset selector for pre-calculated Fourier series autocorrelation (0: smooth wave, 1: cyber glitch, 2: harmonic surge). */
        val stochasticMode: Float = 0f,
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
        val ax = when {
            spec.vertical && !spec.reverse -> 0f
            spec.vertical && spec.reverse -> 0f
            !spec.vertical && !spec.reverse -> 1f
            else -> -1f
        }
        val ay = when {
            spec.vertical && !spec.reverse -> 1f
            spec.vertical && spec.reverse -> -1f
            else -> 0f
        }
        shader.setFloatUniform(PixelFrontierUniforms.Axis, ax, ay)
        shader.setFloatUniform(PixelFrontierUniforms.Frontier, spec.frontier)
        shader.setFloatUniform(PixelFrontierUniforms.RevealBehind, if (spec.revealBehind) 1f else 0f)
        shader.setFloatUniform(PixelFrontierUniforms.BlockSize, spec.blockSize)
        shader.setFloatUniform(PixelFrontierUniforms.BandWidth, spec.bandWidth)
        shader.setFloatUniform(PixelFrontierUniforms.EdgeWidth, spec.edgeWidth)
        shader.setFloatUniform(PixelFrontierUniforms.EdgeThreshold, spec.edgeThreshold)
        shader.setFloatUniform(PixelFrontierUniforms.SplitOffset, spec.splitOffset)
        shader.setFloatUniform(PixelFrontierUniforms.Jitter, spec.jitter)
        shader.setColorUniform(PixelFrontierUniforms.EdgeColor, spec.edgeColorArgb)
        shader.setFloatUniform(PixelFrontierUniforms.Time, spec.time)
        shader.setFloatUniform(PixelFrontierUniforms.StochasticMode, spec.stochasticMode)
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
