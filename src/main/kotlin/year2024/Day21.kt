/**
 * Problem: Day21: Keypad Conundrum
 * https://adventofcode.com/2024/day/21
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler
import utils.Constants.A_CAP_CHAR
import utils.Constants.CARET_CHAR
import utils.Constants.EMPTY
import utils.Constants.GREATER_CHAR
import utils.Constants.LESSER_CHAR
import utils.Constants.NO_0_CHAR
import utils.Constants.NO_1_CHAR
import utils.Constants.NO_2_CHAR
import utils.Constants.NO_3_CHAR
import utils.Constants.NO_4_CHAR
import utils.Constants.NO_5_CHAR
import utils.Constants.NO_6_CHAR
import utils.Constants.NO_7_CHAR
import utils.Constants.NO_8_CHAR
import utils.Constants.NO_9_CHAR
import utils.Constants.SPACE_CHAR
import utils.Constants.V_SMALL_CHAR
import utils.grid.ILattice
import utils.grid.Lattice
import utils.grid.Point2d
import utils.grid.TransverseDirection.BOTTOM
import utils.grid.TransverseDirection.TOP
import utils.grid.toDirectionalChar
import java.util.*
import utils.grid.TransverseDirection as Direction

private class Day21 : BaseProblemHandler() {

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
        MultiKeypadAnalyzer.parse(input)
            .getTotalOfAllCodeComplexity(3)

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        MultiKeypadAnalyzer.parse(input)
            .getTotalOfAllCodeComplexity(26)

}

fun main() {
    with(Day21()) {
        solveSample(1, false, 0, 126384L)
        solveActual(1, false, 0, 157230L)
        solveSample(2, false, 0, 154115708116294L)
        solveActual(2, false, 0, 195969155897936L)
    }
}

private enum class NumericKeyType(val type: Char) {
    KEY_0(NO_0_CHAR),
    KEY_1(NO_1_CHAR),
    KEY_2(NO_2_CHAR),
    KEY_3(NO_3_CHAR),
    KEY_4(NO_4_CHAR),
    KEY_5(NO_5_CHAR),
    KEY_6(NO_6_CHAR),
    KEY_7(NO_7_CHAR),
    KEY_8(NO_8_CHAR),
    KEY_9(NO_9_CHAR),
    KEY_ACTIVATE(A_CAP_CHAR),
    NO_KEY(SPACE_CHAR);

    companion object {
        private val typeMap = entries.associateBy(NumericKeyType::type)

        fun fromType(type: Char): NumericKeyType = typeMap[type]!!
    }
}

private class NumericKeypadButtonLocation(x: Int, y: Int) : Point2d<Int>(x, y)

private class NumericKeypadGrid(
    pattern: List<String> = listOf(
        "789",
        "456",
        "123",
        " 0A"
    )
) : Lattice<NumericKeypadButtonLocation, NumericKeyType>(pattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value of location's row
     * @param column [Int] value of location's column
     */
    override fun provideLocation(row: Int, column: Int): NumericKeypadButtonLocation =
        NumericKeypadButtonLocation(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar [Char] found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): NumericKeyType =
        NumericKeyType.fromType(locationChar)

}

/**
 * Class interfacing [numericKeypadGrid].
 *
 * @property numericKeypadGrid A [Lattice] Grid for a Numeric Keypad with keys of type [NumericKeyType].
 *
 * @constructor Constructs [NumericKeypadProcessor] to communicate with [numericKeypadGrid].
 */
private class NumericKeypadProcessor(
    private val numericKeypadGrid: NumericKeypadGrid
) : ILattice<NumericKeypadButtonLocation, NumericKeyType> by numericKeypadGrid {

    // Map of Key to Button Location
    private val keyLocationMap: Map<Char, NumericKeypadButtonLocation> by lazy {
        NumericKeyType.entries.associate { numericKeypadType ->
            numericKeypadType.type to getAllLocations().first { location ->
                location.toValue() == numericKeypadType
            }
        }
    }

    /**
     * Class for Tracing path of [NumericKeypadButtonLocation]s in [numericKeypadGrid].
     *
     * @property location A [NumericKeypadButtonLocation] being traced in the path.
     * @property direction [Direction] of traversal. Defaults to a randomly selected Direction - [TOP].
     * @property code A [String] of Directional keys of type [DirectionalKeyType.type] used while tracing.
     * Starts as an empty [String].
     */
    private class TraceData(
        val location: NumericKeypadButtonLocation,
        val direction: Direction = TOP,
        val code: String = EMPTY
    )

    /**
     * Returns next [List] of [TraceData] from [this] Trace information.
     */
    private fun TraceData.getNextLocationsTraceData(): List<TraceData> =
        location.getAllNeighboursWithDirection().map { (nextDirection, nextLocation) ->
            nextDirection to nextLocation
        }.filterNot { (_, nextLocation) ->
            // Exclude any neighbour without a key
            nextLocation.toValue() == NumericKeyType.NO_KEY
        }.map { (nextDirection, nextLocation) ->
            // Transform next neighbour with direction to a TraceData containing the Key for the direction used
            // to reach this next neighbour
            TraceData(nextLocation, nextDirection, code + nextDirection.toDirectionalChar())
        }

    /**
     * Returns [List] of shortest [String] codes containing keys (characters) for the directions used
     * to reach [nextKey] from [previousKey].
     *
     * Shortest input codes are obtained by following [Dijkstra](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm)
     * algorithm using [PriorityQueue] which prioritizes on the length of [TraceData.code].
     */
    fun getNextShortestInputCode(
        previousKey: Char,
        nextKey: Char
    ): List<String> =
        if (previousKey == nextKey) {
            // If both previous and next keys are same, then return a single element list for key character 'A'
            // denoting Button-A for ACTIVATE being pushed
            listOf(NumericKeyType.KEY_ACTIVATE.type.toString())
        } else {
            // If both previous and next keys are different

            // Button Location of Previous key
            val startLocation = keyLocationMap[previousKey]!!

            // Button Location of Next key
            val endLocation = keyLocationMap[nextKey]!!

            // A PriorityQueue based Frontier that prioritizes on the length of code traced thus far,
            // for minimizing its length
            val frontier = PriorityQueue<TraceData>(
                compareBy { it.code.length }
            ).apply {
                // Begin with the TraceData for Previous key
                add(TraceData(startLocation))
            }

            // Set to keep track of Button locations reached along with their direction of visit
            // Begin with Button location of Previous key with random direction like TOP
            val visitedSet: MutableSet<Pair<NumericKeypadButtonLocation, Direction>> =
                mutableSetOf(startLocation to TOP)

            // List of Shortest input codes formed for reaching Next key from Previous key
            val shortestCodes: MutableList<String> = mutableListOf()

            // Keeps track of the best code length found
            var bestCodeLength = Int.MAX_VALUE

            // Repeat till the PriorityQueue based Frontier becomes empty
            while (frontier.isNotEmpty()) {
                // Get the Top TraceData
                val current = frontier.poll()

                if (current.location == endLocation) {
                    // When the Button location of Next key is reached from Previous key Button location

                    if (current.code.length < bestCodeLength) {
                        // If the current input code length is smaller than the best length so far, then save
                        // the new length as the best length, and clear all previous input codes as they are longer
                        // and load this new shortest input code into the list of shortest input codes.
                        bestCodeLength = current.code.length
                        shortestCodes.clear()
                        // Append key character 'A' to the current input code for ACTIVATE Button being pushed.
                        shortestCodes.add(current.code + NumericKeyType.KEY_ACTIVATE.type)
                    } else if (current.code.length == bestCodeLength) {
                        // If the current input code length is same as the best length so far, then load this
                        // shortest input code into the list of shortest input codes.
                        // Append key character 'A' to the current input code for ACTIVATE Button being pushed.
                        shortestCodes.add(current.code + NumericKeyType.KEY_ACTIVATE.type)
                    } else {
                        // Continue to Next Top TraceData from the Frontier when current input code length is longer
                        // than the best length so far
                        continue
                    }
                }

                // Retrieve Next list of TraceData
                current.getNextLocationsTraceData()
                    .filterNot { next ->
                        // Exclude if the location of Next TraceData is not the Button location of given Next key
                        // and is already visited in the direction
                        next.location != endLocation && (next.location to next.direction) in visitedSet
                    }
                    .forEach { next ->
                        // Mark the Button location of Next TraceData as visited in the direction
                        visitedSet.add(next.location to next.direction)
                        // Add this Next TraceData to the Frontier
                        frontier.add(next)
                    }
            }

            // Return all Shortest input codes formed
            shortestCodes
        }

}

private enum class DirectionalKeyType(val type: Char) {
    NO_KEY(SPACE_CHAR),
    KEY_UP(CARET_CHAR),
    KEY_ACTIVATE(A_CAP_CHAR),
    KEY_LEFT(LESSER_CHAR),
    KEY_DOWN(V_SMALL_CHAR),
    KEY_RIGHT(GREATER_CHAR);

    companion object {
        private val typeMap = entries.associateBy(DirectionalKeyType::type)

        fun fromType(type: Char): DirectionalKeyType = typeMap[type]!!
    }
}

private class DirectionalKeypadButtonLocation(x: Int, y: Int) : Point2d<Int>(x, y)

private class DirectionalKeypadGrid(
    pattern: List<String> = listOf(
        " ^A",
        "<v>"
    )
) : Lattice<DirectionalKeypadButtonLocation, DirectionalKeyType>(pattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value of location's row
     * @param column [Int] value of location's column
     */
    override fun provideLocation(row: Int, column: Int): DirectionalKeypadButtonLocation =
        DirectionalKeypadButtonLocation(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar [Char] found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): DirectionalKeyType =
        DirectionalKeyType.fromType(locationChar)

}

/**
 * Class interfacing [directionalKeypadGrid].
 *
 * @property directionalKeypadGrid A [Lattice] Grid for a Directional Keypad with keys of type [DirectionalKeyType].
 *
 * @constructor Constructs [DirectionalKeypadProcessor] to communicate with [directionalKeypadGrid].
 */
private class DirectionalKeypadProcessor(
    private val directionalKeypadGrid: DirectionalKeypadGrid
) : ILattice<DirectionalKeypadButtonLocation, DirectionalKeyType> by directionalKeypadGrid {

    // Map of Key to Button Location
    private val keyLocationMap: Map<Char, DirectionalKeypadButtonLocation> by lazy {
        DirectionalKeyType.entries.associate { directionalKeypadType ->
            directionalKeypadType.type to getAllLocations().first { location ->
                location.toValue() == directionalKeypadType
            }
        }
    }

    /**
     * Class for Tracing path of [DirectionalKeypadButtonLocation]s in [directionalKeypadGrid].
     *
     * @property location A [DirectionalKeypadButtonLocation] being traced in the path.
     * @property direction [Direction] of traversal. Defaults to a randomly selected Direction - [BOTTOM].
     * @property code A [String] of Directional keys of type [DirectionalKeyType.type] used while tracing.
     * Starts as an empty [String].
     */
    private class TraceData(
        val location: DirectionalKeypadButtonLocation,
        val direction: Direction = BOTTOM,
        val code: String = EMPTY
    )

    /**
     * Returns next [List] of [TraceData] from [this] Trace information.
     */
    private fun TraceData.getNextLocationsTraceData(): List<TraceData> =
        location.getAllNeighboursWithDirection().map { (nextDirection, nextLocation) ->
            nextDirection to nextLocation
        }.filterNot { (_, nextLocation) ->
            // Exclude any neighbour without a key
            nextLocation.toValue() == DirectionalKeyType.NO_KEY
        }.map { (nextDirection, nextLocation) ->
            // Transform next neighbour with direction to a TraceData containing the Key for the direction used
            // to reach this next neighbour
            TraceData(nextLocation, nextDirection, code + nextDirection.toDirectionalChar())
        }

    /**
     * Returns [List] of shortest [String] codes containing keys (characters) for the directions used
     * to reach [nextKey] from [previousKey].
     *
     * Shortest input codes are obtained by following [Dijkstra](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm)
     * algorithm using [PriorityQueue] which prioritizes on the length of [TraceData.code].
     */
    fun getNextShortestInputCode(
        previousKey: Char,
        nextKey: Char
    ): List<String> =
        if (previousKey == nextKey) {
            // If both previous and next keys are same, then return a single element list for key character 'A'
            // denoting Button-A for ACTIVATE being pushed
            listOf(DirectionalKeyType.KEY_ACTIVATE.type.toString())
        } else {
            // If both previous and next keys are different

            // Button Location of Previous key
            val startLocation = keyLocationMap[previousKey]!!

            // Button Location of Next key
            val endLocation = keyLocationMap[nextKey]!!

            // A PriorityQueue based Frontier that prioritizes on the length of code traced thus far,
            // for minimizing its length
            val frontier = PriorityQueue<TraceData>(
                compareBy { it.code.length }
            ).apply {
                // Begin with the TraceData for Previous key
                add(TraceData(startLocation))
            }

            // Set to keep track of Button locations reached along with their direction of visit
            // Begin with Button location of Previous key with random direction like BOTTOM
            val visitedSet: MutableSet<Pair<DirectionalKeypadButtonLocation, Direction>> =
                mutableSetOf(startLocation to BOTTOM)

            // List of Shortest input codes formed for reaching Next key from Previous key
            val shortestCodes: MutableList<String> = mutableListOf()

            // Keeps track of the best code length found
            var bestCodeLength = Int.MAX_VALUE

            // Repeat till the PriorityQueue based Frontier becomes empty
            while (frontier.isNotEmpty()) {
                // Get the Top TraceData
                val current = frontier.poll()

                if (current.location == endLocation) {
                    // When the Button location of Next key is reached from Previous key Button location

                    if (current.code.length < bestCodeLength) {
                        // If the current input code length is smaller than the best length so far, then save
                        // the new length as the best length, and clear all previous input codes as they are longer
                        // and load this new shortest input code into the list of shortest input codes.
                        bestCodeLength = current.code.length
                        shortestCodes.clear()
                        // Append key character 'A' to the current input code for ACTIVATE Button being pushed.
                        shortestCodes.add(current.code + DirectionalKeyType.KEY_ACTIVATE.type)
                    } else if (current.code.length == bestCodeLength) {
                        // If the current input code length is same as the best length so far, then load this
                        // shortest input code into the list of shortest input codes.
                        // Append key character 'A' to the current input code for ACTIVATE Button being pushed.
                        shortestCodes.add(current.code + DirectionalKeyType.KEY_ACTIVATE.type)
                    } else {
                        // Continue to Next Top TraceData from the Frontier when current input code length is longer
                        // than the best length so far
                        continue
                    }
                }

                // Retrieve Next list of TraceData
                current.getNextLocationsTraceData()
                    .filterNot { next ->
                        // Exclude if the location of Next TraceData is not the Button location of given Next key
                        // and is already visited in the direction
                        next.location != endLocation && (next.location to next.direction) in visitedSet
                    }
                    .forEach { next ->
                        // Mark the Button location of Next TraceData as visited in the direction
                        visitedSet.add(next.location to next.direction)
                        // Add this Next TraceData to the Frontier
                        frontier.add(next)
                    }
            }

            // Return all Shortest input codes formed
            shortestCodes
        }

}

/**
 * Class to parse the input, analyze and solve the problem at hand.
 *
 * @property doorCodes [List] of door codes to enter by the 'n'-th Robot
 * @property numericKeypadProcessor [NumericKeypadProcessor] to communicate with Numeric Keypad
 * @property directionalKeypadProcessor [DirectionalKeypadProcessor] to communicate with Directional Keypad
 */
private class MultiKeypadAnalyzer private constructor(
    private val doorCodes: List<String>,
    private val numericKeypadProcessor: NumericKeypadProcessor,
    private val directionalKeypadProcessor: DirectionalKeypadProcessor
) {

    companion object {
        private const val BUTTON_ACTIVATE = A_CAP_CHAR

        fun parse(input: List<String>): MultiKeypadAnalyzer = MultiKeypadAnalyzer(
            doorCodes = input,
            numericKeypadProcessor = NumericKeypadProcessor(NumericKeypadGrid()),
            directionalKeypadProcessor = DirectionalKeypadProcessor(DirectionalKeypadGrid())
        )
    }

    /**
     * Data class used as a Key of the [Cache Map][adjacentKeysLengthCacheMap] to get the Length of
     * the shortest input code required to reach [nextKey] from [previousKey] for the Robot [robotIndex].
     *
     * @property isKeypadNumeric [Boolean] that indicates whether the Keypad to be used is Numeric or Directional.
     * `true` for Numeric Keypad; `false` for Directional Keypad.
     * @property robotIndex [Int] identifier of the Robot where Robot - 1 is the Robot we are controlling
     * using a Directional Keypad and Robot - 'n' is the Robot at the Door entering the Door code
     * using a Numeric Keypad.
     */
    private data class AdjacentKeysLengthLookup(
        val previousKey: Char,
        val nextKey: Char,
        val isKeypadNumeric: Boolean,
        val robotIndex: Int
    )

    // Cache Map of Adjacent Keys to their shortest input codes
    private val adjacentKeysInputCodeCacheMap: MutableMap<Pair<Char, Char>, List<String>> = mutableMapOf()

    // Cache Map of `AdjacentKeysLengthLookup` to the shortest input code length
    private val adjacentKeysLengthCacheMap: MutableMap<AdjacentKeysLengthLookup, Long> = mutableMapOf()

    /**
     * Returns [List] of shortest [String] codes containing keys (characters) for the directions used
     * to reach [nextKey] from [previousKey].
     *
     * @param isKeypadNumeric [Boolean] that indicates whether the Keypad to be used is Numeric or Directional.
     * `true` for Numeric Keypad; `false` for Directional Keypad.
     */
    private fun getNextShortestInputCode(
        previousKey: Char,
        nextKey: Char,
        isKeypadNumeric: Boolean
    ): List<String> = adjacentKeysInputCodeCacheMap.getOrPut(previousKey to nextKey) {
        // Return shortest input codes from the Cache Map if present; else compute, update cache and return

        if (isKeypadNumeric) {
            // When Keypad to be used is Numeric
            numericKeypadProcessor.getNextShortestInputCode(previousKey, nextKey)
        } else {
            // When Keypad to be used is Directional
            directionalKeypadProcessor.getNextShortestInputCode(previousKey, nextKey)
        }
    }

    /**
     * Returns a [Long] of the shortest input code length required to reach [nextKey] from [previousKey]
     * for the Robot [robotIndex].
     *
     * @param isKeypadNumeric [Boolean] that indicates whether the Keypad to be used is Numeric or Directional.
     * `true` for Numeric Keypad; `false` for Directional Keypad.
     * @param robotIndex [Int] identifier of the Robot where Robot - 1 is the Robot we are controlling
     * using a Directional Keypad and Robot - 'n' is the Robot at the Door entering the Door code
     * using a Numeric Keypad.
     */
    private fun getLengthForAdjacentKeys(
        previousKey: Char,
        nextKey: Char,
        isKeypadNumeric: Boolean,
        robotIndex: Int
    ): Long = adjacentKeysLengthCacheMap.getOrPut(
        AdjacentKeysLengthLookup(
            previousKey, nextKey, isKeypadNumeric, robotIndex
        )
    ) {
        // Return shortest input code length from the Cache Map if present; else compute, update cache and return

        if (robotIndex == 1) {
            // For the Robot we are controlling, just return the length of the first shortest input code found
            getNextShortestInputCode(previousKey, nextKey, isKeypadNumeric).first().length.toLong()
        } else {
            // For all the other Robots in series from 'n'-th Robot down to the 2nd Robot

            // Keeps track of the Minimum length of shortest code to be input to this Robot.
            // Initialized to MAX value of LONG.
            var minLength: Long = Long.MAX_VALUE

            // Find and update the minimum length for each shortest code found
            getNextShortestInputCode(previousKey, nextKey, isKeypadNumeric).forEach { inputCode ->
                minLength = minOf(minLength, getCodeLength(inputCode, robotIndex - 1))
            }

            // Return the Minimum length of shortest code to be input to this Robot
            minLength
        }
    }

    /**
     * Returns length of shortest [input code][inputCode] for the Robot [robotIndex].
     *
     * @param robotIndex [Int] identifier of the Robot where Robot - 1 is the Robot we are controlling
     * using a Directional Keypad and Robot - 'n' is the Robot at the Door entering the Door code
     * using a Numeric Keypad.
     */
    private fun getCodeLength(inputCode: String, robotIndex: Int): Long {
        // Since all input codes start from ACTIVATE Button, include it into the code
        val code = BUTTON_ACTIVATE + inputCode
        // Length of shortest code to be input to this Robot. Initialized to 0.
        var length = 0L

        // Find and update length with the length of shortest input code found for each Adjacent Key pairs
        code.map { it }.zipWithNext().forEach { (previousKey, nextKey) ->
            length += getLengthForAdjacentKeys(
                previousKey,
                nextKey,
                inputCode.any { it.isDigit() },
                robotIndex
            )
        }

        // Return length of shortest code to be input to this Robot
        return length
    }

    /**
     * [Solution for Part 1 and 2]
     *
     * Returns the Sum of the complexities of all [Door codes][doorCodes].
     *
     * @param robotCount [Int] number of Robots involved where Robot - 1 is the Robot we are controlling
     * using a Directional Keypad and Robot - 'n = robotCount' is the Robot at the Door entering the Door code
     * using a Numeric Keypad.
     */
    fun getTotalOfAllCodeComplexity(robotCount: Int): Long =
        doorCodes.sumOf { doorCode: String ->
            doorCode.filter { it.isDigit() }.toLong() * getCodeLength(doorCode, robotCount)
        }

}