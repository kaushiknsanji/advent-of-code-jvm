/**
 * Problem: Day7: Camel Cards
 * https://adventofcode.com/2023/day/7
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler

private class Day7 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 6440
    println("=====")
    solveActual(1)      // 250120186
    println("=====")
    solveSample(2)      // 5905
    println("=====")
    solveActual(2)      // 250665248
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day7.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day7.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    CamelCardPokerAnalyzer.parse(input)
        .getTotalWinnings()
        .also(::println)
}

private fun doPart2(input: List<String>) {
    CamelCardPokerAnalyzer.parse(input, isJackJoker = true)
        .getTotalWinnings()
        .also(::println)
}

private abstract class CamelCardPokerAbstract {
    val aceCard = 'A'
    val jokerCard = 'J'

    private val cards = arrayOf('2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', aceCard)

    private val cardsWithJackAsJoker =
        arrayOf(jokerCard, '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'Q', 'K', aceCard)

    enum class HandRank {
        HIGH_CARD, ONE_PAIR, TWO_PAIR, THREE_KIND, FULL_HOUSE, FOUR_KIND, FIVE_KIND
    }

    fun Char.toCardStrength(isJackJoker: Boolean = false): Int = if (isJackJoker) {
        cardsWithJackAsJoker.indexOf(this)
    } else {
        cards.indexOf(this)
    }
}

private class CamelCardHand private constructor(
    private val hand: String,
    val bid: Int,
    private val isJackJoker: Boolean
) : CamelCardPokerAbstract(), Comparable<CamelCardHand> {

    companion object {
        fun parse(handBidString: String, isJackJoker: Boolean): CamelCardHand =
            handBidString.split(" ").let { splitStrings ->
                CamelCardHand(
                    hand = splitStrings[0],
                    bid = splitStrings[1].toInt(),
                    isJackJoker
                )
            }
    }

    private val cardToCountMap: Map<Char, Int> = hand.groupingBy { it }.eachCount()

    private val handRank: HandRank = computeHandRank(cardToCountMap)

    private val jokerHandRank: HandRank = if (isJackJoker) {
        // If Jack is treated as Joker
        if (cardToCountMap.containsKey(jokerCard)) {
            cardToCountMap.toMutableMap().let { jokerCardToCountMap: MutableMap<Char, Int> ->
                val jokerCardCount = jokerCardToCountMap.remove(jokerCard)!!
                if (jokerCardCount == hand.length) {
                    // When all cards in hand are Jokers, change all to Ace and compute hand rank
                    computeHandRank(
                        jokerCardToCountMap.apply {
                            put(aceCard, jokerCardCount)
                        }
                    )
                } else {
                    // When there are other cards in hand, add the joker card count to that of the card having
                    // the highest count followed by highest strength, and then compute hand rank
                    jokerCardToCountMap.maxWith(
                        compareBy<Map.Entry<Char, Int>> { (_: Char, count: Int) ->
                            count
                        }.thenBy { (card: Char, _: Int) ->
                            card.toCardStrength(true)
                        })
                        .let { (card: Char, count: Int) ->
                            computeHandRank(
                                jokerCardToCountMap.apply {
                                    put(card, count + jokerCardCount)
                                }
                            )
                        }
                }
            }
        } else {
            // When there is no joker card, return the computed hand rank
            handRank
        }
    } else {
        // If Jack is NOT treated as Joker, then return the computed hand rank
        handRank
    }

    private fun computeHandRank(cardToCountMap: Map<Char, Int>): HandRank =
        if (cardToCountMap.size == hand.length) {
            HandRank.HIGH_CARD
        } else if (cardToCountMap.size == 4) {
            HandRank.ONE_PAIR
        } else if (cardToCountMap.size == 3) {
            if (cardToCountMap.values.any { it == 3 }) {
                HandRank.THREE_KIND
            } else {
                HandRank.TWO_PAIR
            }
        } else if (cardToCountMap.size == 2) {
            if (cardToCountMap.values.any { it == 4 }) {
                HandRank.FOUR_KIND
            } else {
                HandRank.FULL_HOUSE
            }
        } else if (cardToCountMap.size == 1) {
            HandRank.FIVE_KIND
        } else {
            throw IllegalStateException("Bad Input: Not able to evaluate Hand Rank for the Hand $CamelCardHand")
        }

    override fun compareTo(other: CamelCardHand): Int =
        if (!this.isJackJoker && this.handRank == other.handRank ||
            this.isJackJoker && this.jokerHandRank == other.jokerHandRank
        ) {
            this.hand.zip(other.hand).first { it.first != it.second }.let { pair: Pair<Char, Char> ->
                pair.first.toCardStrength(isJackJoker)
                    .compareTo(pair.second.toCardStrength(isJackJoker))
            }
        } else {
            if (this.isJackJoker) {
                this.jokerHandRank.compareTo(other.jokerHandRank)
            } else {
                this.handRank.compareTo(other.handRank)
            }
        }

}

private class CamelCardPokerAnalyzer private constructor(
    private val hands: List<CamelCardHand>
) {
    companion object {
        fun parse(input: List<String>, isJackJoker: Boolean = false): CamelCardPokerAnalyzer =
            input.map { line -> CamelCardHand.parse(line, isJackJoker) }.let(::CamelCardPokerAnalyzer)
    }

    /**
     * [Solution for Part 1 & 2]
     * Returns the total winnings for the hands in set
     */
    fun getTotalWinnings(): Int =
        hands.sorted().mapIndexed { index, camelCardHand ->
            camelCardHand.bid * (index + 1)
        }.sum()
}