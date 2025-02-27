/**
 * Problem: Day14: Parabolic Reflector Dish
 * https://adventofcode.com/2023/day/14
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import utils.grid.CardinalDirection.*
import utils.grid.CardinalDirection as Direction

private class Day14 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 136
    println("=====")
    solveActual(1)      // 109424
    println("=====")
    solveSample(2)      // 64
    println("=====")
    solveActual(2)      // 102509
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day14.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day14.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    ReflectorDishRockAnalyzer(input)
        .getTotalLoadOnNorthSupportBeams()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    ReflectorDishRockAnalyzer(input)
        .getTotalLoadOnNorthSupportBeams(spinCycleOn = true, spinCount = 1_000_000_000)
        .also { println(it) }
}

private class DishRockPosition(val x: Int, val y: Int)

private enum class DishRockType(val type: Char) {
    ROLLING_ROCK('O'),
    CUBE_ROCK('#'),
    EMPTY_SPACE('.')
}

private interface IDishRockGrid {
    fun getRockPositionOrNull(row: Int, column: Int): DishRockPosition?
    fun getRockPosition(row: Int, column: Int): DishRockPosition
    fun getAllRockPositions(): Collection<DishRockPosition>
    fun getAllRollingRockPositions(): Collection<DishRockPosition>
    fun DishRockPosition.toType(): DishRockType
    fun DishRockPosition.getNeighbour(direction: Direction): DishRockPosition?
    fun DishRockPosition.getAllNeighbours(): Collection<DishRockPosition>
    fun DishRockPosition.getRockPositionsInDirection(direction: Direction): Sequence<DishRockPosition>
}

private class DishRockGrid private constructor(
    val rows: Int,
    columns: Int,
    rockPatternList: List<String>
) : IDishRockGrid {

    constructor(rockPatternList: List<String>) : this(
        rows = rockPatternList.size,
        columns = rockPatternList.first().length,
        rockPatternList = rockPatternList
    )

    private val dishRockGridMap: Map<Int, List<DishRockPosition>> = (0 until rows).flatMap { row ->
        (0 until columns).map { column ->
            DishRockPosition(row, column)
        }
    }.groupBy { dishRockPosition: DishRockPosition -> dishRockPosition.x }

    private val dishRockGridValueMap: MutableMap<DishRockPosition, DishRockType> =
        rockPatternList.flatMapIndexed { rowIndex: Int, rowPattern: String ->
            rowPattern.mapIndexed { columnIndex: Int, value: Char ->
                getRockPosition(rowIndex, columnIndex) to DishRockType.entries.single { it.type == value }
            }
        }.toMap().toMutableMap()

    operator fun get(dishRockPosition: DishRockPosition): DishRockType = dishRockGridValueMap[dishRockPosition]!!

    operator fun set(dishRockPosition: DishRockPosition, dishRockType: DishRockType) {
        dishRockGridValueMap[dishRockPosition] = dishRockType
    }

    override fun getRockPositionOrNull(row: Int, column: Int): DishRockPosition? = try {
        dishRockGridMap[row]?.get(column)
    } catch (e: IndexOutOfBoundsException) {
        null
    }

    override fun getRockPosition(row: Int, column: Int): DishRockPosition =
        getRockPositionOrNull(row, column) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${DishRockPosition::class.simpleName} at the given location ($row, $column)"
        )

    override fun getAllRockPositions(): Collection<DishRockPosition> =
        dishRockGridMap.values.flatten()

    override fun getAllRollingRockPositions(): Collection<DishRockPosition> =
        getAllRockPositions().filter { dishRockPosition -> dishRockPosition.toType() == DishRockType.ROLLING_ROCK }

    override fun DishRockPosition.toType(): DishRockType = get(this)

    override fun DishRockPosition.getNeighbour(direction: Direction): DishRockPosition? = when (direction) {
        TOP -> getRockPositionOrNull(x - 1, y)
        BOTTOM -> getRockPositionOrNull(x + 1, y)
        RIGHT -> getRockPositionOrNull(x, y + 1)
        LEFT -> getRockPositionOrNull(x, y - 1)
    }

    override fun DishRockPosition.getAllNeighbours(): Collection<DishRockPosition> =
        Direction.entries.mapNotNull { direction: Direction -> getNeighbour(direction) }

    override fun DishRockPosition.getRockPositionsInDirection(direction: Direction): Sequence<DishRockPosition> =
        generateSequence(this) { dishRockPosition ->
            dishRockPosition.getNeighbour(direction)
        }.drop(1)

    override fun toString(): String = dishRockGridMap.mapValues { (_: Int, rowLocations: List<DishRockPosition>) ->
        rowLocations.joinToString("") { dishRockPosition -> dishRockPosition.toType().type.toString() }
    }.values.joinToString(separator = System.lineSeparator())
}

private class ReflectorDishRockAnalyzer private constructor(
    private val dishRockGrid: DishRockGrid
) : IDishRockGrid by dishRockGrid {

    constructor(input: List<String>) : this(
        dishRockGrid = DishRockGrid(input)
    )

    // List of directions in order, in which the platform will be titled for spin cycle operation
    private val spinCycleDirectionsOrder: List<Direction> = listOf(TOP, LEFT, BOTTOM, RIGHT)

    /**
     * Tilts rocks on the platform in the requested [direction].
     */
    private fun tiltRocks(direction: Direction) {
        getAllRollingRockPositions().let { rockPositions: Collection<DishRockPosition> ->
            when (direction) {
                BOTTOM -> rockPositions.sortedByDescending { it.x }
                RIGHT -> rockPositions.sortedByDescending { it.y }
                else -> rockPositions
            }
        }.forEach { currentDishRockPosition ->
            currentDishRockPosition.getRockPositionsInDirection(direction).takeWhile { dishRockPosition ->
                dishRockPosition.toType() == DishRockType.EMPTY_SPACE
            }.lastOrNull()?.let { newDishRockPosition ->
                dishRockGrid[newDishRockPosition] = DishRockType.ROLLING_ROCK
                dishRockGrid[currentDishRockPosition] = DishRockType.EMPTY_SPACE
            }
        }
    }

    /**
     * Returns the computed load on North support beams caused by the arrangement of rocks on the platform.
     */
    private fun computeLoadOnNorthSupportBeams(): Int =
        getAllRollingRockPositions().groupBy { dishRockPosition: DishRockPosition ->
            dishRockPosition.x
        }.mapValues { (rowIndex: Int, dishRockPositionsInRow: List<DishRockPosition>) ->
            dishRockPositionsInRow.count() * (dishRockGrid.rows - rowIndex)
        }.values.sum()

    /**
     * [Solution for Part-1 & Part-2]
     *
     * Returns the Total load on North support beams after tilting the platform in the requested directions
     * to roll the rounded rocks.
     *
     * @param spinCycleOn Set to true for Part-2 which tilts/spins the platform in all directions
     * in the order defined by [spinCycleDirectionsOrder].
     * @param spinCount Used for Part-2 along with [spinCycleOn], which represents the number of spin cycles to be
     * performed on the platform.
     */
    fun getTotalLoadOnNorthSupportBeams(spinCycleOn: Boolean = false, spinCount: Int = 0): Int {
        if (spinCycleOn) {
            // For Part-2, spin cycle is requested with the number of spin cycles to be performed

            // Map to save the rock pattern along with their cycle count
            // for detecting pattern that repeats after certain cycles
            val rockPatternCycleIndexMap = mutableMapOf<String, Int>()

            // For saving the repeat interval found for pattern occurring again after certain cycles
            var repeatingInterval = -1

            // Current cycle index
            var cycleIndex = 0

            // Repeat till requested spinCount or for the first occurrence of repeating pattern
            while (cycleIndex++ < spinCount) {
                // Spin platform in directions requested
                spinCycleDirectionsOrder.forEach(::tiltRocks)

                if (rockPatternCycleIndexMap.containsKey(dishRockGrid.toString())) {
                    // When same rock pattern is found, calculate the cyclic interval and bail out
                    repeatingInterval = cycleIndex - rockPatternCycleIndexMap[dishRockGrid.toString()]!!
                    break
                } else {
                    // When rock pattern is new, save it to the map along with their cycle index
                    rockPatternCycleIndexMap[dishRockGrid.toString()] = cycleIndex
                }
            }

            if (repeatingInterval > 0) {
                // When interval is found, repeat the spin operation on the platform for remaining cycles
                // till the pattern that would occur for the total [spinCount] appears again,
                // which is as given by the modulo interval
                repeat((spinCount - cycleIndex).rem(repeatingInterval)) {
                    spinCycleDirectionsOrder.forEach(::tiltRocks)
                }
            }
        } else {
            // For Part-1, just tilt the rocks towards North
            tiltRocks(TOP)
        }

        // Return the computed load on North support beams
        return computeLoadOnNorthSupportBeams()
    }

}