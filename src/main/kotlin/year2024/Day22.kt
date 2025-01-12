/**
 * Problem: Day22: Monkey Market
 * https://adventofcode.com/2024/day/22
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import org.junit.jupiter.api.Assertions.assertEquals

private class Day22 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    listOf(
        ::solveSamplePart1 to arrayOf<Any?>(1, 37327623L),
        ::solveActual to arrayOf<Any?>(1, 17960270302L),
        ::solveSamplePart2 to arrayOf<Any?>(2, 23),
        ::solveActual to arrayOf<Any?>(2, 2042)
    ).forEach { (solver, args: Array<Any?>) ->
        val result = solver(args[0] as Int).also(::println)

        // Last argument should be the expected value. If unknown, it will be `null`. When known, following statement
        // asserts the `result` with the expected value.
        if (args.last() != null) {
            assertEquals(args.last(), result)
        }
        println("=====")
    }
}

private fun solveSamplePart1(executeProblemPart: Int): Any =
    execute(Day22.getSampleFile("_part1").readLines(), executeProblemPart)

private fun solveSamplePart2(executeProblemPart: Int): Any =
    execute(Day22.getSampleFile("_part2").readLines(), executeProblemPart)

private fun solveActual(executeProblemPart: Int): Any =
    execute(Day22.getActualTestFile().readLines(), executeProblemPart)

private fun execute(input: List<String>, executeProblemPart: Int): Any =
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
        else -> throw Error("Unexpected Problem Part: $executeProblemPart")
    }

private fun doPart1(input: List<String>): Any =
    SecretNumberAnalyzer.parse(input)
        .getTotalOfAllSecretNumbers(2000)

private fun doPart2(input: List<String>): Any =
    SecretNumberAnalyzer.parse(input)
        .getMaxCountOfBananasPossible(2000)


private class SecretNumberAnalyzer private constructor(
    private val initialSecretNumbers: List<Long>
) {

    companion object {
        fun parse(input: List<String>): SecretNumberAnalyzer = SecretNumberAnalyzer(
            initialSecretNumbers = input.map(String::toLong)
        )
    }

    /**
     * Mixes given [value] into the given [secret number][secretNumber] and then prunes the resulting secret number.
     *
     * @return A [Long] value of the new secret number resulting from mix and prune operation done
     * on the given [secret number][secretNumber].
     */
    private fun mixAndPruneNumber(value: Long, secretNumber: Long): Long =
        (value xor secretNumber) % 16777216

    /**
     * Returns a [Long] value of the New secret number generated from the given [secret number][secretNumber].
     */
    private fun getNextSecretNumber(secretNumber: Long): Long {
        val result1 = mixAndPruneNumber(secretNumber * 64L, secretNumber)
        val result2 = mixAndPruneNumber(result1 shr 5, result1)
        return mixAndPruneNumber(result2 * 2048L, result2)
    }

    /**
     * Returns an [Int] value of the Price, that is the number of Bananas the buyer is offering, which is derived
     * from the [secret number][secretNumber].
     */
    private fun getPrice(secretNumber: Long): Int =
        (secretNumber % 10).toInt()

    /**
     * Class for Buyer's future price information.
     *
     * @property secretNumber [Long] value of the secret number
     * @property price [Int] value of the Price which is essentially the number of Bananas the buyer is offering
     * @property priceChange [Int] value of the change in [price] compared to previous. Defaulted to 0.
     */
    private class BuyerData(
        val secretNumber: Long,
        val price: Int,
        val priceChange: Int = 0
    )

    /**
     * [Solution for Part-1]
     *
     * Returns the sum of all 'n'-th secret number, generated for each of
     * the [initial secret numbers][initialSecretNumbers], where 'n = [newSecretNumberCount]'.
     *
     * @param newSecretNumberCount [Int] number of new secret numbers to generate for each of
     * the [initial secret numbers][initialSecretNumbers]
     */
    fun getTotalOfAllSecretNumbers(newSecretNumberCount: Int): Long =
        initialSecretNumbers.sumOf { secretNumber ->
            generateSequence(secretNumber) { number ->
                getNextSecretNumber(number)
            }.drop(1).take(newSecretNumberCount).last()
        }

    /**
     * [Solution for Part-2]
     *
     * Returns the Maximum number of Bananas one can get based on the best four price change sequence
     * in every buyer's future prices.
     *
     * @param newSecretNumberCount [Int] number of new secret numbers to generate for each of
     * the [initial secret numbers][initialSecretNumbers]
     */
    fun getMaxCountOfBananasPossible(newSecretNumberCount: Int): Int =
        initialSecretNumbers.asSequence().flatMap { secretNumber ->
            generateSequence(BuyerData(secretNumber, getPrice(secretNumber))) { buyerData ->
                // Get the next secret number
                val nextNumber = getNextSecretNumber(buyerData.secretNumber)
                // Get the new Price which is the number of Bananas this buyer offers, derived from next secret number
                val nextPrice = getPrice(nextNumber)
                // Return BuyerData with information of next secret number, next price and change in price
                // with respect to current price
                BuyerData(nextNumber, nextPrice, nextPrice - buyerData.price)
            }.drop(1).take(newSecretNumberCount).toList() // Drop initial BuyerData and take the rest
                .windowed(4, 1) // Sliding window of 4 to get four price change sequence
                .map { windowedBuyerDataList: List<BuyerData> ->
                    // Transform each sliding window into a Pair of
                    // four price change sequence to last price of the window
                    windowedBuyerDataList.map { buyerData ->
                        buyerData.priceChange
                    } to windowedBuyerDataList.last().price
                }.distinctBy { fourPriceChangesToPricePair: Pair<List<Int>, Int> ->
                    // Pick only the first occurrence of this four price change sequence because the Monkey sells
                    // hiding spot to the buyer for bananas when it hears the sequence for the first time
                    fourPriceChangesToPricePair.first
                }
        }.groupingBy { fourPriceChangesToPricePair: Pair<List<Int>, Int> ->
            // Group future price information from across buyers based on the four price change sequence
            fourPriceChangesToPricePair.first
        }.fold(0) { accumulator: Int, fourPriceChangesToPricePair: Pair<List<Int>, Int> ->
            // Fold on price from each buyer
            accumulator + fourPriceChangesToPricePair.second
        }.values.max() // Return the Maximum price found

}