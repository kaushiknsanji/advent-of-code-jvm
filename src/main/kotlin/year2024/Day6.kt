/**
 * Problem: Day6: Guard Gallivant
 * https://adventofcode.com/2024/day/6
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import utils.grid.ILattice
import utils.grid.Lattice
import utils.grid.Point2d
import utils.grid.TransverseDirection

private class Day6 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 41
    println("=====")
    solveActual(1)      // 5177
    println("=====")
    solveSample(2)      // 6
    println("=====")
    solveActual(2)      // 1686
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day6.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day6.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    LabGuardTracer.parse(input)
        .getCountOfDistinctPositionsVisitedByGuard()
        .also(::println)
}

private fun doPart2(input: List<String>) {
    LabGuardTracer.parse(input)
        .getCountOfSingleObstructingPositionsThatSetsGuardStuckInLoop()
        .also(::println)
}

private class LabLayoutPlanTile(x: Int, y: Int) : Point2d<Int>(x, y)

private enum class LabLayoutPlanTileType(val type: Char) {
    PATH('.'),
    BLOCK('#'),
    GUARD('^')
}

private class LabLayoutGrid(
    layoutPattern: List<String>
) : Lattice<LabLayoutPlanTile, LabLayoutPlanTileType>(layoutPattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value location's row
     * @param column [Int] value location's column
     */
    override fun provideLocation(row: Int, column: Int): LabLayoutPlanTile =
        LabLayoutPlanTile(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar Char found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): LabLayoutPlanTileType =
        LabLayoutPlanTileType.entries.single { it.type == locationChar }

}

private class LabGuardTracer private constructor(
    private val labLayoutGrid: LabLayoutGrid
) : ILattice<LabLayoutPlanTile, LabLayoutPlanTileType> by labLayoutGrid {

    companion object {

        fun parse(input: List<String>): LabGuardTracer = LabGuardTracer(LabLayoutGrid(input))
    }

    /**
     * Returns [LabLayoutPlanTileType] value found at [this] in the [labLayoutGrid].
     */
    private fun LabLayoutPlanTile.toType(): LabLayoutPlanTileType = labLayoutGrid[this]

    /**
     * Returns a [TransverseDirection] at right angle to [this].
     */
    private fun TransverseDirection.nextRightQuarterTurn(): TransverseDirection = when (this) {
        TransverseDirection.TOP -> TransverseDirection.RIGHT
        TransverseDirection.BOTTOM -> TransverseDirection.LEFT
        TransverseDirection.RIGHT -> TransverseDirection.BOTTOM
        TransverseDirection.LEFT -> TransverseDirection.TOP
    }

    // The start direction of the Patrolling Guard
    private val startDirection: TransverseDirection = TransverseDirection.TOP

    // The starting [LabLayoutPlanTile] of the Guard in the [labLayoutGrid]
    private val startPosition: LabLayoutPlanTile by lazy {
        getAllLocations().single { tile: LabLayoutPlanTile ->
            tile.toType() == LabLayoutPlanTileType.GUARD
        }
    }

    // Tiles traced by the Guard patrolling
    private val guardTracedPositions: List<LabLayoutPlanTile> by lazy {
        var position = startPosition
        var direction = startDirection

        buildList {
            // Add start position
            add(position)

            // Keep tracing tiles in the direction only when it has a Block, since without it Guard will exit
            while (position.getLocationsInDirection(direction).any { nextTile ->
                    nextTile.toType() == LabLayoutPlanTileType.BLOCK
                }) {

                addAll(
                    // Add all tiles till the guard reaches a Block in the same direction. Exclude the first tile
                    // since it has been already added
                    position.getLocationsInDirection(direction).drop(1)
                        .takeWhile { it.toType() != LabLayoutPlanTileType.BLOCK }
                        .also { tiles: Sequence<LabLayoutPlanTile> ->
                            // Since guard has reached the last reachable tile in current direction,
                            // pick this last tile as the current position of guard
                            position = tiles.last()
                        }
                )

                // Rotate guard since he has reached a Block
                direction = direction.nextRightQuarterTurn()
            }

            // When there are no more Blocks in current direction, the guard exits. Add these remaining tiles.
            addAll(position.getLocationsInDirection(direction).drop(1))
        }
    }

    /**
     * [Solution for Part-1]
     *
     * Returns Total number of distinct tiles visited by Guard during patrol.
     */
    fun getCountOfDistinctPositionsVisitedByGuard(): Int =
        guardTracedPositions.distinct().count()

    /**
     * [Solution for Part-2]
     *
     * Returns Total number of new single obstructions that can be placed in the path of patrolling Guard
     * to ensure the Guard stays stuck in a loop.
     */
    fun getCountOfSingleObstructingPositionsThatSetsGuardStuckInLoop(): Int =
        guardTracedPositions.distinct().filterNot { tile: LabLayoutPlanTile ->
            tile == startPosition
        }.count { blockTile: LabLayoutPlanTile ->
            var position = startPosition
            var direction = startDirection
            var isGuardInLoop = false

            // Set of Tiles where the Guard makes a quarter turn to the right. Tile visited with entry direction
            // is saved in order to detect if the Guard is in loop when the Guard revisits the same Tile
            // in the same direction as previously logged.
            val visitedQuarterTurnTiles: MutableSet<Pair<LabLayoutPlanTile, TransverseDirection>> =
                mutableSetOf(position to direction)

            // Keep tracing tiles in the direction only when there is a Block, since without it Guard will exit
            while (position.getLocationsInDirection(direction).any { nextTile ->
                    nextTile.toType() == LabLayoutPlanTileType.BLOCK || nextTile == blockTile
                }) {

                // Get all tiles till the guard reaches a block in the same direction. Exclude the first tile
                // since it was visited in the previous direction before turning to the current direction.
                val nextPositions = position.getLocationsInDirection(direction).drop(1)
                    .takeWhile { nextTile ->
                        nextTile.toType() != LabLayoutPlanTileType.BLOCK && nextTile != blockTile
                    }

                // Above [nextPositions] can be empty when the very next tile in the direction from the current
                // [position] happens to be blocked
                if (nextPositions.iterator().hasNext()) {
                    // When there were tiles in the direction, get the last tile from [nextPositions] as this
                    // is where the Guard will make the next quarter turn to the right as we had already ensured
                    // that the next tile in this direction was blocked.
                    position = nextPositions.last()

                    // If this tile had already been visited in the same direction, then the Guard is stuck in loop.
                    // If not visited in the same direction or visited at all, then log it.
                    if (!visitedQuarterTurnTiles.add(position to direction)) {
                        // When stuck, update state and bail out
                        isGuardInLoop = true
                        break
                    }
                }

                // Turn right since this while loop always ensures there is a block in the end
                direction = direction.nextRightQuarterTurn()
            }

            // Returns true when the Guard was successfully made to be stuck in a loop
            // with the current [blockTile] as a new Obstruction
            isGuardInLoop
        }

}