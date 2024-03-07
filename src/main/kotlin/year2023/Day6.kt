/**
 * Problem: Day6: Wait For It
 * https://adventofcode.com/2023/day/6
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import utils.product
import kotlin.math.abs

private class Day6 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 288
    println("=====")
    solveActual(1)      // 4811940
    println("=====")
    solveSample(2)      // 71503
    println("=====")
    solveActual(2)      // 30077773
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
    BoatRaceGameAnalyzer.parse(input)
        .getProductOfWinPossibility()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    BoatRaceGameAnalyzer.parse(input, inputHasKerningError = true)
        .getProductOfWinPossibility()
        .also { println(it) }
}

private class BoatRaceGame private constructor(
    private val raceTime: Long,
    private val recordDistance: Long
) {

    constructor(timeDistancePair: Pair<Long, Long>) : this(timeDistancePair.first, timeDistancePair.second)

    val recordsPossible
        get() = (1 until raceTime)
            .first { chargeTime ->
                // Get the first occurrence of a possible record
                chargeTime * (raceTime - chargeTime) > recordDistance
            }
            .let { chargeTime ->
                // Calculate the number of ways one can beat the record of this game
                // based on the first occurrence of a possible record
                abs(raceTime - 2 * chargeTime) + 1
            }

}

private class BoatRaceGameAnalyzer private constructor(
    private val raceGames: List<BoatRaceGame>
) {
    companion object {
        private const val TIME = "Time"
        private const val DISTANCE = "Distance"
        private const val COLON = ":"

        private val spaceRegex = """\s+""".toRegex()

        private val extractNumbers: (line: String) -> List<Long> = { line ->
            line.split(spaceRegex).map(String::toLong)
        }

        fun parse(input: List<String>, inputHasKerningError: Boolean = false): BoatRaceGameAnalyzer =
            input.associate { inputLine ->
                inputLine.substringBefore(COLON) to inputLine.substringAfter(COLON).trim()
            }.let { inputMap: Map<String, String> ->
                if (inputHasKerningError) {
                    listOf(
                        inputMap[TIME]!!.replace(spaceRegex, "").toLong() to
                                inputMap[DISTANCE]!!.replace(spaceRegex, "").toLong()
                    )
                } else {
                    extractNumbers(inputMap[TIME]!!).zip(extractNumbers(inputMap[DISTANCE]!!))
                }
            }.map(::BoatRaceGame).let(::BoatRaceGameAnalyzer)
    }

    /**
     * [Solution for Part 1 & 2]
     * Returns the product of the number of ways one can beat the record of each of the given boat [raceGames].
     * For Part-2, [raceGames] will have only one game.
     */
    fun getProductOfWinPossibility(): Long = raceGames.map(BoatRaceGame::recordsPossible).product()

}