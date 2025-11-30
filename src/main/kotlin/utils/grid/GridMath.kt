/**
 * Kotlin file for functions on coordinate grid based mathematical computations.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package utils.grid

import kotlin.math.abs
import kotlin.math.absoluteValue

/**
 * Extension function on an [Iterable] of [Point2D] to compute the Area enclosed by the path of [Point2D]s in a
 * 2D Grid system using the [Shoelace formula](https://mathworld.wolfram.com/ShoelaceFormula.html).
 *
 * This function assumes that all [Point2D]s are in proper path order for obtaining the required result.
 */
fun Iterable<Point2D<Int>>.toShoelacePolygonArea(): Long =
    this.zipWithNext { point1, point2 ->
        (point1.xPos * point2.yPos - point2.xPos * point1.yPos).toLong()
    }.sum().absoluteValue shr 1

/**
 * Extension function on an [Iterable] of [Point2D] to compute the number of Points enclosed by the Polygon formed
 * by the path of [Point2D]s in a 2D Grid system using the [Pick's theorem](https://en.wikipedia.org/wiki/Pick%27s_theorem).
 *
 * This function assumes that all [Point2D]s are in proper path order for obtaining the required result.
 */
fun Iterable<Point2D<Int>>.toTotalPointsEnclosedByPolygon(): Long =
    this.toShoelacePolygonArea() + 1 - (this.count() shr 1)

/**
 * Extension function on a [Sequence] of [Point2D] to compute the Area enclosed by the path of [Point2D]s in a
 * 2D Grid system using the [Shoelace formula](https://mathworld.wolfram.com/ShoelaceFormula.html).
 *
 * This function assumes that all [Point2D]s are in proper path order for obtaining the required result.
 */
fun Sequence<Point2D<Int>>.toShoelacePolygonArea(): Long =
    this.zipWithNext { point1, point2 ->
        (point1.xPos * point2.yPos - point2.xPos * point1.yPos).toLong()
    }.sum().absoluteValue shr 1

/**
 * Extension function on a [Sequence] of [Point2D] to compute the number of Points enclosed by the Polygon formed
 * by the path of [Point2D]s in a 2D Grid system using the [Pick's theorem](https://en.wikipedia.org/wiki/Pick%27s_theorem).
 *
 * This function assumes that all [Point2D]s are in proper path order for obtaining the required result.
 */
fun Sequence<Point2D<Int>>.toTotalPointsEnclosedByPolygon(): Long =
    this.toShoelacePolygonArea() + 1 - (this.count() shr 1)

/**
 * Extension function on [Point2D] of type [Int] to compute Manhattan Distance to the [destination]
 * from source [this].
 *
 * To be used in 2D Grid system where only Cardinal movements are permitted.
 */
fun Point2D<Int>.manhattanDistance(destination: Point2D<Int>): Int =
    abs(destination.xPos - xPos) + abs(destination.yPos - yPos)

/**
 * Extension function on [Point2D] of type [Int] to compute slope with [other] location
 * in a 2D Grid system.
 *
 * @throws IllegalArgumentException when [this] and [other] locations share the same x-coordinate, since slope
 * of a line connecting such locations is undefined.
 */
fun Point2D<Int>.slope(other: Point2D<Int>): Int {
    require(xPos != other.xPos) {
        "Slope cannot be determined when two locations have the same x-coordinate = $xPos"
    }

    return (other.yPos - yPos) / (other.xPos - xPos)
}

/**
 * Extension function on [Point2D] of type [Int] to check if [this] is Collinear with the given
 * two locations [location1] and [location2] in a 2D Grid system.
 */
fun Point2D<Int>.isCollinearWith(location1: Point2D<Int>, location2: Point2D<Int>): Boolean =
    (location2.yPos - location1.yPos) * (xPos - location1.xPos) == (location2.xPos - location1.xPos) * (yPos - location1.yPos)

/**
 * Extension function on [Point2D] of type [Int] to find locations at the given [manhattanDistance]
 * with respect to [this] in a 2D Grid system. Locations found can be `null` only when it is out of the Grid range.
 *
 * @param T type of [Point2D]
 * @param manhattanDistance [Int] distance at which new locations needs to be found with respect to [this] location
 * @param locationProvider Lambda for the instance of a location found in the Grid that is of type [T]. Can be `null`
 * when it is found to be out of the Grid range.
 */
fun <T : Point2D<Int>> T.manhattanDistantLocations(
    manhattanDistance: Int,
    locationProvider: (row: Int, column: Int) -> T?
): Sequence<T?> =
    sequence {
        ((xPos - manhattanDistance)..(xPos + manhattanDistance)).forEach { x ->
            listOf(
                yPos - (manhattanDistance - (x - xPos).absoluteValue),
                yPos + (manhattanDistance - (x - xPos).absoluteValue)
            ).forEach { y ->
                yield(locationProvider(x, y))
            }
        }
    }
