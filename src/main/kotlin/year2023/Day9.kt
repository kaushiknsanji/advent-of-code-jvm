/**
 * Problem: Day9: Mirage Maintenance
 * https://adventofcode.com/2023/day/9
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import extensions.difference
import extensions.reversed

private class Day9 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 114
    println("=====")
    solveActual(1)      // 1479011877
    println("=====")
    solveSample(2)      // 2
    println("=====")
    solveActual(2)      // 973
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day9.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day9.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    input.map { line ->
        OasisSensorReport.parse(line)
    }.sumOf { oasisSensorReport ->
        oasisSensorReport.getNextPrediction()
    }.also { println(it) }
}

private fun doPart2(input: List<String>) {
    input.map { line ->
        OasisSensorReport.parse(line)
    }.sumOf { oasisSensorReport ->
        oasisSensorReport.getPreviousPrediction()
    }.also { println(it) }
}

private class OasisSensorReport private constructor(
    private val historyReadingsSequence: Sequence<Long>
) {
    companion object {
        private val numbersRegex = """(-?\d+)""".toRegex()

        fun parse(input: String): OasisSensorReport = OasisSensorReport(
            historyReadingsSequence = numbersRegex.findAll(input).map { it.groupValues[1] }.map(String::toLong)
        )
    }

    /**
     * [Solution for Part-1]
     * Returns the next predicted value for the [historyReadingsSequence]
     */
    fun getNextPrediction(): Long = generateSequence(historyReadingsSequence) { sequence ->
        sequence.windowed(size = 2, step = 1).map { numbers -> numbers.reversed().difference() }
    }.takeWhile { numbers -> !numbers.all { it == 0L } }
        .sumOf { numbers: Sequence<Long> ->
            numbers.last()
        }

    /**
     * [Solution for Part-2]
     * Returns the previous predicted value for the [historyReadingsSequence]
     */
    fun getPreviousPrediction(): Long = generateSequence(historyReadingsSequence) { sequence ->
        sequence.windowed(size = 2, step = 1).map { numbers -> numbers.reversed().difference() }
    }.takeWhile { numbers -> !numbers.all { it == 0L } }
        .map { numbers: Sequence<Long> ->
            numbers.first()
        }
        .reversed()
        .reduce { acc: Long, number: Long ->
            number - acc
        }

}