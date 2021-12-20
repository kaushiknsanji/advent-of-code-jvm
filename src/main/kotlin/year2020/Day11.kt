/**
 * Problem: Day11: Seating System
 * https://adventofcode.com/2020/day/11
 *
 * @author Kaushik N Sanji
 */

package year2020

import base.BaseFileHandler
import utils.grid.OmniDirection.*
import utils.grid.OmniDirection as Direction

private class Day11 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)  // 37
    println("=====")
    solveActual(1)  // 2406
    println("=====")
    solveSample(2)  // 26
    println("=====")
    solveActual(2)  // 2149
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day11.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day11.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    SeatLayout.parse(input)
        .simulateSeatingForPart1()
        .getCountOfSeatsOccupied()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    SeatLayout.parse(input)
        .simulateSeatingForPart2()
        .getCountOfSeatsOccupied()
        .also { println(it) }
}

private class SeatCell(val x: Int, val y: Int)

private interface ISeatGrid {
    fun getSeatCellOrNull(row: Int, column: Int): SeatCell?
    fun getSeatCell(row: Int, column: Int): SeatCell
    fun getAllSeatCells(): Collection<SeatCell>
    fun SeatCell.getNeighbour(direction: Direction): SeatCell?
    fun SeatCell.getAllNeighbours(): Collection<SeatCell>
    fun SeatCell.getSeatCellsInDirection(direction: Direction): Sequence<SeatCell>
    fun SeatCell.getSeatCellsInAllDirections(): Map<Direction, Sequence<SeatCell>>
    fun SeatCell.noneNeighbour(predicate: (seatCell: SeatCell) -> Boolean): Boolean
    fun SeatCell.countNeighbour(predicate: (seatCell: SeatCell) -> Boolean): Int
    fun SeatCell.noneNearbyVisibleSeatCellsInAllDirections(predicate: (seatCell: SeatCell) -> Boolean): Boolean
    fun SeatCell.countNearbyVisibleSeatCellsInAllDirections(predicate: (seatCell: SeatCell) -> Boolean): Int
    fun SeatCell.isOccupied(): Boolean
    fun SeatCell.isVacant(): Boolean
    fun isSameSeating(other: Map<SeatCell, Char>?): Boolean
    fun forEach(action: (Map.Entry<SeatCell, Char>) -> Unit)
    fun Map<SeatCell, Char>.toSeatPattern(): String
}

private class SeatGrid private constructor(rows: Int, columns: Int, seatPatternList: List<String>) : ISeatGrid {

    constructor(seatPatternList: List<String>) : this(
        seatPatternList.size,
        seatPatternList[0].length,
        seatPatternList
    )

    private val seatGridMap: Map<Int, List<SeatCell>> = (0 until rows).flatMap { x: Int ->
        (0 until columns).map { y: Int ->
            SeatCell(x, y)
        }
    }.groupBy { seatCell: SeatCell -> seatCell.x }

    private val seatGridValueMap: MutableMap<SeatCell, Char> =
        seatPatternList.flatMapIndexed { x: Int, rowSeatsPattern: String ->
            rowSeatsPattern.mapIndexed { y: Int, seat: Char ->
                getSeatCell(x, y) to seat
            }
        }.toMap(mutableMapOf())

    override fun getSeatCellOrNull(row: Int, column: Int): SeatCell? = try {
        seatGridMap[row]?.get(column)
    } catch (e: IndexOutOfBoundsException) {
        null
    }

    override fun getSeatCell(row: Int, column: Int): SeatCell =
        getSeatCellOrNull(row, column) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${SeatCell::class.simpleName} at the given location ($row, $column)"
        )

    override fun getAllSeatCells(): Collection<SeatCell> = seatGridMap.values.flatten()

    operator fun get(seatCell: SeatCell): Char = seatGridValueMap[seatCell] ?: SeatLayout.WALL

    operator fun set(seatCell: SeatCell, value: Char) {
        seatGridValueMap[seatCell] = value
    }

    override fun SeatCell.getNeighbour(direction: Direction): SeatCell? = when (direction) {
        TOP -> getSeatCellOrNull(this.x - 1, this.y)
        BOTTOM -> getSeatCellOrNull(this.x + 1, this.y)
        RIGHT -> getSeatCellOrNull(this.x, this.y + 1)
        LEFT -> getSeatCellOrNull(this.x, this.y - 1)
        TOP_LEFT -> getSeatCellOrNull(this.x - 1, this.y - 1)
        TOP_RIGHT -> getSeatCellOrNull(this.x - 1, this.y + 1)
        BOTTOM_LEFT -> getSeatCellOrNull(this.x + 1, this.y - 1)
        BOTTOM_RIGHT -> getSeatCellOrNull(this.x + 1, this.y + 1)
    }

    override fun SeatCell.getAllNeighbours(): Collection<SeatCell> =
        Direction.values().mapNotNull { direction: Direction -> getNeighbour(direction) }

    override fun SeatCell.getSeatCellsInDirection(direction: Direction): Sequence<SeatCell> =
        generateSequence(this) { previousSeatCell ->
            previousSeatCell.getNeighbour(direction)
        }.drop(1)

    override fun SeatCell.getSeatCellsInAllDirections(): Map<Direction, Sequence<SeatCell>> =
        Direction.values().associateWith { direction -> this.getSeatCellsInDirection(direction) }

    override fun SeatCell.noneNeighbour(predicate: (seatCell: SeatCell) -> Boolean): Boolean =
        getAllNeighbours().none(predicate)

    override fun SeatCell.countNeighbour(predicate: (seatCell: SeatCell) -> Boolean): Int =
        getAllNeighbours().count(predicate)

    override fun SeatCell.noneNearbyVisibleSeatCellsInAllDirections(predicate: (seatCell: SeatCell) -> Boolean): Boolean =
        getSeatCellsInAllDirections().none { (_: Direction, seatCells: Sequence<SeatCell>) ->
            seatCells.indexOfFirst(predicate).takeUnless { it == -1 }?.let { index ->
                val value = get(seatCells.elementAt(index))
                val blockingSeatIndex = seatCells.indexOfFirst { get(it) != value && get(it) != SeatLayout.FLOOR }
                !(blockingSeatIndex > -1 && blockingSeatIndex < index)
            } ?: false
        }

    override fun SeatCell.countNearbyVisibleSeatCellsInAllDirections(predicate: (seatCell: SeatCell) -> Boolean): Int =
        getSeatCellsInAllDirections().count { (_: Direction, seatCells: Sequence<SeatCell>) ->
            seatCells.indexOfFirst(predicate).takeUnless { it == -1 }?.let { index ->
                val value = get(seatCells.elementAt(index))
                val blockingSeatIndex = seatCells.indexOfFirst { get(it) != value && get(it) != SeatLayout.FLOOR }
                !(blockingSeatIndex > -1 && blockingSeatIndex < index)
            } ?: false
        }

    override fun SeatCell.isOccupied(): Boolean = get(this) == SeatLayout.SEAT_OCCUPIED

    override fun SeatCell.isVacant(): Boolean = get(this) == SeatLayout.SEAT_VACANT

    override fun isSameSeating(other: Map<SeatCell, Char>?): Boolean = seatGridValueMap == other

    override fun forEach(action: (Map.Entry<SeatCell, Char>) -> Unit) = seatGridValueMap.entries.forEach(action)

    override fun Map<SeatCell, Char>.toSeatPattern(): String =
        this.entries.groupBy { (seatCell: SeatCell, _: Char) -> seatCell.x }
            .map { (_, list) -> list.map { it.value }.joinToString(separator = "") }
            .joinToString(separator = System.lineSeparator())

    override fun toString(): String = seatGridValueMap.toSeatPattern()
}

private class SeatLayout private constructor(
    val seatGrid: SeatGrid
) : ISeatGrid by seatGrid {

    companion object {
        const val FLOOR = '.'
        const val SEAT_VACANT = 'L'
        const val SEAT_OCCUPIED = '#'
        const val WALL = '|'

        const val MAX_EMPTY_NEIGHBOURS = 4
        const val MAX_EMPTY_NEARBY = 5

        fun parse(input: List<String>): SeatLayout = SeatLayout(SeatGrid(input))
    }

    fun getCountOfSeatsOccupied(): Int = getAllSeatCells().count { seatCell: SeatCell -> seatCell.isOccupied() }

    fun simulateSeatingForPart1(): SeatLayout = this.apply {
        var nextSeatingMap: Map<SeatCell, Char>? = null

        while (!seatGrid.isSameSeating(nextSeatingMap)) {
            // Save previous seating state in the seat grid
            nextSeatingMap?.forEach { (seatCell: SeatCell, value: Char) -> seatGrid[seatCell] = value }
            // Get the next seating state
            nextSeatingMap = changeSeatStateForPart1()
        }
    }

    fun changeSeatStateForPart1(): Map<SeatCell, Char> = mutableMapOf<SeatCell, Char>().apply {
        seatGrid.forEach { (seatCell: SeatCell, value: Char) ->
            if (seatCell.isVacant() && seatCell.noneNeighbour { it.isOccupied() }) {
                this[seatCell] = SEAT_OCCUPIED
            } else if (seatCell.isOccupied() && seatCell.countNeighbour { it.isOccupied() } >= MAX_EMPTY_NEIGHBOURS) {
                this[seatCell] = SEAT_VACANT
            } else {
                this[seatCell] = value
            }
        }
    }

    fun simulateSeatingForPart2(): SeatLayout = this.apply {
        var nextSeatingMap: Map<SeatCell, Char>? = null

        while (!seatGrid.isSameSeating(nextSeatingMap)) {
            // Save previous seating state in the seat grid
            nextSeatingMap?.forEach { (seatCell: SeatCell, value: Char) -> seatGrid[seatCell] = value }
            // Get the next seating state
            nextSeatingMap = changeSeatStateForPart2()
        }
    }

    fun changeSeatStateForPart2(): Map<SeatCell, Char> = mutableMapOf<SeatCell, Char>().apply {
        seatGrid.forEach { (seatCell: SeatCell, value: Char) ->
            if (seatCell.isVacant() && seatCell.noneNearbyVisibleSeatCellsInAllDirections { it.isOccupied() }) {
                this[seatCell] = SEAT_OCCUPIED
            } else if (seatCell.isOccupied() && seatCell.countNearbyVisibleSeatCellsInAllDirections { it.isOccupied() } >= MAX_EMPTY_NEARBY) {
                this[seatCell] = SEAT_VACANT
            } else {
                this[seatCell] = value
            }
        }
    }

}