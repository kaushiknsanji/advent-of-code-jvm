/**
 * Problem: Day10: Pipe Maze
 * https://adventofcode.com/2023/day/10
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import extensions.whileLoop
import utils.grid.CardinalDirection.*
import utils.grid.Point2D
import utils.grid.toTotalPointsEnclosedByPolygon
import utils.grid.CardinalDirection as Direction

private class Day10 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSamplePart1Type1()     // 4
    println("=====")
    solveSamplePart1Type2()     // 8
    println("=====")
    solveActual(1)      // 6754
    println("=====")
    solveSamplePart2Type1()     // 4
    println("=====")
    solveSamplePart2Type2()     // 4
    println("=====")
    solveSamplePart2Type3()     // 8
    println("=====")
    solveSamplePart2Type4()     // 10
    println("=====")
    solveActual(2)      // 567
    println("=====")
}

private fun solveSamplePart1Type1(executeProblemPart: Int = 1) {
    execute(Day10.getSampleFile("_part1_1").readLines(), executeProblemPart)
}

private fun solveSamplePart1Type2(executeProblemPart: Int = 1) {
    execute(Day10.getSampleFile("_part1_2").readLines(), executeProblemPart)
}

private fun solveSamplePart2Type1(executeProblemPart: Int = 2) {
    execute(Day10.getSampleFile("_part2_1").readLines(), executeProblemPart)
}

private fun solveSamplePart2Type2(executeProblemPart: Int = 2) {
    execute(Day10.getSampleFile("_part2_2").readLines(), executeProblemPart)
}

private fun solveSamplePart2Type3(executeProblemPart: Int = 2) {
    execute(Day10.getSampleFile("_part2_3").readLines(), executeProblemPart)
}

private fun solveSamplePart2Type4(executeProblemPart: Int = 2) {
    execute(Day10.getSampleFile("_part2_4").readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day10.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    PipeMazeProcessor(input)
        .getStepCountToFarthestTileFromStart()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    PipeMazeProcessor(input)
        .getCountOfTilesEnclosedByTheLoop()
        .also { println(it) }
}

private class PipeTileLocation(val x: Int, val y: Int) : Point2D<Int>(x, y)

private enum class PipeType(val type: Char) {
    VERTICAL_PIPE('|'),
    HORIZONTAL_PIPE('-'),
    L_BEND_PIPE('L'),
    J_BEND_PIPE('J'),
    SEVEN_BEND_PIPE('7'),
    F_BEND_PIPE('F'),
    S_BEND_PIPE('S'),
    NOT_PIPE('.')
}

private class Pipe private constructor(
    val pipeType: PipeType,
    val directions: List<Direction>
) {

    companion object {
        private val pipeTypeToPipeMap: MutableMap<PipeType, Pipe> = mutableMapOf()

        private val directionToConnectablePipeTypesMap: Map<Direction, List<PipeType>> = mapOf(
            TOP to listOf(PipeType.SEVEN_BEND_PIPE, PipeType.F_BEND_PIPE, PipeType.VERTICAL_PIPE, PipeType.S_BEND_PIPE),
            BOTTOM to listOf(PipeType.VERTICAL_PIPE, PipeType.J_BEND_PIPE, PipeType.L_BEND_PIPE, PipeType.S_BEND_PIPE),
            LEFT to listOf(PipeType.HORIZONTAL_PIPE, PipeType.F_BEND_PIPE, PipeType.L_BEND_PIPE, PipeType.S_BEND_PIPE),
            RIGHT to listOf(
                PipeType.HORIZONTAL_PIPE,
                PipeType.SEVEN_BEND_PIPE,
                PipeType.J_BEND_PIPE,
                PipeType.S_BEND_PIPE
            )
        )

        fun create(connectorType: Char): Pipe = PipeType.entries.single { it.type == connectorType }
            .let { pipeType: PipeType ->
                pipeTypeToPipeMap.getOrPut(pipeType) {
                    Pipe(
                        pipeType,
                        directions = when (pipeType) {
                            PipeType.VERTICAL_PIPE -> listOf(TOP, BOTTOM)
                            PipeType.HORIZONTAL_PIPE -> listOf(LEFT, RIGHT)
                            PipeType.L_BEND_PIPE -> listOf(TOP, RIGHT)
                            PipeType.J_BEND_PIPE -> listOf(TOP, LEFT)
                            PipeType.SEVEN_BEND_PIPE -> listOf(LEFT, BOTTOM)
                            PipeType.F_BEND_PIPE -> listOf(RIGHT, BOTTOM)
                            PipeType.S_BEND_PIPE -> Direction.entries
                            PipeType.NOT_PIPE -> emptyList()
                        }
                    )
                }
            }

    }

    fun getPossibleConnectingPipeTypes(): Map<Direction, List<PipeType>> =
        directionToConnectablePipeTypesMap.filterKeys { it in directions }

    override fun toString(): String = "Pipe: ${pipeType.type}"
}

private interface IPipeTileGrid {
    fun getLocationOrNull(row: Int, column: Int): PipeTileLocation?
    fun getLocation(row: Int, column: Int): PipeTileLocation
    fun getAllLocations(): Collection<PipeTileLocation>
    fun PipeTileLocation.getNeighbour(direction: Direction): PipeTileLocation?
    fun getStartLocation(): PipeTileLocation
}

private class PipeTileGrid private constructor(
    rows: Int, columns: Int, pipePattern: List<String>
) : IPipeTileGrid {

    constructor(pipePattern: List<String>) : this(
        rows = pipePattern.size,
        columns = pipePattern.first().length,
        pipePattern = pipePattern
    )

    private val pipeTileGridMap: Map<Int, List<PipeTileLocation>> = (0 until rows).flatMap { x ->
        (0 until columns).map { y ->
            PipeTileLocation(x, y)
        }
    }.groupBy { pipeTileLocation: PipeTileLocation -> pipeTileLocation.x }

    private val pipeTileGridValueMap: Map<PipeTileLocation, Pipe> =
        pipePattern.flatMapIndexed { x: Int, rowPipesPattern: String ->
            rowPipesPattern.mapIndexed { y: Int, connectorType: Char ->
                getLocation(x, y) to Pipe.create(connectorType)
            }
        }.toMap()

    operator fun get(pipeTileLocation: PipeTileLocation): Pipe = pipeTileGridValueMap[pipeTileLocation]!!

    override fun getLocationOrNull(row: Int, column: Int): PipeTileLocation? = try {
        pipeTileGridMap[row]?.get(column)
    } catch (e: IndexOutOfBoundsException) {
        null
    }

    override fun getLocation(row: Int, column: Int): PipeTileLocation =
        getLocationOrNull(row, column) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${PipeTileLocation::class.simpleName} at the given location ($row, $column)"
        )

    override fun getAllLocations(): Collection<PipeTileLocation> = pipeTileGridMap.values.flatten()

    override fun PipeTileLocation.getNeighbour(direction: Direction): PipeTileLocation? = when (direction) {
        TOP -> getLocationOrNull(x - 1, y)
        BOTTOM -> getLocationOrNull(x + 1, y)
        RIGHT -> getLocationOrNull(x, y + 1)
        LEFT -> getLocationOrNull(x, y - 1)
    }

    override fun getStartLocation(): PipeTileLocation = pipeTileGridValueMap.filterKeys { pipeTileLocation ->
        this[pipeTileLocation].pipeType == PipeType.S_BEND_PIPE
    }.keys.single()

}

private interface IPipeFinder {
    fun PipeTileLocation.getConnectingPipeLocations(): Map<Direction, PipeTileLocation>
    fun PipeTileLocation.getConnectedPipeLocation(fromDirection: Direction): PipeTileLocation?
    fun PipeTileLocation.getConnectedDirection(previousLocation: PipeTileLocation): Direction?
    fun PipeTileLocation.toPipe(): Pipe
}

private class PipeFinder(
    private val pipeTileGrid: PipeTileGrid
) : IPipeFinder, IPipeTileGrid by pipeTileGrid {

    override fun PipeTileLocation.getConnectingPipeLocations(): Map<Direction, PipeTileLocation> =
        toPipe().let { pipe: Pipe ->
            if (pipe.pipeType == PipeType.NOT_PIPE) {
                emptyMap()
            } else {
                pipe.directions.associateWith { direction ->
                    this.getNeighbour(direction)
                        ?.let { pipeTileLocation -> pipeTileLocation to pipeTileLocation.toPipe() }
                }.filter { (direction: Direction, nextLocationPipePair: Pair<PipeTileLocation, Pipe>?) ->
                    nextLocationPipePair?.second?.pipeType in pipe.getPossibleConnectingPipeTypes()[direction]!!
                }.mapValues { (_: Direction, nextLocationPipePair: Pair<PipeTileLocation, Pipe>?) ->
                    nextLocationPipePair!!.first
                }
            }
        }

    override fun PipeTileLocation.getConnectedPipeLocation(fromDirection: Direction): PipeTileLocation? =
        getConnectingPipeLocations().takeUnless { it.isEmpty() }
            ?.filterNot { (toDirection: Direction, _: PipeTileLocation) ->
                toDirection == fromDirection
            }?.values?.single()

    override fun PipeTileLocation.getConnectedDirection(previousLocation: PipeTileLocation): Direction? =
        if (this.x > previousLocation.x) {
            TOP
        } else if (this.x < previousLocation.x) {
            BOTTOM
        } else if (this.y > previousLocation.y) {
            LEFT
        } else if (this.y < previousLocation.y) {
            RIGHT
        } else {
            null
        }

    override fun PipeTileLocation.toPipe(): Pipe = pipeTileGrid[this]

}

private class PipeTracer(
    private val pipeFinder: IPipeFinder,
    val pipeLocationsTraced: List<PipeTileLocation?>
) : IPipeFinder by pipeFinder {

    private val lastLocation = pipeLocationsTraced.lastOrNull()

    private val previousToLastLocation: () -> PipeTileLocation? = {
        if (pipeLocationsTraced.size > 1) {
            pipeLocationsTraced[pipeLocationsTraced.lastIndex - 1]
        } else {
            null
        }
    }

    val isTraceFinished = pipeLocationsTraced.size > 1 && pipeLocationsTraced.first() == lastLocation

    val isTraceDead = lastLocation == null || lastLocation.getConnectingPipeLocations().size != 2

    val totalPipesTraced get() = pipeLocationsTraced.size - 1

    operator fun plus(location: PipeTileLocation?): PipeTracer =
        PipeTracer(pipeFinder, pipeLocationsTraced + location)

    fun findNextPipeLocation(): PipeTracer =
        this + previousToLastLocation()?.let { previousToLastLocation ->
            lastLocation?.getConnectedDirection(previousToLastLocation)
                ?.let { connectedDirection ->
                    lastLocation.getConnectedPipeLocation(connectedDirection)
                }
        }

    private fun getPipeLocationsTraced(): String = pipeLocationsTraced.joinToString { pipeTileLocation ->
        pipeTileLocation?.toPipe().toString()
    }

    override fun toString(): String =
        "Trace total: $totalPipesTraced, TraceDead: $isTraceDead, TraceFinished: $isTraceFinished \nPipesTraced: ${getPipeLocationsTraced()} \n"
}

private class PipeMazeProcessor private constructor(
    private val pipeTileGrid: PipeTileGrid
) : IPipeFinder by PipeFinder(pipeTileGrid), IPipeTileGrid by pipeTileGrid {

    constructor(input: List<String>) : this(
        pipeTileGrid = PipeTileGrid(input)
    )

    /**
     * Returns all possible paths from the Start tile to the same Start tile, as traced by [PipeTracer].
     */
    private fun getAllPossiblePathsFromStart(): List<PipeTracer> =
        whileLoop(
            loopStartCounter = 0,
            initialResult = pipeTileGrid.getStartLocation().let { startPipeLocation ->
                startPipeLocation.getConnectingPipeLocations().values.map { nextLocation ->
                    PipeTracer(this, listOf(startPipeLocation)) + nextLocation
                }
            },
            exitCondition = { _: Int, lastIterationResult: List<PipeTracer>? ->
                lastIterationResult?.all { pipeTracer -> pipeTracer.isTraceFinished } ?: false
            }
        ) { loopCounter, lastIterationResult ->

            loopCounter to lastIterationResult.filterNot { pipeTracer ->
                pipeTracer.isTraceDead
            }.map { pipeTracer ->
                if (pipeTracer.isTraceFinished) {
                    pipeTracer
                } else {
                    pipeTracer.findNextPipeLocation()
                }
            }
        }

    /**
     * [Solution for Part-1]
     *
     * Returns the number of steps required to reach the Farthest tile from the Start tile
     * in the loop that connects back to the same Start tile.
     */
    fun getStepCountToFarthestTileFromStart(): Int = getAllPossiblePathsFromStart().first().totalPipesTraced shr 1

    /**
     * [Solution for Part-2]
     *
     * Returns total number of [PipeTileLocation]s enclosed by the loop traversed from the Start tile.
     */
    fun getCountOfTilesEnclosedByTheLoop(): Long =
        getAllPossiblePathsFromStart().first().pipeLocationsTraced.filterNotNull().toTotalPointsEnclosedByPolygon()

}

