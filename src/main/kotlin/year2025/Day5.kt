/**
 * Problem: Day5: Cafeteria
 * https://adventofcode.com/2025/day/5
 *
 * @author <a href='mailto:kaushiknsanji@gmail.com'>Kaushik N Sanji</a>
 */

package year2025

import base.BaseProblemHandler
import extensions.mergeLongRanges
import extensions.rangeLength
import utils.Constants.HYPHEN_STRING
import utils.findAllPositiveLong
import utils.splitWhenLineBlankOrEmpty

class Day5 : BaseProblemHandler() {

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
        CafeteriaInventoryAnalyzer.parse(input)
            .getAvailableFreshIngredientCount()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        CafeteriaInventoryAnalyzer.parse(input)
            .getFreshIngredientCountFromRanges()

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, 3)
        solveActual(1, false, 0, 698)
        solveSample(2, false, 0, 14L)
        solveActual(2, false, 0, 352807801032167L)
    }

}

fun main() {
    try {
        Day5().start()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

private class CafeteriaInventoryAnalyzer private constructor(
    private val freshIngredientIDRanges: List<LongRange>,
    private val availableIngredientIDs: List<Long>
) {

    companion object {

        fun parse(input: List<String>): CafeteriaInventoryAnalyzer = input.splitWhenLineBlankOrEmpty()
            .partition { pattern: Iterable<String> -> pattern.first().contains(HYPHEN_STRING) }
            .let { (rangeStrings: List<Iterable<String>>, idStrings: List<Iterable<String>>) ->
                CafeteriaInventoryAnalyzer(
                    freshIngredientIDRanges = rangeStrings.single().map { rangeString ->
                        rangeString.findAllPositiveLong().let { longNumbers ->
                            longNumbers.first()..longNumbers.last()
                        }
                    },
                    availableIngredientIDs = idStrings.single().map { numberString ->
                        numberString.toLong()
                    }
                )
            }
    }

    /**
     * [Solution for Part-1]
     *
     * Returns the total number of Fresh Ingredients found
     * from the list of [available ingredients][availableIngredientIDs].
     */
    fun getAvailableFreshIngredientCount(): Int = availableIngredientIDs.count { ingredientID: Long ->
        freshIngredientIDRanges.any { freshIngredientIDRange: LongRange ->
            ingredientID in freshIngredientIDRange
        }
    }

    /**
     * [Solution for Part-2]
     *
     * Returns the total number of Fresh Ingredients found from the [Ingredient ID Ranges][freshIngredientIDRanges].
     */
    fun getFreshIngredientCountFromRanges(): Long =
        freshIngredientIDRanges.mergeLongRanges().sumOf { mergedRange -> mergedRange.rangeLength() }

}