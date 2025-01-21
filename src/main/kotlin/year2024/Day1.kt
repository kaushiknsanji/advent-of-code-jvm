/**
 * Problem: Day1: Historian Hysteria
 * https://adventofcode.com/2024/day/1
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler
import utils.findAllInt
import kotlin.math.absoluteValue

private class Day1 : BaseProblemHandler() {

    /**
     * Returns the Package name of this problem class
     */
    override fun getCurrentPackageName(): String = this::class.java.`package`.name

    /**
     * Returns the Class name of this problem class
     */
    override fun getClassName(): String = this::class.java.simpleName

    /**
     * Executes "Part-1" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart1(input: List<String>, otherArgs: Array<out Any?>): Any =
        MomentousLocationsAnalyzer.parse(input)
            .getTotalDistanceBetweenLists()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        MomentousLocationsAnalyzer.parse(input)
            .getSimilarityScore()

}

fun main() {
    with(Day1()) {
        solveSample(1, false, 0, 11)
        solveActual(1, false, 0, 2166959)
        solveSample(2, false, 0, 31)
        solveActual(2, false, 0, 23741109)
    }
}

private class MomentousLocationsAnalyzer private constructor(
    private val leftNumbers: List<Int>,
    private val rightNumbers: List<Int>
) {

    companion object {

        fun parse(input: List<String>): MomentousLocationsAnalyzer =
            input.map(String::findAllInt)
                .flatMap(List<Int>::zipWithNext)
                .unzip().let { (leftNumbers: List<Int>, rightNumbers: List<Int>) ->
                    MomentousLocationsAnalyzer(leftNumbers.sorted(), rightNumbers.sorted())
                }

    }

    // Map of occurrences of each Location ID
    val frequencyMap: Map<Int, Int> by lazy {
        rightNumbers.groupingBy { it }.eachCount()
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
        leftId * frequencyMap.getOrDefault(leftId, 0)
    }

}