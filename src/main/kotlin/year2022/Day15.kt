/**
 * Problem: Day15: Beacon Exclusion Zone
 * https://adventofcode.com/2022/day/15
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseProblemHandler
import extensions.intersectRange
import extensions.mergeIntRanges
import extensions.rangeLength
import utils.findAllInt
import utils.grid.Point2d
import utils.grid.manhattanDistance
import utils.grid.manhattanDistantLocations

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
        BeaconAnalyzer.parse(input)
            .getCountOfNonBeaconSpotsAtRow(otherArgs[0] as Int)

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        BeaconAnalyzer.parse(input)
            .getDistressBeaconTuningFrequency(otherArgs[0] as IntRange)

}

fun main() {
    with(Day15()) {
        solveSample(1, false, 0, 26, 10)
        solveActual(1, false, 0, 5166077, 2000000)
        solveSample(2, false, 0, 56000011L, 0..20)
        solveActual(2, false, 0, 13071206703981L, 0..4000000)
    }
}

/**
 * Class for Sensor Location.
 *
 * @param x [Int] value of x-coordinate
 * @param y [Int] value of y-coordinate
 */
private class SensorLocus(x: Int, y: Int) : Point2d<Int>(x, y)

/**
 * Class for Beacon Location.
 *
 * @param x [Int] value of x-coordinate
 * @param y [Int] value of y-coordinate
 */
private class BeaconLocus(x: Int, y: Int) : Point2d<Int>(x, y)

/**
 * Class to parse the input, analyze and solve the problem at hand.
 *
 * @property sensorBeaconLocusPairs [List] of [Pair] of Sensor at [SensorLocus] to its closest Beacon at [BeaconLocus]
 */
private class BeaconAnalyzer private constructor(
    private val sensorBeaconLocusPairs: List<Pair<SensorLocus, BeaconLocus>>
) {

    companion object {

        fun parse(input: List<String>): BeaconAnalyzer = BeaconAnalyzer(
            sensorBeaconLocusPairs = input.map { line ->
                line.findAllInt().let { coordinatesList: List<Int> ->
                    SensorLocus(coordinatesList[0], coordinatesList[1]) to
                            BeaconLocus(coordinatesList[2], coordinatesList[3])
                }
            }
        )

    }

    // List of all Beacon locations
    private val beaconLocations: List<BeaconLocus> by lazy {
        sensorBeaconLocusPairs.map { locusPair ->
            locusPair.second
        }.distinct()
    }

    // Pairs of Sensor to its distance to its closest Beacon
    private val sensorManhattanDistancePairs: List<Pair<SensorLocus, Int>> by lazy {
        sensorBeaconLocusPairs.map { locusPair ->
            locusPair.first to locusPair.first.manhattanDistance(locusPair.second)
        }
    }

    /**
     * Returns column ranges of Sensor coverage of all Sensors at given [row][yRow].
     *
     * Using [sensorManhattanDistancePairs], it gets Sensor coordinate [Points][Point2d] at given [row][yRow]
     * and returns the merged coverage range of all Sensors along the row.
     */
    private fun getSensorCoverageSpotsAtRow(yRow: Int): Collection<IntRange> =
        sensorManhattanDistancePairs.map { (sensorLocus, distance) ->
            sensorLocus.manhattanDistantLocations(distance) { x, y ->
                Point2d(x, y)
            }.filterNotNull().filter { coveragePoint: Point2d<Int> ->
                coveragePoint.yPos == yRow
            }.map { coveragePoint: Point2d<Int> ->
                coveragePoint.xPos
            }.toList()
                .takeUnless(List<Int>::isEmpty)?.sorted()?.let { rowCoveragePoints: List<Int> ->
                    rowCoveragePoints.first()..rowCoveragePoints.last()
                } ?: IntRange.EMPTY
        }.mergeIntRanges()

    /**
     * Returns a single [Point][Point2d] within the given [search range][searchRange] which is not reachable by
     * any Sensor.
     *
     * @throws NoSuchElementException when dead spot could not be found.
     */
    private fun getSingleSensorCoverageDeadSpot(searchRange: IntRange): Point2d<Int> {
        // Coverage Map of all Sensors within the search range of both rows and columns
        val fullCoverageMap = mutableMapOf<Int, MutableList<IntRange>>()

        sensorManhattanDistancePairs.forEach { (sensorLocus, distance) ->
            // Coverage Map of current Sensor within the search range of rows only
            val sensorRowCoverageMap = mutableMapOf<Int, MutableList<Int>>()

            // Get Manhattan Distant Points of the current Sensor within the search range of rows only
            sensorLocus.manhattanDistantLocations(distance) { x, y ->
                Point2d(x, y)
            }.filterNotNull().filter { coveragePoint: Point2d<Int> ->
                coveragePoint.yPos in searchRange
            }.forEach { coveragePoint: Point2d<Int> ->
                // Update coverage point to [sensorRowCoverageMap] for the identified row
                val rowCoveragePoints = sensorRowCoverageMap.getOrPut(coveragePoint.yPos) { mutableListOf() }
                rowCoveragePoints.add(coveragePoint.xPos)
            }

            // Transform each row coverage points of the current Sensor to row coverage range
            // within the search range of columns and update it to [fullCoverageMap]
            sensorRowCoverageMap.forEach { (yRow, rowCoveragePoints: List<Int>) ->
                val rowCoverageRanges = fullCoverageMap.getOrPut(yRow) { mutableListOf() }
                rowCoverageRanges.add(
                    rowCoveragePoints.sorted().let {
                        it.first()..it.last()
                    }.intersectRange(searchRange)
                )
            }
        }

        // Merge row coverage ranges of each row to find a row that results in more than one merged row range
        // which would contain a single dead spot between those merged ranges. Return this dead spot if found
        // or throw an exception
        return fullCoverageMap.firstNotNullOf { (yRow, rowCoverageRanges) ->
            rowCoverageRanges.mergeIntRanges().takeIf { it.size > 1 }?.zipWithNext { currentRange, nextRange ->
                (currentRange.last + 1) until nextRange.first
            }?.flatten()?.singleOrNull()?.let { xColumn: Int ->
                Point2d(xColumn, yRow)
            }
        }

    }

    /**
     * Returns a [Long] of the Tuning Frequency value for [this] location containing the distress Beacon
     */
    private fun Point2d<Int>.getTuningFrequency(): Long = xPos * 4000000L + yPos

    /**
     * [Solution for Part-1]
     *
     * Returns the number of positions in the given [row][yRow] that cannot contain a beacon
     */
    fun getCountOfNonBeaconSpotsAtRow(yRow: Int): Int =
        getSensorCoverageSpotsAtRow(yRow).fold(0) { acc: Int, range: IntRange ->
            // Accumulate number of positions covered by the Sensors along the given row
            acc + range.rangeLength()
        } - beaconLocations.count { beaconLocus ->
            // Exclude any beacons already present in the given row
            beaconLocus.yPos == yRow
        }

    /**
     * [Solution for Part-2]
     *
     * Returns a [Long] of the Tuning Frequency value for the location containing the distress Beacon found
     * within the given [search range][searchRange]
     */
    fun getDistressBeaconTuningFrequency(searchRange: IntRange): Long =
        getSingleSensorCoverageDeadSpot(searchRange).getTuningFrequency()

}