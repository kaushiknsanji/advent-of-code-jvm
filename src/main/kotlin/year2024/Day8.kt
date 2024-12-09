/**
 * Problem: Day8: Resonant Collinearity
 * https://adventofcode.com/2024/day/8
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import extensions.distinctPairs
import utils.grid.*

private class Day8 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 14
    println("=====")
    solveActual(1)      // 299
    println("=====")
    solveSample(2)      // 34
    println("=====")
    solveActual(2)      // 1032
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day8.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day8.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    AntennaAntinodeAnalyzer.parse(input)
        .getDistinctCountOfAntinodes(isDistanceBased = true)
        .also(::println)
}

private fun doPart2(input: List<String>) {
    AntennaAntinodeAnalyzer.parse(input)
        .getDistinctCountOfAntinodes(isDistanceBased = false)
        .also(::println)
}

private class AntennaCell(x: Int, y: Int) : Point2d<Int>(x, y)

private class AntennaGrid(
    gridPattern: List<String>
) : Grid2dGraph<AntennaCell, Char>(gridPattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value location's row
     * @param column [Int] value location's column
     */
    override fun provideLocation(row: Int, column: Int): AntennaCell =
        AntennaCell(row, column)


    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar Char found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): Char = locationChar

}

private class AntennaAntinodeAnalyzer private constructor(
    private val antennaGrid: AntennaGrid
) : IGrid2dGraph<AntennaCell, Char> by antennaGrid {

    companion object {
        private const val NO_ANTENNA = '.'

        fun parse(input: List<String>): AntennaAntinodeAnalyzer = AntennaAntinodeAnalyzer(AntennaGrid(input))
    }

    // Locations of all different Antennas in the Grid
    private val allAntennaLocations: Collection<AntennaCell> = getAllLocations().filterNot { cell: AntennaCell ->
        antennaGrid[cell] == NO_ANTENNA
    }

    // Map of Antennas for each distinct frequency as key
    private val frequencyToLocationsMap: Map<Char, List<AntennaCell>> by lazy {
        allAntennaLocations.groupBy { cell: AntennaCell -> antennaGrid[cell] }
    }

    // Distinct Pairs of Antennas having the same frequency
    private val frequencyBasedDistinctAntennaPairs: List<Pair<AntennaCell, AntennaCell>> by lazy {
        frequencyToLocationsMap.values.flatMap { sameFrequencyAntennas: List<AntennaCell> ->
            sameFrequencyAntennas.distinctPairs()
        }
    }

    /**
     * Finds Antinodes forming at a distance of [antennaDistance] with respect to two Antennas [antenna1] and
     * [antenna2] of the same frequency.
     *
     * (Part-1): When [antennaDistance] is more than 0, it finds a collinear Antinode forming at a
     * distance of [antennaDistance] to [antenna1] and twice of [antennaDistance] to [antenna2], and one more at a
     * distance of [antennaDistance] to [antenna2] and twice of [antennaDistance] to [antenna1].
     *
     * (Part-2): When [antennaDistance] is 0, it finds all collinear Antinodes forming at locations
     * irrespective of distance with respect to Antennas [antenna1] and [antenna2].
     */
    private fun findAntinodes(antenna1: AntennaCell, antenna2: AntennaCell, antennaDistance: Int): List<AntennaCell> =
        if (antennaDistance == 0) {
            // Part-2: Find all locations that are collinear with [antenna1] and [antenna2] irrespective of distance
            getAllLocations().filter { cell ->
                cell.isCollinearWith(antenna1, antenna2)
            }
        } else {
            // Part-1: Determine Antinodes based on distance

            // Get locations at distance of [antennaDistance] to [antenna1] and pick only those that are at
            // twice of [antennaDistance] to [antenna2]
            antenna1.manhattanDistantLocations(antennaDistance) { row: Int, column: Int ->
                getLocationOrNull(row, column)
            }.filterNotNull().filter { possibleAntinode ->
                possibleAntinode.manhattanDistance(antenna2) == antennaDistance * 2
            }.plus(
                // Get locations at distance of [antennaDistance] to [antenna2] and pick only those that are at
                // twice of [antennaDistance] to [antenna1]
                antenna2.manhattanDistantLocations(antennaDistance) { row: Int, column: Int ->
                    getLocationOrNull(row, column)
                }.filterNotNull().filter { possibleAntinode ->
                    possibleAntinode.manhattanDistance(antenna1) == antennaDistance * 2
                }
            ).filter { antinode: AntennaCell ->
                // Pick Antinodes that are collinear with both [antenna1] and [antenna2]
                antinode.isCollinearWith(antenna1, antenna2)
            }.toList()
        }

    /**
     * [Solution for Part 1 and 2]
     *
     * Returns number of unique locations within the Grid having an Antinode.
     *
     * @param isDistanceBased [Boolean] to toggle between distance-based and distance-independent way of determining
     * Antinode locations. `true` for Part-1 which determines Antinodes based on the distance between
     * Antennas of the same frequency; `false` for Part-2 which determines Antinodes irrespective of the distance
     * between Antennas of the same frequency.
     */
    fun getDistinctCountOfAntinodes(isDistanceBased: Boolean): Int =
        if (isDistanceBased) {
            // Part-1: Use Manhattan distance between Antennas of the same frequency to determine Antinode locations
            frequencyBasedDistinctAntennaPairs.flatMap { (antenna1: AntennaCell, antenna2: AntennaCell) ->
                findAntinodes(antenna1, antenna2, antenna1.manhattanDistance(antenna2))
            }
        } else {
            // Part-2: Determine Antinode locations irrespective of the distance between Antennas
            // of the same frequency by passing 0 for the distance
            frequencyBasedDistinctAntennaPairs.flatMap { (antenna1: AntennaCell, antenna2: AntennaCell) ->
                findAntinodes(antenna1, antenna2, 0)
            }
        }.distinct().size

}