/**
 * Problem: Day15: Chiton
 * https://adventofcode.com/2021/day/15
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2021

import base.BaseProblemHandler
import utils.grid.ILattice
import utils.grid.Lattice
import utils.grid.Point2d
import java.util.*

private class Day15 : BaseProblemHandler() {

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
        ChitonDensityAnalyzer.parse(input, 1)
            .getLeastTotalRisk()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        ChitonDensityAnalyzer.parse(input, 5)
            .getLeastTotalRisk()

}

fun main() {
    with(Day15()) {
        solveSample(1, false, 0, 40)
        solveActual(1, false, 0, 458)
        solveSample(2, false, 0, 315)
        solveActual(2, false, 0, 2800)
    }
}

/**
 * Class for Location in the [Cavern Grid][CavernGrid].
 *
 * @param x [Int] value of x-coordinate
 * @param y [Int] value of y-coordinate
 */
private class ChitonLocus(x: Int, y: Int) : Point2d<Int>(x, y)

/**
 * A [Lattice] of the Cavern constructed from the given `pattern`.
 *
 * @property tileRepeatCount [Int] number of times the given `pattern` gets repeated in both directions.
 */
private class CavernGrid(
    pattern: List<String>,
    val tileRepeatCount: Int
) : Lattice<ChitonLocus, Int>(pattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value of location's row
     * @param column [Int] value of location's column
     */
    override fun provideLocation(row: Int, column: Int): ChitonLocus =
        ChitonLocus(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar [Char] found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): Int = locationChar.digitToInt()

    /**
     * Returns location if present at given [row] and [column] in the Expanded grid; otherwise `null`.
     *
     * Called by [getLocationOrNull] when location is not found in the original grid.
     */
    override fun getExpandedLocationOrNull(row: Int, column: Int): ChitonLocus? =
        if (tileRepeatCount > 1 && row in 0 until tileRepeatCount * rows
            && column in 0 until tileRepeatCount * columns
        ) {

            // If location requested is found to be within the expanded grid, return the location instance for it
            provideLocation(row, column)
        } else {
            // If location requested is out of bounds even for the expanded grid, return `null`
            null
        }

    /**
     * Returns value present in the grid at given [location]
     */
    override fun get(location: ChitonLocus): Int =
        if (tileRepeatCount > 1) {
            // For the expanded grid

            // Find the original tile location from the given location
            val originalTileLocation = getLocation(
                location.xPos % rows,
                location.yPos % columns
            )

            if (originalTileLocation == location) {
                // When given location happens to be in the original tile, delegate to super to handle
                super.get(location)
            } else {
                // When given location is outside the original tile, compute the corresponding risk level
                // from its original risk level
                val riskLevel = (location.xPos / rows) + (location.yPos / columns) + originalTileLocation.toValue()

                // When risk level is greater than 9, ensure it stays within the range of 1 to 9
                if (riskLevel >= 10) {
                    riskLevel % 10 + 1
                } else {
                    riskLevel
                }
            }

        } else {
            // For the non-expanded grid, delegate to super to handle
            super.get(location)
        }

}

/**
 * Class to parse the input, analyze and solve the problem at hand.
 *
 * @property cavernGrid A [Lattice] representing the Cavern
 */
private class ChitonDensityAnalyzer private constructor(
    private val cavernGrid: CavernGrid
) : ILattice<ChitonLocus, Int> by cavernGrid {

    companion object {

        fun parse(input: List<String>, tileRepeatCount: Int): ChitonDensityAnalyzer =
            ChitonDensityAnalyzer(CavernGrid(input, tileRepeatCount))
    }

    // Top left location is the start location
    private val startLocation: ChitonLocus = getLocation(0, 0)

    // Bottom right location is the end location
    private val endLocation: ChitonLocus =
        getLocation(
            cavernGrid.tileRepeatCount * cavernGrid.rows - 1,
            cavernGrid.tileRepeatCount * cavernGrid.columns - 1
        )

    /**
     * [Solution for Part 1 and 2]
     *
     * Returns lowest total risk of any path from [startLocation] to [endLocation].
     *
     * Lowest risk path is obtained by following [Dijkstra](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm)
     * algorithm using [PriorityQueue] which prioritizes on the accumulated risk level.
     */
    fun getLeastTotalRisk(): Int {
        // A PriorityQueue based Frontier that prioritizes on the risk level of path traced thus far,
        // for minimizing its risk
        val frontier = PriorityQueue<Pair<ChitonLocus, Int>>(
            compareBy { it.second }
        ).apply {
            // Begin with start location having a risk level of 0
            add(startLocation to 0)
        }

        // Map of Location to Risk level
        val riskMap: MutableMap<ChitonLocus, Int> = mutableMapOf(startLocation to 0)

        // Repeat till the PriorityQueue based Frontier becomes empty
        while (frontier.isNotEmpty()) {
            // Get the Top Location-Risk pair
            val current = frontier.poll()

            // Bail out when end location is reached
            if (current.first == endLocation) break

            // Retrieve Next locations
            current.first.getAllNeighbours()
                .forEach { nextLocus ->
                    // Risk level accumulated with this next location
                    val newRiskLevel = nextLocus.toValue() + current.second

                    // When Risk level accumulated for this next location happens to be the least thus far,
                    // update the Risk level map for the location, and add this next Location-Risk pair
                    // to the PriorityQueue for further processing
                    if (newRiskLevel < riskMap.getOrDefault(nextLocus, Int.MAX_VALUE)) {
                        riskMap[nextLocus] = newRiskLevel
                        frontier.add(nextLocus to newRiskLevel)
                    }
                }
        }

        // Return Least accumulated Risk Level of the end location
        return riskMap[endLocation]!!
    }

}