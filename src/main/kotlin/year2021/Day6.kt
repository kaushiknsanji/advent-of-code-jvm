/**
 * Problem: Day6: Lanternfish
 * https://adventofcode.com/2021/day/6
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2021

import base.BaseFileHandler

private class Day6 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)  // 5934
    println("=====")
    solveActual(1)  // 350605
    println("=====")
    solveSample(2)  // 26984457539
    println("=====")
    solveActual(2)  // 1592778185024
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day6.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day6.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    LanternFishGrowth.parse(input)
        .getCountOfFishAfter(80)
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    LanternFishGrowth.parse(input)
        .getCountOfFishAfter(256)
        .also { println(it) }
}

private class LanternFishGrowth private constructor(
    private val initialFishTimers: List<Int>
) {
    companion object {
        // Constant for New Fish Timer
        const val TIMER_START_NEW_FISH = 8

        // Constant for Old Fish Timer
        const val TIMER_START_OLD_FISH = 6

        fun parse(input: List<String>): LanternFishGrowth = LanternFishGrowth(
            initialFishTimers = input[0].split(",").map(String::toInt)
        )
    }

    // Saves the count of Fish based on their current timers
    private val fishTimerCountStateArray = LongArray(TIMER_START_NEW_FISH + 1).apply {
        initialFishTimers.forEach { timerIndex ->
            this[timerIndex]++
        }
    }

    private fun spawnFishForTheDay() = fishTimerCountStateArray.apply {
        // Save off the count that spawns Fish from Timer 0
        val spawningFishCount = this[0]
        // Pick last 8 Fish Timers and propagate their Fish count downwards
        this.takeLast(TIMER_START_NEW_FISH).forEachIndexed { timerIndex, fishCount ->
            // NOTE: "timerIndex" here starts at 0 and ends at 7
            // Remove count at original Timer
            this[timerIndex + 1] -= fishCount
            // Propagate above count downwards by adding to the present Timer
            this[timerIndex] += fishCount
        }
        // Spawn new fish by incrementing count of Timer 8 by the spawning fish count
        this[TIMER_START_NEW_FISH] += spawningFishCount
        // Update old fish count by incrementing count of Timer 6 by the spawning fish count
        this[TIMER_START_OLD_FISH] += spawningFishCount
        // Remove the spawning fish count from Timer 0
        this[0] -= spawningFishCount
    }

    /**
     * [Solution for Part-1 and Part-2]
     * Returns the count of Lantern Fish after the given [period] of Days.
     */
    fun getCountOfFishAfter(period: Int): Long = generateSequence {
        spawnFishForTheDay()
    }.take(period).last().sum()

}