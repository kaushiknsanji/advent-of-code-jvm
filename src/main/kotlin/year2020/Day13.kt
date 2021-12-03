/**
 * Problem: Day13: Shuttle Search
 * https://adventofcode.com/2020/day/13
 *
 * @author Kaushik N Sanji
 */

package year2020

import base.BaseFileHandler
import extensions.whileLoop

private class Day13 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)
    println("=====")
    solveActual(1)
    println("=====")
    solveSample(2)
    println("=====")
    solvePart2Sample1()
    println("=====")
    solvePart2Sample2()
    println("=====")
    solvePart2Sample3()
    println("=====")
    solvePart2Sample4()
    println("=====")
    solvePart2Sample5()
    println("=====")
    solveActual(2)
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day13.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day13.getActualTestFile().readLines(), executeProblemPart)
}

private fun solvePart2Sample1() {
    execute(Day13.getSampleFile("_part2_1").readLines(), 2)
}

private fun solvePart2Sample2() {
    execute(Day13.getSampleFile("_part2_2").readLines(), 2)
}

private fun solvePart2Sample3() {
    execute(Day13.getSampleFile("_part2_3").readLines(), 2)
}

private fun solvePart2Sample4() {
    execute(Day13.getSampleFile("_part2_4").readLines(), 2)
}

private fun solvePart2Sample5() {
    execute(Day13.getSampleFile("_part2_5").readLines(), 2)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    Shuttle.parse(input)
        .run {
            getNextTimestampWithBusId().let { (nextTimestamp, nextBusId) ->
                (nextTimestamp - earliestDepartureTimestamp) * nextBusId
            }
        }
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    Shuttle.parse(input)
        .getEarliestOffsetTimestampFromStartByLCMOfBussesWithOffsets()
        .also { println(it) }
}

private class Shuttle private constructor(
    val earliestDepartureTimestamp: Int,
    val operationalBusIds: List<Int>,
    val zeroOffsetOperationalBusId: Int,
    val offsetOperationalBusIds: List<Pair<Int, Int>>
) {

    companion object {
        const val NON_OPERATIONAL_BUS_ID = "x"

        fun parse(input: List<String>): Shuttle = Shuttle(
            input[0].toInt(),
            input[1].split(",").filterNot { busIdStr: String -> busIdStr == NON_OPERATIONAL_BUS_ID }
                .map { busIdStr: String -> busIdStr.toInt() },
            input[1].split(",").first().toInt(),
            input[1].split(",").withIndex()
                .filterNot { indexedBusIdStr: IndexedValue<String> -> indexedBusIdStr.value == NON_OPERATIONAL_BUS_ID }
                .map { indexedBusIdStr: IndexedValue<String> -> indexedBusIdStr.value.toInt() to indexedBusIdStr.index }
                .drop(1)
        )
    }

    // For Part-1
    fun getNextTimestampWithBusId(): Pair<Int, Int> = whileLoop(
        loopStartCounter = earliestDepartureTimestamp,
        { _, lastIterationResult: Pair<Int, Int?>? ->
            lastIterationResult?.second != null
        }
    ) { loopCounter: Int ->
        val nextTimestamp = loopCounter + 1
        nextTimestamp to (loopCounter to operationalBusIds.firstOrNull { busId: Int ->
            (loopCounter % busId) == 0
        })
    }.let { it.first to it.second!! }

    // For Part-2: Brute Force method
    fun getEarliestOffsetTimestampFromStartByBruteForce(): Long = whileLoop(
        loopStartCounter = zeroOffsetOperationalBusId.toLong(),
        { _, lastIterationResult: Pair<Long, Boolean>? ->
            lastIterationResult?.second == true
        }
    ) { loopCounter: Long ->
        val nextTimestamp: Long = loopCounter + zeroOffsetOperationalBusId
        nextTimestamp to (loopCounter to offsetOperationalBusIds.all { (busId, offset) ->
            ((loopCounter + offset) % busId) == 0L
        })
    }.first

    // For Part-2: LCM of Busses with their Offsets
    fun getEarliestOffsetTimestampFromStartByLCMOfBussesWithOffsets(): Long {
        // Initial jump will be the zero offset Bus ID value
        var lcmJump = zeroOffsetOperationalBusId.toLong()
        // Timestamp to determine the earliest repetition (in other words, their LCM)
        var timestamp = 0L

        // Evaluate for all Busses with their Offsets
        offsetOperationalBusIds.forEach { (busId, offset) ->
            while ((timestamp + offset) % busId != 0L) {
                // Increment by the last LCM till we find the Timestamp(i.e., LCM) for the current Bus with its offset
                timestamp += lcmJump
            }

            // Get the LCM for evaluating the next Bus
            lcmJump *= busId
        }

        // Return the earliest timestamp
        return timestamp
    }

}