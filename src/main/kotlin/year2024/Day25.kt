/**
 * Problem: Day25: Code Chronicle
 * https://adventofcode.com/2024/day/25
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import extensions.splitWhen
import utils.grid.Lattice
import utils.grid.Point2d
import utils.grid.TransverseDirection.*

private class Day25 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample()      // 3
    println("=====")
    solveActual()      // 2824
    println("=====")
}

private fun solveSample() {
    doPart1(Day25.getSampleFile().readLines())
}

private fun solveActual() {
    doPart1(Day25.getActualTestFile().readLines())
}

private fun doPart1(input: List<String>) {
    TumblerLockAndKeyAnalyzer.parse(input)
        .getCountOfCompatibleLockKeyPairs()
        .also(::println)
}

private enum class LockKeySchemaType(val type: Char) {
    EMPTY('.'),
    FILLED('#')
}

private class LockKeySchemaPin(x: Int, y: Int) : Point2d<Int>(x, y)

private class LockKeySchemaGrid(
    pattern: List<String>
) : Lattice<LockKeySchemaPin, LockKeySchemaType>(pattern) {

    // Identifies whether this schema grid is for a Lock or a Key based on first row contents. If entire row is filled,
    // then it is a Lock; otherwise, it is a Key
    private val isLock: Boolean by lazy {
        getLocation(0, 0).getLocationsInDirection(RIGHT).all { lockKeySchemaPin ->
            lockKeySchemaPin.toValue() == LockKeySchemaType.FILLED
        }
    }

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value location's row
     * @param column [Int] value location's column
     */
    override fun provideLocation(row: Int, column: Int): LockKeySchemaPin =
        LockKeySchemaPin(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar Char found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): LockKeySchemaType =
        LockKeySchemaType.entries.single { lockKeySchemaType ->
            lockKeySchemaType.type == locationChar
        }

    /**
     * Returns heights of [LockKeySchemaType.FILLED] content along the columns.
     *
     * For a Lock, this starts from the Top row extending downward and for a Key, this starts from
     * the Bottom row extending upward. For a Lock, the heights per column are the pins themselves,
     * while for a Key, the heights per column are the shape of the key where it aligns with the pins.
     *
     * Base row is excluded from the height consideration for both Lock and Key.
     */
    fun getHeights(): List<Int> =
        (0 until columns).map { columnIndex ->
            if (isLock) {
                getLocation(0, columnIndex).getLocationsInDirection(BOTTOM)
                    .takeWhile { lockKeySchemaPin: LockKeySchemaPin ->
                        lockKeySchemaPin.toValue() != LockKeySchemaType.EMPTY
                    }.count() - 1   // -1 since base is not considered
            } else {
                getLocation(rows - 1, columnIndex).getLocationsInDirection(TOP)
                    .takeWhile { lockKeySchemaPin: LockKeySchemaPin ->
                        lockKeySchemaPin.toValue() != LockKeySchemaType.EMPTY
                    }.count() - 1   // -1 since base is not considered
            }
        }

}

private class TumblerLockAndKeyAnalyzer private constructor(
    private val locks: List<LockKeySchemaGrid>,
    private val keys: List<LockKeySchemaGrid>
) {

    companion object {

        fun parse(input: List<String>): TumblerLockAndKeyAnalyzer = input.splitWhen { line ->
            line.isEmpty() || line.isBlank()
        }.partition { pattern: Iterable<String> ->
            pattern.first().all { it == LockKeySchemaType.FILLED.type }
        }.let { (locks: List<Iterable<String>>, keys: List<Iterable<String>>) ->
            TumblerLockAndKeyAnalyzer(
                locks = locks.map { pattern -> LockKeySchemaGrid(pattern.toList()) },
                keys = keys.map { pattern -> LockKeySchemaGrid(pattern.toList()) }
            )
        }
    }

    /**
     * Returns number of [keys] that work for the given [lock].
     *
     * @param heightsOfAllKeys [List] of [Int] Heights per column of each key grouped into a [List]
     */
    private fun countOfKeysThatWorkForTheLock(lock: LockKeySchemaGrid, heightsOfAllKeys: List<List<Int>>): Int =
        lock.getHeights().let { lockPinHeights: List<Int> ->
            // Count keys that work for the given lock
            heightsOfAllKeys.count { shapeHeightsOfKey: List<Int> ->
                // Check shape heights of the key at every column to see if there is any overlap with the
                // corresponding pin heights of the lock. If there is no overlap at all columns, then
                // this key will work for the given lock.
                shapeHeightsOfKey.withIndex().all { (columnIndex, shapeHeightOfKeyAtColumn) ->
                    // If the height of the shape of the key at column and height of the pin of the lock at same column,
                    // while including the base height of key, i.e., 1 is less than the number of rows in lock schema,
                    // then they do not overlap
                    1 + shapeHeightOfKeyAtColumn + lockPinHeights[columnIndex] < lock.rows
                }
            }
        }

    /**
     * [Solution for Part-1]
     *
     * Returns count of compatible unique lock-key pairs
     */
    fun getCountOfCompatibleLockKeyPairs(): Int =
        keys.map { keySchemaGrid ->
            // Get shape heights of the Key
            keySchemaGrid.getHeights()
        }.let { heightsOfAllKeys: List<List<Int>> ->
            // Return total count of compatible unique lock-key pairs
            locks.sumOf { lockSchemaGrid ->
                // Get the number of Keys that work for the lock
                countOfKeysThatWorkForTheLock(lockSchemaGrid, heightsOfAllKeys)
            }
        }

}