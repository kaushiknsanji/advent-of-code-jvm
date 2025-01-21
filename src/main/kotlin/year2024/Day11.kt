/**
 * Problem: Day11: Plutonian Pebbles
 * https://adventofcode.com/2024/day/11
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler
import utils.splitContentByWhitespaces

private class Day11 : BaseProblemHandler() {

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
        PebbleProcessor.parse(input)
            .getCountOfPebbles(25)

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        PebbleProcessor.parse(input)
            .getCountOfPebbles(75)

}

fun main() {
    with(Day11()) {
        solveSample(1, false, 0, 55312L)
        solveActual(1, false, 0, 229043L)
        solveSample(2, false, 0, 65601038650482L)
        solveActual(2, false, 0, 272673043446478L)
    }
}

private class PebbleProcessor private constructor(
    private val pebbles: List<Long>
) {

    companion object {

        fun parse(input: List<String>): PebbleProcessor = PebbleProcessor(
            pebbles = input.single().splitContentByWhitespaces().map(String::toLong)
        )
    }

    /**
     * Simulates the transformations that happens to the stones every time we blink.
     *
     * @param blinkTimes [Int] number of times to blink
     * @return [Long] value of the number of stones that appear after blinking for [blinkTimes] times.
     */
    private fun simulateBlinks(blinkTimes: Int): Long {
        // Map of Pebble number to their occurrence count
        val pebbleCountMap: MutableMap<Long, Long> = pebbles.groupingBy { it }.eachCount()
            .mapValues { entry -> entry.value.toLong() }
            .toMutableMap()

        // Repeat for the number of blinks to be simulated
        repeat(blinkTimes) {
            // Current iteration's Map of Pebble number to their occurrence count
            val currentPebbleCountMap = mutableMapOf<Long, Long>()

            // For every Pebble number and their occurrence count read from the previous iteration
            pebbleCountMap.forEach { (pebbleNumber: Long, count: Long) ->
                when {
                    pebbleNumber == 0L -> {
                        // Pebble with number 0 is changed to 1
                        // Update on the current iteration's map while including the count of current Pebble number
                        currentPebbleCountMap[1L] = currentPebbleCountMap.getOrDefault(1L, 0) + count
                    }

                    pebbleNumber.toString().length % 2 == 0 -> {
                        // For Pebble with number having even length, split into two parts and update the same
                        // onto the current iteration's map while including the count of current Pebble number
                        val halfLength = pebbleNumber.toString().length shr 1
                        val leftPebbleNumber =
                            pebbleNumber.toString().substring(0, halfLength).toLong()
                        val rightPebbleNumber =
                            pebbleNumber.toString().substring(halfLength).toLong()
                        currentPebbleCountMap[leftPebbleNumber] =
                            currentPebbleCountMap.getOrDefault(leftPebbleNumber, 0) + count
                        currentPebbleCountMap[rightPebbleNumber] =
                            currentPebbleCountMap.getOrDefault(rightPebbleNumber, 0) + count
                    }

                    else -> {
                        // On all else, multiply the Pebble number with 2024 and update the same
                        // onto the current iteration's map while including the count of current Pebble number
                        val newPebbleNumber = pebbleNumber * 2024L
                        currentPebbleCountMap[newPebbleNumber] =
                            currentPebbleCountMap.getOrDefault(newPebbleNumber, 0) + count
                    }
                }
            }

            // Clear the map having previous iteration data
            pebbleCountMap.clear()
            // Update the map with the current iteration data
            pebbleCountMap.putAll(currentPebbleCountMap)
        }

        // Return the total of all occurrences of each Pebble. This will be the number of stones that appear
        // after blinking for [blinkTimes] times.
        return pebbleCountMap.values.sum()
    }

    /**
     * [Solution for Part 1 and 2]
     *
     * Returns total number of stones that will appear after blinking for [blinkTimes] times.
     */
    fun getCountOfPebbles(blinkTimes: Int): Long = simulateBlinks(blinkTimes)

}