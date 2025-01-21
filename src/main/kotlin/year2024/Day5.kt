/**
 * Problem: Day5: Print Queue
 * https://adventofcode.com/2024/day/5
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler
import utils.findAllInt
import utils.splitWhenLineBlankOrEmpty

private class Day5 : BaseProblemHandler() {

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
        PrintQueueProcessor.parse(input)
            .getSumOfCorrectlyOrderedMiddlePageNumbers()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        PrintQueueProcessor.parse(input)
            .getSumOfRectifiedIncorrectlyOrderedMiddlePageNumbers()

}

fun main() {
    with(Day5()) {
        solveSample(1, false, 0, 143)
        solveActual(1, false, 0, 5509)
        solveSample(2, false, 0, 123)
        solveActual(2, false, 0, 4407)
    }
}

private class PrintQueueProcessor private constructor(
    private val orderingRulesMap: Map<Int, Set<Int>>,
    private val pagesToProduce: List<List<Int>>
) {

    companion object {

        fun parse(input: List<String>): PrintQueueProcessor =
            input.splitWhenLineBlankOrEmpty()
                .let { splitBlocks: Iterable<Iterable<String>> ->
                    PrintQueueProcessor(
                        orderingRulesMap = splitBlocks.first()
                            .map(String::findAllInt)
                            .groupBy(List<Int>::first)
                            .mapValues { (_: Int, value: List<List<Int>>) ->
                                value.map(List<Int>::last).toSet()
                            },

                        pagesToProduce = splitBlocks.last()
                            .map(String::findAllInt)
                    )
                }

    }

    /**
     * Returns `true` if [this] update has correctly ordered page numbers according
     * to the rules from [orderingRulesMap]; otherwise `false`.
     */
    private fun List<Int>.isCorrectlyOrdered(): Boolean =
        this.withIndex().all { (index: Int, pageNumber: Int) ->
            // For the current page number, if all the numbers present after it in the update are found in its
            // ordering rule, then it is in correct order with respect to the numbers following it.
            this.subList(index + 1, size)
                .intersect(orderingRulesMap.getOrDefault(pageNumber, emptySet()))
                .size == size - index - 1
        }

    /**
     * Returns middle page number from [this] list of page numbers.
     */
    private fun List<Int>.getMiddlePageNumber(): Int = this[this.size shr 1]

    /**
     * [Solution for Part-1]
     *
     * Returns sum of all middle page numbers of each correctly ordered update.
     */
    fun getSumOfCorrectlyOrderedMiddlePageNumbers(): Int =
        pagesToProduce.filter { pages: List<Int> ->
            pages.isCorrectlyOrdered()
        }.sumOf { pages: List<Int> ->
            pages.getMiddlePageNumber()
        }

    /**
     * [Solution for Part-2]
     *
     * Returns sum of all middle page numbers of each incorrectly ordered update, post reordering it
     * according to the rules from [orderingRulesMap].
     */
    fun getSumOfRectifiedIncorrectlyOrderedMiddlePageNumbers(): Int =
        pagesToProduce.filterNot { pages: List<Int> ->
            pages.isCorrectlyOrdered()
        }.sumOf { pages: List<Int> ->
            pages.sortedWith { currentNumber, nextNumber ->
                // Swap when [currentNumber] has no ordering rule or when it has but does not contain [nextNumber]
                // in its ordering rule. This is because, if the current number has no ordering rule, then it
                // should be the last number, and when it is present with the next number in its rule,
                // then it is in correct order.
                if (orderingRulesMap[currentNumber] == null
                    || !orderingRulesMap[currentNumber]!!.contains(nextNumber)
                ) {
                    1  // [currentNumber] should come after [nextNumber]
                } else -1 // [currentNumber] is before [nextNumber] as expected
            }.getMiddlePageNumber()
        }

}