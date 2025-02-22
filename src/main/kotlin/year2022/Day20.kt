/**
 * Problem: Day20: Grove Positioning System
 * https://adventofcode.com/2022/day/20
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseProblemHandler

private class Day20 : BaseProblemHandler() {

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
        GrovePositioningSystemAnalyzer.parse(input)
            .getSumOfGroveCoordinates()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        GrovePositioningSystemAnalyzer.parse(input, otherArgs[0] as Long)
            .getSumOfGroveCoordinates(otherArgs[1] as Int)

}

fun main() {
    with(Day20()) {
        solveSample(1, false, 0, 3L)
        solveActual(1, false, 0, 9687L)
        solveSample(2, false, 0, 1623178306L, 811589153L, 10)
        solveActual(2, false, 0, 1338310513297L, 811589153L, 10)
    }
}

/**
 * Class to parse the input, analyze and solve the problem at hand.
 *
 * @property numbers Indexed [List] of numbers in an encrypted file
 */
private class GrovePositioningSystemAnalyzer private constructor(
    private val numbers: List<IndexedValue<Long>>
) {

    companion object {

        fun parse(input: List<String>, decryptionKey: Long = 1L): GrovePositioningSystemAnalyzer =
            GrovePositioningSystemAnalyzer(
                numbers = input.mapIndexed { index, numberString ->
                    IndexedValue(index, numberString.toLong() * decryptionKey)
                }
            )
    }

    // List of Grove Coordinates
    private val groveCoordinatesList = listOf(1000, 2000, 3000)

    /**
     * Returns a decrypted file of the encrypted [numbers] by moving each number forward or backward in the file
     * by a number of circular positions equal to the value of the number being moved.
     *
     * This process is known as "Mixing" and is repeated for the given [number of times][mixTimes].
     *
     * For each "Mixing" iteration, [numbers] are moved in the order they originally appear.
     */
    private fun mixNumbers(mixTimes: Int): List<IndexedValue<Long>> = numbers.toMutableList().apply {
        repeat(mixTimes) {
            // Using indices to pick indexed numbers in the order they originally appear
            indices.forEach { originalIndex ->
                // Lookup the current position of the number using original index
                val currentPosition = indexOfFirst { indexedNumber -> indexedNumber.index == originalIndex }
                // Remove from the current position and save the indexed number
                val indexedNumber = removeAt(currentPosition)
                // Compute its new circular position based on the value of the indexed number being moved
                val newPosition = (currentPosition + indexedNumber.value).mod(size)
                // Place the indexed number in the new circular position
                add(newPosition, indexedNumber)
            }
        }
    }

    /**
     * [Solution for Part 1 and 2]
     *
     * Returns the sum of three numbers that form the Grove coordinates after decrypting the encrypted [numbers] by
     * "Mixing" the [numbers] for the given [number of times][mixTimes].
     */
    fun getSumOfGroveCoordinates(mixTimes: Int = 1) = with(mixNumbers(mixTimes)) {
        // With the decrypted file of the encrypted [numbers]

        // Lookup the current position of number 0
        val zeroIndex = indexOfFirst { indexedNumber -> indexedNumber.value == 0L }

        // Return the sum of three numbers that form the Grove coordinates
        groveCoordinatesList.sumOf { offset ->
            // Get Grove coordinate
            val position = (zeroIndex + offset) % size
            // Return the value found at this Grove coordinate
            this[position].value
        }
    }

}