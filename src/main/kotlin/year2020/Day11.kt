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
    solveSample(1)
    println("=====")
    solveActual(1)
    println("=====")
    solveSample(2)
    println("=====")
    solveActual(2)
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
    SeatLayout(input)
        .simulateSeatingForPart1()
        .getCountOfSeatsOccupied()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    SeatLayout(input)
        .simulateSeatingForPart2()
        .getCountOfSeatsOccupied()
        .also { println(it) }
}

private data class SeatCell(val x: Int, val y: Int)

private interface ISeatGrid {
    fun getSeatCellOrNull(row: Int, column: Int): SeatCell?
    fun getSeatCell(row: Int, column: Int): SeatCell
    fun getAllSeatCells(): Collection<SeatCell>
    fun SeatCell.getNeighbour(direction: Direction): SeatCell?
    fun SeatCell.getAllNeighbours(): Collection<SeatCell?>
    fun SeatCell.getSeatCellsInDirection(direction: Direction): Collection<SeatCell>
    fun SeatCell.getSeatCellsInAllDirections(): Map<Direction, Collection<SeatCell>>
}

private class SeatGrid(val rows: Int, val columns: Int) : ISeatGrid {
    private val seatGridMap: Map<Int, List<SeatCell>> = mutableListOf<SeatCell>().apply {
        (0 until rows).forEach { x: Int ->
            (0 until columns).forEach { y: Int ->
                add(SeatCell(x, y))
            }
        }
    }
        .groupBy { seatCell: SeatCell ->
            seatCell.x
        }

    val xMax: Int get() = seatGridMap.size
    val yMax: Int get() = seatGridMap[0]?.size ?: 0

    override fun getSeatCellOrNull(row: Int, column: Int): SeatCell? = try {
        seatGridMap[row]?.get(column)
    } catch (e: IndexOutOfBoundsException) {
        null
    }

    override fun getSeatCell(row: Int, column: Int): SeatCell =
        getSeatCellOrNull(row, column) ?: throw IllegalArgumentException(
            "${this.javaClass.simpleName} does not have a ${SeatCell::class.simpleName} at the given location ($row, $column)"
        )

    override fun getAllSeatCells(): Collection<SeatCell> = seatGridMap.values.flatten()

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

    override fun SeatCell.getAllNeighbours(): Collection<SeatCell?> =
        Direction.values().map { direction: Direction -> getNeighbour(direction) }

    override fun SeatCell.getSeatCellsInDirection(direction: Direction): Collection<SeatCell> =
        mutableListOf<SeatCell>().apply {
            val immediateNeighbour = this@getSeatCellsInDirection.getNeighbour(direction)
            if (immediateNeighbour != null) {
                var (x: Int, y: Int) = immediateNeighbour

                when (direction) {
                    TOP -> while (x >= 0) {
                        add(getSeatCell(x--, y))
                    }
                    BOTTOM -> while (x < xMax) {
                        add(getSeatCell(x++, y))
                    }
                    RIGHT -> while (y < yMax) {
                        add(getSeatCell(x, y++))
                    }
                    LEFT -> while (y >= 0) {
                        add(getSeatCell(x, y--))
                    }
                    TOP_LEFT -> while (x >= 0 && y >= 0) {
                        add(getSeatCell(x--, y--))
                    }
                    TOP_RIGHT -> while (x >= 0 && y < yMax) {
                        add(getSeatCell(x--, y++))
                    }
                    BOTTOM_LEFT -> while (x < xMax && y >= 0) {
                        add(getSeatCell(x++, y--))
                    }
                    BOTTOM_RIGHT -> while (x < xMax && y < yMax) {
                        add(getSeatCell(x++, y++))
                    }
                }
            }

        }

    override fun SeatCell.getSeatCellsInAllDirections(): Map<Direction, Collection<SeatCell>> =
        Direction.values().associateWith { direction -> this.getSeatCellsInDirection(direction) }

}

private class SeatLayout(
    val allRowsSeatPattern: List<String>
) : ISeatGrid by SeatGrid(allRowsSeatPattern.size, allRowsSeatPattern[0].length) {

    companion object {
        const val FLOOR = '.'
        const val SEAT_VACANT = 'L'
        const val SEAT_OCCUPIED = '#'
        const val WALL = '|'

        const val MAX_EMPTY_NEIGHBOURS = 4
        const val MAX_EMPTY_NEARBY = 5
    }

    private val seatGridValueMap: MutableMap<SeatCell, Char> = mutableMapOf<SeatCell, Char>().apply {
        allRowsSeatPattern.forEachIndexed { rowIndex, rowSeatsPattern ->
            rowSeatsPattern.forEachIndexed { columnIndex, seat: Char ->
                this[getSeatCell(rowIndex, columnIndex)] = seat
            }
        }
    }

    operator fun get(seatCell: SeatCell?): Char = seatCell?.let { cell: SeatCell -> seatGridValueMap[cell]!! } ?: WALL

    operator fun set(seatCell: SeatCell, value: Char) {
        seatGridValueMap[seatCell] = value
    }

    fun SeatCell.noneNeighbour(predicate: (seatCell: SeatCell) -> Boolean): Boolean =
        getAllNeighbours().filterNotNull().none(predicate)

    fun SeatCell.countNeighbour(predicate: (seatCell: SeatCell) -> Boolean): Int =
        getAllNeighbours().filterNotNull().count(predicate)

    fun SeatCell.noneNearbyVisibleSeatCellsInAllDirections(predicate: (seatCell: SeatCell) -> Boolean): Boolean =
        getSeatCellsInAllDirections().none { (_: Direction, seatCells: Collection<SeatCell>) ->
            seatCells.indexOfFirst(predicate).takeUnless { it == -1 }?.let { index ->
                val value = get(seatCells.toList()[index])
                val blockingSeatIndex = seatCells.indexOfFirst { get(it) != value && get(it) != FLOOR }
                !(blockingSeatIndex > -1 && blockingSeatIndex < index)
            } ?: false
        }

    fun SeatCell.countNearbyVisibleSeatCellsInAllDirections(predicate: (seatCell: SeatCell) -> Boolean): Int =
        getSeatCellsInAllDirections().count { (_: Direction, seatCells: Collection<SeatCell>) ->
            seatCells.indexOfFirst(predicate).takeUnless { it == -1 }?.let { index ->
                val value = get(seatCells.toList()[index])
                val blockingSeatIndex = seatCells.indexOfFirst { get(it) != value && get(it) != FLOOR }
                !(blockingSeatIndex > -1 && blockingSeatIndex < index)
            } ?: false
        }

    fun SeatCell.isOccupied(): Boolean = get(this) == SEAT_OCCUPIED

    fun SeatCell.isVacant(): Boolean = get(this) == SEAT_VACANT

    fun getCountOfSeatsOccupied(): Int = getAllSeatCells().count { seatCell: SeatCell -> seatCell.isOccupied() }

    fun simulateSeatingForPart1(): SeatLayout {
        var nextSeatingMap: Map<SeatCell, Char>? = null

        while (seatGridValueMap != nextSeatingMap) {
            nextSeatingMap?.forEach { (seatCell: SeatCell, value: Char) -> this[seatCell] = value }
            nextSeatingMap = changeSeatStateForPart1()
        }

        return this
    }

    fun changeSeatStateForPart1(): Map<SeatCell, Char> = mutableMapOf<SeatCell, Char>().apply {
        seatGridValueMap.forEach { (seatCell: SeatCell, value: Char) ->
            if (seatCell.isVacant() && seatCell.noneNeighbour { it.isOccupied() }) {
                this[seatCell] = SEAT_OCCUPIED
            } else if (seatCell.isOccupied() && seatCell.countNeighbour { it.isOccupied() } >= MAX_EMPTY_NEIGHBOURS) {
                this[seatCell] = SEAT_VACANT
            } else {
                this[seatCell] = value
            }
        }
    }

    fun simulateSeatingForPart2(): SeatLayout {
        var nextSeatingMap: Map<SeatCell, Char>? = null

        while (seatGridValueMap != nextSeatingMap) {
            nextSeatingMap?.forEach { (seatCell: SeatCell, value: Char) -> this[seatCell] = value }
            nextSeatingMap = changeSeatStateForPart2()
        }

        return this
    }

    fun changeSeatStateForPart2(): Map<SeatCell, Char> = mutableMapOf<SeatCell, Char>().apply {
        seatGridValueMap.forEach { (seatCell: SeatCell, value: Char) ->
            if (seatCell.isVacant() && seatCell.noneNearbyVisibleSeatCellsInAllDirections { it.isOccupied() }) {
                this[seatCell] = SEAT_OCCUPIED
            } else if (seatCell.isOccupied() && seatCell.countNearbyVisibleSeatCellsInAllDirections { it.isOccupied() } >= MAX_EMPTY_NEARBY) {
                this[seatCell] = SEAT_VACANT
            } else {
                this[seatCell] = value
            }
        }
    }

    fun Map<SeatCell, Char>.toSeatPattern(): String =
        this.entries.groupBy { (seatCell: SeatCell, _: Char) -> seatCell.x }
            .map { (_, list) -> list.map { it.value }.joinToString(separator = "") }
            .joinToString(separator = System.lineSeparator())

}