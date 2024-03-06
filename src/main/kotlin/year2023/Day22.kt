/**
 * Problem: Day22: Sand Slabs
 * https://adventofcode.com/2023/day/22
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import extensions.intersectRange

private class Day22 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 5
    println("=====")
    solveActual(1)      // 509
    println("=====")
    solveSample(2)      // 7
    println("=====")
    solveActual(2)      // 102770
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day22.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day22.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    FallingBricksAnalyzer.parse(input)
        .getCountOfSafelyDisintegrableBricks()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    FallingBricksAnalyzer.parse(input)
        .getSumOfTotalFallingBricksOnEachBestBrickDisintegrated()
        .also { println(it) }
}

/**
 * Class for the coordinate location of [Brick]s.
 *
 * @property x [Int] value of the coordinate position in X-plane.
 * @property y [Int] value of the coordinate position in Y-plane.
 * @property zInitial [Int] value of the coordinate position in Z-plane.
 */
private class BrickLocation private constructor(val x: Int, val y: Int, private val zInitial: Int) {

    companion object {
        fun create(coordinatesList: List<Int>): BrickLocation = BrickLocation(
            coordinatesList[0],
            coordinatesList[1],
            coordinatesList[2]
        )
    }

    // Since Z-position can change when the Brick falls, we expose only the new/updated Z-position
    var z: Int = zInitial
        private set

    /**
     * Updates Z-position to the given [new Z-position][newZ].
     */
    fun updateZ(newZ: Int) {
        z = newZ
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BrickLocation) return false

        if (x != other.x) return false
        if (y != other.y) return false
        // Only initial Z-position is considered since it will be unique
        if (zInitial != other.zInitial) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        // Only initial Z-position is considered since it will be unique
        result = 31 * result + zInitial
        return result
    }

    override fun toString(): String = "($x, $y, $z)"

}

/**
 * Class for Bricks which includes several functions that facilitate with repositioning of Bricks
 * and to establish its relation with other Bricks and also to simulate falling Bricks
 * when a particular Brick is disintegrated.
 *
 * @property start Start [BrickLocation]
 * @property end End inclusive [BrickLocation]
 */
private class Brick private constructor(val start: BrickLocation, val end: BrickLocation) {

    companion object {
        private val numbersRegex = """(\d+)""".toRegex()
        private const val TILDE = "~"

        fun parse(brickCoordinatesInput: String): Brick =
            brickCoordinatesInput.split(TILDE).let { splitBrickCoordinatesStrings ->
                val start = BrickLocation.create(
                    numbersRegex.findAll(splitBrickCoordinatesStrings[0]).map { it.groupValues[1] }
                        .map(String::toInt)
                        .toList()
                )
                val end = BrickLocation.create(
                    numbersRegex.findAll(splitBrickCoordinatesStrings[1]).map { it.groupValues[1] }
                        .map(String::toInt)
                        .toList()
                )

                Brick(
                    start = start,
                    end = end
                )
            }
    }

    // Identifier for the Brick
    val id = start.hashCode() + end.hashCode()

    // Range of positions occupied by the Brick in X-plane
    private val xRange = start.x..end.x

    // Range of positions occupied by the Brick in Y-plane
    private val yRange = start.y..end.y

    // Length of the Brick along the Z-plane
    val zSize = end.z - start.z + 1

    // Brick is a Z-Brick when its length along the Z-plane is more than 1
    val isZBrick = zSize > 1

    // List of Bricks that [this] Brick is being supported by
    private val supportedByBricks: MutableList<Brick> = mutableListOf()

    // List of Bricks that [this] Brick supports
    private val supportingBricks: MutableList<Brick> = mutableListOf()

    // A Brick is an Integral Brick when any Brick [this] Brick supports, is the Brick being
    // supported only by [this] Brick
    val isIntegralBrick: Boolean by lazy {
        supportingBricks.any { brickOnTop: Brick ->
            brickOnTop.supportedByBricks.size == 1
        }
    }

    /**
     * `This` Brick is said to support the [Brick on Top][brickOnTop] when they both share positions
     * along the X and Y plane.
     */
    fun canSupport(brickOnTop: Brick): Boolean =
        !xRange.intersectRange(brickOnTop.xRange).isEmpty() && !yRange.intersectRange(brickOnTop.yRange).isEmpty()

    /**
     * When `this` Brick can support the [Brick on Top][brickOnTop], this function is called to establish the support
     * relation between them. Basically, it updates the [supportedByBricks] of [brickOnTop] with `this` Brick
     * and the [supportingBricks] of `this` Brick with the [brickOnTop].
     */
    fun supports(brickOnTop: Brick) {
        brickOnTop.supportedByBricks += this
        supportingBricks += brickOnTop
    }

    /**
     * Repositions `this` Brick along the Z-plane according to the given [new starting position][newStartZ].
     */
    fun updateStartZ(newStartZ: Int) {
        // First, save the Length of the Brick along the Z-plane
        val zSize = zSize
        // Update the start Z-location of Brick with the given new position
        start.updateZ(newStartZ)
        // Update the end Z-location of Brick with the new start Z-location and same Brick length
        end.updateZ(start.z + zSize - 1)
    }

    /**
     * Disintegrates `this` integral Brick and then returns the total number of [Brick]s that will
     * fall on disintegration.
     *
     * This function simulates disintegration and falling of Bricks, and does not really update the data on [Brick]s.
     * It explores the [Brick]s that could fall on disintegration using Breadth-First Search technique.
     *
     * @param allBricks List of all [Brick]s present in the scenario.
     */
    fun disintegrate(allBricks: List<Brick>): Int {
        // Using two lists instead of a Queue as it is faster for items that are already initialized
        // and since Queue would be just holding Bricks that are at a distance of 'd' and 'd+1' only.
        // List of Bricks that are at a distance of 'd'. Initialized with an Integral Brick that will be
        // disintegrated to cause certain Bricks to fall.
        var currentFallingBricks: MutableList<Brick> = mutableListOf(this)
        // List of Bricks that are at a distance of 'd+1'
        val nextFallingBricks: MutableList<Brick> = mutableListOf()

        // A View Map of the Bricks on Top to the Bricks they are supported by
        val brickOnTopToSupportedByMap: MutableMap<Brick, MutableMap<Int, Brick>> =
            allBricks.filter { brick: Brick ->
                // Excluding the Bricks at the starting Z-location of '1', as we need only the Bricks on Top
                brick.start.z > 1
            }.associateWith { brickOnTop: Brick ->
                brickOnTop.supportedByBricks.associateBy { it.id }.toMutableMap()
            }.toMutableMap()

        // Counter for the Bricks falling. Initialized to '-1' as the process starts by disintegrating a Brick
        // that triggers the fall
        var countOfBricksFalling = -1

        // Repeat till the List holding Bricks at a distance of 'd' becomes empty
        while (currentFallingBricks.isNotEmpty()) {
            currentFallingBricks.forEach { currentBrick: Brick ->
                // For each current falling brick, increment its counter
                countOfBricksFalling++

                // For each Brick that current falling Brick supports, remove their support relation with current
                // falling Brick from the View Map in order to see which Brick ends up being not supported
                // by any Brick at all
                currentBrick.supportingBricks.forEach { brickOnTop: Brick ->
                    brickOnTopToSupportedByMap[brickOnTop]!!.remove(currentBrick.id)
                }

                brickOnTopToSupportedByMap.filterValues { supportedByBricksOfTopBrick: MutableMap<Int, Brick> ->
                    // Filter Bricks that are not supported by any Brick
                    supportedByBricksOfTopBrick.isEmpty()
                }.keys.forEach { unsupportedBrick: Brick ->
                    // Each of the Unsupported Brick will be the Next falling Brick, hence add it to
                    // the list of Next falling Bricks
                    nextFallingBricks.add(unsupportedBrick)
                    // Also, remove this unsupported Brick from the View Map
                    brickOnTopToSupportedByMap.remove(unsupportedBrick)
                }
            }

            // Copy over to Current falling Bricks and clear Next falling Bricks
            currentFallingBricks = nextFallingBricks.toMutableList()
            nextFallingBricks.clear()
        }

        // Return the count of Bricks that will fall on disintegration
        return countOfBricksFalling
    }

    override fun toString(): String = "Brick: $start $TILDE $end, isZBrick: $isZBrick"
}

private class FallingBricksAnalyzer private constructor(
    private val bricks: List<Brick>
) {
    companion object {
        fun parse(input: List<String>): FallingBricksAnalyzer = input.map { line ->
            Brick.parse(line)
        }.let { bricks: List<Brick> ->
            FallingBricksAnalyzer(bricks)
        }
    }

    // The lowest starting position of all Bricks along the Z-plane
    private val minZStart = bricks.minOf { it.start.z }

    // Map of Bricks by its starting position along the Z-plane
    private val zPositionToBricksMap: Map<Int, MutableMap<Int, Brick>> by lazy {
        // The highest starting position of all Bricks along the Z-plane
        val maxZStart = bricks.maxOf { it.start.z }

        bricks.groupBy { brick: Brick ->
            // Group bricks by its starting Z-position
            brick.start.z
        }.mapValues { (_: Int, bricks: List<Brick>) ->
            // On each group, convert list of Bricks into a Map of Bricks with their id as key
            bricks.associateBy { it.id }.toMutableMap()
        }.toMutableMap().apply {
            // Place an empty Map for unoccupied positions along the Z-plane
            (minZStart..maxZStart).forEach { zPosition: Int ->
                putIfAbsent(zPosition, mutableMapOf())
            }
        }.toSortedMap()// Sort resulting Map by Z-position so that it is convenient to simulate Brick falling
    }

    /**
     * Getter for the last occupied [Int] position along the Z-plane.
     */
    private val lastOccupiedZPosition: Int
        get() = zPositionToBricksMap.filterNot { (_: Int, bricks: MutableMap<Int, Brick>) ->
            // Exclude unoccupied/emptied positions
            bricks.isEmpty()
        }.keys.last() // Return the last occupied Z-position

    /**
     * Simulates Brick falling and settling. Updates Z-positions of all the [bricks] accordingly.
     */
    private fun simulateFall() {
        // Boolean to check if any Brick is still settling
        var isStillSettling = true

        // List of Bricks that needs to be removed from their Z-position in [zPositionToBricksMap] after update
        val bricksToRemoveAfterUpdate = mutableListOf<Brick>()

        /**
         * Lambda that returns [List] of [Z-Bricks][Brick.isZBrick] found occupying the given `zPosition`, that is,
         * the position along the Z-plane.
         */
        val zBricksAtPosition: (zPosition: Int) -> List<Brick> = { zPosition ->
            bricks.filter { brick: Brick ->
                brick.isZBrick && zPosition in brick.start.z..brick.end.z
            }
        }

        /**
         * Lambda that repositions Brick `brickToReposition` to new starting Z-position `newZPosition`.
         */
        val repositionBrick: (
            brickToReposition: Brick,
            newZPosition: Int
        ) -> Unit = { brickToReposition, newZPosition ->
            // Add the brick to the new Z-position in [zPositionToBricksMap]
            zPositionToBricksMap[newZPosition]!![brickToReposition.id] = brickToReposition
            // Remove the brick from its previous Z-position in [zPositionToBricksMap] by adding it
            // to the remove list `bricksToRemoveAfterUpdate` which takes care of it later
            bricksToRemoveAfterUpdate += brickToReposition
            // Update the brick's starting position along the Z-plane
            brickToReposition.updateStartZ(newZPosition)
        }

        // Repeat till all the Bricks have settled
        while (isStillSettling) {
            // Reset boolean to `false`, to check if any Brick is still settling
            isStillSettling = false

            // Scan all Z-positions starting from the lowest to the last occupied position
            (minZStart..lastOccupiedZPosition).forEach { currentZPosition ->
                zPositionToBricksMap.filterNot { (_: Int, bricks: MutableMap<Int, Brick>) ->
                    // Exclude unoccupied/emptied positions in between the selected range of Z-positions
                    bricks.isEmpty()
                }.keys.firstOrNull { nextZPosition ->
                    // Pick the first occupied Z-position if any above the current Z-position
                    nextZPosition > currentZPosition
                }.takeUnless { it == null }?.let { nextZPosition ->
                    zPositionToBricksMap[nextZPosition]!!.values.forEach { nextZPositionBrick ->
                        // For each brick occupying the Z-position above the current Z-position

                        // Get Z-Bricks occupying the current Z-position and then combine with
                        // current Z-position bricks to check if the brick above can settle
                        val currentZPositionBricks: List<Brick> =
                            zBricksAtPosition(currentZPosition) + zPositionToBricksMap[currentZPosition]!!.values

                        // Check if the brick above can settle
                        val canSettle = currentZPositionBricks.none { currentZPositionBrick: Brick ->
                            // Brick above can settle if and only if all the current Z-position bricks do NOT
                            // share the same positions along X and Y plane with that of the Brick above
                            currentZPositionBrick.canSupport(nextZPositionBrick)
                        }

                        if (canSettle) {
                            // When the brick above can settle

                            // Reposition the brick above to current Z-position
                            repositionBrick(nextZPositionBrick, currentZPosition)
                            // Set the Boolean to `true`, since we found a brick that just settled
                            isStillSettling = true
                        } else {
                            // When the brick above cannot settle and hence needs to be supported

                            // Get the Brick that can support the Brick above
                            val supportingBrick = currentZPositionBricks.first { currentZPositionBrick: Brick ->
                                currentZPositionBrick.canSupport(nextZPositionBrick)
                            }

                            // Compute the supporting Brick's occupied length along the Z-plane
                            val supportingBrickZSize = if (supportingBrick.isZBrick) {
                                // When supporting Brick is a Z-Brick, take the length from
                                // the current Z-position to the Brick's end Z-position
                                supportingBrick.end.z - currentZPosition + 1
                            } else {
                                // When supporting Brick is NOT a Z-Brick, take the existing occupied length
                                // along the Z-plane
                                supportingBrick.zSize
                            }

                            // Current Z-position plus the supporting Brick's occupied length, gives the new Z-position
                            // where the Brick being supported will have to be repositioned to
                            (currentZPosition + supportingBrickZSize).let { newZPosition ->
                                // Repositioning is necessary only if the new Z-position falls in between
                                // the current Z-position and the next Z-position being checked
                                if (newZPosition < nextZPosition) {
                                    // Reposition the brick being supported to the new Z-position
                                    repositionBrick(nextZPositionBrick, newZPosition)
                                }
                            }
                        }
                    }

                    // Remove all the bricks to be removed from their previous Z-position in [zPositionToBricksMap]
                    bricksToRemoveAfterUpdate.forEach { brickToRemove ->
                        zPositionToBricksMap[nextZPosition]!!.remove(brickToRemove.id)
                    }

                    // Clear the list of bricks to be removed for processing the remaining occupied Z-position
                    bricksToRemoveAfterUpdate.clear()
                }
            }
        }
    }

    /**
     * Builds support relation between all [bricks].
     */
    private fun buildBrickSupportRelation() {
        // Scan all Z-positions starting from the lowest to the last occupied position
        (minZStart..lastOccupiedZPosition).forEach { currentZPosition: Int ->
            zPositionToBricksMap[currentZPosition]!!.values.forEach { currentZPositionBrick: Brick ->
                // For each Brick from the current Z-position, get the bricks on top based on the size
                // of the current Z-position Brick along the Z-plane
                zPositionToBricksMap[currentZPosition + currentZPositionBrick.zSize]!!.values.forEach { brickOnTop: Brick ->
                    // Establish relation for each Brick on top of the current Z-position Brick, based on whether
                    // the current Z-position Brick can support the Brick on top
                    if (currentZPositionBrick.canSupport(brickOnTop)) {
                        // When it can support the Brick on top, establish the said relation
                        currentZPositionBrick.supports(brickOnTop)
                    }
                }
            }
        }
    }

    /**
     * [Solution for Part-1]
     *
     * Returns the [Int] total number of [Brick]s that can be safely disintegrated
     * after all the [bricks] have fallen and settled.
     */
    fun getCountOfSafelyDisintegrableBricks(): Int {
        // Reposition Bricks based on how they would fall and settle
        simulateFall()

        // Establish support relation between all bricks
        buildBrickSupportRelation()

        // From the total number of bricks present, exclude the count of bricks that are integral
        // since integral bricks when disintegrated will cause all the unsupported bricks to fall
        return bricks.size - bricks.count { brick: Brick -> brick.isIntegralBrick }
    }

    /**
     * [Solution for Part-2]
     *
     * Returns the [Int] Sum of the number of Bricks that would fall in each case of disintegrating
     * the best brick that triggers a chain reaction of falling bricks.
     */
    fun getSumOfTotalFallingBricksOnEachBestBrickDisintegrated(): Int {
        // Reposition Bricks based on how they would fall and settle
        simulateFall()

        // Establish support relation between all bricks
        buildBrickSupportRelation()

        // Return the Sum of the number of bricks that will fall in each case of disintegrating the best brick
        return bricks.filter { brick: Brick ->
            // The best brick to disintegrate such that it will cause many unsupported bricks to fall
            // is the Integral brick. So, select only the Integral bricks to disintegrate
            brick.isIntegralBrick
        }.sumOf { integralBrick: Brick ->
            // Disintegrate each Integral brick to get the total number of bricks that will fall
            integralBrick.disintegrate(bricks)
        }
    }

}