/**
 * Problem: Day4: Ceres Search
 * https://adventofcode.com/2024/day/4
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import utils.grid.DiagonalLattice
import utils.grid.IDiagonalLattice
import utils.grid.OmniDirection.*
import utils.grid.Point2d
import utils.grid.OmniDirection as Direction

private class Day4 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 18
    println("=====")
    solveActual(1)      // 2549
    println("=====")
    solveSample(2)      // 9
    println("=====")
    solveActual(2)      // 2003
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day4.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day4.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    WordSearcher.parse(input)
        .getCountOfXMAS()
        .also(::println)
}

private fun doPart2(input: List<String>) {
    WordSearcher.parse(input)
        .getCountOfXShapedMAS()
        .also(::println)
}

private class XmasLetterCell(x: Int, y: Int) : Point2d<Int>(x, y)

private class XmasLetterGrid(
    xmasPattern: List<String>
) : DiagonalLattice<XmasLetterCell, Char>(xmasPattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value location's row
     * @param column [Int] value location's column
     */
    override fun provideLocation(row: Int, column: Int): XmasLetterCell =
        XmasLetterCell(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar Char found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): Char = locationChar

}

private class WordSearcher private constructor(
    private val xmasLetterGrid: XmasLetterGrid
) : IDiagonalLattice<XmasLetterCell, Char> by xmasLetterGrid {

    companion object {
        private const val XMAS = "XMAS"
        private const val MAS = "MAS"
        private const val MAS_REVERSE = "SAM"

        fun parse(input: List<String>): WordSearcher = WordSearcher(
            XmasLetterGrid(input)
        )
    }

    /**
     * Returns all [XmasLetterCell]s having character 'X'
     */
    private fun getAllXCharCells(): Collection<XmasLetterCell> = getAllLocations().filter { letterCell ->
        xmasLetterGrid[letterCell] == 'X'
    }

    /**
     * Returns all [XmasLetterCell]s having character 'A'
     */
    private fun getAllACharCells(): Collection<XmasLetterCell> = getAllLocations().filter { letterCell ->
        xmasLetterGrid[letterCell] == 'A'
    }

    /**
     * Returns a [Map] of Diagonal neighbours of [this], with Keys as their [Direction]
     */
    private fun XmasLetterCell.getDiagonalNeighbours(): Map<Direction, XmasLetterCell?> =
        Direction.entries.filter { direction: Direction ->
            direction in listOf(TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT, TOP_RIGHT)
        }.associateWith { direction: Direction ->
            getNeighbour(direction)
        }

    /**
     * [Solution for Part-1]
     *
     * Returns the number of times "XMAS" word appears in the word search puzzle.
     */
    fun getCountOfXMAS(): Int =
        getAllXCharCells().sumOf { letterCell ->
            letterCell.getLocationsInAllDirections().values.map { letterCellsSequence: Sequence<XmasLetterCell> ->
                letterCellsSequence.take(4).map { letterCell ->
                    xmasLetterGrid[letterCell]
                }.joinToString("")
            }.count { word: String ->
                word == XMAS
            }
        }

    /**
     * [Solution for Part-2]
     *
     * Returns the number of times "MAS" word appears in the shape of X in the word search puzzle.
     */
    fun getCountOfXShapedMAS() =
        getAllACharCells().map { letterCell ->
            letterCell.getDiagonalNeighbours().let { diagonalNeighbours: Map<Direction, XmasLetterCell?> ->
                buildList {
                    add(listOf(diagonalNeighbours[TOP_LEFT], letterCell, diagonalNeighbours[BOTTOM_RIGHT]))
                    add(listOf(diagonalNeighbours[BOTTOM_LEFT], letterCell, diagonalNeighbours[TOP_RIGHT]))
                }.map { letterCellsList: List<XmasLetterCell?> ->
                    letterCellsList.filterNotNull().map { letterCell ->
                        xmasLetterGrid[letterCell]
                    }.joinToString("")
                }.count { word: String ->
                    word == MAS || word == MAS_REVERSE
                }
            }
        }.count { countOfMAS: Int ->
            // "MAS" word should appear twice to form the X shape
            countOfMAS == 2
        }

}