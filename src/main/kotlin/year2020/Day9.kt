/**
 * Problem: Day9: Encoding Error
 * https://adventofcode.com/2020/day/9
 *
 * @author Kaushik N Sanji
 */

package year2020

import base.BaseFileHandler
import extensions.whileLoop

private class Day9 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1, arrayOf(5))
    println("=====")
    solveActual(1, arrayOf(25))
    println("=====")
    solveSample(2, arrayOf(5))
    println("=====")
    solveActual(2, arrayOf(25))
    println("=====")
}

private fun solveSample(executeProblemPart: Int, params: Array<Int>) {
    execute(Day9.getSampleFile().readLines(), executeProblemPart, params)
}

private fun solveActual(executeProblemPart: Int, params: Array<Int>) {
    execute(Day9.getActualTestFile().readLines(), executeProblemPart, params)
}

private fun execute(input: List<String>, executeProblemPart: Int, params: Array<Int>) {
    when (executeProblemPart) {
        1 -> doPart1(input, params)
        2 -> doPart2(input, params)
    }
}

private fun doPart1(input: List<String>, params: Array<Int>) {
    XmasDecipher.create(input, params[0])
        .getInvalidNumber()
        .also { println(it) }
}

private fun doPart2(input: List<String>, params: Array<Int>) {
    XmasDecipher.create(input, params[0])
        .getEncryptionWeakness()
        .also { println(it) }
}

private class XmasDecipher private constructor(
    val numbers: List<Long>,
    val preambleLength: Int
) {
    companion object {
        fun create(numbers: List<String>, preambleLength: Int): XmasDecipher =
            XmasDecipher(numbers.map { it.toLong() }, preambleLength)
    }

    fun getInvalidNumber(): Long =
        numbers.asSequence()
            .windowed(preambleLength + 1) { windowedList: List<Long> ->
                windowedList[preambleLength] to windowedList.slice(0 until preambleLength)
            }
            .map { (sumTo: Long, windowedNumbers: List<Long>) ->
                sumTo to windowedNumbers.asSequence()
                    .map { number: Long -> number to (sumTo - number) }
                    .filterNot { (number: Long, complement: Long) ->
                        number == complement && windowedNumbers.count { it == number } == 1
                    }
                    .any { (_, complement: Long) -> complement in windowedNumbers }
            }
            .first { (_, isValidNumber: Boolean) -> !isValidNumber }
            .first

    fun getEncryptionWeakness(): Long = getInvalidNumber().let { invalidNumber: Long ->
        whileLoop(
            2,
            { loopCounter: Int, lastIterationResult: List<Long>? ->
                lastIterationResult?.let { it.size >= 2 && it.size == loopCounter - 1 } ?: false
            }) { windowLength: Int ->
            windowLength + 1 to numbers
                .asSequence()
                .windowed(windowLength) { windowedList: List<Long> ->
                    windowedList.sum() to windowedList
                }
                .firstOrNull { (sumTo: Long, _) -> sumTo == invalidNumber }
                ?.second
        }.let { windowedList: List<Long> ->
            windowedList.minOrNull()!! + windowedList.maxOrNull()!!
        }
    }

}