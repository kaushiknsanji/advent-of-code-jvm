/**
 * Problem: Day14: Restroom Redoubt
 * https://adventofcode.com/2024/day/14
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler
import utils.findAllInt
import utils.grid.Point2d
import utils.product

private class Day14 : BaseProblemHandler() {

    /**
     * Returns the Package name of this problem class
     */
    override fun getCurrentPackageName(): String = this::class.java.`package`.name

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
        RobotAnalyzer.parse(input)
            .getSafetyFactor(
                yRows = otherArgs[0] as Int,
                xColumns = otherArgs[1] as Int,
                elapsedTimeInSeconds = otherArgs[2] as Int
            )

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        if (otherArgs[2] == 0) {
            RobotAnalyzer.parse(input)
                .getFewestSecondsToDisplayChristmasTree(otherArgs[0] as Int, otherArgs[1] as Int)
        } else {
            RobotAnalyzer.parse(input)
                .getSafetyFactor(
                    yRows = otherArgs[0] as Int,
                    xColumns = otherArgs[1] as Int,
                    elapsedTimeInSeconds = otherArgs[2] as Int
                )
        }

}

fun main() {
    with(Day14()) {
        solveSample(1, false, 0, 12, 7, 11, 100)
        solveActual(1, false, 0, 229632480, 103, 101, 100)
        solveActual(2, false, 0, 7051, 103, 101, 0)
    }
}

/**
 * Class for a Robot.
 *
 * @property location [Point2d] location of a Robot
 * @property velocity [Point2d] denoting the velocity of a Robot moving by [Point2d.xPos] and [Point2d.yPos]
 * tiles per second along both X and Y directions.
 *
 * @constructor Constructs a [Robot] at [location] moving with [velocity].
 */
private class Robot(val location: Point2d<Int>, val velocity: Point2d<Int>) {

    /**
     * Moves [Robot] to its next location updated by its [velocity].
     *
     * @param yRows [Int] number of rows in the grid
     * @param xColumns [Int] number of columns in the grid
     */
    fun moveRobot(
        yRows: Int,
        xColumns: Int
    ): Robot = Robot(
        location = Point2d(
            // Wrapped X position
            xPos = (location.xPos + velocity.xPos).mod(xColumns),
            // Wrapped Y position
            yPos = (location.yPos + velocity.yPos).mod(yRows)
        ),
        velocity = velocity
    )

}

private class RobotAnalyzer private constructor(
    private val robots: List<Robot>
) {

    companion object {

        fun parse(input: List<String>): RobotAnalyzer = input.map { line ->
            line.findAllInt().let { numbers: List<Int> ->
                check(numbers.size == 4) {
                    "Error: Parsed number count in the input should be 4, was ${numbers.size}"
                }

                Robot(Point2d(numbers[0], numbers[1]), Point2d(numbers[2], numbers[3]))
            }
        }.let { robots: List<Robot> ->
            RobotAnalyzer(robots)
        }

    }

    /**
     * Returns a [List] of all four Quadrants in the grid with ranges of their rows and columns.
     * Center row and center column that forms these Quadrants will not be considered.
     *
     * @param yRows [Int] number of rows in the grid
     * @param xColumns [Int] number of columns in the grid
     */
    private fun getQuadrants(
        yRows: Int,
        xColumns: Int
    ): List<Pair<IntRange, IntRange>> {
        val halfRows = yRows shr 1
        val halfColumns = xColumns shr 1

        return listOf(
            0..<halfRows to 0..<halfColumns,
            0..<halfRows to halfColumns + 1..<xColumns,
            halfRows + 1..<yRows to 0..<halfColumns,
            halfRows + 1..<yRows to halfColumns + 1..<xColumns
        )
    }

    /**
     * Returns a Quadrant [this] location of a [Robot] belongs to. Can be `null` when [this] does not fall into
     * any of the [quadrants] provided.
     */
    private fun Point2d<Int>.toQuadrant(quadrants: List<Pair<IntRange, IntRange>>): Pair<IntRange, IntRange>? =
        quadrants.singleOrNull { (rowRange: IntRange, columnRange: IntRange) ->
            xPos in columnRange && yPos in rowRange
        }

    /**
     * [Solution for Part-1]
     *
     * Returns Safety factor of the grid space after the [elapsed time in seconds][elapsedTimeInSeconds].
     *
     * @param yRows [Int] number of rows in the grid
     * @param xColumns [Int] number of columns in the grid
     */
    fun getSafetyFactor(yRows: Int, xColumns: Int, elapsedTimeInSeconds: Int): Int =
        getQuadrants(yRows, xColumns).let { quadrants: List<Pair<IntRange, IntRange>> ->
            robots.map { robot ->
                var currentRobot = robot

                // Simulate movement for [elapsedTimeInSeconds] and get the updated Robot data
                repeat(elapsedTimeInSeconds) {
                    currentRobot = currentRobot.moveRobot(yRows, xColumns)
                }

                // Return a Pair of Robot data to the Quadrant it belongs
                currentRobot to currentRobot.location.toQuadrant(quadrants)
            }.filterNot { (_, quadrant) ->
                // Exclude Robots that are not in any Quadrant
                quadrant == null
            }.groupBy { (_, quadrant) ->
                quadrant
            }.values.map { robotsInQuadrant ->
                robotsInQuadrant.size
            }.product()
        }

    /**
     * [Solution for Part-2]
     *
     * Returns fewest number of seconds needed for the [robots] to display a Christmas tree.
     *
     * @param yRows [Int] number of rows in the grid
     * @param xColumns [Int] number of columns in the grid
     */
    fun getFewestSecondsToDisplayChristmasTree(yRows: Int, xColumns: Int): Int {
        var seconds = 0
        var currentRobots = robots

        do {
            // Increment second counter
            seconds++
            // Update all Robots to their next position
            currentRobots = currentRobots.map { robot ->
                robot.moveRobot(yRows, xColumns)
            }
        } while (
        // Christmas Tree can be formed only when no more than 1 Robot is at a tile.
        // Loop till such a situation occurs.
            currentRobots.groupBy { robot ->
                robot.location
            }.any { (_, locationGroupedRobots: List<Robot>) ->
                locationGroupedRobots.size > 1
            }
        )

        return seconds
    }

}