/**
 * Problem: Day11: Cosmic Expansion
 * https://adventofcode.com/2023/day/11
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import kotlin.math.abs

private class Day11 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1, 2)      // 374
    println("=====")
    solveActual(1, 2)      // 9965032
    println("=====")
    solveSample(2, 10)      // 1030
    println("=====")
    solveSample(2, 100)     // 8410
    println("=====")
    solveActual(2, 1_000_000)      // 550358864332
    println("=====")
}

private fun solveSample(executeProblemPart: Int, expansionRate: Int) {
    execute(Day11.getSampleFile().readLines(), executeProblemPart, expansionRate)
}

private fun solveActual(executeProblemPart: Int, expansionRate: Int) {
    execute(Day11.getActualTestFile().readLines(), executeProblemPart, expansionRate)
}

private fun execute(input: List<String>, executeProblemPart: Int, expansionRate: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input, expansionRate)
        2 -> doPart2(input, expansionRate)
    }
}

private fun doPart1(input: List<String>, expansionRate: Int) {
    CosmicExpansionProcessor.parse(input)
        .getSumOfShortestDistancesBetweenGalaxyPairs(expansionRate)
        .also { println(it) }
}

private fun doPart2(input: List<String>, expansionRate: Int) {
    CosmicExpansionProcessor.parse(input)
        .getSumOfShortestDistancesBetweenGalaxyPairs(expansionRate)
        .also { println(it) }
}

private class CosmicLocation(val x: Int, val y: Int)

private enum class CosmicType(val type: Char) {
    GALAXY('#'),
    EMPTY_SPACE('.')
}

private interface ICosmicGrid {
    fun getCosmicLocationOrNull(row: Int, column: Int): CosmicLocation?
    fun getCosmicLocation(row: Int, column: Int): CosmicLocation
    fun getAllCosmicLocations(): Collection<CosmicLocation>
    fun getAllGalaxyTypeLocations(): Collection<CosmicLocation>
    fun getAllEmptySpaceRowIndexes(): Collection<Int>
    fun getAllEmptySpaceColumnIndexes(): Collection<Int>
}

private class CosmicGrid private constructor(
    rows: Int, columns: Int, cosmicPatternList: List<String>
) : ICosmicGrid {

    constructor(cosmicPatternList: List<String>) : this(
        rows = cosmicPatternList.size,
        columns = cosmicPatternList.first().length,
        cosmicPatternList = cosmicPatternList
    )

    private val cosmicGridMap: Map<Int, List<CosmicLocation>> = (0 until rows).flatMap { row ->
        (0 until columns).map { column ->
            CosmicLocation(row, column)
        }
    }.groupBy { cosmicLocation: CosmicLocation -> cosmicLocation.x }

    private val cosmicGridValueMap: Map<CosmicLocation, CosmicType> =
        cosmicPatternList.flatMapIndexed { row: Int, pattern: String ->
            pattern.mapIndexed { column: Int, cosmicCharacter: Char ->
                getCosmicLocation(row, column) to CosmicType.entries.single { it.type == cosmicCharacter }
            }
        }.toMap()

    operator fun get(cosmicLocation: CosmicLocation): CosmicType = cosmicGridValueMap[cosmicLocation]!!

    override fun getCosmicLocationOrNull(row: Int, column: Int): CosmicLocation? = try {
        cosmicGridMap[row]?.get(column)
    } catch (e: IndexOutOfBoundsException) {
        null
    }

    override fun getCosmicLocation(row: Int, column: Int): CosmicLocation =
        getCosmicLocationOrNull(row, column) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${CosmicLocation::class.simpleName} at the given location ($row, $column)"
        )

    override fun getAllCosmicLocations(): Collection<CosmicLocation> =
        cosmicGridMap.values.flatten()

    override fun getAllGalaxyTypeLocations(): Collection<CosmicLocation> =
        getAllCosmicLocations().filter { cosmicLocation ->
            this[cosmicLocation] == CosmicType.GALAXY
        }

    override fun getAllEmptySpaceRowIndexes(): Collection<Int> =
        cosmicGridValueMap.map { entry ->
            Triple(entry.key.x, entry.key.y, entry.value)
        }.groupBy { (rowIndex, _, _) ->
            rowIndex
        }.filterValues { valuesByRowIndex: List<Triple<Int, Int, CosmicType>> ->
            valuesByRowIndex.all { (_, _, columnValue) ->
                columnValue == CosmicType.EMPTY_SPACE
            }
        }.keys


    override fun getAllEmptySpaceColumnIndexes(): Collection<Int> =
        cosmicGridValueMap.map { entry ->
            Triple(entry.key.x, entry.key.y, entry.value)
        }.groupBy { (_, columnIndex, _) ->
            columnIndex
        }.filterValues { valuesByColumnIndex: List<Triple<Int, Int, CosmicType>> ->
            valuesByColumnIndex.all { (_, _, columnValue) ->
                columnValue == CosmicType.EMPTY_SPACE
            }
        }.keys

}

private class CosmicExpansionProcessor private constructor(
    private val cosmicGrid: CosmicGrid
) : ICosmicGrid by cosmicGrid {

    companion object {
        fun parse(input: List<String>): CosmicExpansionProcessor = CosmicExpansionProcessor(CosmicGrid(input))
    }

    private val allGalaxyLocations get() = getAllGalaxyTypeLocations()

    private val emptySpaceRowIndexes get() = getAllEmptySpaceRowIndexes()

    private val emptySpaceColumnIndexes get() = getAllEmptySpaceColumnIndexes()

    /**
     * Returns Galaxies represented by their locations in [Pair]s.
     *
     * If there are `n` galaxies, then a total of `n*(n-1)/2` galaxy pairs are returned.
     */
    private val galaxyLocationsAsPairs: List<Pair<CosmicLocation, CosmicLocation>>
        get() = mutableListOf<Pair<CosmicLocation, CosmicLocation>>().apply {
            allGalaxyLocations.forEachIndexed { currentIndex, currentCosmicLocation ->
                allGalaxyLocations.withIndex().filter { (otherIndex, _) ->
                    otherIndex > currentIndex
                }.forEach { (_, nextCosmicLocation) ->
                    add(currentCosmicLocation to nextCosmicLocation)
                }
            }
        }

    /**
     * Returns the total number of empty spaces present between the galaxies represented by their
     * locations [start] and [end].
     *
     * This will be used for the computation of expansion between the galaxies which is in turn needed
     * for the calculation of shortest path distance between them.
     */
    private fun getTotalEmptySpacesForGalaxyPairs(start: CosmicLocation, end: CosmicLocation): Int =
        listOf(start, end).let { cosmicLocations ->
            val xRange = cosmicLocations.map { it.x }.let { rowIndexes ->
                rowIndexes.min() + 1 until rowIndexes.max()
            }

            val yRange = cosmicLocations.map { it.y }.let { columnIndexes ->
                columnIndexes.min() + 1 until columnIndexes.max()
            }

            emptySpaceRowIndexes.count { it in xRange } + emptySpaceColumnIndexes.count { it in yRange }
        }

    /**
     * [Solution for Part-1 & Part-2]
     *
     * Returns the sum of all shortest path distances between every pair of galaxies including space expansion
     * happening between these galaxies based on their [expansionRate] which is caused by the gravitational effects.
     */
    fun getSumOfShortestDistancesBetweenGalaxyPairs(expansionRate: Int): Long =
        galaxyLocationsAsPairs.map { (start: CosmicLocation, end: CosmicLocation) ->
            abs(end.x - start.x) + abs(end.y - start.y)
        }.sumOf { it.toLong() } + galaxyLocationsAsPairs.map { (start: CosmicLocation, end: CosmicLocation) ->
            getTotalEmptySpacesForGalaxyPairs(start, end)
        }.sumOf { it.toLong() } * (expansionRate - 1)

}