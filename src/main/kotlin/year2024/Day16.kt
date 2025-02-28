/**
 * Problem: Day16: Reindeer Maze
 * https://adventofcode.com/2024/day/16
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler
import utils.Constants.DOT_CHAR
import utils.Constants.E_CAP_CHAR
import utils.Constants.HASH_CHAR
import utils.Constants.S_CAP_CHAR
import utils.grid.CardinalDirection.RIGHT
import utils.grid.ILattice
import utils.grid.Lattice
import utils.grid.Point2d
import java.util.*
import utils.grid.CardinalDirection as Direction

class Day16 : BaseProblemHandler() {

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
        ReindeerMazeAnalyzer.parse(input)
            .getLowestScoreOfReindeer()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        ReindeerMazeAnalyzer.parse(input)
            .getCountOfTilesAlongBestPaths()

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 1, 7036)
        solveSample(1, false, 2, 11048)
        solveActual(1, false, 0, 143580)
        solveSample(2, false, 1, 45)
        solveSample(2, false, 2, 64)
        solveActual(2, false, 0, 645)
    }

}

fun main() {
    Day16().start()
}

private enum class ReindeerMazeType(val type: Char) {
    WALL(HASH_CHAR),
    START(S_CAP_CHAR),
    END(E_CAP_CHAR),
    SPACE(DOT_CHAR);

    companion object {
        private val typeMap = entries.associateBy(ReindeerMazeType::type)

        fun fromType(type: Char): ReindeerMazeType = typeMap[type]!!
    }
}

private class ReindeerMazeTile(x: Int, y: Int) : Point2d<Int>(x, y)

private class ReindeerMazeGrid(
    pattern: List<String>
) : Lattice<ReindeerMazeTile, ReindeerMazeType>(pattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value of location's row
     * @param column [Int] value of location's column
     */
    override fun provideLocation(row: Int, column: Int): ReindeerMazeTile =
        ReindeerMazeTile(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar [Char] found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): ReindeerMazeType =
        ReindeerMazeType.fromType(locationChar)

}

private class ReindeerMazeAnalyzer private constructor(
    private val reindeerMazeGrid: ReindeerMazeGrid
) : ILattice<ReindeerMazeTile, ReindeerMazeType> by reindeerMazeGrid {

    companion object {

        fun parse(input: List<String>): ReindeerMazeAnalyzer = ReindeerMazeAnalyzer(ReindeerMazeGrid(input))
    }

    // Start Tile
    private val startLocation: ReindeerMazeTile = getAllLocations().single { tile ->
        tile.toValue() == ReindeerMazeType.START
    }

    // End Tile
    private val endLocation: ReindeerMazeTile = getAllLocations().single { tile ->
        tile.toValue() == ReindeerMazeType.END
    }

    // Start Direction
    private val startDirection = RIGHT

    /**
     * Data class used for Tracing path of [ReindeerMazeTile]s in [reindeerMazeGrid]
     *
     * @property tile A [ReindeerMazeTile] being traced in the path
     * @property direction [Direction] of traversal
     * @property score [Int] value of score accumulated thus far
     *
     * @constructor Constructs [TraceData] for tracing path in [reindeerMazeGrid]
     */
    private data class TraceData(
        val tile: ReindeerMazeTile,
        val direction: Direction,
        val score: Int
    )

    /**
     * Returns next [List] of [TraceData] from [this] Trace information
     */
    private fun TraceData.getNextTilesTraceData(): List<TraceData> =
        tile.getAllNeighboursWithDirection().map { entry: Map.Entry<Direction, ReindeerMazeTile> ->
            // Convert neighbours with their direction information from current tile to a Triple with a step of 1
            Triple(entry.value, entry.key, 1)
        }.filterNot { (nextTile, _, _) ->
            // Exclude next tiles that contains a WALL
            nextTile.toValue() == ReindeerMazeType.WALL
        }.filter { (_, nextDirection, _) ->
            // Allow only next tiles with direction at an angle of 90-degree to the current or same as the current
            nextDirection.isQuarterTurnTo(direction) || nextDirection == direction
        }.map { (nextTile, nextDirection, nextStep) ->
            // Compute the new score for this next tile traced, based on the current score and next direction of travel
            val newScore = score + if (nextDirection.isQuarterTurnTo(direction)) {
                1000 + nextStep
            } else {
                nextStep
            }

            // Construct and return TraceData for this next tile with its direction and new score
            TraceData(nextTile, nextDirection, newScore)
        }

    /**
     * Returns a [Pair] of the [Int] value of the Lowest score of the best path towards [endLocation]
     * from [startLocation] and a [List] of [ReindeerMazeTile]s that belongs to any of the best paths
     * towards [endLocation] from [startLocation].
     *
     * Best paths are obtained by following [Dijkstra](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm) algorithm
     * using [PriorityQueue] which prioritizes on [TraceData.score].
     *
     * @param searchAllBestPaths [Boolean] is `true` when we need to search for all best paths towards [endLocation]
     * from [startLocation]; `false`, when we are interested in only one best path.
     */
    private fun getLowestScoreToBestTiles(
        searchAllBestPaths: Boolean = false
    ): Pair<Int, List<ReindeerMazeTile>> {

        // A PriorityQueue based Frontier that prioritizes on the score thus far, for minimizing the score accumulated
        val frontier = PriorityQueue<TraceData>(
            compareBy { traceData -> traceData.score }
        ).apply {
            // Begin with the TraceData for the starting tile
            add(TraceData(startLocation, startDirection, 0))
        }

        // Map that saves which TraceData we came from for a current List of TraceData resulting from
        // different paths taken. This facilitates to build traversed paths without the need for storing them
        // in a List of Lists for each path discovered.
        // Begin with the map of TraceData of the starting tile coming from a List of TraceData containing the same.
        val cameFromMap: MutableMap<TraceData, MutableList<TraceData>> = mutableMapOf(
            TraceData(startLocation, startDirection, 0) to
                    mutableListOf(TraceData(startLocation, startDirection, 0))
        )

        // Map of Tiles reached with their best lowest score. Begin with the starting tile and its score of 0.
        val scoreMap: MutableMap<ReindeerMazeTile, Int> = mutableMapOf(startLocation to 0)

        // Set to keep track of Tiles reached along with their direction of visit
        val visitedSet: MutableSet<Pair<ReindeerMazeTile, Direction>> = mutableSetOf()

        // List of Best Tiles TraceData
        val bestTilesTraceDataList: MutableList<TraceData> = mutableListOf()

        // Builds `bestTilesTraceDataList` by backtracking from the given `traceData` with the help of TraceData
        // of the tiles traced and mapped in `cameFromMap`
        val buildBestTilesTraceDataList: (traceData: TraceData) -> Unit =
            {
                // ArrayDeque to backtrack from the given `traceData`
                val currentQueue = ArrayDeque<TraceData>().apply { add(it) }

                // Process till ArrayDeque becomes empty
                while (currentQueue.isNotEmpty()) {
                    // Get the first TraceData
                    val current = currentQueue.removeFirst()

                    // Continue to next if already added to `bestTilesTraceDataList`
                    if (current in bestTilesTraceDataList) continue

                    // Lookup current TraceData in `cameFromMap` to get the previous list of TraceData
                    cameFromMap[current]!!
                        .filterNot { previousTraceData ->
                            // Exclude those already added to `bestTilesTraceDataList`
                            previousTraceData in bestTilesTraceDataList
                        }
                        .forEach { previousTraceData ->
                            // Add previous TraceData to ArrayDeque for further processing
                            currentQueue.add(previousTraceData)
                        }

                    // After current TraceData is processed, add it to `bestTilesTraceDataList`
                    bestTilesTraceDataList.add(current)
                }

            }

        // Repeat till the PriorityQueue based Frontier becomes empty
        while (frontier.isNotEmpty()) {
            // Get the Top TraceData
            val current = frontier.poll()

            if (current.tile == endLocation) {
                // When the end is reached, build `bestTilesTraceDataList` and exit
                buildBestTilesTraceDataList(current)
                break
            }

            // Continue if current tile is already visited in the direction
            if ((current.tile to current.direction) in visitedSet) continue

            // Retrieve Next list of TraceData
            current.getNextTilesTraceData()
                .filterNot { next ->
                    // Exclude next tile if already visited in the direction
                    (next.tile to next.direction) in visitedSet
                }.forEach { next ->
                    // Save the current TraceData in the list of TraceData, this next TraceData came from, in the Map
                    cameFromMap[next] = cameFromMap.getOrDefault(next, mutableListOf()).apply {
                        add(current)
                    }

                    // Mark current tile as visited in the direction
                    visitedSet.add(current.tile to current.direction)

                    // When we are required to search for all best paths to end,
                    // add this next TraceData to the Frontier
                    if (searchAllBestPaths) {
                        frontier.add(next)
                    }

                    // When we have found the next best lowest score for this next TraceData
                    if (next.score < scoreMap.getOrDefault(next.tile, Int.MAX_VALUE)) {
                        // Update the score map with the new lowest score for this next tile
                        scoreMap[next.tile] = next.score

                        // When we are only required to find one of the best paths to end,
                        // add this next TraceData to the Frontier
                        if (!searchAllBestPaths) {
                            frontier.add(next)
                        }
                    }
                }

        }

        // Return a Pair of the lowest score and the list of tiles traced
        // for all best paths taken towards end from start
        return scoreMap.getOrDefault(endLocation, -1) to
                bestTilesTraceDataList.map { traceData -> traceData.tile }.distinct()
    }

    /**
     * [Solution for Part-1]
     *
     * Returns the Lowest Score a Reindeer could get in reaching the [endLocation] from [startLocation]
     */
    fun getLowestScoreOfReindeer(): Int =
        getLowestScoreToBestTiles().first

    /**
     * [Solution for Part-2]
     *
     * Returns number of tiles found along the best paths to [endLocation] from [startLocation]
     */
    fun getCountOfTilesAlongBestPaths(): Int =
        getLowestScoreToBestTiles(searchAllBestPaths = true).second.count()

}