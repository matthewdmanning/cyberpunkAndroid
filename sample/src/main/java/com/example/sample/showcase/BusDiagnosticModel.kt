package com.example.sample.showcase

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Fixed values for the Mil-Spec Hardware Bus Diagnostic card (roadmap card 6).
 *
 * Values marked "dossier" come from `docs/inspirations/mil-spec-hardware-bus-diagnostic.md`.
 * Values marked "design" are visual tuning choices. The dossier does not give them.
 */
object BusDiagnosticSpec {
    /** Dossier §1: the optical bus monitor has 32 vertical light-pipes, one per compute thread. */
    const val LIGHT_PIPE_COUNT = 32

    /** Dossier §2: combat systems draw current pulses of up to 80 A. */
    const val MAX_SPIKE_CURRENT_AMPS = 80f

    /** Dossier §4.2: spinal booster firings kick spikes up to 45 A during active load. */
    const val COMBAT_PEAK_CURRENT_AMPS = 45f

    /** Dossier §4.1 and §4.2: the +12 V rail needle stays at nominal; baseline voltage does not drop. */
    const val NOMINAL_RAIL_VOLTS = 12.0f

    /** Dossier §4.1: the rail that the analog needle reads. It is the only rail with a stated voltage. */
    const val NEEDLE_RAIL_LABEL = "+12V"

    /** Dossier §1: the four monitored power rails, in panel order. */
    val RAIL_LABELS = listOf("+5V", NEEDLE_RAIL_LABEL, "+48V HIGH-RAIL", "SPINAL-JACK V-REF")

    /** Design: seconds for the load centroid to sweep across the pipe bank and back. */
    const val DRIFT_CYCLE_SECONDS = 6f

    /** Design: width (in pipes) of the bright load packet around the centroid. */
    const val PACKET_WIDTH_PIPES = 4f

    /** Design: brightness of the secondary per-pipe ripple, as a fraction of full scale. */
    const val RIPPLE_AMPLITUDE = 0.18f

    /** Design: ripple frequency in cycles per second, so packets look like running waves. */
    const val RIPPLE_HZ = 1.4f

    /** Design: phase offset between neighbor pipes, in cycles, so the ripple travels sideways. */
    const val RIPPLE_PHASE_PER_PIPE = 0.11f

    /** Design: seconds between booster current spikes on the power rail bars. */
    const val SPIKE_PERIOD_SECONDS = 1.5f

    /** Design: time constant, in seconds, of the exponential fall after each current spike. */
    const val SPIKE_DECAY_SECONDS = 0.25f

    /** Design: pipe brightness above which a thread counts as "active" in the readout. */
    const val ACTIVE_THREAD_THRESHOLD = 0.5f
}

/**
 * One frame of the bus diagnostic display.
 *
 * @property pipeIntensities Brightness of each light-pipe, from 0 (dark) to 1 (full), left to right.
 * @property centroidThread Zero-based index of the brightest thread column. This is where the load is.
 * @property activeThreads Number of pipes brighter than [BusDiagnosticSpec.ACTIVE_THREAD_THRESHOLD].
 * @property railCurrentAmps Present booster current on the power rail, in amperes.
 * @property firewallIntegrity Firewall barrier integrity, from 0 (breached) to 1 (intact).
 */
data class BusDiagnosticFrame(
    val pipeIntensities: List<Float>,
    val centroidThread: Int,
    val activeThreads: Int,
    val railCurrentAmps: Float,
    val firewallIntegrity: Float,
)

/** Firewall integrity while no intrusion occurs. The card has no breach state yet. */
private const val FIREWALL_INTACT = 1f

/** Divisor that turns a packet width into a Gaussian variance term (2 sigma squared). */
private const val GAUSSIAN_VARIANCE_FACTOR = 2f

/**
 * Calculates the "Combat Load Drift" state of the bus diagnostic (dossier §4.2) at one time.
 *
 * The load centroid sweeps sinusoidally across the pipe bank. A Gaussian packet
 * lights the pipes near the centroid. A traveling ripple adds the running-wave
 * look. The power rail shows a sawtooth of booster current spikes that decay
 * exponentially.
 *
 * @param timeSeconds Simulation time in seconds. The same time always gives the same frame.
 * @return The display values for that time.
 */
fun busDiagnosticFrameAt(timeSeconds: Float): BusDiagnosticFrame {
    val lastPipe = BusDiagnosticSpec.LIGHT_PIPE_COUNT - 1
    val sweep = sin(2f * PI.toFloat() * timeSeconds / BusDiagnosticSpec.DRIFT_CYCLE_SECONDS)
    val centroid = lastPipe * (1f + sweep) / 2f
    val variance = GAUSSIAN_VARIANCE_FACTOR * BusDiagnosticSpec.PACKET_WIDTH_PIPES * BusDiagnosticSpec.PACKET_WIDTH_PIPES

    val intensities = List(BusDiagnosticSpec.LIGHT_PIPE_COUNT) { pipe ->
        val distance = pipe - centroid
        val packet = exp(-(distance * distance) / variance)
        val ripplePhase = BusDiagnosticSpec.RIPPLE_HZ * timeSeconds + pipe * BusDiagnosticSpec.RIPPLE_PHASE_PER_PIPE
        val ripple = BusDiagnosticSpec.RIPPLE_AMPLITUDE * sin(2f * PI.toFloat() * ripplePhase)
        (packet + ripple).coerceIn(0f, 1f)
    }

    val spikePhase = timeSeconds.mod(BusDiagnosticSpec.SPIKE_PERIOD_SECONDS)
    val current = BusDiagnosticSpec.COMBAT_PEAK_CURRENT_AMPS * exp(-spikePhase / BusDiagnosticSpec.SPIKE_DECAY_SECONDS)

    return BusDiagnosticFrame(
        pipeIntensities = intensities,
        centroidThread = centroid.roundToInt(),
        activeThreads = intensities.count { it > BusDiagnosticSpec.ACTIVE_THREAD_THRESHOLD },
        railCurrentAmps = current,
        firewallIntegrity = FIREWALL_INTACT,
    )
}
