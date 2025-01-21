/**
 * Problem: Day4: Ceres Search
 * https://adventofcode.com/2024/day/4
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler
import utils.Constants.A_CAP_CHAR
import utils.Constants.EMPTY
import utils.Constants.X_CAP_CHAR
import utils.grid.IOmniLattice
import utils.grid.OmniDirection.*
import utils.grid.OmniLattice
import utils.grid.Point2d
import utils.grid.OmniDirection as Direction

private class Day4 : BaseProblemHandler() {

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
        WordSearcher.parse(input).getCountOfXMAS()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        WordSearcher.parse(input).getCountOfXShapedMAS()

}

fun main() {
    with(Day4()) {
        solveSample(1, false, 0, 18)
        solveActual(1, false, 0, 2549)
        solveSample(2, false, 0, 9)
        solveActual(2, false, 0, 2003)
    }
}

private class XmasLetterCell(x: Int, y: Int) : Point2d<Int>(x, y)

private class XmasLetterGrid(
    xmasPattern: List<String>
) : OmniLattice<XmasLetterCell, Char>(xmasPattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value of location's row
     * @param column [Int] value of location's column
     */
    override fun provideLocation(row: Int, column: Int): XmasLetterCell =
        XmasLetterCell(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar [Char] found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): Char = locationChar

}

private class WordSearcher private constructor(
    private val xmasLetterGrid: XmasLetterGrid
) : IOmniLattice<XmasLetterCell, Char> by xmasLetterGrid {

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
        letterCell.toValue() == X_CAP_CHAR
    }

    /**
     * Returns all [XmasLetterCell]s having character 'A'
     */
    private fun getAllACharCells(): Collection<XmasLetterCell> = getAllLocations().filter { letterCell ->
        letterCell.toValue() == A_CAP_CHAR
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
                    letterCell.toValue()
                }.joinToString(EMPTY)
            }.count { word: String ->
                word == XMAS
            }
        }

    /**
     * [Solution for Part-2]
     *
     * Returns the number of times "MAS" word appears in the shape of X in the word search puzzle.
     */
    fun getCountOfXShapedMAS(): Int =
        getAllACharCells().map { letterCell ->
            letterCell.getOrdinalNeighboursWithDirection().let { ordinalNeighbours: Map<Direction, XmasLetterCell> ->
                buildList {
                    add(
                        listOf(
                            ordinalNeighbours.getOrDefault(TOP_LEFT, null),
                            letterCell,
                            ordinalNeighbours.getOrDefault(BOTTOM_RIGHT, null)
                        )
                    )
                    add(
                        listOf(
                            ordinalNeighbours.getOrDefault(BOTTOM_LEFT, null),
                            letterCell,
                            ordinalNeighbours.getOrDefault(TOP_RIGHT, null)
                        )
                    )
                }.map { letterCellsList: List<XmasLetterCell?> ->
                    letterCellsList.filterNotNull().map { letterCell ->
                        letterCell.toValue()
                    }.joinToString(EMPTY)
                }.count { word: String ->
                    word == MAS || word == MAS_REVERSE
                }
            }
        }.count { countOfMAS: Int ->
            // "MAS" word should appear twice to form the X shape
            countOfMAS == 2
        }

}