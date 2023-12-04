/**
 * Problem: Day4: Scratchcards
 * https://adventofcode.com/2023/day/4
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler

private class Day4 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 13
    println("=====")
    solveActual(1)      // 22193
    println("=====")
    solveSample(2)      // 30
    println("=====")
    solveActual(2)      // 5625994
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day4.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day4.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    ScratchCardAnalyzer.parse(input).getTotalPointsWon().also { println(it) }
}

private fun doPart2(input: List<String>) {
    ScratchCardAnalyzer.parse(input)
        .getScratchCardsCount()
        .also { println(it) }
}

private class ScratchCard private constructor(
    val cardId: Int,
    val winningNumbers: Set<Int>,
    val numbersAtHand: Set<Int>
) {
    constructor(cardIdString: String, winningNumbersString: String, numbersAtHandString: String) : this(
        cardId = cardIdString.trim().toInt(),
        winningNumbers = winningNumbersString.trim().split("""\s+""".toRegex()).map { it.toInt() }.toSet(),
        numbersAtHand = numbersAtHandString.trim().split("""\s+""".toRegex()).map { it.toInt() }.toSet()
    )

    val numbersWon: Set<Int> = winningNumbers.intersect(numbersAtHand)

    val pointsWon: Int = numbersWon.takeUnless { it.isEmpty() }?.let { numbersWonSet ->
        // Multiply with the powers of 2 using bit shift left operation
        1 shl (numbersWonSet.count() - 1)
    } ?: 0

    val cardsWon: List<Int> = numbersWon.takeUnless { it.isEmpty() }?.let { numbersWonSet ->
        generateSequence(cardId) { previousCardId: Int ->
            previousCardId + 1
        }.take(numbersWonSet.size + 1).drop(1).toList()
    } ?: emptyList()

}

private class ScratchCardAnalyzer private constructor(
    private val scratchCards: List<ScratchCard>
) {
    companion object {
        private const val COLON = ":"
        private const val NUMBER_SEPARATOR = "|"
        private const val CARD = "Card"

        fun parse(inputCardsInfo: List<String>): ScratchCardAnalyzer = ScratchCardAnalyzer(
            scratchCards = inputCardsInfo.map { cardInfo: String ->
                ScratchCard(
                    cardIdString = cardInfo.substringBefore(COLON).substringAfter(CARD),
                    winningNumbersString = cardInfo.substringBefore(NUMBER_SEPARATOR).substringAfter(COLON),
                    numbersAtHandString = cardInfo.substringAfter(NUMBER_SEPARATOR)
                )
            }
        )
    }

    private val cardIdToCardsMap: Map<Int, ScratchCard> = scratchCards.associateBy { card -> card.cardId }

    /**
     * [Solution for Part-1]
     * Returns total points won for the scratch cards
     */
    fun getTotalPointsWon(): Int = scratchCards.sumOf { it.pointsWon }

    /**
     * [Solution for Part-2]
     * Returns total count of scratch cards including those that were won
     */
    fun getScratchCardsCount(): Int {
        // Array to save the count of copies won including the original card identified by the array index
        val countArray = IntArray(scratchCards.size + 1) { 0 }

        // Iterate backwards. This is based on the provided assumption that Cards will never have a copy
        // past the end of the entire list
        (scratchCards.size downTo 1).forEach { cardId ->
            // For each card identified by its index, save the count of itself (original) with that of the copies won
            countArray[cardId] += (cardIdToCardsMap[cardId]!!.cardsWon.takeUnless { it.isEmpty() }
                ?.sumOf { idOfCardWon ->
                    // Count of cards will be tracked at their respective index and calculated during
                    // each previous iteration. Since we are iterating backwards, this helps to use the result
                    // already calculated for the card being evaluated.
                    countArray[idOfCardWon]
                } ?: 0) + 1
        }

        // Return the sum total of all scratch cards which includes the copies won
        return countArray.sum()
    }
}