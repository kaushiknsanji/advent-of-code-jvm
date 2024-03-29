/**
 * Kotlin file for functions on coordinate grid based mathematical computations.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package utils.grid

import kotlin.math.abs
import kotlin.math.absoluteValue

/**
 * Extension function on an [Iterable] of [Point2d] to compute the Area enclosed by the path of [Point2d]s in a
 * 2D Grid system using the [Shoelace formula](https://mathworld.wolfram.com/ShoelaceFormula.html).
 *
 * This function assumes that all [Point2d]s are in proper path order for obtaining the required result.
 */
fun Iterable<Point2d<Int>>.toShoelacePolygonArea(): Long =
    this.zipWithNext { point1, point2 ->
        (point1.xPos * point2.yPos - point2.xPos * point1.yPos).toLong()
    }.sum().absoluteValue shr 1

/**
 * Extension function on an [Iterable] of [Point2d] to compute the number of Points enclosed by the Polygon formed
 * by the path of [Point2d]s in a 2D Grid system using the [Pick's theorem](https://en.wikipedia.org/wiki/Pick%27s_theorem).
 *
 * This function assumes that all [Point2d]s are in proper path order for obtaining the required result.
 */
fun Iterable<Point2d<Int>>.toTotalPointsEnclosedByPolygon(): Long =
    this.toShoelacePolygonArea() + 1 - (this.count() shr 1)

/**
 * Extension function on a [Sequence] of [Point2d] to compute the Area enclosed by the path of [Point2d]s in a
 * 2D Grid system using the [Shoelace formula](https://mathworld.wolfram.com/ShoelaceFormula.html).
 *
 * This function assumes that all [Point2d]s are in proper path order for obtaining the required result.
 */
fun Sequence<Point2d<Int>>.toShoelacePolygonArea(): Long =
    this.zipWithNext { point1, point2 ->
        (point1.xPos * point2.yPos - point2.xPos * point1.yPos).toLong()
    }.sum().absoluteValue shr 1

/**
 * Extension function on a [Sequence] of [Point2d] to compute the number of Points enclosed by the Polygon formed
 * by the path of [Point2d]s in a 2D Grid system using the [Pick's theorem](https://en.wikipedia.org/wiki/Pick%27s_theorem).
 *
 * This function assumes that all [Point2d]s are in proper path order for obtaining the required result.
 */
fun Sequence<Point2d<Int>>.toTotalPointsEnclosedByPolygon(): Long =
    this.toShoelacePolygonArea() + 1 - (this.count() shr 1)

/**
 * Extension function on [Point2d] of type [Int] to compute Manhattan Distance to the [destination]
 * from source [this].
 *
 * To be used in 2D Grid system where only Cardinal movements are permitted.
 */
fun Point2d<Int>.manhattanDistance(destination: Point2d<Int>): Int =
    abs(destination.xPos - xPos) + abs(destination.yPos - yPos)