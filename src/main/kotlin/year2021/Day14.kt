/**
 * Problem: Day14: Extended Polymerization
 * https://adventofcode.com/2021/day/14
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2021

import base.BaseProblemHandler
import utils.Constants.EMPTY
import utils.Constants.RIGHT_ARROW
import utils.splitWhenLineBlankOrEmpty

class Day14 : BaseProblemHandler() {

    /**
     * Returns the Package name of this problem class
     */
    override fun getCurrentPackageName(): String = this::class.java.packageName

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
        PolymerGrowthAnalyzer.parse(input)
            .getDifferenceOfMostAndLeastCommonElements(10)

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        PolymerGrowthAnalyzer.parse(input)
            .getDifferenceOfMostAndLeastCommonElements(40)

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, 1588L)
        solveActual(1, false, 0, 3411L)
        solveSample(2, false, 0, 2188189693529L)
        solveActual(2, false, 0, 7477815755570L)
    }

}

fun main() {
    Day14().start()
}

private class PolymerGrowthAnalyzer private constructor(
    private val polymerTemplate: String,
    private val adjacentElementsInsertionRuleMap: Map<String, String>
) {
    companion object {

        fun parse(input: List<String>): PolymerGrowthAnalyzer =
            input.splitWhenLineBlankOrEmpty()
                .let { splitBlocks: Iterable<Iterable<String>> ->
                    PolymerGrowthAnalyzer(
                        polymerTemplate = splitBlocks.first().single(),
                        adjacentElementsInsertionRuleMap = splitBlocks.last().flatMap { line ->
                            line.split(RIGHT_ARROW).zipWithNext { adjacentElements, insertion ->
                                adjacentElements.trim() to insertion.trim()
                            }
                        }.toMap()
                    )
                }
    }

    // Map of Adjacent Elements to Pair Insertion rule applied
    private val adjacentElementsGrowthMap: Map<String, String> by lazy {
        adjacentElementsInsertionRuleMap.keys.associateWith { adjacentElements: String ->
            listOf(
                adjacentElements.first(),
                adjacentElementsInsertionRuleMap[adjacentElements],
                adjacentElements.last()
            ).joinToString(EMPTY)
        }.toMap()
    }

    /**
     * Returns a Map of Adjacent Elements to its Count of Occurrence resulting from the growth of [polymerTemplate]
     * as the pair insertion process is executed for the given [number of steps][totalSteps].
     *
     * Since we are only interested in the count of each element later, we are not actually building the polymer.
     * Instead, we focus on the count of Adjacent Elements occurring at every step of pair insertion process.
     * Initially, it gathers Adjacent Elements from [polymerTemplate] with their count and passes it onto the next
     * step. At every following step, it gets new Adjacent Elements arising from the pair insertion process applied
     * on previous Adjacent Elements using [adjacentElementsGrowthMap]. For the new Adjacent Elements, it gets
     * their count from the previous insertion step, aggregates their counts and passes it onto the next step. After
     * executing the pair insertion process for the given [number of steps][totalSteps], it returns the latest
     * Map of Adjacent Elements to its Count of Occurrence in the final polymer.
     */
    private fun generateAdjacentElementsCountMap(totalSteps: Int): Map<String, Long> =
        generateSequence(
            polymerTemplate.windowed(2, 1)
                .groupingBy { adjacentElements: String -> adjacentElements }
                .fold(0L) { acc: Long, _: String ->
                    acc + 1
                }
        ) { previousAdjacentElementsCountMap: Map<String, Long> ->
            previousAdjacentElementsCountMap.keys.flatMap { previousAdjacentElements: String ->
                adjacentElementsGrowthMap[previousAdjacentElements]!!.windowed(2, 1)
                    .map { nextAdjacentElements: String ->
                        nextAdjacentElements to previousAdjacentElementsCountMap[previousAdjacentElements]!!
                    }
            }.groupingBy { adjacentElementsCountPair: Pair<String, Long> ->
                adjacentElementsCountPair.first
            }.fold(0L) { acc: Long, adjacentElementsCountPair: Pair<String, Long> ->
                acc + adjacentElementsCountPair.second
            }
        }.drop(1).take(totalSteps).last()

    /**
     * [Solution for Part 1 and 2]
     *
     * Returns the difference in the count of occurrence between the Most and the Least occurring element
     * found in the final polymer generated after growing the given [polymer][polymerTemplate]
     * for the given [number of steps][totalSteps] of pair insertion process, based on
     * the [insertion rules][adjacentElementsInsertionRuleMap].
     */
    fun getDifferenceOfMostAndLeastCommonElements(totalSteps: Int): Long =
        with(generateAdjacentElementsCountMap(totalSteps)) {
            // With the Map of Adjacent Elements to its Count of Occurrence

            // Do group-and-fold operation on the first element of every Adjacent Elements
            keys.groupingBy { adjacentElements: String ->
                adjacentElements.first()
            }.fold(0L) { acc: Long, adjacentElements: String ->
                if (acc == 0L && adjacentElements.first() == polymerTemplate.last()) {
                    // At the beginning of accumulation for the current element, if the current element also happens
                    // to be the last element occurring in the polymer template then add one more to its count
                    // derived from the Map
                    this[adjacentElements]!! + 1
                } else {
                    // Else, just accumulate the count of the current element derived from the Map
                    acc + this[adjacentElements]!!
                }
            }.values.sorted().let { elementCounts ->
                // Return the count difference between the Most and the Least occurring element
                // after sorting the counts of elements
                elementCounts.last() - elementCounts.first()
            }
        }

}