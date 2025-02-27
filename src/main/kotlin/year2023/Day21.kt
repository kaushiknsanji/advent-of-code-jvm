/**
 * Problem: Day21: Step Counter
 * https://adventofcode.com/2023/day/21
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import utils.extractQuadraticCoefficients
import utils.findQuadraticNumber
import utils.grid.CardinalDirection.*
import utils.isQuadratic
import utils.grid.CardinalDirection as Direction

private class Day21 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1, 6)       // 16
    println("=====")
    solveActual(1, 64)      // 3639
    println("=====")
    solveSample(2, 6)       // 16
    println("=====")
    solveSample(2, 10)       // 50
    println("=====")
    solveSample(2, 50)       // 1594
    println("=====")
    solveSample(2, 100)       // 6536
    println("=====")
    solveSample(2, 500)       // 167004
    println("=====")
    solveSample(2, 1000)       // 668697
    println("=====")
    solveSample(2, 5000)       // 16733044
    println("=====")
    solveActual(2, 26501365)    // 604592315958630 (takes 4min to give result)
    println("=====")
}

private fun solveSample(executeProblemPart: Int, maxStepCount: Int) {
    execute(Day21.getSampleFile().readLines(), executeProblemPart, maxStepCount)
}

private fun solveActual(executeProblemPart: Int, maxStepCount: Int) {
    execute(Day21.getActualTestFile().readLines(), executeProblemPart, maxStepCount)
}

private fun execute(input: List<String>, executeProblemPart: Int, maxStepCount: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input, maxStepCount)
        2 -> doPart2(input, maxStepCount)
    }
}

private fun doPart1(input: List<String>, maxStepCount: Int) {
    StepCountAnalyzer(input)
        .getTotalGardenPlotsReached(maxStepCount)
        .also { println(it) }
}

private fun doPart2(input: List<String>, maxStepCount: Int) {
    StepCountAnalyzer(input, isMapInfinite = true)
        .getTotalGardenPlotsReached(maxStepCount)
        .also { println(it) }
}

/**
 * Class for grid location of Garden Tiles.
 *
 * @property x [Int] value of the Row position in Grid.
 * @property y [Int] value of the Column position in Grid.
 */
private open class GardenTileLocation(val x: Int, val y: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GardenTileLocation) return false

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}

/**
 * [GardenTileLocation] subclass for an infinite Grid having out of range [row][x] and [column][y] position
 * that needs to be wrapped so that they are within the bounds defined by `xMax` for Row and `yMax` for Column.
 */
private class WrappedGardenTileLocation(x: Int, y: Int, xMax: Int, yMax: Int) : GardenTileLocation(x, y) {
    // Wrapped Row position
    val xWrapped: Int = x.mod(xMax)

    // Wrapped Column position
    val yWrapped: Int = y.mod(yMax)
}

/**
 * Enum class for the different types of values that can be present in any [GardenTileLocation] of the Garden Grid.
 */
private enum class GardenType(val type: Char) {
    PLOT('.'), ROCK('#'), START('S')
}

private interface IGardenGrid {
    fun getGardenTileLocationWrappedOrNull(row: Int, column: Int): GardenTileLocation?
    fun getGardenTileLocation(row: Int, column: Int): GardenTileLocation
    fun getAllGardenTileLocations(): Collection<GardenTileLocation>
    fun GardenTileLocation.toGardenType(): GardenType
    fun getStartingGardenTile(): GardenTileLocation
    fun GardenTileLocation.getNeighbour(direction: Direction): GardenTileLocation?
    fun GardenTileLocation.getAllNeighbours(): Collection<GardenTileLocation>
    fun GardenTileLocation.getNeighbouringPlots(): Collection<GardenTileLocation>
}

/**
 * Class that prepares the Garden Grid for the provided input `gardenPatternList` and implements [IGardenGrid] to
 * provide functions to facilitate working with the Grid.
 *
 * @property rows [Int] value of the number of Rows present in the Garden Grid.
 * @property columns [Int] value of the number of Columns present in the Garden Grid.
 * @property isMapInfinite [Boolean] value that indicates if this Grid is infinite. `true` if infinite; `false` otherwise.
 */
private class GardenGrid private constructor(
    private val rows: Int,
    val columns: Int,
    gardenPatternList: List<String>,
    val isMapInfinite: Boolean
) : IGardenGrid {

    constructor(gardenPatternList: List<String>, isMapInfinite: Boolean) : this(
        rows = gardenPatternList.size,
        columns = gardenPatternList.first().length,
        gardenPatternList = gardenPatternList,
        isMapInfinite = isMapInfinite
    )

    // Map of row index to GardenTileLocations
    private val gardenGridMap: Map<Int, List<GardenTileLocation>> = (0 until rows).flatMap { x: Int ->
        (0 until columns).map { y: Int ->
            GardenTileLocation(x, y)
        }
    }.groupBy { gardenTileLocation: GardenTileLocation -> gardenTileLocation.x }

    // Map of GardenTileLocation to GardenType
    private val gardenGridValueMap: Map<GardenTileLocation, GardenType> =
        gardenPatternList.flatMapIndexed { x: Int, rowPattern: String ->
            rowPattern.mapIndexed { y: Int, gardenChar: Char ->
                getGardenTileLocation(x, y) to GardenType.entries.single { it.type == gardenChar }
            }
        }.toMap()

    /**
     * [GardenGrid] getter for [GardenType] at the given [gardenTileLocation].
     */
    operator fun get(gardenTileLocation: GardenTileLocation) =
        if (gardenTileLocation is WrappedGardenTileLocation) {
            // When location is out of range in an infinite Grid, get the wrapped location and then get its GardenType
            gardenGridValueMap[getGardenTileLocation(gardenTileLocation.xWrapped, gardenTileLocation.yWrapped)]!!
        } else {
            gardenGridValueMap[gardenTileLocation]!!
        }

    override fun getGardenTileLocationWrappedOrNull(row: Int, column: Int): GardenTileLocation? = try {
        if (!gardenGridMap.containsKey(row)) {
            throw NoSuchElementException()
        }
        gardenGridMap[row]!!.single { it.y == column }
    } catch (e: NoSuchElementException) {
        if (isMapInfinite) {
            // When location is out of range and grid is infinite,
            // return the wrapped subclass instance of `GardenTileLocation`
            WrappedGardenTileLocation(row, column, rows, columns)
        } else {
            null
        }
    }

    override fun getGardenTileLocation(row: Int, column: Int): GardenTileLocation =
        getGardenTileLocationWrappedOrNull(row, column) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${GardenTileLocation::class.simpleName} at given location ($row, $column)"
        )

    override fun getAllGardenTileLocations(): Collection<GardenTileLocation> =
        gardenGridMap.values.flatten()

    override fun GardenTileLocation.toGardenType(): GardenType = get(this)

    override fun getStartingGardenTile(): GardenTileLocation =
        getAllGardenTileLocations().single { gardenTileLocation ->
            gardenTileLocation.toGardenType() == GardenType.START
        }

    override fun GardenTileLocation.getNeighbour(direction: Direction): GardenTileLocation? = when (direction) {
        TOP -> getGardenTileLocationWrappedOrNull(x - 1, y)
        BOTTOM -> getGardenTileLocationWrappedOrNull(x + 1, y)
        RIGHT -> getGardenTileLocationWrappedOrNull(x, y + 1)
        LEFT -> getGardenTileLocationWrappedOrNull(x, y - 1)
    }

    override fun GardenTileLocation.getAllNeighbours(): Collection<GardenTileLocation> =
        Direction.entries.mapNotNull { direction -> getNeighbour(direction) }

    override fun GardenTileLocation.getNeighbouringPlots(): Collection<GardenTileLocation> =
        getAllNeighbours().filterNot { gardenTileLocation ->
            gardenTileLocation.toGardenType() == GardenType.ROCK
        }

}

private class StepCountAnalyzer private constructor(
    private val gardenGrid: GardenGrid
) : IGardenGrid by gardenGrid {

    constructor(input: List<String>, isMapInfinite: Boolean = false) : this(GardenGrid(input, isMapInfinite))

    /**
     * Returns the total number of [Garden plots][GardenType.PLOT] reached for the given
     * [maximum number of steps][maxStepCount].
     *
     * Explores Garden plots that can be reached from the current step count using Breadth-First-Search technique.
     *
     * @param distanceMap [Map] of [Garden plots][GardenTileLocation] reached along with their recent step count.
     * Can be used as a cache to jump start search for the next [maxStepCount] from this map's maximum step count.
     * Defaulted to [emptyMap].
     */
    private fun findTotalGardenPlotsReached(
        maxStepCount: Int,
        distanceMap: MutableMap<GardenTileLocation, Int> = mutableMapOf()
    ): Int {
        // Get the previous maximum step count from the [distanceMap] if provided
        var distance = distanceMap.takeUnless { it.isEmpty() }?.values?.max() ?: 0

        // Using two lists for Frontier instead of a Queue as it is faster for items that are already initialized
        // and since Queue would be just holding GardenTileLocations that are at a distance of 'd' and 'd+1' only.
        // Frontier list of GardenTileLocations that are at a distance of 'd'
        var currentFrontier: MutableList<GardenTileLocation> = if (distance == 0) {
            // When previous maximum step count is 0, we start with the starting GardenTileLocation
            mutableListOf(getStartingGardenTile())
        } else {
            // When previous maximum step count is greater than 0, we take all GardenTileLocations
            // with this maximum step count to jump start search for the next [maxStepCount]
            distanceMap.filter { (_: GardenTileLocation, stepCount: Int) ->
                stepCount == distance
            }.keys.toMutableList()
        }

        // Frontier list of Next GardenTileLocations that are at a distance of 'd + 1'
        val nextFrontier: MutableList<GardenTileLocation> = mutableListOf()

        // Repeat till the Frontier holding GardenTileLocations at distance of 'd' becomes empty
        while (currentFrontier.isNotEmpty()) {
            // Increment current step count
            distance++

            currentFrontier.forEach { currentLocation: GardenTileLocation ->
                // Get neighbouring Garden plots from the current location
                currentLocation.getNeighbouringPlots().forEach { nextLocation: GardenTileLocation ->
                    // For each neighbouring Garden plot, when the new step count has not yet crossed [maxStepCount]
                    // and is greater than the previous step count taken to reach the same Garden plot, then
                    // save the new step count taken to reach this Garden plot in [distanceMap] and
                    // add this Garden plot for the Next Frontier
                    if (distance <= maxStepCount && distance > distanceMap.getOrDefault(nextLocation, 0)) {
                        distanceMap[nextLocation] = distance
                        nextFrontier.add(nextLocation)
                    }
                }
            }

            // Copy over to Current Frontier and clear Next Frontier
            currentFrontier = nextFrontier.toMutableList()
            nextFrontier.clear()
        }

        // Return the total number of Garden plots reached with [maxStepCount]
        return distanceMap.values.count { it == maxStepCount }
    }

    /**
     * Returns `true` if the given series starter [startStepCount] along with the next three "step counts" in
     * the series generated with increments of [GardenGrid.columns] form a
     * Quadratic series of total [Garden plots][GardenTileLocation] reached; `false` otherwise.
     *
     * @param plotsReachedCacheMap Cache [Map] of [Int] step count to [Int] total number of
     * [Garden plots][GardenTileLocation] reached.
     * @param distanceMap [Map] of [Garden plots][GardenTileLocation] reached along with their recent step count.
     */
    private fun isQuadraticStarter(
        startStepCount: Int,
        plotsReachedCacheMap: MutableMap<Int, Int>,
        distanceMap: MutableMap<GardenTileLocation, Int>
    ): Boolean =
        generateSequence(startStepCount) { previousStepCount: Int ->
            // Generate series of four "step counts" starting with [startStepCount] using increments
            // of [gardenGrid.columns]
            previousStepCount + gardenGrid.columns
        }.take(4).map { generatedStepCount: Int ->
            // For the current step count, get total number of Garden plots reached from the Cache if available,
            // else compute-save and return the result
            plotsReachedCacheMap.getOrPut(generatedStepCount) {
                findTotalGardenPlotsReached(
                    generatedStepCount,
                    distanceMap
                )
            }
        }.toList()
            .isQuadratic() // Test if the generated series of total Garden plots reached
    // for each "step count" is Quadratic

    /**
     * [Solution for Part-1 & Part-2]
     *
     * Returns the total number of [Garden plots][GardenTileLocation] reached for the given [step count][maxStepCount].
     */
    fun getTotalGardenPlotsReached(maxStepCount: Int): Long =
        if (gardenGrid.isMapInfinite) {
            // Part-2: When the grid is infinite

            // Cache Map of step count to total number of Garden plots reached
            val plotsReachedCacheMap: MutableMap<Int, Int> = mutableMapOf()
            // Map of Garden plots reached along with their recent step count
            val distanceMap: MutableMap<GardenTileLocation, Int> = mutableMapOf()

            (1..maxStepCount).asSequence()
                .filter { stepCount ->
                    // As Quadratic series are forming at intervals of [GardenGrid.columns],
                    // we find a `stepCount` such that it becomes [maxStepCount] at some interval, in other words,
                    // their difference is divisible by the interval.
                    (maxStepCount - stepCount).rem(gardenGrid.columns) == 0
                }.firstOrNull { stepCount ->
                    // Pick the `stepCount` that yields a Quadratic series
                    isQuadraticStarter(stepCount, plotsReachedCacheMap, distanceMap)
                }?.let { quadraticStarter: Int ->
                    generateSequence(quadraticStarter) { previousStepCount: Int ->
                        // With the starting step count that yields a Quadratic series,
                        // generate next three "step counts" at intervals of [GardenGrid.columns]
                        previousStepCount + gardenGrid.columns
                    }.take(4).map { generatedStepCount: Int ->
                        // Convert series of step counts to their total number of Garden plots reached from the Cache
                        plotsReachedCacheMap[generatedStepCount]!!
                    }.toList()
                        .extractQuadraticCoefficients() // Extract Quadratic coefficients and constant from the series
                        .map(Int::toLong)
                        .findQuadraticNumber {
                            // Find Quadratic Number of the next 'nth' number, which is 1 plus the result of the
                            // same formula used to determine the start step count. It is 1 plus the result because
                            // `quadraticStarter` itself is the 1st term number of the series.
                            ((maxStepCount - quadraticStarter) / gardenGrid.columns) + 1
                        }
                } ?: findTotalGardenPlotsReached(maxStepCount).toLong()
            // When Quadratic series is not possible, return total Garden plots reached using BFS
        } else {
            // Part-1: When the grid is finite

            // Return total Garden plots reached using BFS
            findTotalGardenPlotsReached(maxStepCount).toLong()
        }

}