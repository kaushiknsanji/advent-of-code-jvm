/**
 * Problem: Day7: Laboratories
 * https://adventofcode.com/2025/day/7
 *
 * @author <a href='mailto:kaushiknsanji@gmail.com'>Kaushik N Sanji</a>
 */

package year2025

import base.BaseProblemHandler
import utils.Constants.CARET_CHAR
import utils.Constants.DOT_CHAR
import utils.Constants.S_CAP_CHAR
import utils.grid.CardinalDirection.*
import utils.grid.ILattice
import utils.grid.Lattice
import utils.grid.Point2D

class Day7 : BaseProblemHandler() {

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
        TachyonBeamAnalyzer.parse(input)
            .getBeamSplitCount()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        TachyonBeamAnalyzer.parse(input)
            .getNumberOfActiveTimelines()

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, 21)
        solveActual(1, false, 0, 1642)
        solveSample(2, false, 0, 40L)
        solveActual(2, false, 0, 47274292756692L)
    }

}

fun main() {
    try {
        Day7().start()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

private enum class ManifoldLocationType(val type: Char) {
    START(S_CAP_CHAR),
    SPLITTER(CARET_CHAR),
    EMPTY_SPACE(DOT_CHAR);

    companion object {
        private val typeMap = entries.associateBy(ManifoldLocationType::type)

        fun fromType(type: Char): ManifoldLocationType = typeMap[type]!!
    }
}

private class ManifoldLocation(x: Int, y: Int) : Point2D<Int>(x, y)

private class ManifoldGrid(
    pattern: List<String>
) : Lattice<ManifoldLocation, ManifoldLocationType>(pattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value of location's row
     * @param column [Int] value of location's column
     */
    override fun provideLocation(row: Int, column: Int): ManifoldLocation =
        ManifoldLocation(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar [Char] found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): ManifoldLocationType =
        ManifoldLocationType.fromType(locationChar)

}

private class TachyonBeamAnalyzer private constructor(
    private val manifoldGrid: ManifoldGrid
) : ILattice<ManifoldLocation, ManifoldLocationType> by manifoldGrid {

    companion object {

        fun parse(input: List<String>): TachyonBeamAnalyzer = TachyonBeamAnalyzer(
            manifoldGrid = ManifoldGrid(input)
        )
    }

    // Beam starting location
    private val startLocation = getAllLocations().first { location ->
        location.toValue() == ManifoldLocationType.START
    }

    // Relative directions in which downward beams are spawned after a beam hits a Splitter
    private val splitterBeamDirections = listOf(LEFT, RIGHT)

    /**
     * Returns [List] of [ManifoldLocation]s where beams are spawned after a beam hits a Splitter at [this] location.
     */
    private fun ManifoldLocation.getSplitterNeighbours(): List<ManifoldLocation> =
        splitterBeamDirections.mapNotNull { direction -> getNeighbourOrNull(direction) }

    // Set of ManifoldLocations to store the locations of Splitters that were hit with a beam
    private val beamHitSplitterSet: MutableSet<ManifoldLocation> = mutableSetOf()

    // Set of ManifoldLocations to store the locations where the beam last exited the Manifold
    private val beamExits: MutableSet<ManifoldLocation> = mutableSetOf()

    // Map of ManifoldLocation to the number of paths that can be taken to reach it.
    // Begin with start location and its path count of 1
    private val beamPathCountMap: MutableMap<ManifoldLocation, Long> = mutableMapOf(startLocation to 1L)

    /**
     * Traces Tachyon Beam and builds [beamHitSplitterSet], [beamExits] and [beamPathCountMap].
     */
    private fun traceTachyonBeam() {
        // Using two Lists for Frontier instead of a Queue as it is faster since Queue would be just
        // holding locations that are at a distance of 'd' and 'd + 1' only.
        // Frontier list of locations that are at a distance of 'd'. Begin with start location of the Beam.
        var currentFrontier: MutableList<ManifoldLocation> = mutableListOf(startLocation)

        // Frontier list of locations that are at a distance of 'd + 1'
        val nextFrontier: MutableList<ManifoldLocation> = mutableListOf()

        // List of locations visited by the beam
        val visitedLocations: MutableSet<ManifoldLocation> = mutableSetOf()

        // Lambda to update 'beamPathCountMap' for the 'nextLocation' based on the current path count of 'nextLocation'
        // and path count of 'previousLocation'
        val updatePathCount: (previousLocation: ManifoldLocation, nextLocation: ManifoldLocation) -> Unit =
            { previousLocation, nextLocation ->

                beamPathCountMap[nextLocation] = beamPathCountMap.getOrDefault(
                    nextLocation,
                    0
                ) + beamPathCountMap[previousLocation]!!
            }

        // Loop till the Frontier holds any locations at distance 'd'
        while (currentFrontier.isNotEmpty()) {
            currentFrontier.forEach { currentLocation ->
                when (currentLocation.toValue()) {
                    ManifoldLocationType.START, ManifoldLocationType.EMPTY_SPACE -> {
                        // When current location is the start location or an empty space, the beam moves downward
                        // to the next location

                        // Get the next location
                        val nextLocation = currentLocation.getNeighbourOrNull(BOTTOM)

                        if (nextLocation == null) {
                            // If the next location is null, then the beam has exited the Manifold.
                            // Save the current location to the Set of Beam exits
                            beamExits.add(currentLocation)
                        } else {
                            // If the next location is available, add to the Next Frontier for further processing
                            nextFrontier.add(nextLocation)
                        }
                    }

                    ManifoldLocationType.SPLITTER -> {
                        // When current location is a Splitter, then the beam has hit a Splitter.
                        // Save the current location to the Set of Splitters hit.
                        beamHitSplitterSet.add(currentLocation)

                        // Add the locations of beams spawned by the Splitter to the Next Frontier
                        // for further processing
                        nextFrontier.addAll(currentLocation.getSplitterNeighbours())
                    }
                }

                // Mark the current location as Visited
                visitedLocations.add(currentLocation)
            }

            // Copy over distinct locations at distance 'd + 1' to the Current Frontier and clear Next Frontier
            currentFrontier = nextFrontier.toSet().toMutableList()
            nextFrontier.clear()
        }

        // Get the last row which will be the row where all beams exited the Manifold
        val lastRow = beamExits.first().xPos

        // Iterate through visited locations of all the rows starting from the row after start location's row
        (1..lastRow).forEach { rowIndex ->
            // Get current row's locations visited by the beams, with sorted column order
            val currentRowBeamLocations = visitedLocations.filter { location ->
                location.xPos == rowIndex
            }.sortedBy { location ->
                location.yPos
            }

            // Evaluate splitter locations first, as they pass on the path count to the locations of spawned beams
            currentRowBeamLocations.filter { location ->
                location.toValue() == ManifoldLocationType.SPLITTER
            }.forEach { splitterLocation ->
                // Get the location on top of the Splitter since the beam moves downwards and hits the Splitter
                val previousLocation = splitterLocation.getNeighbourOrNull(TOP)!!

                // Copy over the path count from the previous location to the locations of spawned beams
                splitterLocation.getSplitterNeighbours().forEach { nextLocation ->
                    updatePathCount(previousLocation, nextLocation)
                }
            }

            // Evaluate empty locations next, to accumulate path count from overlapping beams
            currentRowBeamLocations.filter { location ->
                location.toValue() == ManifoldLocationType.EMPTY_SPACE
            }.forEach { currentLocation ->
                // Get the location on top of this empty space since the beam always moves downwards
                val previousLocation = currentLocation.getNeighbourOrNull(TOP)!!

                // If the previous location was one of the locations visited by the beam, then copy over the path count
                // from the previous location and accumulate it with the path count from other overlapping beams if any
                if (previousLocation in visitedLocations) {
                    updatePathCount(previousLocation, currentLocation)
                }
            }
        }

    }

    /**
     * [Solution for Part-1]
     *
     * Returns [Int] number of times the beam gets split.
     */
    fun getBeamSplitCount(): Int {
        // Trace the Tachyon Beam
        traceTachyonBeam()
        // Return the number of splits the beam goes through with the count of Splitters hit by the beam
        return beamHitSplitterSet.count()
    }

    /**
     * [Solution for Part-2]
     *
     * Returns [Long] number of timelines a single Tachyon particle could end up on.
     */
    fun getNumberOfActiveTimelines(): Long {
        // Trace the Tachyon Beam
        traceTachyonBeam()
        // For the List of Beam exits, return the sum of their path counts from
        // the Map of locations to the number of paths that can be taken to reach it
        return beamExits.sumOf { location -> beamPathCountMap[location]!! }
    }

}