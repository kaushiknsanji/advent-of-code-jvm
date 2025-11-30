/**
 * Problem: Day11: Dumbo Octopus
 * https://adventofcode.com/2021/day/11
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2021

import base.BaseFileHandler
import utils.grid.OmniDirection.*
import utils.grid.Point2D
import utils.grid.OmniDirection as Direction

private class Day11 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)  // 1656
    println("=====")
    solveActual(1)  // 1640
    println("=====")
    solveSample(2)  // 195
    println("=====")
    solveActual(2)  // 312
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day11.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day11.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    OctopusCavern.parse(input)
        .getTotalEnergyFlashesAfter(100)
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    OctopusCavern.parse(input)
        .getFirstStepOfAllOctopusesFlashingAtSameTime()
        .also { println(it) }
}

private class OctopusLocus(val x: Int, val y: Int) : Point2D<Int>(x, y)

private interface EnergyLevelGrid {
    fun getOctopusLocusOrNull(x: Int, y: Int): OctopusLocus?
    fun getOctopusLocus(x: Int, y: Int): OctopusLocus
    fun getAllOctopusLocations(): Collection<OctopusLocus>
    fun getEnergyLevelsOfAllOctopuses(): Collection<Int>
    fun getTotalCountOfOctopuses(): Int
    fun OctopusLocus.getNeighbour(direction: Direction): OctopusLocus?
    fun OctopusLocus.getAllNeighbours(): Collection<OctopusLocus>
}

private class OctopusEnergyLevelGrid private constructor(
    val rows: Int,
    val columns: Int,
    energyLevelGridList: List<List<Int>>
) : EnergyLevelGrid {

    constructor(input: List<String>) : this(
        input.size,
        input[0].length,
        input.map { line -> line.map { it.digitToInt() } }
    )

    private val octopusLocusGridMap: Map<Int, List<OctopusLocus>> = (0 until rows).flatMap { x ->
        (0 until columns).map { y ->
            OctopusLocus(x, y)
        }
    }.groupBy { octopusLocus: OctopusLocus -> octopusLocus.x }

    private val octopusEnergyLevelGridMap: MutableMap<OctopusLocus, Int> =
        energyLevelGridList.map { it.withIndex() }.withIndex().flatMap { (x: Int, indexedEnergyLevels) ->
            indexedEnergyLevels.map { (y: Int, energyLevel) ->
                getOctopusLocus(x, y) to energyLevel
            }
        }.associate { it.first to it.second }.toMutableMap()

    override fun getOctopusLocusOrNull(x: Int, y: Int): OctopusLocus? = try {
        octopusLocusGridMap[x]?.get(y)
    } catch (e: IndexOutOfBoundsException) {
        null
    }

    override fun getOctopusLocus(x: Int, y: Int): OctopusLocus =
        getOctopusLocusOrNull(x, y) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${OctopusLocus::class.simpleName} at the given location ($x, $y)"
        )

    operator fun set(locus: OctopusLocus, energyLevel: Int) {
        octopusEnergyLevelGridMap[locus] = energyLevel
    }

    operator fun get(locus: OctopusLocus): Int = octopusEnergyLevelGridMap[locus]!!

    override fun getAllOctopusLocations(): Collection<OctopusLocus> = octopusLocusGridMap.values.flatten()

    override fun getEnergyLevelsOfAllOctopuses(): Collection<Int> = octopusEnergyLevelGridMap.values

    override fun getTotalCountOfOctopuses(): Int = rows * columns

    override fun OctopusLocus.getNeighbour(direction: Direction): OctopusLocus? = when (direction) {
        TOP -> getOctopusLocusOrNull(x - 1, y)
        BOTTOM -> getOctopusLocusOrNull(x + 1, y)
        RIGHT -> getOctopusLocusOrNull(x, y + 1)
        LEFT -> getOctopusLocusOrNull(x, y - 1)
        TOP_LEFT -> getOctopusLocusOrNull(x - 1, y - 1)
        TOP_RIGHT -> getOctopusLocusOrNull(x - 1, y + 1)
        BOTTOM_LEFT -> getOctopusLocusOrNull(x + 1, y - 1)
        BOTTOM_RIGHT -> getOctopusLocusOrNull(x + 1, y + 1)
    }

    override fun OctopusLocus.getAllNeighbours(): Collection<OctopusLocus> =
        Direction.values().mapNotNull { direction: Direction -> getNeighbour(direction) }

    override fun toString(): String =
        (0 until rows).joinToString("\n") { x ->
            (0 until columns).joinToString(separator = "") { y ->
                "${octopusEnergyLevelGridMap[getOctopusLocus(x, y)]}"
            }
        }

}

private class OctopusCavern private constructor(
    private val octopusEnergyLevelGrid: OctopusEnergyLevelGrid
) : EnergyLevelGrid by octopusEnergyLevelGrid {

    companion object {
        const val OCTOPUS_ENERGY_FLASH_THRESHOLD = 9
        const val OCTOPUS_ENERGY_RESET = 0

        fun parse(input: List<String>): OctopusCavern = OctopusCavern(OctopusEnergyLevelGrid(input))
    }

    private fun Collection<OctopusLocus>.incrementEnergyLevels(): Collection<OctopusLocus> =
        this.onEach { octopusLocus ->
            octopusEnergyLevelGrid[octopusLocus] += 1
        }

    private fun Collection<OctopusLocus>.filterEnergyFlashLevels(): Collection<OctopusLocus> =
        this.filter { octopusLocus ->
            octopusEnergyLevelGrid[octopusLocus] > OCTOPUS_ENERGY_FLASH_THRESHOLD
        }

    private fun Collection<OctopusLocus>.resetEnergyLevels(): Collection<OctopusLocus> = this.onEach { octopusLocus ->
        octopusEnergyLevelGrid[octopusLocus] = OCTOPUS_ENERGY_RESET
    }

    private fun computeCountOfEnergyFlashesForSingleStep(): Int =
        getAllOctopusLocations()
            .incrementEnergyLevels() // Increment energy levels of all Octopuses
            .filterEnergyFlashLevels() // Filter for those with very high energy greater than level 9
            .takeUnless { it.isEmpty() }?.let { highEnergyOctoLocuses ->
                // Save off the list of Octopuses processed for their high energy levels
                mutableListOf<OctopusLocus>().apply {
                    // Mark the current filtered list of Octopuses as "to be processed" and
                    // add any Octopuses with high energy which may occur for further processing
                    // when current high energy Octopuses are being processed
                    val locusToBeProcessedList = highEnergyOctoLocuses.toMutableList()
                    while (locusToBeProcessedList.isNotEmpty()) {
                        // Remove the first Octopus to process for flashing
                        val processingLocus = locusToBeProcessedList.removeFirst()
                        // Retrieve and add all adjacent Octopuses (to the "to be processed" list) that started flashing
                        // as a result of the current Octopus flashing
                        locusToBeProcessedList.addAll(
                            0,
                            processingLocus.getAllNeighbours()
                                .filterNot { neighbourLocus ->
                                    // Do not consider the Octopuses that have been already found to be flashing
                                    neighbourLocus in (locusToBeProcessedList + this)
                                }
                                .incrementEnergyLevels()
                                .filterEnergyFlashLevels()
                        )
                        // Mark current Octopus as processed by adding to the "processed list"
                        this.add(processingLocus)
                    }
                }
            }?.resetEnergyLevels()?.count()
            ?: 0  // Reset energy levels of all Octopuses that flashed and return their count

    /**
     * [Solution for Part-1]
     * Returns the Total number of energy flashes of Octopuses after the given number of [steps].
     */
    fun getTotalEnergyFlashesAfter(steps: Int) = generateSequence {
        computeCountOfEnergyFlashesForSingleStep()
    }.take(steps).sum()

    /**
     * [Solution for Part-2]
     * Returns the step count of the first occurrence of all Octopuses flashing at the same time.
     */
    fun getFirstStepOfAllOctopusesFlashingAtSameTime(): Int = generateSequence {
        computeCountOfEnergyFlashesForSingleStep()
    }.withIndex().first { indexedCount ->
        indexedCount.value == getTotalCountOfOctopuses()
    }.index + 1
}