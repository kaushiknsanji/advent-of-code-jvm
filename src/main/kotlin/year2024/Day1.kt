/**
 * Problem: Day1: Historian Hysteria
 * https://adventofcode.com/2024/day/1
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import kotlin.math.absoluteValue

private class Day1 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 11
    println("=====")
    solveActual(1)      // 2166959
    println("=====")
    solveSample(2)      // 31
    println("=====")
    solveActual(2)      // 23741109
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day1.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day1.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    MomentousLocationsAnalyzer.parse(input)
        .getTotalDistanceBetweenLists()
        .also(::println)
}

private fun doPart2(input: List<String>) {
    MomentousLocationsAnalyzer.parse(input)
        .getSimilarityScore()
        .also(::println)
}

private class MomentousLocationsAnalyzer private constructor(
    private val leftNumbers: List<Int>,
    private val rightNumbers: List<Int>
) {
    companion object {

        fun parse(input: List<String>): MomentousLocationsAnalyzer {
            val leftNumbers = mutableListOf<Int>()
            val rightNumbers = mutableListOf<Int>()
            val numberRegex = """(\d+)""".toRegex()

            input.forEach { line ->
                numberRegex.findAll(line).map { matchResult ->
                    matchResult.groupValues[1].toInt()
                }.toList().also { numbers ->
                    leftNumbers.add(numbers.first())
                    rightNumbers.add(numbers.last())
                }
            }

            return MomentousLocationsAnalyzer(
                leftNumbers = leftNumbers.sorted(),
                rightNumbers = rightNumbers.sorted()
            )
        }
    }

    /**
     * [Solution for Part-1]
     *
     * Returns the Total distance found between two lists of locations.
     */
    fun getTotalDistanceBetweenLists(): Int = leftNumbers.zip(rightNumbers) { leftId: Int, rightId: Int ->
        (leftId - rightId).absoluteValue
    }.sum()

    /**
     * [Solution for Part-2]
     *
     * Returns a Similarity Score by adding each number from [leftNumbers] with
     * the product of their occurrence in [rightNumbers].
     */
    fun getSimilarityScore(): Int = leftNumbers.sumOf { leftId: Int ->
        leftId * rightNumbers.count { occurrenceLeftId: Int -> leftId == occurrenceLeftId }
    }

}