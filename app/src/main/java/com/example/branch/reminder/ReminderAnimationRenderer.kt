package com.example.branch.reminder

object ReminderAnimationRenderer {
    private const val LIT = 4095
    private const val DIM = 200
    private const val OFF = 0

    /**
     * Returns an array of frames, each frame being an IntArray of size 169.
     * Sweeps a line down and then back up.
     */
    fun createSweepAnimation(): List<IntArray> {
        val frames = mutableListOf<IntArray>()

        // Sweep down
        for (sweepRow in 0 until 13) {
            frames.add(createFrame(sweepRow))
        }
        
        // Sweep up
        for (sweepRow in 11 downTo 0) {
            frames.add(createFrame(sweepRow))
        }

        // Add a few flashes of the Hub at the end
        for (i in 0 until 3) {
            frames.add(createHubFrame(LIT))
            frames.add(createHubFrame(OFF))
        }

        return frames
    }

    private fun createFrame(litRow: Int): IntArray {
        val grid = IntArray(169) { OFF }
        for (row in 0 until 13) {
            for (col in 0 until 13) {
                if (row == litRow) {
                    grid[row * 13 + col] = LIT
                } else if (Math.abs(row - litRow) == 1) {
                    grid[row * 13 + col] = DIM
                }
            }
        }
        return grid
    }

    private fun createHubFrame(brightness: Int): IntArray {
        val grid = IntArray(169) { OFF }
        val hub = listOf(6 to 6, 5 to 6, 7 to 6, 6 to 5, 6 to 7)
        for ((col, row) in hub) {
            grid[row * 13 + col] = brightness
        }
        return grid
    }
}
