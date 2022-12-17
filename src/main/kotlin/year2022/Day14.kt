/**
 * Problem: Day14: Regolith Reservoir
 * https://adventofcode.com/2022/day/14
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseFileHandler
import utils.grid.OmniDirection
import utils.grid.OmniDirection.*
import utils.grid.Point2d
import utils.grid.OmniDirection as Direction

private class Day14 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 24
    println("=====")
    solveActual(1) // 779
    println("=====")
    solveSample(2) // 93
    println("=====")
    solveActual(2) // 27426
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day14.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day14.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    RegolithReservoir.parse(
        input,
        sandPouringPoint = StructureLocus(500, 0)
    )
        .startFallingSandSimulation()
        .getTotalUnitsOfSandPoured()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    RegolithReservoir.parse(
        input,
        sandPouringPoint = StructureLocus(500, 0),
        isFloorPresent = true
    )
        .startFallingSandSimulation()
        .getTotalUnitsOfSandPoured()
        .also { println(it) }
}

private data class StructureLocus(val x: Int, val y: Int) : Point2d<Int>(x, y) {
    companion object {
        fun parse(coordinatesLine: String) = coordinatesLine.split(",").let { coordinates ->
            StructureLocus(coordinates.first().trim().toInt(), coordinates.last().trim().toInt())
        }
    }
}

private enum class CaveTomoStructure(val type: Char) {
    ROCK(CaveTomoGrid.ROCK),
    AIR(CaveTomoGrid.AIR),
    SAND(CaveTomoGrid.SAND)
}

private interface ICaveTomoGrid {
    fun getAllStructureLoci(): Collection<StructureLocus>
    fun getStructureLocusOrNull(x: Int, y: Int): StructureLocus?
    fun getStructureLocus(x: Int, y: Int): StructureLocus
    fun StructureLocus.getNeighbour(direction: Direction): StructureLocus?
    fun StructureLocus.getAllNeighbours(): Collection<StructureLocus>
    fun StructureLocus.getAllBottomNeighbours(): Map<Direction, StructureLocus>
    fun StructureLocus.getStructureLociInDirection(direction: Direction): Sequence<StructureLocus>
}

private class CaveTomoGrid(
    private val rockCoordinatesList: List<List<StructureLocus>>,
    val sandPouringPoint: StructureLocus,
    val isFloorPresent: Boolean,
    private val floorDistanceBeyondAbyss: Int
) : ICaveTomoGrid {

    companion object {
        const val ROCK = '#'
        const val AIR = '.'
        const val SAND = 'o'
    }

    private val rockCoordinatesSeq = rockCoordinatesList.asSequence().flatten()
    private val rockYMin = 0
    private val rockYMax = rockCoordinatesSeq.maxOf { structureLocus: StructureLocus -> structureLocus.y }.let { yMax ->
        if (isFloorPresent) {
            yMax + floorDistanceBeyondAbyss
        } else {
            yMax
        }
    }
    private val rockXMin = rockCoordinatesSeq.minOf { structureLocus: StructureLocus -> structureLocus.x }.let { xMin ->
        if (isFloorPresent) {
            minOf(xMin - 1, sandPouringPoint.x - rockYMax - 1)
        } else {
            xMin - 1
        }
    }
    private val rockXMax = rockCoordinatesSeq.maxOf { structureLocus: StructureLocus -> structureLocus.x }.let { xMax ->
        if (isFloorPresent) {
            maxOf(xMax + 1, sandPouringPoint.x + rockYMax + 1)
        } else {
            xMax + 1
        }
    }
    private val bottomDirections = listOf(BOTTOM, BOTTOM_LEFT, BOTTOM_RIGHT)

    private val structureLocusMap: Map<Int, List<StructureLocus>> = (rockYMin..rockYMax).flatMap { y ->
        (rockXMin..rockXMax).map { x ->
            rockCoordinatesSeq.singleOrNull { structureLocus -> structureLocus.x == x && structureLocus.y == y }
                ?: StructureLocus(x, y)
        }
    }.groupBy { structureLocus: StructureLocus -> structureLocus.y }

    private val structureLocusValueMap: MutableMap<StructureLocus, CaveTomoStructure> =
        structureLocusMap.values.flatten()
            .associateWith { CaveTomoStructure.AIR }
            .toMutableMap().apply {
                rockCoordinatesList.flatMap { rockLoci -> rockLoci.zipWithNext() }
                    .forEach { (firstLocus, secondLocus) ->
                        if (firstLocus.x == secondLocus.x) {
                            val min = minOf(firstLocus.y, secondLocus.y)
                            val max = maxOf(firstLocus.y, secondLocus.y)
                            (min..max).forEach { y ->
                                this[getStructureLocus(firstLocus.x, y)] = CaveTomoStructure.ROCK
                            }
                        } else if (firstLocus.y == secondLocus.y) {
                            val min = minOf(firstLocus.x, secondLocus.x)
                            val max = maxOf(firstLocus.x, secondLocus.x)
                            (min..max).forEach { x ->
                                this[getStructureLocus(x, firstLocus.y)] = CaveTomoStructure.ROCK
                            }
                        }
                    }

                // Add Floor if present
                if (isFloorPresent) {
                    (rockXMin..rockXMax).forEach { x ->
                        this[getStructureLocus(x, rockYMax)] = CaveTomoStructure.ROCK
                    }
                }
            }

    operator fun get(structureLocus: StructureLocus): CaveTomoStructure = structureLocusValueMap[structureLocus]!!

    operator fun set(structureLocus: StructureLocus, structure: CaveTomoStructure) {
        structureLocusValueMap[structureLocus] = structure
    }

    fun getBottomDirections(): List<Direction> = bottomDirections

    fun getDepth(): Int = rockYMax

    override fun getAllStructureLoci(): Collection<StructureLocus> = structureLocusMap.values.flatten()

    override fun getStructureLocusOrNull(x: Int, y: Int): StructureLocus? = try {
        structureLocusMap[y]?.singleOrNull { structureLoci -> structureLoci.x == x }
    } catch (e: IndexOutOfBoundsException) {
        null
    }

    override fun getStructureLocus(x: Int, y: Int): StructureLocus =
        getStructureLocusOrNull(x, y) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${StructureLocus::class.simpleName} at the given coordinates ($x, $y)"
        )

    override fun StructureLocus.getNeighbour(direction: Direction): StructureLocus? = when (direction) {
        TOP -> getStructureLocusOrNull(x, y - 1)
        BOTTOM -> getStructureLocusOrNull(x, y + 1)
        RIGHT -> getStructureLocusOrNull(x + 1, y)
        LEFT -> getStructureLocusOrNull(x - 1, y)
        TOP_LEFT -> getStructureLocusOrNull(x - 1, y - 1)
        TOP_RIGHT -> getStructureLocusOrNull(x + 1, y - 1)
        BOTTOM_LEFT -> getStructureLocusOrNull(x - 1, y + 1)
        BOTTOM_RIGHT -> getStructureLocusOrNull(x + 1, y + 1)
    }

    override fun StructureLocus.getAllNeighbours(): Collection<StructureLocus> =
        Direction.values().mapNotNull { direction -> getNeighbour(direction) }

    @Suppress("UNCHECKED_CAST")
    override fun StructureLocus.getAllBottomNeighbours(): Map<Direction, StructureLocus> =
        bottomDirections.associateWith { direction ->
            getNeighbour(direction)
        }.filterValues { structureLocus -> structureLocus != null } as Map<Direction, StructureLocus>

    override fun StructureLocus.getStructureLociInDirection(direction: OmniDirection): Sequence<StructureLocus> =
        generateSequence(this) { nextStructureLocus ->
            nextStructureLocus.getNeighbour(direction)
        }

    override fun toString(): String = (rockYMin..rockYMax).joinToString("\n") { y ->
        (rockXMin..rockXMax).joinToString(" ") { x ->
            this[getStructureLocus(x, y)].type.toString()
        }
    }
}

private class SandDropper(
    private val caveTomoGrid: CaveTomoGrid
) : ICaveTomoGrid by caveTomoGrid {

    fun startFallingSandSimulation() {
        generateSequence {
            pourSand()
        }.takeWhile { it }.toList()
    }

    fun getTotalUnitsOfSandPoured(): Int = getAllStructureLoci().asSequence()
        .filterNot { structureLocus -> structureLocus.y == caveTomoGrid.getDepth() }
        .map { structureLocus -> caveTomoGrid[structureLocus] }
        .count { structure -> structure == CaveTomoStructure.SAND }

    /**
     * Pours sand into the cave and returns `true`: (Part1) as long as the sand has not made into the abyss;
     * (Part2) or till the sand comes to rest at the sand pouring point itself.
     */
    private fun pourSand(): Boolean =
        generateSequence(caveTomoGrid.sandPouringPoint.getFirstSandLandingLocus()) { sandSettlingLocus ->
            sandSettlingLocus.getNextSandLandingLocus()
        }.last().let { sandSettlingLocus ->
            // Save the settled location of sand dropped
            caveTomoGrid[sandSettlingLocus] = CaveTomoStructure.SAND
            // Switch based on whether Floor is present in the cave tomography
            if (caveTomoGrid.isFloorPresent) {
                // (Part2): Continue simulation till any sand settles at the pouring point itself
                sandSettlingLocus != caveTomoGrid.sandPouringPoint
            } else {
                // (Part1): Continue simulation till any sand settles to the depth
                sandSettlingLocus.y != caveTomoGrid.getDepth()
            }
        }

    private fun StructureLocus.getFirstSandLandingLocus(): StructureLocus =
        getStructureLociInDirection(BOTTOM).takeWhile { structureLocus ->
            caveTomoGrid[structureLocus] == CaveTomoStructure.AIR
        }.last()

    private fun StructureLocus.getNextSandLandingLocus(): StructureLocus? =
        getStructureInAllBottomNeighbours().filter { (_, _, structure) ->
            structure == CaveTomoStructure.AIR
        }.minByOrNull { (direction, _, _) ->
            caveTomoGrid.getBottomDirections().indexOf(direction)
        }?.second

    private fun StructureLocus.getStructureInAllBottomNeighbours(): List<Triple<Direction, StructureLocus, CaveTomoStructure>> =
        getAllBottomNeighbours().map { (direction, structureLocus) ->
            Triple(direction, structureLocus, caveTomoGrid[structureLocus])
        }

    override fun toString(): String = caveTomoGrid.toString()
}

private class RegolithReservoir private constructor(
    private val sandDropper: SandDropper
) {

    companion object {
        fun parse(
            input: List<String>,
            sandPouringPoint: StructureLocus,
            isFloorPresent: Boolean = false,
            floorDistanceBeyondAbyss: Int = 2
        ): RegolithReservoir = RegolithReservoir(
            SandDropper(
                caveTomoGrid = CaveTomoGrid(
                    input.map { line ->
                        line.split("->").map { coordinateString ->
                            StructureLocus.parse(coordinateString)
                        }
                    },
                    sandPouringPoint,
                    isFloorPresent,
                    floorDistanceBeyondAbyss
                )
            )
        )
    }

    fun startFallingSandSimulation(): RegolithReservoir = this.apply {
        sandDropper.startFallingSandSimulation()
    }

    /**
     * [Solution for Part 1 & 2]
     * Returns the total units of sand settled in the cave after simulating the falling sand.
     */
    fun getTotalUnitsOfSandPoured(): Int = sandDropper.getTotalUnitsOfSandPoured()
}