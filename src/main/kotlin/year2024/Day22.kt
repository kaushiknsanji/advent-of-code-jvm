/**
 * Problem: Day22: Monkey Market
 * https://adventofcode.com/2024/day/22
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler

class Day22 : BaseProblemHandler() {

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
        SecretNumberAnalyzer.parse(input)
            .getTotalOfAllSecretNumbers(2000)

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        SecretNumberAnalyzer.parse(input)
            .getMaxCountOfBananasPossible(2000)

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, true, 0, 37327623L)
        solveActual(1, false, 0, 17960270302L)
        solveSample(2, true, 0, 23)
        solveActual(2, false, 0, 2042)
    }

}

fun main() {
    Day22().start()
}

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