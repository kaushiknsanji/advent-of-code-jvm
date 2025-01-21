/**
 * Problem: Day20: Race Condition
 * https://adventofcode.com/2024/day/20
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler
import utils.Constants.DOT_CHAR
import utils.Constants.E_CAP_CHAR
import utils.Constants.HASH_CHAR
import utils.Constants.S_CAP_CHAR
import utils.grid.ILattice
import utils.grid.Lattice
import utils.grid.Point2d
import utils.grid.manhattanDistance

private class Day20 : BaseProblemHandler() {

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
    @Suppress("UNCHECKED_CAST")
    override fun doPart1(input: List<String>, otherArgs: Array<out Any?>): Any =
        RaceTrackAnalyzer.parse(input)
            .getCountOfCheatsForSavingTime(
                otherArgs[0] as Int,
                2,
                otherArgs[1] as (Int, Int) -> Boolean
            )

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    @Suppress("UNCHECKED_CAST")
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        RaceTrackAnalyzer.parse(input)
            .getCountOfCheatsForSavingTime(
                otherArgs[0] as Int,
                20,
                otherArgs[1] as (Int, Int) -> Boolean
            )

}

fun main() {
    with(Day20()) {
        val timeComparisonAtLeast: (timeSaved: Int, timeToSave: Int) -> Boolean = { timeSaved, timeToSave ->
            timeSaved >= timeToSave
        }

        solveSample(1, false, 0, 14, 2, Int::equals)
        solveSample(1, false, 0, 14, 4, Int::equals)
        solveSample(1, false, 0, 2, 6, Int::equals)
        solveSample(1, false, 0, 4, 8, Int::equals)
        solveSample(1, false, 0, 2, 10, Int::equals)
        solveSample(1, false, 0, 3, 12, Int::equals)
        solveSample(1, false, 0, 1, 20, Int::equals)
        solveSample(1, false, 0, 1, 36, Int::equals)
        solveSample(1, false, 0, 1, 38, Int::equals)
        solveSample(1, false, 0, 1, 40, Int::equals)
        solveSample(1, false, 0, 1, 64, Int::equals)
        solveActual(1, false, 0, 1459, 100, timeComparisonAtLeast)
        solveSample(2, false, 0, 32, 50, Int::equals)
        solveSample(2, false, 0, 31, 52, Int::equals)
        solveSample(2, false, 0, 29, 54, Int::equals)
        solveSample(2, false, 0, 39, 56, Int::equals)
        solveSample(2, false, 0, 25, 58, Int::equals)
        solveSample(2, false, 0, 23, 60, Int::equals)
        solveSample(2, false, 0, 20, 62, Int::equals)
        solveSample(2, false, 0, 19, 64, Int::equals)
        solveSample(2, false, 0, 12, 66, Int::equals)
        solveSample(2, false, 0, 14, 68, Int::equals)
        solveSample(2, false, 0, 12, 70, Int::equals)
        solveSample(2, false, 0, 22, 72, Int::equals)
        solveSample(2, false, 0, 4, 74, Int::equals)
        solveSample(2, false, 0, 3, 76, Int::equals)
        solveActual(2, false, 0, 1016066, 100, timeComparisonAtLeast)
    }
}

private enum class RaceTrackType(val type: Char) {
    START(S_CAP_CHAR),
    END(E_CAP_CHAR),
    WALL(HASH_CHAR),
    TRACK(DOT_CHAR);

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
     * @param row [Int] value of location's row
     * @param column [Int] value of location's column
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