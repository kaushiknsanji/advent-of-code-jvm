/**
 * Problem: Day4: Printing Department
 * https://adventofcode.com/2025/day/4
 *
 * @author <a href='mailto:kaushiknsanji@gmail.com'>Kaushik N Sanji</a>
 */

package year2025

import base.BaseProblemHandler
import utils.Constants.AT_CHAR
import utils.Constants.DOT_CHAR
import utils.grid.IOmniLattice
import utils.grid.OmniLattice
import utils.grid.Point2D

class Day4 : BaseProblemHandler() {

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
        PrintDeptLayoutAnalyzer.parse(input)
            .getCountOfAccessiblePaperRollsByForklifts(otherArgs[0] as Int)

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        PrintDeptLayoutAnalyzer.parse(input)
            .getCountOfPaperRollsRemoved(otherArgs[0] as Int)

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, 13, 4)
        solveActual(1, false, 0, 1540, 4)
        solveSample(2, false, 0, 43, 4)
        solveActual(2, false, 0, 8972, 4)
    }

}

fun main() {
    try {
        Day4().start()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

private enum class PrintDeptLayoutType(val type: Char) {
    PAPER_ROLL(AT_CHAR),
    EMPTY_SPACE(DOT_CHAR);

    companion object {
        private val typeMap = entries.associateBy(PrintDeptLayoutType::type)

        fun fromType(type: Char): PrintDeptLayoutType = typeMap[type]!!
    }
}

private class PrintDeptLayoutLocation(x: Int, y: Int) : Point2D<Int>(x, y)

private class PrintDeptLayoutGrid(
    pattern: List<String>
) : OmniLattice<PrintDeptLayoutLocation, PrintDeptLayoutType>(pattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value of location's row
     * @param column [Int] value of location's column
     */
    override fun provideLocation(row: Int, column: Int): PrintDeptLayoutLocation =
        PrintDeptLayoutLocation(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar [Char] found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): PrintDeptLayoutType =
        PrintDeptLayoutType.fromType(locationChar)

}

private class PrintDeptLayoutAnalyzer private constructor(
    private val printDeptLayoutGrid: PrintDeptLayoutGrid
) : IOmniLattice<PrintDeptLayoutLocation, PrintDeptLayoutType> by printDeptLayoutGrid {

    companion object {

        fun parse(input: List<String>): PrintDeptLayoutAnalyzer = PrintDeptLayoutAnalyzer(
            printDeptLayoutGrid = PrintDeptLayoutGrid(input)
        )
    }

    /**
     * [Solution for Part-1]
     *
     * Returns the total number of Paper Rolls that can be accessed by Forklifts.
     *
     * @param minBlockingAdjacentPaperRolls [Int] number of minimum neighbouring Paper rolls of the
     * current Paper roll that blocks access to the current Paper roll by Forklifts.
     */
    fun getCountOfAccessiblePaperRollsByForklifts(minBlockingAdjacentPaperRolls: Int): Int =
        getAllLocations().filter { location ->
            // Select locations with Paper rolls
            location.toValue() == PrintDeptLayoutType.PAPER_ROLL
        }.count { paperRollLocation ->
            // Return count of Paper rolls which can be accessed when the neighbouring locations
            // have less than the given number of Paper rolls that can otherwise block access
            // to the current Paper roll
            paperRollLocation.getAllNeighbours().count { adjacentLocation: PrintDeptLayoutLocation ->
                adjacentLocation.toValue() == PrintDeptLayoutType.PAPER_ROLL
            } < minBlockingAdjacentPaperRolls
        }

    /**
     * Returns the next [List] of [Paper roll locations][PrintDeptLayoutLocation] that can be accessed by Forklifts.
     *
     * @param minBlockingAdjacentPaperRolls [Int] number of minimum neighbouring Paper rolls of the
     * current Paper roll that blocks access to the current Paper roll by Forklifts.
     */
    private fun getNextAccessiblePaperRollLocations(
        minBlockingAdjacentPaperRolls: Int
    ): List<PrintDeptLayoutLocation> = getAllLocations().filter { location ->
        // Select a Paper roll location whose neighbours have Paper rolls amounting to less than those
        // that blocks access to this Paper roll
        location.toValue() == PrintDeptLayoutType.PAPER_ROLL &&
                location.getAllNeighbours().count { adjacentLocation: PrintDeptLayoutLocation ->
                    adjacentLocation.toValue() == PrintDeptLayoutType.PAPER_ROLL
                } < minBlockingAdjacentPaperRolls
    }

    /**
     * [Solution for Part-2]
     *
     * Returns total number of Paper rolls that can be removed by Forklifts in stages.
     *
     * @param minBlockingAdjacentPaperRolls [Int] number of minimum neighbouring Paper rolls of the
     * current Paper roll that blocks access to the current Paper roll by Forklifts.
     */
    fun getCountOfPaperRollsRemoved(minBlockingAdjacentPaperRolls: Int): Int {
        // Get the initial count of Paper rolls present
        val initialCountOfPaperRolls: Int = getAllLocations().count { location ->
            location.toValue() == PrintDeptLayoutType.PAPER_ROLL
        }

        // Get the first list of Paper roll locations that can be accessed
        var nextAccessiblePaperRollLocations = getNextAccessiblePaperRollLocations(minBlockingAdjacentPaperRolls)

        do {
            // Update every accessible Paper roll location data to Empty space,
            // to indicate that the Paper roll has been removed
            nextAccessiblePaperRollLocations.forEach { paperRollLocation ->
                printDeptLayoutGrid[paperRollLocation] = PrintDeptLayoutType.EMPTY_SPACE
            }

            // Read the next list of Paper roll locations that can be accessed
            // and repeat till no remaining paper roll locations can be accessed
            nextAccessiblePaperRollLocations = getNextAccessiblePaperRollLocations(minBlockingAdjacentPaperRolls)
        } while (nextAccessiblePaperRollLocations.isNotEmpty())

        // Return the number of Paper rolls successfully removed
        return initialCountOfPaperRolls - getAllLocations().count { location ->
            location.toValue() == PrintDeptLayoutType.PAPER_ROLL
        }
    }

}