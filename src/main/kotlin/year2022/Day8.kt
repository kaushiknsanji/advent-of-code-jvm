/**
 * Problem: Day8: Treetop Tree House
 * https://adventofcode.com/2022/day/8
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseFileHandler
import utils.grid.CardinalDirection.*
import utils.product
import utils.grid.CardinalDirection as Direction

private class Day8 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 21
    println("=====")
    solveActual(1) // 1820
    println("=====")
    solveSample(2) // 8
    println("=====")
    solveActual(2) // 385112
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day8.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day8.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    TreePatch.parse(input)
        .getCountOfTreesVisibleFromOutsideGrid()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    TreePatch.parse(input)
        .getMaxScenicScoreFromInteriorTree()
        .also { println(it) }
}

private class TreeLocation(val x: Int, val y: Int)

private interface ITreeHeightGrid {
    fun getAllTreeLocations(): Collection<TreeLocation>
    fun getAllExteriorTreeLocations(): Collection<TreeLocation>
    fun getAllInteriorTreeLocations(): Collection<TreeLocation>
    fun getTreeLocationOrNull(x: Int, y: Int): TreeLocation?
    fun getTreeLocation(x: Int, y: Int): TreeLocation
    fun TreeLocation.getNeighbour(direction: Direction): TreeLocation?
    fun TreeLocation.getAllNeighbours(): Collection<TreeLocation>
    fun TreeLocation.getTreeLocationsInDirection(direction: Direction): Sequence<TreeLocation>
    fun TreeLocation.getTreeLocationsInAllDirections(): Map<Direction, Sequence<TreeLocation>>
}

private class TreeHeightGrid private constructor(
    private val rows: Int,
    private val columns: Int,
    treeHeightGridList: List<List<Int>>
) : ITreeHeightGrid {

    constructor(input: List<String>) : this(
        rows = input.size,
        columns = input[0].length,
        treeHeightGridList = input.map { line -> line.map { it.digitToInt() } }
    )

    private val treeLocationMap: Map<Int, List<TreeLocation>> = (0 until rows).flatMap { x ->
        (0 until columns).map { y ->
            TreeLocation(x, y)
        }
    }.groupBy { treeLocation -> treeLocation.x }

    private val treeLocationHeightMap: Map<TreeLocation, Int> = treeHeightGridList.map { it.withIndex() }.withIndex()
        .flatMap { (x: Int, indexedHeightValues) ->
            indexedHeightValues.map { (y: Int, heightValue: Int) ->
                getTreeLocation(x, y) to heightValue
            }
        }.toMap()

    val totalTreesInEdges = (columns * 2) + (rows * 2) - 4

    operator fun get(treeLocation: TreeLocation): Int = treeLocationHeightMap[treeLocation]!!

    override fun getAllTreeLocations(): Collection<TreeLocation> = treeLocationMap.values.flatten()

    override fun getAllExteriorTreeLocations(): Collection<TreeLocation> =
        treeLocationMap.mapValues { entry ->
            if (entry.key == 0 || entry.key == rows - 1) {
                entry.value
            } else {
                with(entry.value) {
                    listOf(first(), last())
                }
            }
        }.values.flatten()

    override fun getAllInteriorTreeLocations(): Collection<TreeLocation> =
        getAllTreeLocations() - getAllExteriorTreeLocations()

    override fun getTreeLocationOrNull(x: Int, y: Int): TreeLocation? = try {
        treeLocationMap[x]?.get(y)
    } catch (e: IndexOutOfBoundsException) {
        null
    }

    override fun getTreeLocation(x: Int, y: Int): TreeLocation =
        getTreeLocationOrNull(x, y) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${TreeLocation::class.simpleName} at the given coordinates ($x, $y)"
        )

    override fun TreeLocation.getNeighbour(direction: Direction): TreeLocation? =
        when (direction) {
            TOP -> getTreeLocationOrNull(x - 1, y)
            BOTTOM -> getTreeLocationOrNull(x + 1, y)
            RIGHT -> getTreeLocationOrNull(x, y + 1)
            LEFT -> getTreeLocationOrNull(x, y - 1)
        }

    override fun TreeLocation.getAllNeighbours(): Collection<TreeLocation> =
        Direction.entries.mapNotNull { direction -> getNeighbour(direction) }

    override fun TreeLocation.getTreeLocationsInDirection(direction: Direction): Sequence<TreeLocation> =
        generateSequence(this) { lastTreeLocation ->
            lastTreeLocation.getNeighbour(direction)
        }.drop(1)

    override fun TreeLocation.getTreeLocationsInAllDirections(): Map<Direction, Sequence<TreeLocation>> =
        Direction.entries.associateWith { direction -> getTreeLocationsInDirection(direction) }

}

private class TreePatch private constructor(
    private val treeHeightGrid: TreeHeightGrid
) : ITreeHeightGrid by treeHeightGrid {

    companion object {
        fun parse(input: List<String>): TreePatch = TreePatch(TreeHeightGrid(input))
    }

    /**
     * [Solution for Part-1]
     * Returns the number of trees visible from outside the grid.
     */
    fun getCountOfTreesVisibleFromOutsideGrid(): Int =
        treeHeightGrid.totalTreesInEdges + getAllInteriorTreeLocations()
            .count { currentTreeLocation: TreeLocation ->
                currentTreeLocation.getTreeLocationsInAllDirections().any { (_, treeLocationsSeq) ->
                    treeLocationsSeq.map { treeLocation -> treeHeightGrid[treeLocation] }
                        .all { otherTreeHeight -> otherTreeHeight < treeHeightGrid[currentTreeLocation] }
                }
            }

    /**
     * [Solution for Part-2]
     * Returns the highest scenic score possible for any interior tree.
     */
    fun getMaxScenicScoreFromInteriorTree(): Int =
        getAllInteriorTreeLocations().maxOf { currentTreeLocation: TreeLocation ->
            currentTreeLocation.getTreeLocationsInAllDirections()
                .mapValues { (_, treeLocationsSeq) ->
                    treeLocationsSeq.map { treeLocation -> treeHeightGrid[treeLocation] - treeHeightGrid[currentTreeLocation] }
                }
                .mapValues { (_, treesRelativeHeightGradientSeq) ->
                    treesRelativeHeightGradientSeq.indexOfFirst { treeHeightGradient -> treeHeightGradient >= 0 }
                        .takeUnless { it == -1 }?.let { it + 1 } ?: treesRelativeHeightGradientSeq.count()
                }
                .values
                .product()
        }

}