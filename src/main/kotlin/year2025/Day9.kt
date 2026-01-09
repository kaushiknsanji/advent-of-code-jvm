/**
 * Problem: Day9: Movie Theater
 * https://adventofcode.com/2025/day/9
 *
 * @author <a href='mailto:kaushiknsanji@gmail.com'>Kaushik N Sanji</a>
 */

package year2025

import base.BaseProblemHandler
import extensions.distinctPairs
import extensions.intersectRange
import extensions.rangeLength
import utils.findAllPositiveInt
import utils.grid.Point2D
import kotlin.math.abs

class Day9 : BaseProblemHandler() {

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
        MovieTheatreFloorAnalyzer.parse(input)
            .getLargestRectangleAreaFormedByTwoCornerRedTiles()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        MovieTheatreFloorAnalyzer.parse(input)
            .getLargestEnclosedRectangleAreaFormedByTwoCornerRedTiles()

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, 50L)
        solveActual(1, false, 0, 4777824480L)
        solveSample(2, false, 0, 24L)
        solveActual(2, false, 0, 1542119040L)
    }

}

fun main() {
    try {
        Day9().start()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

/**
 * [Point2D] subclass for Coordinate location of a Tile on the Floor.
 *
 * @param x [Int] value of x-coordinate
 * @param y [Int] value of y-coordinate
 */
private class TileLocation(x: Int, y: Int) : Point2D<Int>(x, y)

/**
 * Class for a Rectangle formed by two red tiles as opposite corners [cornerTile1] and [cornerTile2].
 */
private class Rectangle(cornerTile1: TileLocation, cornerTile2: TileLocation) {

    constructor(cornerTilePair: Pair<TileLocation, TileLocation>) : this(
        cornerTilePair.first, cornerTilePair.second
    )

    private val xLength: Long = abs(cornerTile1.xPos - cornerTile2.xPos) + 1L
    private val yLength: Long = abs(cornerTile1.yPos - cornerTile2.yPos) + 1L

    // Area of this Rectangle
    val area: Long = xLength * yLength

    private val xMin = minOf(cornerTile1.xPos, cornerTile2.xPos)
    private val xMax = maxOf(cornerTile1.xPos, cornerTile2.xPos)
    private val yMin = minOf(cornerTile1.yPos, cornerTile2.yPos)
    private val yMax = maxOf(cornerTile1.yPos, cornerTile2.yPos)

    // x and y ranges
    val xRange = xMin..xMax
    val yRange = yMin..yMax

    /**
     * Returns [Rectangle] enclosed within the current boundary
     */
    fun getInnerRectangle(): Rectangle = Rectangle(
        TileLocation(xMin + 1, yMin + 1),
        TileLocation(xMax - 1, yMax - 1)
    )

}

private class MovieTheatreFloorAnalyzer private constructor(
    private val redTileLocations: List<TileLocation>
) {

    companion object {

        fun parse(input: List<String>): MovieTheatreFloorAnalyzer = MovieTheatreFloorAnalyzer(
            redTileLocations = input.map { coordinateString ->
                Point2D.parse(coordinateString.findAllPositiveInt(), ::TileLocation)
            }
        )
    }

    // All Rectangles formed from Red Tile corner pairs, sorted by largest area first
    private val rectanglesByLargestArea: List<Rectangle> by lazy {
        redTileLocations.distinctPairs().map(::Rectangle).sortedByDescending(Rectangle::area)
    }

    /**
     * [Solution for Part-1]
     *
     * Returns the Largest Area of any [Rectangle] that can be formed from any two [red tiles][redTileLocations]
     * as opposite corners.
     */
    fun getLargestRectangleAreaFormedByTwoCornerRedTiles(): Long =
        rectanglesByLargestArea.first().area


    /**
     * [Solution for Part-2]
     *
     * Returns the Largest Area of any [Rectangle] that can be formed from any two [red tiles][redTileLocations]
     * as opposite corners, while it encompasses only the Red and/or Green tiles.
     */
    fun getLargestEnclosedRectangleAreaFormedByTwoCornerRedTiles(): Long {
        // Form thin rectangles with all adjacent red tile locations, essentially forming a boundary path
        val boundaryPathRectangles: List<Rectangle> =
            (redTileLocations + redTileLocations.first()).zipWithNext(::Rectangle)

        return rectanglesByLargestArea.first { rectangle ->
            // Get the inner rectangle of the current rectangle
            val innerRectangle = rectangle.getInnerRectangle()

            // Exclude this rectangle if its inner rectangle is found to cross any of the boundary path rectangles
            boundaryPathRectangles.none { boundaryPathRectangle ->
                innerRectangle.xRange.intersectRange(boundaryPathRectangle.xRange).rangeLength() > 0 &&
                        innerRectangle.yRange.intersectRange(boundaryPathRectangle.yRange).rangeLength() > 0
            }
        }.area // Return the area of the first rectangle that is found to be enclosed within the boundary path rectangles
    }

}