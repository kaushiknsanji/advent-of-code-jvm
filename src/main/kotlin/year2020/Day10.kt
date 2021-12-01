/**
 * Problem: Day10: Adapter Array
 * https://adventofcode.com/2020/day/10
 *
 * @author Kaushik N Sanji
 */

package year2020

import base.BaseFileHandler
import extensions.product
import extensions.whileLoop

private class Day10 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample1(1)
    println("=====")
    solveSample2(1)
    println("=====")
    solveActual(1)
    println("=====")
    solveSample1(2)
    println("=====")
    solveSample2(2)
    println("=====")
    solveActual(2)
    println("=====")
}

private fun solveSample1(executeProblemPart: Int) {
    execute(Day10.getSampleFile("1").readLines(), executeProblemPart)
}

private fun solveSample2(executeProblemPart: Int) {
    execute(Day10.getSampleFile("2").readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day10.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    AdapterBag.create(input)
        .getJoltDifferenceProduct()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    AdapterBag.create(input)
        .getCountOfDistinctArrangements()
        .also { println(it) }
}

private class AdapterBag private constructor(
    val ratedAdapters: List<Int>,
    val deviceRating: Int,
    val outletRating: Int
) {
    companion object {
        fun create(ratings: List<String>, outletRating: Int = 0): AdapterBag =
            ratings.map { it.toInt() }.let { ratedAdapters: List<Int> ->
                AdapterBag(
                    ratedAdapters = ratedAdapters,
                    deviceRating = (ratedAdapters.maxOrNull() ?: 0) + 3,
                    outletRating = outletRating
                )
            }
    }

    fun getJoltDifferenceProduct(): Int =
        getAllCompatibleComponentRatings()
            .zipWithNext { rating1, rating2 -> rating2 - rating1 }
            .groupingBy { it }
            .eachCount()
            .values
            .product()

    private fun getAllCompatibleComponentRatings(): List<Int> = mutableListOf(outletRating).let { allComponentRatings ->
        whileLoop(
            outletRating,
            { lastComponentRating: Int, _: List<Int>? -> lastComponentRating == deviceRating }
        ) { lastComponentRating: Int ->
            val nextComponentRating: Int = listOf(
                lastComponentRating + 1,
                lastComponentRating + 2,
                lastComponentRating + 3
            )
                .first { rating: Int -> rating == deviceRating || rating in ratedAdapters }

            nextComponentRating to allComponentRatings.apply { add(nextComponentRating) }
        }
    }

    fun getCountOfDistinctArrangements(): Long {
        val allComponentRatings = getAllCompatibleComponentRatings()
        val adapterArrangementCountMap = mutableMapOf<Int, Long>().apply { this[outletRating] = 1 }

        allComponentRatings.slice(1 until allComponentRatings.size).forEach { adapterRating: Int ->
            listOf(adapterRating - 1, adapterRating - 2, adapterRating - 3)
                .filter { rating: Int -> rating in allComponentRatings }
                .sumOf { rating -> adapterArrangementCountMap[rating] ?: 0 }
                .also { sum: Long ->
                    adapterArrangementCountMap[adapterRating] = sum
                }
        }

        return adapterArrangementCountMap[allComponentRatings.last()]!!
    }

}