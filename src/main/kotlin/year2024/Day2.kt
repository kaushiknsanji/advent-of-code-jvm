/**
 * Problem: Day2: Red-Nosed Reports
 * https://adventofcode.com/2024/day/2
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import utils.Quadruple
import kotlin.math.absoluteValue

private class Day2 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 2
    println("=====")
    solveActual(1)      // 598
    println("=====")
    solveSample(2)      // 4
    println("=====")
    solveActual(2)      // 634
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day2.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day2.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    NuclearReportsAnalyzer.parse(input)
        .getTotalSafeReports()
        .also(::println)
}

private fun doPart2(input: List<String>) {
    NuclearReportsAnalyzer.parse(input)
        .getTotalSafeReports(hasDampener = true)
        .also(::println)
}

private class NuclearReportsAnalyzer private constructor(
    private val reports: List<List<Int>>
) {

    companion object {
        // Regular expression to capture numbers
        val numberRegex = """(\d+)""".toRegex()

        fun parse(input: List<String>): NuclearReportsAnalyzer = NuclearReportsAnalyzer(
            reports = input.map { line ->
                numberRegex.findAll(line).map { matchResult ->
                    matchResult.groupValues[1].toInt()
                }.toList()
            }
        )
    }

    /**
     * [Solution for Part 1 and 2]
     *
     * Returns the Total number of [reports] that are safe.
     *
     * @param hasDampener A Problem Dampener that can dampen a single bad level. `true` as in used for Part-2.
     * Defaulted to `false` for Part-1.
     */
    fun getTotalSafeReports(hasDampener: Boolean = false): Int =
        reports.count { report ->
            report.zipWithNext { currentNumber, nextNumber ->
                currentNumber - nextNumber
            }.let { differences: List<Int> ->
                // Extract Metadata for adjacent level differences
                val (decreaseCount, increaseCount, zeroCount, unsafeLevels) = extractMetadata(differences)

                // Unsafe when many levels are decreasing and increasing or has many unsafe adjacent levels
                val isUnsafe = decreaseCount * increaseCount > 0 || unsafeLevels > 0

                if (isUnsafe && hasDampener && unsafeLevels < 2 && zeroCount < 2) {
                    // When unsafe and has a problem dampener
                    // Excluding more than 1 unsafe adjacent levels and levels that did not decrease or increase,
                    // since they always lead to being unsafe or in other words, cannot be dampened

                    when {
                        decreaseCount == 1 -> {
                            // When only 1 adjacent level is decreasing in a report of all increasing levels

                            // From the index of such an adjacent level till the last,
                            // try dampening by removing current level each time and test again for safety
                            (differences.indexOfFirst { it > 0 }..report.lastIndex).any { testIndex ->
                                testForSafety(report.toMutableList().apply { removeAt(testIndex) })
                            }
                        }

                        increaseCount == 1 -> {
                            // When only 1 adjacent level is increasing in a report of all decreasing levels

                            // From the index of such an adjacent level till the last,
                            // try dampening by removing current level each time and test again for safety
                            (differences.indexOfFirst { it < 0 }..report.lastIndex).any { testIndex ->
                                testForSafety(report.toMutableList().apply { removeAt(testIndex) })
                            }
                        }

                        increaseCount * decreaseCount > 0 -> {
                            // When there are many levels decreasing and increasing, report cannot be dampened
                            false
                        }

                        zeroCount == 1 -> {
                            // When all levels are either decreasing or increasing, and also has a level
                            // that did not decrease or increase, report can always be dampened
                            // by removing the redundant level
                            true
                        }

                        else -> {
                            // When all levels are either decreasing or increasing, but has a level that is unsafe.
                            // Such a level when present either at the beginning or end of the report,
                            // can always be dampened
                            differences.indexOfFirst { it.absoluteValue !in 1..3 }.let { index ->
                                index == 0 || index + 1 == report.lastIndex
                            }
                        }
                    }

                } else {
                    !isUnsafe
                }

            }
        }

    /**
     * Tests the [modified report][adjustedReport] after dampening a level.
     *
     * Returns `true` when all levels are safe; `false` otherwise.
     */
    private fun testForSafety(adjustedReport: List<Int>): Boolean =
        adjustedReport.zipWithNext { currentNumber, nextNumber ->
            currentNumber - nextNumber
        }.let { differences: List<Int> ->

            // Extract Metadata for adjacent level differences
            val (decreaseCount, increaseCount, _, unsafeLevels) = extractMetadata(differences)

            // Return safe when all levels are either decreasing or increasing without any unsafe adjacent levels
            !(decreaseCount * increaseCount > 0 || unsafeLevels > 0)
        }

    /**
     * Extracts and returns a [Quadruple] Metadata for the adjacent level [differences] of a report.
     *
     * [Quadruple.first] will have the count of adjacent decreasing levels.
     * [Quadruple.second] will have the count of adjacent increasing levels.
     * [Quadruple.third] will have the count of adjacent levels that did not decrease or increase.
     * [Quadruple.fourth] will have the count of adjacent levels that are not in safe values.
     */
    private fun extractMetadata(differences: List<Int>): Quadruple<Int, Int, Int, Int> =
        differences.fold(
            Quadruple(0, 0, 0, 0)
        ) { (decreaseCount, increaseCount, zeroCount, unsafeLevels), difference: Int ->
            when {
                // When difference of two adjacent levels is decreasing
                difference > 0 -> Quadruple(
                    decreaseCount + 1,
                    increaseCount,
                    zeroCount,
                    updateUnsafeLevelCount(unsafeLevels, difference)
                )

                // When difference of two adjacent levels is increasing
                difference < 0 -> Quadruple(
                    decreaseCount,
                    increaseCount + 1,
                    zeroCount,
                    updateUnsafeLevelCount(unsafeLevels, difference)
                )

                // When difference of two adjacent levels is 0
                else -> Quadruple(decreaseCount, increaseCount, zeroCount + 1, unsafeLevels + 1)
            }
        }

    /**
     * Calculates and returns an updated [Int] count of unsafe adjacent levels for the provided [difference]
     * of two adjacent levels in a report.
     *
     * @param currentUnsafeCount Current count of adjacent levels that are not in safe values
     */
    private fun updateUnsafeLevelCount(currentUnsafeCount: Int, difference: Int): Int =
        currentUnsafeCount + if (difference.absoluteValue in 1..3) 0 else 1

}