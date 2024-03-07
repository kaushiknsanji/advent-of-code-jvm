/**
 * Problem: Day9: Smoke Basin
 * https://adventofcode.com/2021/day/9
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2021

import base.BaseFileHandler
import utils.grid.TransverseDirection.*
import utils.product
import utils.grid.TransverseDirection as Direction

private class Day9 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)  // 15
    println("=====")
    solveActual(1)  // 554
    println("=====")
    solveSample(2)  // 1134
    println("=====")
    solveActual(2)  // 1017792
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day9.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day9.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    FloorLayout.parse(input)
        .getTotalRiskLevelOfAllLowPoints()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    FloorLayout.parse(input)
        .getLargestThreeBasinsSizeProduct()
        .also { println(it) }
}

private class FloorPoint(val x: Int, val y: Int)

private interface IFloorHeightGrid {
    fun getAllFloorPoints(): Collection<FloorPoint>
    fun getFloorPointOrNull(x: Int, y: Int): FloorPoint?
    fun getFloorPoint(x: Int, y: Int): FloorPoint
    fun Direction.toPerpendicularDirections(): Collection<Direction>
    fun FloorPoint.getNeighbour(direction: Direction): FloorPoint?
    fun FloorPoint.getAllNeighbours(): Collection<FloorPoint>
    fun FloorPoint.getFloorPointsInSingleDirection(direction: Direction): Sequence<FloorPoint>
    fun FloorPoint.getFloorPointsInMultipleDirections(directions: Collection<Direction>): Map<Direction, Sequence<FloorPoint>>
    fun FloorPoint.getFloorPointsInAllDirections(): Map<Direction, Sequence<FloorPoint>>
}

private class FloorHeightGrid private constructor(rows: Int, columns: Int, heightGridList: List<List<Int>>) :
    IFloorHeightGrid {

    constructor(input: List<String>) : this(
        input.size,
        input[0].length,
        input.map { line -> line.map { it.digitToInt() } }
    )

    private val heightGridMap: Map<Int, List<FloorPoint>> = (0 until rows).flatMap { x ->
        (0 until columns).map { y ->
            FloorPoint(x, y)
        }
    }.groupBy { floorPoint: FloorPoint -> floorPoint.x }

    private val heightGridValueMap: Map<FloorPoint, Int> = heightGridList.map { it.withIndex() }.withIndex()
        .flatMap { (x: Int, indexedValues) ->
            indexedValues.map { (y: Int, value: Int) ->
                getFloorPoint(x, y) to value
            }
        }.toMap()

    operator fun get(floorPoint: FloorPoint): Int = heightGridValueMap[floorPoint]!!

    override fun getAllFloorPoints(): Collection<FloorPoint> = heightGridMap.values.flatten()

    override fun getFloorPointOrNull(x: Int, y: Int): FloorPoint? = try {
        heightGridMap[x]?.get(y)
    } catch (e: IndexOutOfBoundsException) {
        null
    }

    override fun getFloorPoint(x: Int, y: Int): FloorPoint =
        getFloorPointOrNull(x, y) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${FloorPoint::class.simpleName} at the given location ($x, $y)"
        )

    override fun Direction.toPerpendicularDirections(): Collection<Direction> = when (this) {
        TOP, BOTTOM -> listOf(LEFT, RIGHT)
        LEFT, RIGHT -> listOf(TOP, BOTTOM)
    }

    override fun FloorPoint.getNeighbour(direction: Direction): FloorPoint? = when (direction) {
        TOP -> getFloorPointOrNull(x - 1, y)
        BOTTOM -> getFloorPointOrNull(x + 1, y)
        RIGHT -> getFloorPointOrNull(x, y + 1)
        LEFT -> getFloorPointOrNull(x, y - 1)
    }

    override fun FloorPoint.getAllNeighbours(): Collection<FloorPoint> =
        Direction.values().mapNotNull { direction: Direction -> getNeighbour(direction) }

    override fun FloorPoint.getFloorPointsInSingleDirection(direction: Direction): Sequence<FloorPoint> =
        generateSequence(this) { lastFloorPoint ->
            lastFloorPoint.getNeighbour(direction)
        }

    override fun FloorPoint.getFloorPointsInMultipleDirections(
        directions: Collection<Direction>
    ): Map<Direction, Sequence<FloorPoint>> =
        directions.associateWith { direction: Direction -> getFloorPointsInSingleDirection(direction) }

    override fun FloorPoint.getFloorPointsInAllDirections(): Map<Direction, Sequence<FloorPoint>> =
        Direction.values().associateWith { direction: Direction -> getFloorPointsInSingleDirection(direction) }
}

private class FloorLayout private constructor(
    private val heightGrid: FloorHeightGrid
) : IFloorHeightGrid by heightGrid {

    companion object {
        fun parse(input: List<String>): FloorLayout = FloorLayout(FloorHeightGrid(input))
    }

    private val lowFloorPoints: List<FloorPoint> = getAllFloorPoints().filter { floorPoint: FloorPoint ->
        floorPoint.getAllNeighbours().map { neighbourFloorPoint -> heightGrid[neighbourFloorPoint] }
            .all { neighbourHeight -> neighbourHeight > heightGrid[floorPoint] }
    }

    /**
     * [Solution for Part-1]
     * Returns a Total of the Risk levels posed by all the low floor points on Height map.
     */
    fun getTotalRiskLevelOfAllLowPoints(): Int =
        lowFloorPoints.sumOf { floorPoint: FloorPoint -> heightGrid[floorPoint] + 1 }

    /**
     * [Solution for Part-2]
     * Returns product value of the sizes of Three largest basins found
     * originating from all the low floor points on Height map.
     */
    fun getLargestThreeBasinsSizeProduct(): Int =
        lowFloorPoints.associateWith { lowFloorPoint: FloorPoint ->
            // First, get all Floor Points in all Directions originating from the identified Low floor point
            lowFloorPoint.getFloorPointsInAllDirections()
        }.mapValues { (_, directionalFloorPointsSequenceMap: Map<Direction, Sequence<FloorPoint>>) ->
            // From all directional floor points for the identified Low floor point, get remaining floor points
            // that forms the basin
            directionalFloorPointsSequenceMap.mapValues { (direction, floorPointsSequence) ->
                floorPointsSequence.takeWhile { floorPoint: FloorPoint ->
                    // Keep floor points till the first occurrence of a floor point with height of 9
                    // which defines the boundary of a basin
                    heightGrid[floorPoint] < 9
                }.flatMap { directionalFloorPoint ->
                    // For each floor point in all Directions originating from the identified Low floor point,
                    // get additional floor points occurring in corresponding perpendicular directions
                    directionalFloorPoint.getFloorPointsInMultipleDirections(
                        direction.toPerpendicularDirections()
                    ).mapValues { (perpendicularDirection, perpendicularFloorPointsSequence) ->
                        perpendicularFloorPointsSequence.takeWhile { floorPoint: FloorPoint ->
                            // Keep floor points till the boundary of a basin
                            heightGrid[floorPoint] < 9
                        }.flatMap { perpendicularFloorPoint ->
                            // For each perpendicular floor point, get additional floor points occurring
                            // in other corresponding perpendicular directions. This completes the extent of a basin.
                            perpendicularFloorPoint.getFloorPointsInMultipleDirections(
                                perpendicularDirection.toPerpendicularDirections()
                            ).mapValues { (_, otherPerpendicularFloorPointsSequence) ->
                                otherPerpendicularFloorPointsSequence.takeWhile { floorPoint: FloorPoint ->
                                    // Keep floor points till the boundary of a basin
                                    heightGrid[floorPoint] < 9
                                }
                            }.values.asSequence().flatten()
                        }
                    }.values.asSequence().flatten()
                }
            }.values.asSequence().flatten()
                // There will be duplicate floor points as a result of finding them
                // in all directions for each floor point
                .distinct()
                .count() // Final size of the basin for the current Low floor point
        }.values.sortedDescending() // Sorting by largest to find top three basins by size
            .take(3) // Three largest basins
            .product()

}