package com.example.branch.glyph

/**
 * Renders a countdown (seconds remaining) onto a 13x13 LED grid.
 * Uses a minimal 3x5 pixel font for digits, centred in the grid.
 */
object CountdownRenderer {

    private const val ON  = 4095
    private const val OFF = 0

    // 3-wide x 5-tall pixel font for digits 0-9
    // Each digit encoded as 5 rows of 3 bits (MSB left)
    private val DIGITS = arrayOf(
        // 0
        intArrayOf(0b111, 0b101, 0b101, 0b101, 0b111),
        // 1
        intArrayOf(0b010, 0b110, 0b010, 0b010, 0b111),
        // 2
        intArrayOf(0b111, 0b001, 0b111, 0b100, 0b111),
        // 3
        intArrayOf(0b111, 0b001, 0b111, 0b001, 0b111),
        // 4
        intArrayOf(0b101, 0b101, 0b111, 0b001, 0b001),
        // 5
        intArrayOf(0b111, 0b100, 0b111, 0b001, 0b111),
        // 6
        intArrayOf(0b111, 0b100, 0b111, 0b101, 0b111),
        // 7
        intArrayOf(0b111, 0b001, 0b001, 0b001, 0b001),
        // 8
        intArrayOf(0b111, 0b101, 0b111, 0b101, 0b111),
        // 9
        intArrayOf(0b111, 0b101, 0b111, 0b001, 0b111),
    )

    fun render(seconds: Int): IntArray {
        val grid = IntArray(169)
        val s = seconds.coerceIn(0, 99)

        if (s < 10) {
            // Single digit — centre at col 5, row 4
            drawDigit(grid, s, startCol = 5, startRow = 4)
        } else {
            // Two digits — cols 2 and 7, row 4
            drawDigit(grid, s / 10, startCol = 2, startRow = 4)
            drawDigit(grid, s % 10, startCol = 7, startRow = 4)
        }

        return grid
    }

    private fun drawDigit(grid: IntArray, digit: Int, startCol: Int, startRow: Int) {
        val pattern = DIGITS[digit]
        for (row in 0 until 5) {
            val bits = pattern[row]
            for (col in 0 until 3) {
                val bitSet = (bits shr (2 - col)) and 1
                val gridRow = startRow + row
                val gridCol = startCol + col
                if (gridRow in 0 until 13 && gridCol in 0 until 13) {
                    grid[gridRow * 13 + gridCol] = if (bitSet == 1) ON else OFF
                }
            }
        }
    }
}
