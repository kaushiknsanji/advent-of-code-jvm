/**
 * Problem: Day5: Print Queue
 * https://adventofcode.com/2024/day/5
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import extensions.splitWhen

private class Day5 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 143
    println("=====")
    solveActual(1)      // 5509
    println("=====")
    solveSample(2)      // 123
    println("=====")
    solveActual(2)      // 4407
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day5.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day5.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    PrintQueueProcessor.parse(input)
        .getSumOfCorrectlyOrderedMiddlePageNumbers()
        .also(::println)
}

private fun doPart2(input: List<String>) {
    PrintQueueProcessor.parse(input)
        .getSumOfRectifiedIncorrectlyOrderedMiddlePageNumbers()
        .also(::println)
}

private class PrintQueueProcessor private constructor(
    private val orderingRulesMap: Map<Int, Set<Int>>,
    private val pagesToProduce: List<List<Int>>
) {

    companion object {
        // Regular expression to capture numbers
        private val numberRegex = """(\d+)""".toRegex()

        private const val COMMA = ','

        fun parse(input: List<String>): PrintQueueProcessor =
            input.splitWhen { line -> line.isEmpty() || line.isBlank() }
                .partition { lines: Iterable<String> -> lines.any { line -> line.contains(COMMA) } }
                .let { (pageNumberLines: List<Iterable<String>>, ruleLines: List<Iterable<String>>) ->
                    PrintQueueProcessor(
                        orderingRulesMap = ruleLines.single()
                            .map { ruleLine ->
                                numberRegex.findAll(ruleLine).map { ruleNumberMatchResult ->
                                    ruleNumberMatchResult.groupValues[1].toInt()
                                }.toList()
                            }.groupBy { ruleNumbers: List<Int> ->
                                ruleNumbers.first()
                            }.mapValues { (_: Int, value: List<List<Int>>) ->
                                value.map { ruleNumbers: List<Int> -> ruleNumbers.last() }.toSet()
                            },

                        pagesToProduce = pageNumberLines.single()
                            .map { pageNumberLine ->
                                numberRegex.findAll(pageNumberLine).map { pageNumberMatchResult ->
                                    pageNumberMatchResult.groupValues[1].toInt()
                                }.toList()
                            }
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