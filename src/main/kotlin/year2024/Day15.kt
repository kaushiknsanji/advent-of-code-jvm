/**
 * Problem: Day15: Warehouse Woes
 * https://adventofcode.com/2024/day/15
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler
import utils.Constants.AT_CHAR
import utils.Constants.DOT_CHAR
import utils.Constants.EMPTY
import utils.Constants.HASH_CHAR
import utils.Constants.O_CAP_CHAR
import utils.Constants.SQUARE_CLOSE_BRACE_CHAR
import utils.Constants.SQUARE_OPEN_BRACE_CHAR
import utils.grid.CardinalDirection.*
import utils.grid.ILattice
import utils.grid.Lattice
import utils.grid.Point2d
import utils.grid.toCardinalDirection
import utils.splitWhenLineBlankOrEmpty
import utils.grid.CardinalDirection as Direction

private class Day15 : BaseProblemHandler() {

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
        FishWarehouseAnalyzer.parse(input)
            .getTotalBoxGPSCoordinates()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        FishWarehouseAnalyzer.parse(input, isWarehouseWide = true)
            .getTotalBoxGPSCoordinates()

}

fun main() {
    with(Day15()) {
        solveSample(1, false, 0, 10092)
        solveActual(1, false, 0, 1526673)
        solveSample(2, false, 0, 9021)
        solveActual(2, false, 0, 1535509)
    }
}

private enum class FishWarehouseType(val type: Char) {
    ROBOT(AT_CHAR),
    BOX(O_CAP_CHAR),
    BOX_LEFT(SQUARE_OPEN_BRACE_CHAR),
    BOX_RIGHT(SQUARE_CLOSE_BRACE_CHAR),
    WALL(HASH_CHAR),
    SPACE(DOT_CHAR);

    companion object {
        private val typeMap = entries.associateBy(FishWarehouseType::type)

        fun fromType(type: Char): FishWarehouseType = typeMap[type]!!
    }
}

private class FishWarehouseLocation(x: Int, y: Int) : Point2d<Int>(x, y)

private class FishWarehouseGrid(
    pattern: List<String>
) : Lattice<FishWarehouseLocation, FishWarehouseType>(pattern) {

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value of location's row
     * @param column [Int] value of location's column
     */
    override fun provideLocation(row: Int, column: Int): FishWarehouseLocation =
        FishWarehouseLocation(row, column)

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar [Char] found at a location in the input pattern
     */
    override fun provideValue(locationChar: Char): FishWarehouseType =
        FishWarehouseType.fromType(locationChar)

}

private class FishWarehouseAnalyzer private constructor(
    private val fishWarehouseGrid: FishWarehouseGrid,
    private val robotDirections: List<Direction>,
    private val isWarehouseWide: Boolean
) : ILattice<FishWarehouseLocation, FishWarehouseType> by fishWarehouseGrid {

    companion object {

        // Map to expand the grid for when the Warehouse is set to be twice as wide
        private val gridExpansionMap = mapOf(
            HASH_CHAR to "${HASH_CHAR}${HASH_CHAR}",
            O_CAP_CHAR to "${SQUARE_OPEN_BRACE_CHAR}${SQUARE_CLOSE_BRACE_CHAR}",
            DOT_CHAR to "${DOT_CHAR}${DOT_CHAR}",
            AT_CHAR to "${AT_CHAR}${DOT_CHAR}"
        )

        fun parse(input: List<String>, isWarehouseWide: Boolean = false): FishWarehouseAnalyzer =
            input.splitWhenLineBlankOrEmpty().let { splitBlocks: Iterable<Iterable<String>> ->
                FishWarehouseAnalyzer(
                    fishWarehouseGrid = splitBlocks.first().toList().let { pattern: List<String> ->
                        if (isWarehouseWide) {
                            // When Warehouse is said to be twice as wide, expand characters in the pattern
                            // as per the [gridExpansionMap]
                            FishWarehouseGrid(
                                pattern.map { patternLine: String ->
                                    patternLine.map { valueChar ->
                                        gridExpansionMap[valueChar]!!
                                    }.joinToString(EMPTY)
                                }
                            )
                        } else {
                            // When Warehouse is of normal size, use pattern AS-IS
                            FishWarehouseGrid(pattern)
                        }
                    },

                    robotDirections = splitBlocks.last().joinToString(EMPTY).map(Char::toCardinalDirection),
                    isWarehouseWide
                )
            }
    }

    // List of [FishWarehouseType]s that represent a Box in a Wider Warehouse
    private val boxPartLocationTypes = listOf(
        FishWarehouseType.BOX_LEFT,
        FishWarehouseType.BOX_RIGHT
    )

    /**
     * Returns all current [FishWarehouseType.BOX] or [FishWarehouseType.BOX_LEFT] locations present
     * in the [fishWarehouseGrid] depending on [isWarehouseWide].
     */
    private fun getAllBoxLocations(): List<FishWarehouseLocation> =
        getAllLocations().filter { location ->
            if (isWarehouseWide) {
                location.toValue() == FishWarehouseType.BOX_LEFT
            } else {
                location.toValue() == FishWarehouseType.BOX
            }
        }

    /**
     * Moves Robot into the neighbouring location of current [robotLocation] in the direction of [directionToMove]
     * and returns its new [location][FishWarehouseLocation]. Updates old [robotLocation] to
     * contain [FishWarehouseType.SPACE].
     */
    private fun moveRobotIntoSpace(
        robotLocation: FishWarehouseLocation,
        directionToMove: Direction
    ): FishWarehouseLocation = robotLocation.getNeighbourOrNull(directionToMove)!!.apply {
        swap(robotLocation, this)
    }

    /**
     * Returns a [List] of [FishWarehouseLocation] containing Boxes that can be pushed by the Robot
     * from the current [robotLocation] in the direction of [directionToMove], and assuming boxes to be in its
     * simple form of [FishWarehouseType.BOX] even in a wider Warehouse, which works only when these boxes
     * are pushed in the directions of [LEFT] or [RIGHT].
     *
     * Can return `null` in case a location is found with a [FishWarehouseType.WALL] right after
     * the last box location in the direction of [directionToMove], as boxes cannot be pushed.
     *
     * @throws IllegalArgumentException in the case of [isWarehouseWide] and [directionToMove] is not [LEFT] or [RIGHT]
     */
    private fun getBoxesToPushInSimpleForm(
        robotLocation: FishWarehouseLocation,
        directionToMove: Direction
    ): List<FishWarehouseLocation>? {
        // Verify direction passed in case of a wider warehouse
        if (isWarehouseWide) {
            require(directionToMove in listOf(LEFT, RIGHT)) {
                "ERROR: In a Wider Warehouse, Boxes can be pushed assuming its simple form, only " +
                        "in the directions of $LEFT or $RIGHT, but was $directionToMove"
            }
        }

        // Get all boxes in direction till we hit a WALL or find a SPACE
        val boxes = robotLocation.getLocationsInDirection(directionToMove).drop(1)
            .takeWhile { fishWarehouseLocation ->
                fishWarehouseLocation.toValue() != FishWarehouseType.WALL &&
                        fishWarehouseLocation.toValue() != FishWarehouseType.SPACE
            }.toList()

        return if (boxes.last().getNeighbourOrNull(directionToMove)!!.toValue() == FishWarehouseType.WALL) {
            // Return NULL if the next neighbour of last box found is a WALL, as boxes cannot be pushed
            null
        } else {
            // Return all boxes found if the next neighbour of last box found is a SPACE, as boxes can be pushed
            boxes
        }
    }

    /**
     * Returns a [Pair] of [FishWarehouseLocation]s that represents a Box in a Wider warehouse
     * for a given [location with part of the Box][boxPartLocation].
     *
     * @throws IllegalArgumentException when [boxPartLocation] content is not any of [boxPartLocationTypes].
     */
    private fun getExpandedBoxAsPair(
        boxPartLocation: FishWarehouseLocation
    ): Pair<FishWarehouseLocation, FishWarehouseLocation> {
        require(boxPartLocation.toValue() in boxPartLocationTypes) {
            "ERROR: Location $boxPartLocation is not part of a box, rather it is a ${boxPartLocation.toValue()}"
        }

        return if (boxPartLocation.toValue() == FishWarehouseType.BOX_LEFT) {
            boxPartLocation to boxPartLocation.getNeighbourOrNull(RIGHT)!!
        } else {
            boxPartLocation.getNeighbourOrNull(LEFT)!! to boxPartLocation
        }
    }

    /**
     * Returns a [List] of [FishWarehouseLocation] Pairs containing Boxes in a Wider warehouse, that can be pushed
     * by the Robot from the current [robotLocation] in the direction of [directionToMove].
     *
     * Can return `null` in case any location is found with a [FishWarehouseType.WALL] right after a group of
     * locations containing boxes in the direction of [directionToMove], as boxes cannot be pushed.
     */
    private fun getBoxesToPushInExpandedForm(
        robotLocation: FishWarehouseLocation,
        directionToMove: Direction
    ): List<Pair<FishWarehouseLocation, FishWarehouseLocation>>? {
        // Get the first Box location pair from the current [robotLocation] in the direction [directionToMove]
        val firstBoxPair: Pair<FishWarehouseLocation, FishWarehouseLocation> =
            getExpandedBoxAsPair(robotLocation.getNeighbourOrNull(directionToMove)!!)

        // Using two Lists for Frontier instead of a Queue as it is faster since Queue would be just
        // holding Box Location Pairs that are at a distance of 'd' and 'd+1' only.
        // Frontier list of Box Location Pairs that are at a distance of 'd'. Begin with first Box location pair found.
        var currentFrontier: MutableList<Pair<FishWarehouseLocation, FishWarehouseLocation>> =
            mutableListOf(firstBoxPair)

        // Frontier list of Box Location Pairs that are at a distance of 'd + 1'
        val nextFrontier: MutableList<Pair<FishWarehouseLocation, FishWarehouseLocation>> = mutableListOf()

        // A Set to keep track of Box Location Pairs last visited
        val boxVisitedSet: MutableSet<Pair<FishWarehouseLocation, FishWarehouseLocation>> = mutableSetOf()

        // Lambda to update 'nextFrontier' with Box Location Pairs of the given 'location'
        val updateNextFrontier: (location: FishWarehouseLocation) -> Unit = { location: FishWarehouseLocation ->
            if (location.toValue() in boxPartLocationTypes) {
                // When 'location' contains part of a Box, get the complete Box locations as Pair
                // and update it to 'nextFrontier'
                nextFrontier.add(getExpandedBoxAsPair(location))
            } else {
                // When 'location' does not contain any part of a Box, just update it to 'nextFrontier' as Pair.
                // This will be needed to find if any of such locations contain a WALL
                nextFrontier.add(
                    location to location
                )
            }
        }

        // Loop till the Frontier holds any Box location Pairs at distance 'd'
        while (
            currentFrontier.flatMap { boxPair -> boxPair.toList() }.any { location ->
                location.toValue() in boxPartLocationTypes
            }
        ) {
            currentFrontier.filterNot { boxPair ->
                // Evaluate only those box location pairs that are not yet visited
                boxPair in boxVisitedSet
            }.forEach { (leftLocation, rightLocation) ->
                if (leftLocation.toValue() !in boxPartLocationTypes) {
                    // When 'leftLocation' does not contain any part of a Box, just add it to 'nextFrontier'
                    // as it will be needed to find if any of such locations contain a WALL
                    updateNextFrontier(leftLocation)
                } else {
                    // When 'leftLocation' contains part of a Box, extract complete Box locations as Pair
                    // for both 'leftLocation' and 'rightLocation', and then update it to 'nextFrontier'
                    updateNextFrontier(leftLocation.getNeighbourOrNull(directionToMove)!!)
                    updateNextFrontier(rightLocation.getNeighbourOrNull(directionToMove)!!)
                }

                // When 'leftLocation' contains part of a Box, mark these Box location Pairs as visited
                if (leftLocation.toValue() in boxPartLocationTypes) {
                    boxVisitedSet.add(leftLocation to rightLocation)
                }
            }

            // Copy over to Current Frontier and clear Next Frontier
            currentFrontier = nextFrontier.toMutableList()
            nextFrontier.clear()
        }

        return if (
            currentFrontier.flatMap { boxPair -> boxPair.toList() }.any { location ->
                location.toValue() == FishWarehouseType.WALL
            }
        ) {
            // Return NULL if any of the next neighbour of the group of boxes found is a WALL,
            // as boxes cannot be pushed
            null
        } else {
            // Return all boxes if all the next neighbours of the group of boxes found is a SPACE,
            // as boxes can be pushed
            boxVisitedSet.toList()
        }
    }

    /**
     * Tasks the Robot with moving and pushing boxes around as per the sequence of directions
     * given in [robotDirections] from the current [robotLocation].
     *
     * @throws Error when Robot encounters another Robot!
     */
    private fun executeMoves(robotLocation: FishWarehouseLocation) {
        var currentRobotLocation = robotLocation

        robotDirections.forEach { directionToMove: Direction ->
            // For each direction, take action based on the content of the next neighbour of current robot location
            when (currentRobotLocation.getNeighbourOrNull(directionToMove)!!.toValue()) {
                FishWarehouseType.SPACE -> {
                    // When next location is a Space, move Robot into this location
                    currentRobotLocation = moveRobotIntoSpace(currentRobotLocation, directionToMove)
                }

                FishWarehouseType.BOX, FishWarehouseType.BOX_LEFT, FishWarehouseType.BOX_RIGHT -> {
                    // When next location is a Box or part of an expanded Box as in a Wider warehouse

                    if (isWarehouseWide && directionToMove in listOf(TOP, BOTTOM)) {
                        // In a Wider warehouse, when the direction to move is either TOP or BOTTOM

                        // Get all boxes first
                        val boxPairs = getBoxesToPushInExpandedForm(currentRobotLocation, directionToMove)

                        if (boxPairs != null) {
                            // When Robot can push the boxes found, start from last box location pair and
                            // swap each location content with its next neighbour in the direction of 'directionToMove'
                            (boxPairs.lastIndex downTo 0).forEach { index ->
                                boxPairs[index].toList().forEach { boxPartLocation ->
                                    swap(boxPartLocation, boxPartLocation.getNeighbourOrNull(directionToMove)!!)
                                }
                            }

                            // Next location will be a Space after above swap, so move Robot into this location
                            currentRobotLocation = moveRobotIntoSpace(currentRobotLocation, directionToMove)
                        }
                    } else {
                        // In a Wider warehouse, when the direction to move is either LEFT or RIGHT
                        // or In a normal warehouse, with any direction to move to

                        // Get all boxes first
                        val boxes = getBoxesToPushInSimpleForm(currentRobotLocation, directionToMove)

                        if (boxes != null) {
                            // When Robot can push the boxes found, start from last box location and
                            // swap each location content with its next neighbour in the direction of 'directionToMove'
                            (boxes.lastIndex downTo 0).forEach { index ->
                                swap(boxes[index], boxes[index].getNeighbourOrNull(directionToMove)!!)
                            }

                            // Next location will be a Space after above swap, so move Robot into this location
                            currentRobotLocation = moveRobotIntoSpace(currentRobotLocation, directionToMove)
                        }

                    }

                }

                FishWarehouseType.WALL -> {
                    // Silly Robot hit a Wall! No operation here
                }

                else -> throw Error("There should be only one Robot!")
            }

        }
    }

    /**
     * [Solution for Part 1 and 2]
     *
     * Returns the total of all Boxes' GPS coordinates.
     */
    fun getTotalBoxGPSCoordinates(): Int =
        getAllLocations().single { location -> location.toValue() == FishWarehouseType.ROBOT }
            .let { robotLocation ->

                // Let Robot do the work as per its sequence of directions
                executeMoves(robotLocation)

                // From all current Box locations, return the sum of their GPS coordinates
                getAllBoxLocations().sumOf { boxLocation ->
                    boxLocation.xPos * 100 + boxLocation.yPos
                }
            }

}