/**
 * Problem: Day17: Pyroclastic Flow
 * https://adventofcode.com/2022/day/17
 *
 * @author <a href='mailto:kaushiknsanji@gmail.com'>KaushikNSanji</a>
 */

package year2022

import base.BaseProblemHandler
import extensions.rangeLength
import extensions.toIntRanges
import utils.Constants.DOT_CHAR
import utils.Constants.EMPTY
import utils.Constants.GREATER_CHAR
import utils.Constants.HASH_CHAR
import utils.Constants.HYPHEN_CHAR
import utils.grid.*
import utils.grid.CardinalDirection.*
import utils.splitWhenLineBlankOrEmpty

class Day17 : BaseProblemHandler() {

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
        PyroclasticFlowAnalyzer.parse(input)
            .getRockTowerHeight(2022L)

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        PyroclasticFlowAnalyzer.parse(input)
            .getRockTowerHeight(1000000000000L)

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, 3068L)
        solveActual(1, false, 0, 3232L)
        solveSample(2, false, 0, 1514285714288L)
        solveActual(2, false, 0, 1585632183915L)
    }

}

fun main() {
    Day17().start()
}

private enum class ChamberSpaceType(val type: Char) {
    FLOOR(HYPHEN_CHAR),
    ROCK(HASH_CHAR),
    EMPTY(DOT_CHAR);

    companion object {
        private val typeMap = entries.associateBy(ChamberSpaceType::type)

        fun fromType(type: Char): ChamberSpaceType = typeMap[type]!!
    }
}

/**
 * Wrapper class for [type] and Rock [id].
 */
private class ChamberSpaceData(val type: ChamberSpaceType, val id: Long = 0L)

/**
 * Class for Location in the [Rock Grid][RockGrid].
 *
 * @param x [Int] value of x-coordinate
 * @param y [Int] value of y-coordinate
 */
private class RockLocus(x: Int, y: Int) : Point2d<Int>(x, y)

/**
 * A Grid Graph representation class for various peculiar Rock shapes of the given `pattern`.
 */
private class RockGrid(
    pattern: List<String>
) : Grid2dGraph<RockLocus, ChamberSpaceType>(pattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value of location's row
     * @param column [Int] value of location's column
     */
    override fun provideLocation(row: Int, column: Int): RockLocus =
        RockLocus(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar [Char] found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): ChamberSpaceType =
        ChamberSpaceType.fromType(locationChar)

}

/**
 * Class for Location in the [Chamber Grid][ChamberGrid].
 */
private class ChamberLocus(x: Int, y: Int) : Point2d<Int>(x, y)

/**
 * A [Lattice] of Chamber constructed with only the floor pattern read from `initPattern` for the given `width`.
 */
private class ChamberGrid(
    width: Int,
    initPattern: List<String> = listOf(List(width) { ChamberSpaceType.FLOOR.type }.joinToString(separator = EMPTY))
) : Lattice<ChamberLocus, ChamberSpaceData>(initPattern) {

    // Locations of current rock falling
    private var currentRockLocations: List<ChamberLocus> = listOf()

    // Default data used to denote an Empty Chamber space
    private val defaultEmptySpaceData: ChamberSpaceData = ChamberSpaceData(ChamberSpaceType.EMPTY)

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value of location's row
     * @param column [Int] value of location's column
     */
    override fun provideLocation(row: Int, column: Int): ChamberLocus =
        ChamberLocus(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar [Char] found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): ChamberSpaceData =
        ChamberSpaceData(ChamberSpaceType.fromType(locationChar))

    /**
     * Returns location if present at given [row] and [column] in the Expanded grid; otherwise `null`.
     *
     * Called by [getLocationOrNull] when location is not found in the original grid.
     */
    override fun getExpandedLocationOrNull(row: Int, column: Int): ChamberLocus? =
        if (row < 0 && column in (0 until columns)) {
            provideLocation(row, column)
        } else {
            null
        }

    /**
     * Returns value present in the grid at given [location]
     */
    override fun get(location: ChamberLocus): ChamberSpaceData =
        if (getRowLocations(location.xPos).find { it.yPos == location.yPos } != null) {
            super.get(location)
        } else {
            defaultEmptySpaceData
        }

    /**
     * Returns all [locations][ChamberLocus] in Chamber occupied by a Rock
     */
    private fun getAllRockLocations(): Collection<ChamberLocus> =
        getAllLocations().filter { chamberLocus ->
            chamberLocus.toValue().type == ChamberSpaceType.ROCK
        }

    /**
     * Returns [locations][ChamberLocus] of a particular Rock identified by its [id][rockId]
     */
    private fun getRockLocations(rockId: Long): Collection<ChamberLocus> =
        getAllLocations().filter { chamberLocus ->
            with(chamberLocus.toValue()) {
                type == ChamberSpaceType.ROCK && id == rockId
            }
        }

    /**
     * Returns [Set] of row indices of all Rock locations found in Chamber
     */
    private fun getRowsHavingRocks(): Set<Int> =
        getAllRockLocations().map { chamberLocus ->
            chamberLocus.xPos
        }.toSet()

    /**
     * Returns index of the topmost row from the ground of the Chamber having Rocks
     */
    private fun getPeakRowHavingRocks(): Int =
        getRowsHavingRocks().minOrNull() ?: 0

    /**
     * Returns `true` when current falling Rock as represented by its [current locations][currentRockLocations] can be
     * moved to its neighbouring locations in the given [direction]; `false` otherwise.
     */
    private fun canMove(direction: CardinalDirection): Boolean =
        currentRockLocations.map { chamberLocus ->
            chamberLocus.getNeighbourOrNull(direction)
        }.all { nextLocus ->
            // Returns true only when next neighbouring locations are empty
            nextLocus != null && nextLocus.toValue().type == ChamberSpaceType.EMPTY
        }

    /**
     * Updates [current locations][currentRockLocations] of the falling Rock to its neighbouring locations
     * in the given [direction] when [it can be moved][canMove]; otherwise throws [Error].
     */
    fun pushRock(direction: CardinalDirection) {
        if (canMove(direction)) {
            when (direction) {
                LEFT, RIGHT -> {
                    currentRockLocations = currentRockLocations.map { chamberLocus ->
                        chamberLocus.getNeighbourOrNull(direction)!!
                    }
                }

                else -> throw Error("Unrecognized direction of Push operation : $direction")
            }
        }
    }

    /**
     * Simulates Rock falling
     *
     * @param rockId [Long] ID of the Rock falling
     * @param onSettle Callback Lambda to load the next Rock once the current Rock settles
     */
    inline fun fallingRock(rockId: Long, onSettle: () -> Unit) {
        if (canMove(BOTTOM)) {
            // When current Rock can fall a unit, update its current locations accordingly
            currentRockLocations = currentRockLocations.map { chamberLocus ->
                chamberLocus.getNeighbourOrNull(BOTTOM)!!
            }
        } else {
            // When Rock cannot fall further, it has settled

            // Save finalized rock locations of the current rock on Chamber Grid
            currentRockLocations.forEach { chamberLocus ->
                set(addLocation(chamberLocus), ChamberSpaceData(ChamberSpaceType.ROCK, rockId))
            }

            // Invoke the callback to load the next Rock
            onSettle()
        }
    }

    /**
     * Loads next [Rock][newRock] into the Chamber to start this Rock's falling simulation
     */
    fun addRock(newRock: RockGrid) {
        // Get Height of the new Rock
        val rockHeight = newRock.rows

        // Get Peak row having rocks and adjust it for the new Rock
        val currentPeakRow = getPeakRowHavingRocks() - 3 // Minus 3 empty buffer rows

        // Include new Rock contents into the Chamber
        currentRockLocations = ((rockHeight - 1) downTo 0).flatMapIndexed { counter: Int, currentRowOfNewRock: Int ->
            // Chamber Row below the falling rock
            val chamberRow = currentPeakRow - (counter + 1)

            // Get current row locations of the new Rock from Rock Grid
            val currentRowLocationsOfNewRock = newRock.getRowLocations(currentRowOfNewRock)

            // Enumerate locations of the new Rock in relation to the Chamber. Start from Column Index of 2 as
            // each Rock appears two units away from the left wall of the Chamber
            (2 until columns).mapNotNull { chamberColumn ->
                // Get corresponding location from the Rock grid
                val currentRockLocusFromGrid = currentRowLocationsOfNewRock.find { rockLocus ->
                    // Find corresponding column location from the Rock grid
                    rockLocus.yPos == chamberColumn - 2
                }

                // When corresponding Rock location is found from the Rock grid, then
                // return its corresponding location from the Chamber; otherwise `null`
                if (currentRockLocusFromGrid != null && newRock[currentRockLocusFromGrid] == ChamberSpaceType.ROCK) {
                    getLocation(chamberRow, chamberColumn)
                } else {
                    null
                }
            }
        }
    }

    /**
     * Returns a [Long] of how tall the tower of Rocks is in the Chamber
     */
    fun getRockTowerHeight(): Long =
        getRowsHavingRocks().toIntRanges().single().rangeLength().toLong()

    /**
     * Returns a [String] representation of the Rocks settled in the Chamber from the given [ID][fromRockId] of a Rock
     */
    fun extractRockArrangement(fromRockId: Long): String {
        val toRow = getRockLocations(fromRockId).first().xPos
        val fromRow = getPeakRowHavingRocks()

        return (fromRow..toRow).joinToString(System.lineSeparator()) { row ->
            (0 until columns).map { column ->
                getLocation(row, column).toValue().type.type
            }.joinToString(EMPTY)
        }
    }

}

/**
 * Class to parse the input, analyze and solve the problem at hand.
 *
 * @property rockTypes [List] of Rocks represented as [RockGrid]s
 * @property chamberGrid A [Lattice] representing the Chamber
 * @property jetPattern [List] of [Char]s representing the pattern of the jets of hot gas in the Chamber
 */
private class PyroclasticFlowAnalyzer private constructor(
    private val rockTypes: List<RockGrid>,
    private val chamberGrid: ChamberGrid,
    private val jetPattern: List<Char>
) {

    companion object {

        // Constant for the width of the Chamber
        const val CHAMBER_SIZE = 7

        fun parse(input: List<String>): PyroclasticFlowAnalyzer =
            input.splitWhenLineBlankOrEmpty()
                .partition { lines: Iterable<String> ->
                    lines.first().any { it == GREATER_CHAR }
                }.let { (jetPatternPart: List<Iterable<String>>, rockTypesPart: List<Iterable<String>>) ->
                    PyroclasticFlowAnalyzer(
                        rockTypes = rockTypesPart.map { rockPattern: Iterable<String> ->
                            RockGrid(rockPattern.toList())
                        },
                        chamberGrid = ChamberGrid(CHAMBER_SIZE),
                        jetPattern = jetPatternPart.single().single().map { it }
                    )
                }
    }

    /**
     * [Solution for Part 1 and 2]
     *
     * Returns the height of the Tower formed by the Rocks settled in the Chamber for
     * the given [number of Rocks to settle][countOfRocksToSettle]
     */
    fun getRockTowerHeight(countOfRocksToSettle: Long): Long {
        var rockTypeIndex = 0
        var jetIndex = 0
        var rockId = 1L
        val rockTypesSize = rockTypes.size
        val jetPatternSize = jetPattern.size

        // First Rock ID of any repeated Rock arrangement found
        var startRockId = 0L
        // Difference between the first and last Rock ID of any repeated Rock arrangement found
        var rockIdDifference = 0L
        // Target Rock ID till which we need to simulate Rock falling
        var targetRockId = 0L

        // Map of chamber rock arrangements to list of Rock IDs captured at intervals of the first Rock Type
        val arrangementMap: MutableMap<String, MutableList<Long>> = mutableMapOf()
        // Map of Rock ID to its Tower Height
        val rockTowerHeightMap: MutableMap<Long, Long> = mutableMapOf()

        // Introduce the first Rock into the Chamber Grid
        chamberGrid.addRock(rockTypes[rockTypeIndex])

        // Process till the Target Rock ID is reached
        while (targetRockId == 0L || rockId <= targetRockId) {
            // Push falling rock with current jet of hot gas
            chamberGrid.pushRock(jetPattern[jetIndex].toCardinalDirection()).also {
                // Also, increment to next jet
                jetIndex = (jetIndex + 1) % jetPatternSize
            }

            // Simulate Rock falling
            chamberGrid.fallingRock(rockId) {
                // When falling Rock settles

                // Introduce the next Rock Type into the Chamber Grid
                rockTypeIndex = (rockTypeIndex + 1) % rockTypesSize
                chamberGrid.addRock(rockTypes[rockTypeIndex]).also {
                    // Also, increment Rock ID for the next Rock Type
                    rockId++
                }

                if (rockTypeIndex == 0) {
                    // When next Rock Type is the first Rock Type, extract Rock arrangement till last Rock
                    // and update to Map with its Rock ID
                    val arrangement = chamberGrid.extractRockArrangement(rockId - rockTypesSize)
                    arrangementMap[arrangement] = arrangementMap.getOrElse(arrangement) { mutableListOf() }.apply {
                        add(rockId - 1L)
                    }

                    if (arrangementMap[arrangement]!!.size > 1) {
                        // When Rock arrangement repeats

                        // Extract first Rock ID having the same arrangement and
                        // evaluate difference with the last Rock ID of same arrangement
                        startRockId = arrangementMap[arrangement]!!.first()
                        rockIdDifference = arrangementMap[arrangement]!!.last() - startRockId

                        // Evaluate if the current arrangement heuristics can solve for the given number of rocks to settle
                        if (((countOfRocksToSettle - countOfRocksToSettle % rockTypesSize) - startRockId) % rockIdDifference == 0L) {
                            // If it can solve, then compute the Target Rock ID to include the remaining rocks if any
                            // that does not resemble the arrangement in the end
                            targetRockId = startRockId + countOfRocksToSettle % rockTypesSize
                        }
                    }
                }

                // Update Map with the Tower Height changed by the settled Rock
                rockTowerHeightMap[rockId - 1] = chamberGrid.getRockTowerHeight()
            }

        }

        // For the first Rock ID and repeating interval of the arrangement found, compute the number of times
        // the rock arrangement repeats
        val repeatTimes =
            ((countOfRocksToSettle - countOfRocksToSettle % rockTypesSize) - startRockId) / rockIdDifference

        // Compute height difference between the last and first Rock ID of the repeating interval
        val heightDifference = rockTowerHeightMap[startRockId + rockIdDifference]!! - rockTowerHeightMap[startRockId]!!
        // Compute height of the remaining rocks if any that does not resemble the arrangement in the end
        val remainingRockTowerHeight = rockTowerHeightMap[targetRockId]!! - rockTowerHeightMap[startRockId]!!

        // Return the computed Tower Height of Rock arrangement for the given number of rocks to settle
        return rockTowerHeightMap[startRockId]!! + heightDifference * repeatTimes + remainingRockTowerHeight
    }
}