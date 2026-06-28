package com.example.branch.glyph

/**
 * Renders a 13x13 = 169-element brightness array for the Glyph Matrix.
 * index = row * 13 + col
 */
object EmblemRenderer {

    private const val LIT   = 4095
    private const val PULSE = 1500
    private const val GHOST = 200
    private const val OFF   = 0
    private const val HUB_FULL = 4095
    private const val HUB_DIM  = 400

    // Gym (Star) arms: 6 radiating lines
    private val GYM_ARMS: List<List<Pair<Int,Int>>> = listOf(
        listOf(6 to 4, 6 to 2, 6 to 0),   // arm1 top
        listOf(8 to 5, 9 to 4, 11 to 3),  // arm2 upper-right
        listOf(8 to 7, 9 to 8, 11 to 9),  // arm3 lower-right
        listOf(6 to 8, 6 to 10, 6 to 12), // arm4 bottom
        listOf(4 to 7, 3 to 8, 1 to 9),   // arm5 lower-left
        listOf(4 to 5, 3 to 4, 1 to 3),   // arm6 upper-left
    )

    // Flow (Flower) arms: 6 rounded petals
    private val FLOW_ARMS: List<List<Pair<Int,Int>>> = listOf(
        listOf(6 to 1, 5 to 2, 6 to 2, 7 to 2, 6 to 3), // top
        listOf(10 to 3, 9 to 4, 10 to 4, 11 to 4, 10 to 5), // top-right
        listOf(10 to 7, 9 to 8, 10 to 8, 11 to 8, 10 to 9), // bottom-right
        listOf(6 to 9, 5 to 10, 6 to 10, 7 to 10, 6 to 11), // bottom
        listOf(2 to 7, 1 to 8, 2 to 8, 3 to 8, 2 to 9), // bottom-left
        listOf(2 to 3, 1 to 4, 2 to 4, 3 to 4, 2 to 5)  // top-left
    )

    private val HUB: List<Pair<Int,Int>> = listOf(
        6 to 6, 5 to 6, 7 to 6, 6 to 5, 6 to 7
    )

    /**
     * @param style "gym" or "flow"
     * @param filledSections 0–6 sections filled
     * @param pulsePhase 0.0–1.0 for pulsing the next arm (driven by UI animation)
     * @param isPlannedToday Whether a workout is planned for today (if false, skips pulsing)
     */
    fun render(style: String, filledSections: Int, pulsePhase: Float = 0.5f, isPlannedToday: Boolean = true): IntArray {
        val grid = IntArray(169)

        for (armIndex in 0 until 6) {
            val brightness = when {
                armIndex < filledSections    -> LIT
                armIndex == filledSections
                        && filledSections < 6
                        && isPlannedToday    -> (GHOST + (PULSE - GHOST) * pulsePhase).toInt()
                else                         -> GHOST
            }
            val arms = if (style == "gym") GYM_ARMS else FLOW_ARMS
            for ((col, row) in arms[armIndex]) {
                grid[row * 13 + col] = brightness
            }
        }

        val hubBrightness = if (filledSections == 6) HUB_FULL else HUB_DIM
        for ((col, row) in HUB) {
            grid[row * 13 + col] = hubBrightness
        }

        return grid
    }
}
