/**
 * Problem: Day20: Race Condition
 * https://adventofcode.com/2024/day/20
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import org.junit.jupiter.api.Assertions.assertEquals
import utils.grid.ILattice
import utils.grid.Lattice
import utils.grid.Point2d
import utils.grid.manhattanDistance

private class Day20 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    val timeComparisonAtLeast: (timeSaved: Int, timeToSave: Int) -> Boolean = { timeSaved, timeToSave ->
        timeSaved >= timeToSave
    }

    listOf(
        ::solveSample to arrayOf<Any?>(1, 2, Int::equals, 14),
        ::solveSample to arrayOf<Any?>(1, 4, Int::equals, 14),
        ::solveSample to arrayOf<Any?>(1, 6, Int::equals, 2),
        ::solveSample to arrayOf<Any?>(1, 8, Int::equals, 4),
        ::solveSample to arrayOf<Any?>(1, 10, Int::equals, 2),
        ::solveSample to arrayOf<Any?>(1, 12, Int::equals, 3),
        ::solveSample to arrayOf<Any?>(1, 20, Int::equals, 1),
        ::solveSample to arrayOf<Any?>(1, 36, Int::equals, 1),
        ::solveSample to arrayOf<Any?>(1, 38, Int::equals, 1),
        ::solveSample to arrayOf<Any?>(1, 40, Int::equals, 1),
        ::solveSample to arrayOf<Any?>(1, 64, Int::equals, 1),
        ::solveActual to arrayOf<Any?>(1, 100, timeComparisonAtLeast, 1459),
        ::solveSample to arrayOf<Any?>(2, 50, Int::equals, 32),
        ::solveSample to arrayOf<Any?>(2, 52, Int::equals, 31),
        ::solveSample to arrayOf<Any?>(2, 54, Int::equals, 29),
        ::solveSample to arrayOf<Any?>(2, 56, Int::equals, 39),
        ::solveSample to arrayOf<Any?>(2, 58, Int::equals, 25),
        ::solveSample to arrayOf<Any?>(2, 60, Int::equals, 23),
        ::solveSample to arrayOf<Any?>(2, 62, Int::equals, 20),
        ::solveSample to arrayOf<Any?>(2, 64, Int::equals, 19),
        ::solveSample to arrayOf<Any?>(2, 66, Int::equals, 12),
        ::solveSample to arrayOf<Any?>(2, 68, Int::equals, 14),
        ::solveSample to arrayOf<Any?>(2, 70, Int::equals, 12),
        ::solveSample to arrayOf<Any?>(2, 72, Int::equals, 22),
        ::solveSample to arrayOf<Any?>(2, 74, Int::equals, 4),
        ::solveSample to arrayOf<Any?>(2, 76, Int::equals, 3),
        ::solveActual to arrayOf<Any?>(2, 100, timeComparisonAtLeast, 1016066)
    ).forEach { (solver, args: Array<Any?>) ->
        @Suppress("UNCHECKED_CAST") val result = solver(
            args[0] as Int,
            args[1] as Int,
            args[2] as (Int, Int) -> Boolean,
        ).also(::println)

        // Last argument should be the expected value. If unknown, it will be `null`. When known, following statement
        // asserts the `result` with the expected value.
        if (args.last() != null) {
            assertEquals(args.last(), result)
        }
        println("=====")
    }
}

private fun solveSample(
    executeProblemPart: Int,
    timeToSave: Int,
    comparison: (timeSaved: Int, timeToSave: Int) -> Boolean
): Any =
    execute(Day20.getSampleFile().readLines(), executeProblemPart, timeToSave, comparison)

private fun solveActual(
    executeProblemPart: Int,
    timeToSave: Int,
    comparison: (timeSaved: Int, timeToSave: Int) -> Boolean
): Any =
    execute(Day20.getActualTestFile().readLines(), executeProblemPart, timeToSave, comparison)

private fun execute(
    input: List<String>,
    executeProblemPart: Int,
    timeToSave: Int,
    comparison: (timeSaved: Int, timeToSave: Int) -> Boolean
): Any =
    when (executeProblemPart) {
        1 -> doPart1(input, timeToSave, comparison)
        2 -> doPart2(input, timeToSave, comparison)
        else -> throw Error("Unexpected Problem Part: $executeProblemPart")
    }

private fun doPart1(
    input: List<String>,
    timeToSave: Int,
    comparison: (timeSaved: Int, timeToSave: Int) -> Boolean
): Any =
    RaceTrackAnalyzer.parse(input)
        .getCountOfCheatsForSavingTime(timeToSave, 2, comparison)

private fun doPart2(
    input: List<String>,
    timeToSave: Int,
    comparison: (timeSaved: Int, timeToSave: Int) -> Boolean
): Any =
    RaceTrackAnalyzer.parse(input)
        .getCountOfCheatsForSavingTime(timeToSave, 20, comparison)


private enum class RaceTrackType(val type: Char) {
    START('S'),
    END('E'),
    WALL('#'),
    TRACK('.');

    companion object {
        private val typeMap = entries.associateBy(RaceTrackType::type)

        fun fromType(type: Char): RaceTrackType = typeMap[type]!!
    }
}

private class RaceTrackLocation(x: Int, y: Int) : Point2d<Int>(x, y)

private class RaceTrackGrid(
    pattern: List<String>
) : Lattice<RaceTrackLocation, RaceTrackType>(pattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value location's row
     * @param column [Int] value location's column
     */
    override fun provideLocation(row: Int, column: Int): RaceTrackLocation =
        RaceTrackLocation(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar [Char] found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): RaceTrackType =
        RaceTrackType.fromType(locationChar)

}

private class RaceTrackAnalyzer private constructor(
    private val raceTrackGrid: RaceTrackGrid
) : ILattice<RaceTrackLocation, RaceTrackType> by raceTrackGrid {

    companion object {

        fun parse(input: List<String>): RaceTrackAnalyzer = RaceTrackAnalyzer(RaceTrackGrid(input))
    }

    // Start location
    private val startLocation = getAllLocations().single { raceTrackPosition ->
        raceTrackPosition.toValue() == RaceTrackType.START
    }

    // End location
    private val endLocation = getAllLocations().single { raceTrackPosition ->
        raceTrackPosition.toValue() == RaceTrackType.END
    }

    /**
     * Returns a [List] of [RaceTrackLocation] denoting the path of the Race Track
     */
    private fun getRaceTrackPath(): List<RaceTrackLocation> {
        // Using two Lists for Frontier instead of a Queue as it is faster since Queue would be just
        // holding locations that are at a distance of 'd' and 'd+1' only.
        // Frontier list of locations that are at a distance of 'd'. Begin with start location.
        var currentFrontier: MutableList<RaceTrackLocation> = mutableListOf(startLocation)

        // Frontier list of Next locations that are at a distance of 'd + 1'
        val nextFrontier: MutableList<RaceTrackLocation> = mutableListOf()

        // Map that saves which location we came from for a current location.
        // This facilitates to build traversed paths without the need for storing them in a List of Lists
        // for each path discovered.
        // Begin with start location as both key and value.
        val cameFromMap: MutableMap<RaceTrackLocation, RaceTrackLocation> =
            mutableMapOf(startLocation to startLocation)

        // Generates a sequence of locations traversed by backtracking from the given location. Sequence generated
        // will be in the reverse direction, till and including the start location.
        val pathSequence: (currentLocation: RaceTrackLocation) -> Sequence<RaceTrackLocation> = {
            sequence {
                var current = it
                while (current != startLocation) {
                    yield(current)
                    current = cameFromMap[current]!!
                }
                yield(current)
            }
        }

        // Repeat till the Frontier holding locations at distance of 'd' becomes empty
        while (currentFrontier.isNotEmpty()) {
            currentFrontier.forEach { current ->
                // For each current location, get their non-wall neighbours
                current.getAllNeighbours().filterNot { next ->
                    next.toValue() == RaceTrackType.WALL
                }.filterNot { next ->
                    // Exclude the location we came from
                    next == cameFromMap[current]!!
                }.forEach { next ->
                    // Save the current location as the value of the next location in the Map
                    cameFromMap[next] = current
                    // Add next location to the Next Frontier for further exploration
                    nextFrontier.add(next)
                }
            }

            // Copy over to Current Frontier and clear Next Frontier
            currentFrontier = nextFrontier.toMutableList()
            nextFrontier.clear()
        }

        // Build path and return the list of locations that denote the Race Track path
        return pathSequence(endLocation).toList().reversed()
    }

    /**
     * [Solution for Part 1 and 2]
     *
     * Returns the number of cheats that saves time of [timeToSave] and passes the [time comparison][comparison]
     * for the [picoseconds of cheat allowed][picosecondCheatAllowed].
     */
    fun getCountOfCheatsForSavingTime(
        timeToSave: Int,
        picosecondCheatAllowed: Int,
        comparison: (timeSaved: Int, timeToSave: Int) -> Boolean
    ): Int =
        with(getRaceTrackPath()) {
            // Iterate over all indices of the Race Track path locations
            indices.flatMap { currentIndex ->
                // Iterate over the remaining indices (after `currentIndex`) of the Race Track path locations
                (currentIndex + 1..lastIndex).mapNotNull { nextIndex ->
                    // Each location is a picosecond away from its previous one. The difference between the
                    // `currentIndex` and `nextIndex` gives the time taken without cheating between its two locations
                    // we are trying to cheat by passing through walls. While, the manhattan distance between
                    // the two locations gives the time taken with cheating by passing through walls.
                    // If this cheat time happens to be within the picoseconds of cheat allowed, then we take
                    // the difference between the time taken with and without cheating to compute and return the
                    // amount of time that can be saved by passing through walls between the locations pointed
                    // to by the indices `currentIndex` and `nextIndex`.

                    this[currentIndex].manhattanDistance(this[nextIndex]).takeIf { cheatTime: Int ->
                        cheatTime <= picosecondCheatAllowed
                    }?.let { cheatTime ->
                        nextIndex - currentIndex - cheatTime
                    }
                }
            }.count { timeSaved ->
                // Return the number of cheats that passes the time comparison
                comparison(timeSaved, timeToSave)
            }
        }

}