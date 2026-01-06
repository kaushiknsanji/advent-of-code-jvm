/**
 * Problem: Day6: Trash Compactor
 * https://adventofcode.com/2025/day/6
 *
 * @author <a href='mailto:kaushiknsanji@gmail.com'>Kaushik N Sanji</a>
 */

package year2025

import base.BaseProblemHandler
import utils.Constants.DOT_CHAR
import utils.Constants.DOT_STRING
import utils.Constants.EMPTY
import utils.Constants.PLUS_CHAR
import utils.Constants.PLUS_STRING
import utils.Constants.PRODUCT_STRING
import utils.Constants.SPACE_CHAR
import utils.splitContentByWhitespaces

class Day6 : BaseProblemHandler() {

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
        CephalopodMathAnalyzer.parse(input)
            .getGrandTotalOfAllProblems(otherArgs[0] as Boolean)

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        CephalopodMathAnalyzer.parse(input)
            .getGrandTotalOfAllProblems(otherArgs[0] as Boolean)

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, 4277556L, false)
        solveActual(1, false, 0, 4693159084994L, false)
        solveSample(2, false, 0, 3263827L, true)
        solveActual(2, false, 0, 11643736116335L, true)
    }

}

fun main() {
    try {
        Day6().start()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

private class CephalopodMathAnalyzer private constructor(
    private val numbersGridMap: Map<Int, List<String>>,
    private val operationsList: List<(Long, Long) -> Long>
) {

    companion object {

        fun parse(input: List<String>): CephalopodMathAnalyzer = input.partition { pattern: String ->
            pattern.contains(PLUS_CHAR)
        }.let { (operationStrings: List<String>, numberStrings: List<String>) ->
            // Get line containing operators
            val operationString = operationStrings.single()

            // Prepare a List of Pairs of Opcode's position to its operating column size
            val columnLengthPairs: List<Pair<Int, Int>> = operationString.withIndex().filterNot { (_, character) ->
                // Index every character in the line and then filter out all whitespaces
                character.isWhitespace()
            }.zipWithNext { currentIndexedOpcode, nextIndexedOpcode ->
                // Transform to a Pair of Opcode's position to its operating column size
                currentIndexedOpcode.index to (nextIndexedOpcode.index - currentIndexedOpcode.index - 1)
            }.toMutableList().apply {
                // Include the Pair of last Opcode's position in the line to its operating column size
                val lastOpcodeIndex = last().first + last().second + 1
                add(lastOpcodeIndex to operationString.length - lastOpcodeIndex)
            }

            // Transform lines containing Numbers and spaces to lines containing Numbers along with indent marks and
            // a space in between columns of Numbers
            val indentPreservedNumberStrings: List<String> = numberStrings.map { numberString ->
                buildString {
                    columnLengthPairs.forEach { (columnStartIndex: Int, length: Int) ->
                        append(
                            // Replace space with dot character to preserve indent marks
                            numberString.substring(columnStartIndex, columnStartIndex + length).replace(
                                SPACE_CHAR, DOT_CHAR
                            )
                        )
                        append(SPACE_CHAR) // Add space after each column
                    }
                }.trimEnd() // Remove the last space
            }

            CephalopodMathAnalyzer(
                numbersGridMap = indentPreservedNumberStrings.withIndex()
                    .associate { (rowIndex: Int, rowNumbersString) ->
                        rowIndex to rowNumbersString.split(SPACE_CHAR)
                    },
                operationsList = operationString.trimEnd().splitContentByWhitespaces()
                    .map { opCodeString ->
                        when (opCodeString) {
                            PLUS_STRING -> Long::plus
                            PRODUCT_STRING -> Long::times
                            else -> {
                                throw IllegalArgumentException("Unknown opcode '$opCodeString' detected")
                            }
                        }
                    }

            )
        }
    }

    private val totalRows = numbersGridMap.size

    /**
     * Returns the [String] Number found at given [row][row] and [column][col] of the [numbersGridMap].
     *
     * @param readRightToLeft [Boolean] to indicate if the Number reads from Right to Left. When `true`,
     * the [String] Number found is returned AS-IS along with indent marks. When `false`,
     * the [String] Number found is returned after replacing each indent mark with empty character.
     */
    private operator fun get(
        row: Int,
        col: Int,
        readRightToLeft: Boolean
    ): String = numbersGridMap[row]!![col].let { numberString ->
        if (readRightToLeft) {
            // When read Right to Left, return the String number with indent marks preserved
            numberString
        } else {
            // When read normally, return the String number without indent marks
            numberString.replace(DOT_STRING, EMPTY)
        }
    }

    /**
     * When reading a Number from Right to Left, each digit in a Number itself becomes a column. So,
     * the Most significant digit resides in the first [String] Number of [columnNumberStrings] at
     * respective digit column and the Least significant digit resides in the last [String] Number
     * of [columnNumberStrings] of the same digit column.
     *
     * Returns the transformation of given [String]s of Column Numbers into [Long] Numbers
     * read from Right to Left as described.
     */
    private fun getRightToLeftNumbers(columnNumberStrings: List<String>): List<Long> {
        val totalDigitColumns = columnNumberStrings.first().length

        return (totalDigitColumns - 1 downTo 0).map { digitColumnIndex: Int ->
            (0 until totalRows).map { rowIndex: Int ->
                columnNumberStrings[rowIndex][digitColumnIndex]
            }.joinToString("").dropWhile { character ->
                // Drop characters if the Number starts with indent marks
                character == DOT_CHAR
            }.takeWhile(Char::isDigit).toLong() // Exclude if indent marks are present after the last digit
        }
    }

    /**
     * [Solution for Part 1 & 2]
     *
     * Returns the Grand Total of all Problems' solutions computed based on its respective operation read
     * from [operationsList].
     *
     * @param readRightToLeft [Boolean] to indicate if the Number reads from Right to Left.
     */
    fun getGrandTotalOfAllProblems(readRightToLeft: Boolean): Long =
        operationsList.mapIndexed { columnIndex: Int, operation: (Long, Long) -> Long ->
            // Extract Strings of Column Numbers
            val columnNumberStrings: List<String> = (0 until totalRows).map { rowIndex: Int ->
                this[rowIndex, columnIndex, readRightToLeft]
            }

            if (readRightToLeft) {
                // When read from Right to Left, get Right-to-Left digit based column numbers
                getRightToLeftNumbers(columnNumberStrings)
            } else {
                // When read normally, get the numbers without indent marks
                columnNumberStrings.map(String::toLong)
            }.reduce(operation) // Apply the corresponding operation
        }.sum() // Compute and return the Grand Total

}