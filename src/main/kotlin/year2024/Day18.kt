/**
 * Problem: Day18: RAM Run
 * https://adventofcode.com/2024/day/18
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler
import utils.Constants.COMMA_STRING
import utils.Constants.DOT_CHAR
import utils.Constants.EMPTY
import utils.Constants.HASH_CHAR
import utils.findAllInt
import utils.grid.ILattice
import utils.grid.Lattice
import utils.grid.Point2d
import java.util.*

class Day18 : BaseProblemHandler() {

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
        MemoryAnalyzer.parse(input, otherArgs[0] as Int, otherArgs[1] as Int)
            .getMinimumStepsToReachExit(otherArgs[2] as Int)

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        MemoryAnalyzer.parse(input, otherArgs[0] as Int, otherArgs[1] as Int)
            .getFirstBytePositionPreventingExit(otherArgs[2] as Int)

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, 22, 7, 7, 12)
        solveActual(1, false, 0, 302, 71, 71, 1024)
        solveSample(2, false, 0, "6,1", 7, 7, 12)
        solveActual(2, false, 0, "24,32", 71, 71, 1024)
    }

}

fun main() {
    Day18().start()
}

private enum class MemorySpaceType(val type: Char) {
    SPACE(DOT_CHAR),
    CORRUPTED(HASH_CHAR);

    companion object {
        private val typeMap = entries.associateBy(MemorySpaceType::type)

        fun fromType(type: Char): MemorySpaceType = typeMap[type]!!
    }
}

private class BytePosition(x: Int, y: Int) : Point2d<Int>(x, y)

private class MemorySpaceGrid(
    pattern: List<String>
) : Lattice<BytePosition, MemorySpaceType>(pattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value of location's row
     * @param column [Int] value of location's column
     */
    override fun provideLocation(row: Int, column: Int): BytePosition =
        BytePosition(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar [Char] found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): MemorySpaceType =
        MemorySpaceType.fromType(locationChar)

}

private class MemoryAnalyzer private constructor(
    private val bytePositions: List<BytePosition>,
    private val memorySpaceGrid: MemorySpaceGrid
) : ILattice<BytePosition, MemorySpaceType> by memorySpaceGrid {

    companion object {

        fun parse(input: List<String>, rows: Int, columns: Int): MemoryAnalyzer = input.map { line ->
            line.findAllInt().let { positions: List<Int> ->
                BytePosition(positions[0], positions[1])
            }
        }.let { bytePositions ->
            MemoryAnalyzer(
                bytePositions,
                memorySpaceGrid = MemorySpaceGrid(
                    pattern = List(rows) {
                        List(columns) { DOT_CHAR }.joinToString(EMPTY)
                    }
                )
            )
        }

    }

    // Start position
    private val startLocation = getLocation(0, 0)

    // End position
    private val endLocation = getLocation(memorySpaceGrid.rows - 1, memorySpaceGrid.columns - 1)

    /**
     * Simulates bytes falling into [position][BytePosition] inside the [Memory space][memorySpaceGrid]. Such locations
     * inside the [Memory space][memorySpaceGrid] is marked as [MemorySpaceType.CORRUPTED].
     *
     * @param bytesToProcess [Int] value of the number of bytes from [bytePositions] to simulate
     */
    private fun simulateBytesFalling(bytesToProcess: Int) {
        bytePositions.take(bytesToProcess)
            .forEach { bytePosition ->
                memorySpaceGrid[bytePosition] = MemorySpaceType.CORRUPTED
            }
    }

    /**
     * Returns a [List] of neighbouring [BytePosition]s of [this] that are not yet [MemorySpaceType.CORRUPTED]
     * and excludes the given [Previous BytePosition][previousBytePosition]
     */
    private fun BytePosition.getNextBytePositions(previousBytePosition: BytePosition): List<BytePosition> =
        getAllNeighbours().filterNot { nextBytePosition ->
            nextBytePosition.toValue() == MemorySpaceType.CORRUPTED
                    || nextBytePosition == previousBytePosition
        }

    /**
     * Returns shortest path of [BytePosition]s to [endLocation] from [startLocation]. Can return an empty [List]
     * if [endLocation] is NOT reachable.
     *
     * Shortest path is obtained by following [Dijkstra](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm) algorithm
     * using [PriorityQueue] which prioritizes on step distance accumulated in reaching [endLocation].
     */
    private fun getShortestPathToEnd(): List<BytePosition> {
        // A PriorityQueue based Frontier that prioritizes on the distance thus far, for minimizing the
        // distance accumulated
        val frontier = PriorityQueue<Pair<BytePosition, Int>>(
            compareBy { it.second }
        ).apply {
            // Begin with start location for an accumulated distance of 0
            add(startLocation to 0)
        }

        // Map that saves which Byte Position we came from for a current Byte Position along with
        // their distances as Pairs. This facilitates to build traversed paths without the need for storing them in
        // a List of Lists for each path discovered.
        // Begin with a Pair of start location and a distance of 0 as both key and value.
        val cameFromMap: MutableMap<Pair<BytePosition, Int>, Pair<BytePosition, Int>> =
            mutableMapOf((startLocation to 0) to (startLocation to 0))

        // Map of Byte Positions reached along with their best distance. Begin with start location
        // for an accumulated distance of 0.
        val distanceMap: MutableMap<BytePosition, Int> = mutableMapOf(startLocation to 0)

        // Generates a sequence of Byte Positions traversed by backtracking from the given pair of Byte Position
        // and its distance. Sequence generated will be in the reverse direction, till and including the start location.
        val pathSequence: (currentPair: Pair<BytePosition, Int>) -> Sequence<BytePosition> = {
            sequence {
                var currentPair: Pair<BytePosition, Int> = it
                while (currentPair.first != startLocation) {
                    yield(currentPair.first)
                    currentPair = cameFromMap[currentPair]!!
                }
                yield(currentPair.first)
            }
        }

        // Repeat till the PriorityQueue based Frontier becomes empty
        while (frontier.isNotEmpty()) {
            // Get the top Byte Position + Distance pair
            val current = frontier.poll()

            // Exit when end location is reached
            if (current.first == endLocation) break

            // Retrieve next list of Byte Positions
            current.first.getNextBytePositions(cameFromMap[current]!!.first)
                .forEach { nextBytePosition ->
                    // Calculate the new distance to this next Byte Position
                    val newDistance = current.second + 1

                    // When we have found the next best distance to this next Byte Position
                    if (newDistance < distanceMap.getOrDefault(nextBytePosition, Int.MAX_VALUE)) {
                        // Update the distance map with the new distance for this next Byte Position
                        distanceMap[nextBytePosition] = newDistance
                        // Save the current Byte Position Distance pair as the value of
                        // this next Byte Position Distance pair in the Map
                        cameFromMap[nextBytePosition to newDistance] = current
                        // Add this next Byte Position Distance pair to the Frontier
                        frontier.add(nextBytePosition to newDistance)
                    }
                }
        }

        return if (distanceMap.getOrDefault(endLocation, -1) > -1) {
            // When end location is reached, build and return the shortest path
            pathSequence(endLocation to distanceMap[endLocation]!!).toList().reversed()
        } else {
            // When end location is NOT reachable, return an empty list
            emptyList()
        }
    }

    /**
     * [Solution for Part-1]
     *
     * Returns minimum number of steps required to reach [exit][endLocation] from [start][startLocation].
     *
     * @param bytesToProcess [Int] value of the number of bytes from [bytePositions] to simulate
     * falling into [Memory space][memorySpaceGrid]
     */
    fun getMinimumStepsToReachExit(bytesToProcess: Int): Int =
        simulateBytesFalling(bytesToProcess).let {
            // Simulate bytes falling into memory space for the required number of bytes and then find the
            // shortest path to exit from start. Return the step distance to exit if path is found;
            // otherwise, throw an Error
            getShortestPathToEnd().lastIndex.takeIf { it > -1 } ?: throw Error(
                "Could not deduce the Shortest Distance to end"
            )
        }

    /**
     * [Solution for Part-2]
     *
     * Returns comma-separated format of the coordinates of the first byte that prevents the [exit][endLocation]
     * from being reachable from the [start][startLocation].
     *
     * @param bytesToProcess [Int] value of the number of bytes from [bytePositions] to simulate
     * falling into [Memory space][memorySpaceGrid]
     */
    fun getFirstBytePositionPreventingExit(bytesToProcess: Int): String =
        simulateBytesFalling(bytesToProcess).let {
            // Simulate bytes falling into memory space for the required number of bytes and then find the
            // shortest path to exit from start
            var lastShortestPath = getShortestPathToEnd()

            // Simulate remaining bytes falling into memory space until we find the position of a byte falling into
            // memory space that blocks all paths to exit
            bytePositions.drop(bytesToProcess).asSequence()
                .onEach { bytePosition ->
                    // When a byte falls into memory space at `bytePosition`, mark it as CORRUPTED
                    memorySpaceGrid[bytePosition] = MemorySpaceType.CORRUPTED
                }.map { bytePosition ->
                    // If a byte falls into memory space at a position that is already part of the last shortest path,
                    // then rebuild the shortest path to exit as this position is CORRUPTED
                    if (bytePosition in lastShortestPath) {
                        lastShortestPath = getShortestPathToEnd()
                    }

                    // Return a pair of byte position and distance to exit from start
                    bytePosition to lastShortestPath.lastIndex
                }.first { (_, distanceToEnd) ->
                    // Pick the first pair with negative distance to exit.
                    // LastIndex of the shortest path list will be negative for an empty list resulting from
                    // the exit being unreachable from the start location.
                    distanceToEnd == -1
                }.first.toCoordinateList().joinToString(COMMA_STRING)
        }

}