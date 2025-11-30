/**
 * Problem: Day5: Hydrothermal Venture
 * https://adventofcode.com/2021/day/5
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2021

import base.BaseFileHandler
import utils.grid.Point2D
import kotlin.math.absoluteValue

private class Day5 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)  // 5
    println("=====")
    solveActual(1)  // 6225
    println("=====")
    solveSample(2)  // 12
    println("=====")
    solveActual(2)  // 22116
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day5.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day5.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    HydrothermalVents.parse(input)
        .processHorizontalAndVerticalSegments()
        .getCountOfPointsWithTwoOrMoreOverlaps()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    HydrothermalVents.parse(input)
        .processHorizontalAndVerticalSegments()
        .processDiagonalSegments()
        .getCountOfPointsWithTwoOrMoreOverlaps()
        .also { println(it) }
}

private class Point(val x: Int, val y: Int) : Point2D<Int>(x, y) {
    companion object {
        fun parse(pointStr: String): Point = pointStr.split(",").let { coordinates: List<String> ->
            Point(coordinates.first().toInt(), coordinates.last().toInt())
        }
    }
}

private class CoordinateGrid private constructor(val xyMin: Int, xyMax: Int) {
    companion object {
        fun parse(lineSegments: List<Pair<Point, Point>>): CoordinateGrid = lineSegments.flatMap { lineSegment ->
            lineSegment.first.toCoordinateList() + lineSegment.second.toCoordinateList()
        }.let { coordinates ->
            CoordinateGrid(coordinates.minOrNull()!!, coordinates.maxOrNull()!!)
        }
    }

    private val coordinateGridMap: Map<Int, List<Point>> = (xyMin..xyMax).flatMap { x: Int ->
        (xyMin..xyMax).map { y: Int ->
            Point(x, y)
        }
    }.groupBy { point: Point ->
        point.x
    }

    private val coordinateGridValueMap: MutableMap<Point, Int> = getAllPointsInGrid().associateWith { 0 }.toMutableMap()

    fun getAllPointsInGrid(): Collection<Point> = coordinateGridMap.values.flatten()

    fun getAllValuesInGrid(): Collection<Int> = coordinateGridValueMap.values

    operator fun set(point: Point, value: Int) {
        coordinateGridValueMap[point] = value
    }

    operator fun get(point: Point): Int = coordinateGridValueMap[point]!!

    fun getPointOrNull(x: Int, y: Int): Point? = try {
        coordinateGridMap[x]?.get(y - xyMin)
    } catch (e: IndexOutOfBoundsException) {
        null
    }

    fun getPoint(x: Int, y: Int): Point =
        getPointOrNull(x, y) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${Point::class.simpleName} at the given coordinates ($x, $y)"
        )
}

private class HydrothermalVents private constructor(
    private val lineSegments: List<Pair<Point, Point>>,
    private val coordinateGrid: CoordinateGrid
) {
    companion object {
        fun parse(input: List<String>): HydrothermalVents = input.map { line ->
            with(line.split(" -> ")) {
                Point.parse(this.first()) to Point.parse(this.last())
            }
        }.let { lineSegments: List<Pair<Point, Point>> ->
            HydrothermalVents(
                lineSegments,
                CoordinateGrid.parse(lineSegments)
            )
        }
    }

    private val horizontalAndVerticalSegments
        get() = lineSegments.filter { lineSegment ->
            lineSegment.first.x == lineSegment.second.x || lineSegment.first.y == lineSegment.second.y
        }

    private val diagonalSegments
        get() = lineSegments.filter { lineSegment ->
            (lineSegment.first.x - lineSegment.second.x).absoluteValue ==
                    (lineSegment.first.y - lineSegment.second.y).absoluteValue
        }

    private val xCoordinateRange: (lineSegment: Pair<Point, Point>) -> IntProgression = { lineSegment ->
        if (lineSegment.first.x > lineSegment.second.x) {
            lineSegment.first.x downTo lineSegment.second.x
        } else {
            lineSegment.first.x..lineSegment.second.x
        }
    }

    private val yCoordinateRange: (lineSegment: Pair<Point, Point>) -> IntProgression = { lineSegment ->
        if (lineSegment.first.y > lineSegment.second.y) {
            lineSegment.first.y downTo lineSegment.second.y
        } else {
            lineSegment.first.y..lineSegment.second.y
        }
    }

    private val incrementGridValue: (x: Int, y: Int) -> Unit = { x, y ->
        with(coordinateGrid.getPoint(x, y)) {
            coordinateGrid[this] += 1
        }
    }

    /**
     * [Solution for Part-1 and Part-2]
     * Increments value at the Points falling in Horizontal and Vertical segments of the field grid
     * to determine their overlaps.
     */
    fun processHorizontalAndVerticalSegments(): HydrothermalVents = this.apply {
        horizontalAndVerticalSegments.forEach { lineSegment ->
            xCoordinateRange(lineSegment).forEach { x ->
                yCoordinateRange(lineSegment).forEach { y ->
                    incrementGridValue(x, y)
                }
            }
        }
    }

    /**
     * [Solution for Part-2]
     * Increments value at the Points falling in Diagonal segments of the field grid
     * to determine their overlaps.
     */
    fun processDiagonalSegments(): HydrothermalVents = this.apply {
        diagonalSegments.forEach { lineSegment ->
            val xCoordinates = xCoordinateRange(lineSegment).toList()
            val yCoordinates = yCoordinateRange(lineSegment).toList()
            xCoordinates.indices.forEach { i ->
                incrementGridValue(xCoordinates[i], yCoordinates[i])
            }
        }
    }

    fun getCountOfPointsWithTwoOrMoreOverlaps(): Int = coordinateGrid.getAllValuesInGrid().count { value -> value >= 2 }

}