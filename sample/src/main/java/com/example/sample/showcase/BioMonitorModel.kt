package com.example.sample.showcase

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

/**
 * Fixed values for the CRT Biomonitor card (roadmap card 8).
 *
 * Values marked "dossier" come from `docs/inspirations/crt-biomonitor-dumpshock-cold-jack-console.md`.
 * Values marked "design" are visual tuning choices. The dossier does not give them.
 */
object BioMonitorSpec {
    /** Dossier §2: lowest neural signal that the interlink tap amplifiers isolate, in microvolts. */
    const val TAP_MIN_MICROVOLTS = 0.1f

    /** Dossier §2: highest neural signal that the interlink tap amplifiers isolate, in microvolts. */
    const val TAP_MAX_MICROVOLTS = 50f

    /** Dossier §2: feedback that the capacitor bank absorbs, in kilovolts. */
    const val CAPACITOR_ABSORB_KILOVOLTS = 2.5f

    /** Dossier §2: time in which the capacitor bank absorbs the feedback, in microseconds. */
    const val CAPACITOR_ABSORB_MICROSECONDS = 50

    /** Dossier §1: flyback transformer and raster frequency, in kilohertz. */
    const val FLYBACK_KILOHERTZ = 15.75f

    /** Design: seconds of signal that the trace shows across the screen width. */
    const val TRACE_WINDOW_SECONDS = 3f

    /** Design: number of points that make up one trace. */
    const val TRACE_POINTS = 180

    /** Design: trace level, from 0 (bottom) to 1 (top), when the heart signal is 0. */
    const val TRACE_BASELINE = 0.4f

    /** Design: vertical scale from heart signal units to screen fraction. */
    const val TRACE_GAIN = 0.45f

    /** Design: amplitude of the synaptic micro-spikes under stress, in heart signal units. */
    const val STRESS_SPIKE_AMPLITUDE = 0.25f

    /** Design: micro-spike samples per second under stress. */
    const val STRESS_SPIKE_RATE_HZ = 40f

    /** Design: every Nth beat is a premature ventricular contraction under stress. */
    const val STRESS_PVC_EVERY_BEATS = 4

    /** Design: a premature beat arrives this fraction of a period early. */
    const val PVC_EARLY_FRACTION = 0.3f

    /** Design: frequencies, in hertz, of the overlapping waves that make fibrillation chaos. */
    val FIBRILLATION_FREQUENCIES_HZ = listOf(4.3f, 6.1f, 8.7f)

    /** Design: random seed for the deterministic noise, so tests are repeatable. */
    const val NOISE_SEED = 7_331
}

/**
 * The biometric stages of the console (dossier §4).
 *
 * @property label Text for the stage badge.
 * @property heartRateText Heart rate readout. The dossier gives these values. It gives none after a cold-jack.
 * @property beatsPerMinute Heart rate that drives the trace, or `null` when the trace has no regular beat.
 * @property coherenceText Synaptic coherence readout, from the dossier.
 */
enum class BioStage(
    val label: String,
    val heartRateText: String,
    val beatsPerMinute: Float?,
    val coherenceText: String,
) {
    /** Dossier §4.1: 68–74 BPM, 96% coherence. The trace uses the middle of the range. */
    Patrol("STABLE PATROL", "68–74", 71f, "96%"),

    /** Dossier §4.2: 135 BPM, 62% coherence, jagged bursts and premature beats. */
    Stress("SYNAPTIC STRESS", "135", 135f, "62%"),

    /** Dossier §4.3: 210+ BPM ventricular fibrillation, coherence below 15%. */
    Fibrillation("V-FIB // BLACK ICE", "210+", null, "<15%"),

    /** Dossier §4.4: after the cold-jack, a clean baseline trace. The dossier gives no numbers. */
    Stabilized("BUS ISOLATED", "—", 71f, "—"),
}

/** Seconds in one minute. */
private const val SECONDS_PER_MINUTE = 60f

/**
 * One wave of the heartbeat, as a Gaussian bump in beat phase.
 *
 * This uses the Gaussian-sum approach of McSharry et al. (2003), "A dynamical
 * model for generating synthetic electrocardiogram signals". The centers,
 * heights, and widths below are design values. They are not from that paper.
 *
 * @property center Position of the wave in the beat, from 0 to 1.
 * @property height Peak height in heart signal units. Negative values point down.
 * @property width Standard deviation of the bump, as a fraction of the beat.
 */
private data class EcgWave(val center: Float, val height: Float, val width: Float)

/** Design: the P, Q, R, S, and T waves of one normal beat. */
private val NORMAL_BEAT = listOf(
    EcgWave(center = 0.2f, height = 0.12f, width = 0.025f), // P wave: atrial depolarization
    EcgWave(center = 0.37f, height = -0.12f, width = 0.01f), // Q wave
    EcgWave(center = 0.4f, height = 1f, width = 0.012f), // R wave: the tall spike
    EcgWave(center = 0.43f, height = -0.22f, width = 0.01f), // S wave
    EcgWave(center = 0.65f, height = 0.28f, width = 0.04f), // T wave: ventricular repolarization
)

/** Design: a premature ventricular beat is wider and taller, with no P wave. */
private val PVC_BEAT = listOf(
    EcgWave(center = 0.4f, height = 1.3f, width = 0.03f),
    EcgWave(center = 0.5f, height = -0.5f, width = 0.04f),
)

/** Divisor that turns a width into a Gaussian variance term (2 sigma squared). */
private const val GAUSSIAN_VARIANCE_FACTOR = 2f

/**
 * Sums Gaussian waves at one beat phase.
 *
 * @param phase Position in the beat, from 0 to 1.
 * @param waves The waves to add.
 * @return The heart signal in heart signal units.
 */
private fun beatSignal(phase: Float, waves: List<EcgWave>): Float = waves.sumOf { wave ->
    val distance = phase - wave.center
    (wave.height * exp(-(distance * distance) / (GAUSSIAN_VARIANCE_FACTOR * wave.width * wave.width))).toDouble()
}.toFloat()

/**
 * Gives a repeatable noise value for one time slot.
 *
 * @param slot Integer time slot. Equal slots always give equal values.
 * @return A value from −1 to 1.
 */
private fun slotNoise(slot: Long): Float =
    Random(BioMonitorSpec.NOISE_SEED.toLong() xor slot).nextFloat() * 2f - 1f

/**
 * Calculates the heart and synaptic signal for one stage at one time.
 *
 * @param stage The biometric stage.
 * @param timeSeconds Simulation time in seconds.
 * @return The signal in heart signal units. A normal R wave peaks near 1.
 */
fun bioSignalAt(stage: BioStage, timeSeconds: Float): Float {
    val bpm = stage.beatsPerMinute
    if (bpm == null) {
        // Ventricular fibrillation: no regular beat, only overlapping chaotic waves.
        val waves = BioMonitorSpec.FIBRILLATION_FREQUENCIES_HZ.sumOf { hz ->
            sin(2.0 * PI * hz * timeSeconds)
        }.toFloat() / BioMonitorSpec.FIBRILLATION_FREQUENCIES_HZ.size
        val slot = floor(timeSeconds * BioMonitorSpec.STRESS_SPIKE_RATE_HZ).toLong()
        // Scale so a wave trough reaches the screen bottom: the zigzags fill the tube height (dossier §4.3).
        val fullHeightScale = BioMonitorSpec.TRACE_BASELINE / BioMonitorSpec.TRACE_GAIN
        return waves * fullHeightScale +
            BioMonitorSpec.STRESS_SPIKE_AMPLITUDE * slotNoise(slot)
    }

    val period = SECONDS_PER_MINUTE / bpm
    val beatIndex = floor(timeSeconds / period).toLong()
    val phase = (timeSeconds - beatIndex * period) / period
    if (stage != BioStage.Stress) return beatSignal(phase, NORMAL_BEAT)

    val isPrematureBeat = beatIndex % BioMonitorSpec.STRESS_PVC_EVERY_BEATS == 0L
    val beat = if (isPrematureBeat) {
        beatSignal(phase + BioMonitorSpec.PVC_EARLY_FRACTION, PVC_BEAT)
    } else {
        beatSignal(phase, NORMAL_BEAT)
    }
    val slot = floor(timeSeconds * BioMonitorSpec.STRESS_SPIKE_RATE_HZ).toLong()
    return beat + BioMonitorSpec.STRESS_SPIKE_AMPLITUDE * slotNoise(slot)
}

/**
 * Builds the points of the trace that the CRT shows, oldest at the left.
 *
 * @param stage The biometric stage.
 * @param timeSeconds Simulation time at the right edge of the screen.
 * @return [BioMonitorSpec.TRACE_POINTS] values from 0 (bottom) to 1 (top),
 *   in the form that `CyberBiometrics` draws.
 */
fun bioTraceAt(stage: BioStage, timeSeconds: Float): List<Float> {
    val lastIndex = BioMonitorSpec.TRACE_POINTS - 1
    return List(BioMonitorSpec.TRACE_POINTS) { index ->
        val sampleTime = timeSeconds - BioMonitorSpec.TRACE_WINDOW_SECONDS * (lastIndex - index) / lastIndex
        (BioMonitorSpec.TRACE_BASELINE + BioMonitorSpec.TRACE_GAIN * bioSignalAt(stage, sampleTime)).coerceIn(0f, 1f)
    }
}

/**
 * The complete state of the CRT Biomonitor. Both tabs of the card read this one value.
 *
 * @property threatStage The stage that the neural interlink signal source drives.
 * @property coldJacked `true` after the operator completes the cold-jack lever hold.
 */
data class BioMonitorState(
    val threatStage: BioStage = BioStage.Patrol,
    val coldJacked: Boolean = false,
) {
    /** The stage that the monitor shows. A cold-jack overrides the threat. */
    val shownStage: BioStage
        get() = if (coldJacked) BioStage.Stabilized else threatStage

    /** `true` when bezel corona sparks show (dossier §4.3). They stop at disconnect. */
    val sparksActive: Boolean
        get() = shownStage == BioStage.Fibrillation

    /** @return The state after the cold-jack lever opens the knife switch. */
    fun coldJack(): BioMonitorState = copy(coldJacked = true)

    /** @return The state after the operator re-jacks: the link closes and the threat source resets to patrol. */
    fun reJack(): BioMonitorState = BioMonitorState()

    /**
     * @param stage The new threat stage from the signal source.
     * @return The state with that threat stage. A cold-jacked link stays isolated.
     */
    fun withThreat(stage: BioStage): BioMonitorState = copy(threatStage = stage)
}
