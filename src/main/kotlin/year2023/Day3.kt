/**
 * Problem: Day3: Gear Ratios
 * https://adventofcode.com/2023/day/3
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import utils.grid.OmniDirection.*
import utils.product
import utils.grid.OmniDirection as Direction

private class Day3 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 4361
    println("=====")
    solveActual(1)      // 539637
    println("=====")
    solveSample(2)      // 467835
    println("=====")
    solveActual(2)      // 82818007
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day3.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day3.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    EnginePartSchemaAnalyzer.parse(input).getPartNumbersTotal().also { println(it) }
}

private fun doPart2(input: List<String>) {
    EnginePartSchemaAnalyzer.parse(input).getGearRatiosTotal().also { println(it) }
}

private class EnginePartCell(val x: Int, val y: Int)

private interface IEnginePartSchemaGrid {
    fun getPartOrNull(row: Int, column: Int): EnginePartCell?
    fun getPart(row: Int, column: Int): EnginePartCell
    fun getAllParts(): Collection<EnginePartCell>
    fun EnginePartCell.getNeighbour(direction: Direction): EnginePartCell?
    fun EnginePartCell.getAllNeighbours(): Collection<EnginePartCell>
    fun EnginePartCell.getPartsInDirection(direction: Direction): Sequence<EnginePartCell>
    fun EnginePartCell.getPartsInAllDirections(): Map<Direction, Sequence<EnginePartCell>>
    fun EnginePartCell.isDigit(): Boolean
    fun EnginePartCell.isNotSymbol(): Boolean
    fun EnginePartCell.isGearSymbol(): Boolean
    fun Map<EnginePartCell, Char>.toSchemaPattern(): String
}

private class EnginePartSchemaGrid private constructor(
    rows: Int, columns: Int, enginePartsSchemaPattern: List<String>
) : IEnginePartSchemaGrid {

    constructor(enginePartsSchemaPattern: List<String>) : this(
        enginePartsSchemaPattern.size,
        enginePartsSchemaPattern[0].length,
        enginePartsSchemaPattern
    )

    private val schemaGridMap: Map<Int, List<EnginePartCell>> = (0 until rows).flatMap { x ->
        (0 until columns).map { y ->
            EnginePartCell(x, y)
        }
    }.groupBy { enginePartCell: EnginePartCell -> enginePartCell.x }

    private val schemaGridValueMap: Map<EnginePartCell, Char> =
        enginePartsSchemaPattern.flatMapIndexed { x: Int, partsPattern: String ->
            partsPattern.mapIndexed { y: Int, part: Char ->
                getPart(x, y) to part
            }
        }.toMap()

    override fun getPartOrNull(row: Int, column: Int): EnginePartCell? = try {
        schemaGridMap[row]?.get(column)
    } catch (e: IndexOutOfBoundsException) {
        null
    }

    override fun getPart(row: Int, column: Int): EnginePartCell =
        getPartOrNull(row, column) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${EnginePartCell::class.simpleName} at the given location ($row, $column)"
        )

    override fun getAllParts(): Collection<EnginePartCell> = schemaGridMap.values.flatten()

    operator fun get(enginePartCell: EnginePartCell): Char =
        schemaGridValueMap[enginePartCell] ?: EnginePartSchemaAnalyzer.NOT_SYMBOL

    override fun EnginePartCell.getNeighbour(direction: Direction): EnginePartCell? = when (direction) {
        TOP -> getPartOrNull(x - 1, y)
        BOTTOM -> getPartOrNull(x + 1, y)
        RIGHT -> getPartOrNull(x, y + 1)
        LEFT -> getPartOrNull(x, y - 1)
        TOP_LEFT -> getPartOrNull(x - 1, y - 1)
        TOP_RIGHT -> getPartOrNull(x - 1, y + 1)
        BOTTOM_LEFT -> getPartOrNull(x + 1, y - 1)
        BOTTOM_RIGHT -> getPartOrNull(x + 1, y + 1)
    }

    override fun EnginePartCell.getAllNeighbours(): Collection<EnginePartCell> =
        Direction.entries.mapNotNull { direction: Direction -> this.getNeighbour(direction) }

    override fun EnginePartCell.getPartsInDirection(direction: Direction): Sequence<EnginePartCell> =
        generateSequence(this) { previousEnginePartCell ->
            previousEnginePartCell.getNeighbour(direction)
        }.drop(1)

    override fun EnginePartCell.getPartsInAllDirections(): Map<Direction, Sequence<EnginePartCell>> =
        Direction.entries.associateWith { direction: Direction -> this.getPartsInDirection(direction) }

    override fun EnginePartCell.isDigit(): Boolean = get(this).isDigit()

    override fun EnginePartCell.isNotSymbol(): Boolean = get(this) == EnginePartSchemaAnalyzer.NOT_SYMBOL

    override fun EnginePartCell.isGearSymbol(): Boolean = get(this) == EnginePartSchemaAnalyzer.GEAR_SYMBOL

    override fun Map<EnginePartCell, Char>.toSchemaPattern(): String =
        entries.groupBy { (enginePartCell: EnginePartCell, _: Char) ->
            enginePartCell.x
        }.map { (_, rowPatternsList: List<Map.Entry<EnginePartCell, Char>>) ->
            rowPatternsList.map { it.value }.joinToString("")
        }.joinToString(separator = System.lineSeparator())

}

private class EnginePartSchemaAnalyzer private constructor(
    private val enginePartSchemaGrid: EnginePartSchemaGrid
) : IEnginePartSchemaGrid by enginePartSchemaGrid {

    companion object {
        const val NOT_SYMBOL = '.'
        const val GEAR_SYMBOL = '*'

        fun parse(enginePartsSchemaPattern: List<String>): EnginePartSchemaAnalyzer =
            EnginePartSchemaAnalyzer(EnginePartSchemaGrid(enginePartsSchemaPattern))
    }

    private fun getAllSymbolCells(): Collection<EnginePartCell> = getAllParts().filterNot { enginePartCell ->
        enginePartCell.isDigit() || enginePartCell.isNotSymbol()
    }

    private fun getAllGearSymbols(): Collection<EnginePartCell> = getAllParts().filter { enginePartCell ->
        enginePartCell.isGearSymbol()
    }

    private fun EnginePartCell.getPartNumberNeighbours(): Collection<EnginePartCell> =
        this.getAllNeighbours().filter { enginePartCell -> enginePartCell.isDigit() }

    private fun EnginePartCell.getPartNumberHorizontalNeighbours(): Sequence<EnginePartCell> =
        this.getPartsInDirection(LEFT).takeWhile { it.isDigit() }.sortedBy { it.y } +
                this + this.getPartsInDirection(RIGHT).takeWhile { it.isDigit() }.sortedBy { it.y }

    private fun Sequence<EnginePartCell>.toPartNumber(): Int = this.map { partNumberCell ->
        enginePartSchemaGrid[partNumberCell]
    }.joinToString("").toInt()

    private fun EnginePartCell.isActualGear(): Boolean = this.getPartNumberNeighbours().groupBy { enginePartCell ->
        enginePartCell.x
    }.let { rowToPartNumberNeighbourCellsMap: Map<Int, List<EnginePartCell>> ->
        rowToPartNumberNeighbourCellsMap.keys.singleOrNull()?.let { key ->
            rowToPartNumberNeighbourCellsMap[key]?.let { partNumberNeighbours: List<EnginePartCell> ->
                partNumberNeighbours.size == 2 && partNumberNeighbours.none { partNumberNeighbour: EnginePartCell ->
                    partNumberNeighbour.y == this.y
                }
            }
        } ?: run {
            rowToPartNumberNeighbourCellsMap.keys.distinct().count() == 2
        }
    }

    /**
     * [Solution for Part-1]
     * Returns the sum of all Part numbers found in the engine schema
     */
    fun getPartNumbersTotal(): Int =
        getAllSymbolCells().flatMap { symbolCell ->
            symbolCell.getPartNumberNeighbours()
        }.map { partNumberNeighbour ->
            partNumberNeighbour.getPartNumberHorizontalNeighbours()
        }.groupBy { partNumberNeighbourSequence: Sequence<EnginePartCell> ->
            partNumberNeighbourSequence.first().x
        }.mapValues { (_: Int, groupedPartNumberNeighboursList: List<Sequence<EnginePartCell>>) ->
            groupedPartNumberNeighboursList
                .distinctBy { partNumberCellSequence: Sequence<EnginePartCell> ->
                    partNumberCellSequence.toList()
                }.map { partNumberCellSequence: Sequence<EnginePartCell> ->
                    partNumberCellSequence.toPartNumber()
                }
        }.values.flatten().sum()

    /**
     * [Solution for Part-2]
     * Returns the sum of all Gear ratios found in the engine schema
     */
    fun getGearRatiosTotal(): Int =
        getAllGearSymbols().filter { gearSymbolCell ->
            gearSymbolCell.isActualGear()
        }.associateWith { actualGearSymbolCell ->
            actualGearSymbolCell.getPartNumberNeighbours()
        }.mapValues { (_: EnginePartCell, partNumberNeighbours: Collection<EnginePartCell>) ->
            partNumberNeighbours.map { partNumberNeighbour ->
                partNumberNeighbour.getPartNumberHorizontalNeighbours()
            }.distinctBy { partNumberCellSequence: Sequence<EnginePartCell> ->
                partNumberCellSequence.toList()
            }.map { partNumberCellSequence: Sequence<EnginePartCell> ->
                partNumberCellSequence.toPartNumber()
            }.product()
        }.values.sum()

}