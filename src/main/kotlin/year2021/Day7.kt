/**
 * Problem: Day7: The Treachery of Whales
 * https://adventofcode.com/2021/day/7
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2021

import base.BaseFileHandler
import kotlin.math.absoluteValue

private class Day7 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)  // 37
    println("=====")
    solveActual(1)  // 344138
    println("=====")
    solveSample(2)  // 170
    println("=====")
    solveActual(2)  // 94862124
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day7.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day7.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    CrabSubmarines.parse(input)
        .alignSubmarinesForPart1()
        .getFuelSpentForAlignmentToEfficientPosition()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    CrabSubmarines.parse(input)
        .alignSubmarinesForPart2()
        .getFuelSpentForAlignmentToEfficientPosition()
        .also { println(it) }
}

private class CrabSubmarines private constructor(
    private val positions: List<Int>
) {
    companion object {
        fun parse(input: List<String>): CrabSubmarines = CrabSubmarines(
            input[0].split(",").map(String::toInt)
        )
    }

    private val positionToFuelConsumptionMap = mutableMapOf<Int, Int>()

    /**
     * [Solution for Part-1]
     * Tries to align Crab Submarines with respect to each Horizontal position in order to generate a map of
     * Horizontal positions to Fuel efficiency based on Part-1 statement.
     */
    fun alignSubmarinesForPart1(): CrabSubmarines = this.apply {
        positions.forEach { alignToPosition ->
            if (!positionToFuelConsumptionMap.containsKey(alignToPosition)) {
                positionToFuelConsumptionMap[alignToPosition] =
                    positions.sumOf { position -> (position - alignToPosition).absoluteValue }
            }
        }
    }

    /**
     * [Solution for Part-2]
     * Tries to align Crab Submarines with respect to each Horizontal position in order to generate a map of
     * Horizontal positions to Fuel efficiency based on Part-2 statement.
     */
    fun alignSubmarinesForPart2(): CrabSubmarines = this.apply {
        positions.forEach { alignToPosition ->
            if (!positionToFuelConsumptionMap.containsKey(alignToPosition)) {
                positionToFuelConsumptionMap[alignToPosition] = positions.sumOf { position ->
                    (position - alignToPosition).absoluteValue.let { difference ->
                        (difference * (difference + 1)).ushr(1)
                    }
                }
            }
        }
    }

    fun getFuelSpentForAlignmentToEfficientPosition(): Int = positionToFuelConsumptionMap.minOf { it.value }
}