/**
 * Problem: Day1: Report repair
 * https://adventofcode.com/2020/day/1
 *
 * @author Kaushik N. Sanji
 */

package year2020

import base.BaseFileHandler
import extensions.product

private class Day1 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)
    println("=====")
    solveActual(1)
    println("=====")
    solveSample(2)
    println("=====")
    solveActual(2)
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day1.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day1.getActualTestFile("1").readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    evaluateExpenses(
        input.map(String::toInt),
        2020
    )
}

private fun doPart2(input: List<String>) {
    evaluateExpenses(
        input.map(String::toInt),
        2020,
        true
    )
}

/**
 * Solves the expense data.
 */
private fun evaluateExpenses(entries: List<Int>, sumTo: Int, isTriplet: Boolean = false) {
    val computedPair: Pair<Int, Int>? = if (!isTriplet) {
        // When not evaluating for the triplet, return the pair found
        entries.toEntryComplementPair(sumTo)
    } else {
        // When evaluating for the triplet, compute the pair for the triplet
        var computedPairForTriplet: Pair<Int, Int>? = null
        // Loop all entries to find one that fits with a pair for the triplet
        for (number in entries) {
            // Assuming this number to be part of the triplet, find the pair for the remaining sum
            val complementPair = entries.toEntryComplementPair(sumTo - number) ?: continue
            // When evaluated, record it as a pair for the triplet
            computedPairForTriplet = (number to complementPair.toList().product())
            break
        }
        // Return the pair found
        computedPairForTriplet
    }
    // Print the product of the pair if available
    println(computedPair?.toList()?.product())
}

/**
 * Finds and returns a [Pair] that fits the [sum][sumTo].
 */
private fun List<Int>.toEntryComplementPair(sumTo: Int): Pair<Int, Int>? =
    filter { value -> value < sumTo && (sumTo - value) in this } // Pick number pairs that evaluate to the sum
        // Do not count itself for the pair if it is not present more than once
        .filterNot { value -> sumTo - value == value && this.count { it == value } == 1 }
        .firstNotNullOfOrNull { value -> value to sumTo - value } // Return the number pairs found