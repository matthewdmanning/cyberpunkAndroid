package com.example.sample.showcase

import kotlin.math.ceil

/**
 * Fixed values for the ICE Detection Matrix card (roadmap card 7).
 *
 * Values marked "dossier" come from `docs/inspirations/ice-detection-matrix-countermeasures.md`.
 * Values marked "placeholder" are not in the dossier. They exist only so the
 * simulation runs. Replace them when the owner gives real values.
 */
object IceMatrixSpec {
    /** Dossier §4.3: the overwatch score above which the trace locks on. */
    const val TRACE_LOCK_THRESHOLD_PERCENT = 75f

    /** Dossier §2: the score at which Grey ICE defenses activate (start of the Tier 3 band). */
    const val GREY_ICE_ACTIVATION_PERCENT = 45f

    /** Maximum overwatch score, in percent. */
    const val MAX_SCORE_PERCENT = 100f

    /** Dossier §1: each perimeter ICE bar has 20 discrete light blocks. */
    const val ICE_BAR_BLOCKS = 20

    /** Dossier §1: blocks 1–8 are the White ICE tier. */
    const val WHITE_ICE_LAST_BLOCK = 8

    /** Dossier §1: blocks 9–15 are the Grey ICE tier. Blocks 16–20 are Black ICE. */
    const val GREY_ICE_LAST_BLOCK = 15

    /** Dossier §4.1: top-plate temperature while the console is quiet. */
    const val SHUNT_NOMINAL_CELSIUS = 23

    /** Dossier §4.3: top-plate temperature after a Black ICE trace lock. */
    const val SHUNT_TRACE_LOCK_CELSIUS = 52

    /** Dossier §3: nominal scan chirp frequency. */
    const val SIREN_CHIRP_HZ = 400

    /** Dossier §3: start frequency of the warble when the score approaches the threshold. */
    const val SIREN_WARBLE_LOW_HZ = 800

    /** Dossier §3: end frequency of the warble when the score approaches the threshold. */
    const val SIREN_WARBLE_HIGH_HZ = 1_600

    /** Dossier §3: trace-lock klaxon frequency. */
    const val SIREN_KLAXON_HZ = 2_200

    /**
     * Placeholder: stealth masking efficiency, from 0 (no masking) to 1 (full masking).
     * TODO(values): the dossier gives no stealth efficiency. Replace with an owner-supplied value.
     */
    const val PLACEHOLDER_STEALTH_EFFICIENCY = 0.6f
}

/**
 * The four penetration depths that the stepped slider selects (dossier §2).
 *
 * @property label Text for the slider readout.
 * @property bandStartPercent Lower edge of the detection band that the dossier gives for this tier.
 * @property bandEndPercent Upper edge of that detection band.
 * @property placeholderGrowthPercentPerSecond The value of `f(tier)` in the overwatch equation.
 *   TODO(values): the dossier says only that the footprint increases exponentially with depth.
 *   It gives no numbers. These placeholders double per tier and must be replaced.
 */
enum class PenetrationTier(
    val label: String,
    val bandStartPercent: Float,
    val bandEndPercent: Float,
    val placeholderGrowthPercentPerSecond: Float,
) {
    /** Tier 1: low-frequency subnet interrogation. Dossier band: below 20%. */
    PassiveProbe("PASSIVE PROBE", 0f, 20f, 0.5f),

    /** Tier 2: active indexing of encrypted directories. Dossier band: 20–45%. */
    CatalogSearch("CATALOG SEARCH", 20f, 45f, 1f),

    /** Tier 3: high-volume data exfiltration. Dossier band: 45–75%. */
    MemorySiphon("MEMORY SIPHON", 45f, 75f, 2f),

    /** Tier 4: core subsystem takeover. Dossier band: above 75%. */
    RootExecution("ROOT EXECUTION", 75f, 100f, 4f),
}

/**
 * The ICE class that the highest lit perimeter block belongs to (dossier §1).
 *
 * @property threatBadge The threat-level badge text from the roadmap card 7 styling.
 */
enum class IceClass(val threatBadge: String) {
    /** Blocks 1–8: passive monitors, hounds, and trace daemons. */
    White("HARDENED"),

    /** Blocks 9–15: barriers, scramblers, and port lockouts. */
    Grey("INTRUSION TRACE"),

    /** Blocks 16–20: lethal feedback programs. */
    Black("BLACK ICE"),
}

/** The alarm tone that the piezo siren array plays (dossier §3). */
enum class IceSirenState {
    /** No connection. The siren is quiet. */
    Silent,

    /** Nominal scans: low chirps. */
    Chirp,

    /** Score approaches the threshold: rising warble. */
    Warble,

    /** Trace lock: pulsing klaxon. */
    Klaxon,
}

/**
 * The complete state of the ICE Detection Matrix. Both tabs of the card read this one value.
 *
 * The overwatch score follows the dossier §2 equation:
 * `score = f(tier) × (1 − stealth efficiency) × dwell time`.
 *
 * @property tier The penetration depth that the slider selects.
 * @property stealthEngaged `true` when the packet stealth rocker is on.
 * @property dwellSeconds Seconds connected to the target since the last purge or reconnect.
 * @property connected `false` after a purge severs the line, until the operator reconnects.
 */
data class IceMatrixState(
    val tier: PenetrationTier = PenetrationTier.PassiveProbe,
    val stealthEngaged: Boolean = true,
    val dwellSeconds: Float = 0f,
    val connected: Boolean = true,
) {
    /** The `(1 − stealth efficiency)` factor of the equation. It is 1 when stealth is off. */
    val stealthFactor: Float
        get() = if (stealthEngaged) 1f - IceMatrixSpec.PLACEHOLDER_STEALTH_EFFICIENCY else 1f

    /** The grid overwatch score in percent, clamped to 0–100. It is 0 while disconnected. */
    val scorePercent: Float
        get() = if (!connected) {
            0f
        } else {
            (tier.placeholderGrowthPercentPerSecond * stealthFactor * dwellSeconds)
                .coerceIn(0f, IceMatrixSpec.MAX_SCORE_PERCENT)
        }

    /** `true` when the score is above the dossier trace-lock threshold. */
    val traceLocked: Boolean
        get() = scorePercent > IceMatrixSpec.TRACE_LOCK_THRESHOLD_PERCENT

    /** Number of lit perimeter blocks, from 0 to [IceMatrixSpec.ICE_BAR_BLOCKS]. */
    val litBlocks: Int
        get() = ceil(scorePercent / IceMatrixSpec.MAX_SCORE_PERCENT * IceMatrixSpec.ICE_BAR_BLOCKS).toInt()

    /** The ICE class of the highest lit block. */
    val activeIceClass: IceClass
        get() = iceClassOfBlock(litBlocks)

    /** The alarm tone for the present score. */
    val sirenState: IceSirenState
        get() = when {
            !connected -> IceSirenState.Silent
            traceLocked -> IceSirenState.Klaxon
            scorePercent >= IceMatrixSpec.GREY_ICE_ACTIVATION_PERCENT -> IceSirenState.Warble
            else -> IceSirenState.Chirp
        }

    /**
     * The penetration tier whose dossier detection band contains the present score.
     * This can differ from [tier], because the score grows with dwell time.
     */
    val scoreBandTier: PenetrationTier
        get() = PenetrationTier.entries.last { scorePercent >= it.bandStartPercent }

    /**
     * Advances the simulation.
     *
     * @param deltaSeconds Seconds since the last step. Negative values are treated as 0.
     * @return The new state. Dwell time grows only while [connected].
     */
    fun advancedBy(deltaSeconds: Float): IceMatrixState =
        if (connected) copy(dwellSeconds = dwellSeconds + deltaSeconds.coerceAtLeast(0f)) else this

    /** @return The state after the master trace purge: the line is cut and dwell time resets. */
    fun purged(): IceMatrixState = copy(connected = false, dwellSeconds = 0f)

    /** @return The state after the operator reconnects: Tier 1, stealth on, zero dwell time. */
    fun reconnected(): IceMatrixState = IceMatrixState()
}

/**
 * Gives the ICE class of one perimeter block (dossier §1).
 *
 * @param block One-based block number. A value of 0 (nothing lit) gives [IceClass.White].
 * @return The ICE class that the block belongs to.
 */
fun iceClassOfBlock(block: Int): IceClass = when {
    block <= IceMatrixSpec.WHITE_ICE_LAST_BLOCK -> IceClass.White
    block <= IceMatrixSpec.GREY_ICE_LAST_BLOCK -> IceClass.Grey
    else -> IceClass.Black
}
