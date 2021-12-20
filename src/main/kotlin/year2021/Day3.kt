/**
 * Problem: Day3: Binary Diagnostic
 * https://adventofcode.com/2021/day/3
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2021

import base.BaseFileHandler
import extensions.difference
import extensions.whileLoop

private class Day3 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 198
    println("=====")
    solveActual(1) // 4160394
    println("=====")
    solveSample(2) // 230
    println("=====")
    solveActual(2) // 4125600
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day3.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day3.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    SubmarineDiagnostics.parse(input)
        .getPowerConsumption()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    SubmarineDiagnostics.parse(input)
        .getLifeSupportRating()
        .also { println(it) }
}

private class SubmarineDiagnostics private constructor(
    private val diagReports: List<List<Char>>
) {

    companion object {
        fun parse(input: List<String>): SubmarineDiagnostics = SubmarineDiagnostics(
            diagReports = input.map { report: String -> report.map { it } }
        )
    }

    private val binaryCharCountAtPositionMap: (reports: List<List<Char>>) -> Map<Int, Map<Char, Int>> = { reports ->
        reports
            .flatMap { binaryReport: List<Char> -> binaryReport.withIndex() }
            .groupBy { indexedBinaryReport -> indexedBinaryReport.index }
            .mapValues { (_, indexedBinaryCharsAtSamePosition) -> indexedBinaryCharsAtSamePosition.map { it.value } }
            .mapValues { (_, binaryCharsAtPosition) -> binaryCharsAtPosition.groupingBy { it }.eachCount() }
    }

    private val gammaRateBinaryString
        get() = StringBuilder().apply {
            binaryCharCountAtPositionMap(diagReports).forEach { (_, binaryCharCountMap) ->
                binaryCharCountMap.maxByOrNull { it.value }?.let { (binaryChar, _) ->
                    this.append(binaryChar)
                }
            }
        }.toString()

    private val epsilonRateBinaryString
        get() = StringBuilder().apply {
            binaryCharCountAtPositionMap(diagReports).forEach { (_, binaryCharCountMap) ->
                binaryCharCountMap.minByOrNull { it.value }?.let { (binaryChar, _) ->
                    this.append(binaryChar)
                }
            }
        }.toString()

    private val oxygenGeneratorRateString: String
        get() = whileLoop(
            loopStartCounter = 0,
            initialResult = diagReports,
            { _, lastIterationResult: List<List<Char>>? ->
                lastIterationResult?.size == 1
            }
        ) { loopCounter: Int, lastIterationResult: List<List<Char>> ->
            val binaryCharCountMap = binaryCharCountAtPositionMap(lastIterationResult)[loopCounter]!!
            val bitCharToPick: Char = if (binaryCharCountMap.values.difference() == 0) {
                '1'
            } else {
                binaryCharCountMap.maxByOrNull { it.value }!!.key
            }

            (loopCounter + 1) to lastIterationResult.filter { binaryReport: List<Char> ->
                binaryReport[loopCounter] == bitCharToPick
            }
        }.single().joinToString("")

    private val co2ScrubberRateString: String
        get() = whileLoop(
            loopStartCounter = 0,
            initialResult = diagReports,
            { _, lastIterationResult: List<List<Char>>? ->
                lastIterationResult?.size == 1
            }
        ) { loopCounter: Int, lastIterationResult: List<List<Char>> ->
            val binaryCharCountMap = binaryCharCountAtPositionMap(lastIterationResult)[loopCounter]!!
            val bitCharToPick: Char = if (binaryCharCountMap.values.difference() == 0) {
                '0'
            } else {
                binaryCharCountMap.minByOrNull { it.value }!!.key
            }

            (loopCounter + 1) to lastIterationResult.filter { binaryReport: List<Char> ->
                binaryReport[loopCounter] == bitCharToPick
            }
        }.single().joinToString("")

    /**
     * [Solution for Part-1]
     * Returns the value of "Power Consumption" after deriving the "Gamma Rate" and "Epsilon Rate"
     * from the diagnostics.
     */
    fun getPowerConsumption(): Int = gammaRateBinaryString.toInt(2) * epsilonRateBinaryString.toInt(2)

    /**
     * [Solution for Part-2]
     * Returns the rating of "Life Support" after deriving the "Oxygen Generator Rate" and "CO2 Scrubber Rate"
     * from the diagnostics.
     */
    fun getLifeSupportRating(): Int = oxygenGeneratorRateString.toInt(2) * co2ScrubberRateString.toInt(2)

}