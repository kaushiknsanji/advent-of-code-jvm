/**
 * Problem: Day10: Hoof It
 * https://adventofcode.com/2024/day/10
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import utils.grid.ILattice
import utils.grid.Lattice
import utils.grid.Point2d

private class Day10 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 36
    println("=====")
    solveActual(1)      // 717
    println("=====")
    solveSample(2)      // 81
    println("=====")
    solveActual(2)      // 1686
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day10.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day10.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    HikingTrailBuilder.parse(input)
        .getTotalScoreOfAllTrailheads()
        .also(::println)
}

private fun doPart2(input: List<String>) {
    HikingTrailBuilder.parse(input)
        .getTotalRatingOfAllTrailheads()
        .also(::println)
}

private class TrailLocation(x: Int, y: Int) : Point2d<Int>(x, y)

private class HikingTrailGrid(
    topographyPattern: List<String>
) : Lattice<TrailLocation, Int>(topographyPattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value location's row
     * @param column [Int] value location's column
     */
    override fun provideLocation(row: Int, column: Int): TrailLocation =
        TrailLocation(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar Char found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): Int = locationChar.digitToInt()

}

private class HikingTrailBuilder private constructor(
    private val hikingTrailGrid: HikingTrailGrid
) : ILattice<TrailLocation, Int> by hikingTrailGrid {

    companion object {

        fun parse(input: List<String>): HikingTrailBuilder = HikingTrailBuilder(HikingTrailGrid(input))
    }

    /**
     * Returns height of the trail at [this] location.
     */
    private fun TrailLocation.toHeight() = hikingTrailGrid[this]

    // All Trailheads on the topographic map which are locations with the height of 0
    private val trailHeads: Collection<TrailLocation> by lazy {
        getAllLocations().filter { location: TrailLocation ->
            location.toHeight() == 0
        }
    }

    /**
     * Returns neighbouring locations of [this] with a gradual increase in height by 1.
     */
    private fun TrailLocation.getNextNeighbours(): Collection<TrailLocation> =
        getAllNeighbours().filter { location: TrailLocation ->
            location.toHeight() - this.toHeight() == 1
        }

    /**
     * Returns a [Pair] for the number of different locations with the highest height of 9
     * that can be reached from [this] along with the number of unique paths that can be taken to reach such peaks.
     */
    private fun TrailLocation.getPairCountsOfUniquePeaksReachableToUniquePathsFound(): Pair<Int, Int> {
        // Using two Lists for Frontier instead of a Queue as it is faster since Queue would be just
        // holding Trail Locations that are at a distance of 'd' and 'd+1' only.
        // Frontier list of Trail Locations that are at a distance of 'd'. Begin with [this] Trail Location.
        var currentFrontier: MutableList<TrailLocation> = mutableListOf(this)
        // Frontier list of Next Trail Locations that are at a distance of 'd + 1'
        val nextFrontier: MutableList<TrailLocation> = mutableListOf()

        // Set of Trail peaks reached during traversal
        val trailPeaksReached: MutableSet<TrailLocation> = mutableSetOf()

        // Counter for the number of unique paths discovered to all different peaks
        var countOfPathsToPeaks = 0

        // Repeat till the Frontier holding locations at distance of 'd' becomes empty
        while (currentFrontier.isNotEmpty()) {

            currentFrontier.forEach { current: TrailLocation ->
                current.getNextNeighbours()
                    .forEach { nextTrailLocation: TrailLocation ->
                        if (nextTrailLocation.toHeight() == 9) {
                            // When the Next Trail location is the Peak, add it to the set of Peaks
                            trailPeaksReached.add(nextTrailLocation)
                            // Increment the path counter
                            countOfPathsToPeaks++
                        } else {
                            // When the Next Trail location is not a Peak, add it to the Next Frontier
                            nextFrontier.add(nextTrailLocation)
                        }
                    }
            }

            // Copy over to Current Frontier and clear Next Frontier
            currentFrontier = nextFrontier.toMutableList()
            nextFrontier.clear()
        }

        // Return the number of distinct peaks reached to the number of unique paths
        // that can be taken towards such peaks
        return trailPeaksReached.size to countOfPathsToPeaks
    }

    /**
     * [Solution for Part-1]
     *
     * Returns total of all Trailheads' scores on the topographic map [hikingTrailGrid].
     */
    fun getTotalScoreOfAllTrailheads(): Int =
        trailHeads.sumOf { trailHead: TrailLocation ->
            trailHead.getPairCountsOfUniquePeaksReachableToUniquePathsFound().first
        }

    /**
     * [Solution for Part-2]
     *
     * Returns total of all Trailheads' ratings on the topographic map [hikingTrailGrid].
     */
    fun getTotalRatingOfAllTrailheads(): Int =
        trailHeads.sumOf { trailHead: TrailLocation ->
            trailHead.getPairCountsOfUniquePeaksReachableToUniquePathsFound().second
        }

}