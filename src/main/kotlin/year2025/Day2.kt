/**
 * Problem: Day2: Gift Shop
 * https://adventofcode.com/2025/day/2
 *
 * @author <a href='mailto:kaushiknsanji@gmail.com'>Kaushik N Sanji</a>
 */

package year2025

import base.BaseProblemHandler
import utils.Constants.COMMA_CHAR
import utils.findAllPositiveLong

class Day2 : BaseProblemHandler() {

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
    @Suppress("UNCHECKED_CAST")
    override fun doPart1(input: List<String>, otherArgs: Array<out Any?>): Any =
        GiftProductAnalyzer.parse(input)
            .getTotalValueOfInvalidProductIDs(
                requiredRepeatFreq = otherArgs[0] as Int,
                invalidationRule = otherArgs[1] as (Int, Int) -> Boolean
            )

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    @Suppress("UNCHECKED_CAST")
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        GiftProductAnalyzer.parse(input)
            .getTotalValueOfInvalidProductIDs(
                requiredRepeatFreq = otherArgs[0] as Int,
                invalidationRule = otherArgs[1] as (Int, Int) -> Boolean
            )

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        val invalidationRuleAtLeast: (repeatFreq: Int, requiredRepeatFreq: Int) -> Boolean =
            { repeatFreq, requiredRepeatFreq ->
                repeatFreq >= requiredRepeatFreq
            }

        solveSample(1, false, 0, 1227775554L, 2, Int::equals)
        solveActual(1, false, 0, 31210613313L, 2, Int::equals)
        solveSample(2, false, 0, 4174379265L, 2, invalidationRuleAtLeast)
        solveActual(2, false, 0, 41823587546L, 2, invalidationRuleAtLeast)
    }

}

fun main() {
    Day2().start()
}

private class GiftProductAnalyzer private constructor(
    private val idRanges: List<LongRange>
) {

    companion object {

        fun parse(input: List<String>): GiftProductAnalyzer = GiftProductAnalyzer(
            idRanges = input.single().split(COMMA_CHAR)
                .map { rangeString ->
                    rangeString.findAllPositiveLong().let { productIds: List<Long> ->
                        productIds.first()..productIds.last()
                    }
                }
        )
    }

    /**
     * Returns `true` if the given [Product ID][productId] is invalid based on the number of [times][requiredRepeatFreq]
     * a chunk in the [Product ID][productId] is required to repeat in order to qualify
     * its [invalidation rule][invalidationRule].
     */
    private fun isInvalid(
        productId: Long,
        requiredRepeatFreq: Int,
        invalidationRule: (repeatFreq: Int, requiredRepeatFreq: Int) -> Boolean
    ): Boolean {
        val productIdString = productId.toString()

        // Max possible length of repeating chunk is half the length of the given Product ID
        val maxRepeatingChunkLength = productIdString.length shr 1

        return (1..maxRepeatingChunkLength).map { size ->
            // Chunk Product ID for every possible size
            productIdString.chunked(size)
        }.any { productIdChunks: List<String> ->
            // Select this Product ID only when all its chunks are same and meets the required invalidation rule
            productIdChunks.distinct().size == 1 &&
                    invalidationRule(productIdChunks.size, requiredRepeatFreq)
        }
    }

    /**
     * [Solution for Part 1 & 2]
     *
     * Returns sum of all the values of Invalid Product IDs found.
     *
     * @param requiredRepeatFreq [Int] value of the number of times a chunk in Product ID is required to repeat
     * @param invalidationRule Lambda to enforce the rule of invalidation to determine if the Product ID with some
     * repeating chunks is invalid or not
     */
    fun getTotalValueOfInvalidProductIDs(
        requiredRepeatFreq: Int,
        invalidationRule: (repeatFreq: Int, requiredRepeatFreq: Int) -> Boolean
    ): Long = idRanges.flatMap { productIdRange ->
        // Find Invalid Product IDs from all Product ID ranges
        productIdRange.filter { productId ->
            isInvalid(productId, requiredRepeatFreq, invalidationRule)
        }
    }.sum()

}