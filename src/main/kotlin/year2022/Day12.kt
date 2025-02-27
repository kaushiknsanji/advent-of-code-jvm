/**
 * Problem: Day12: Hill Climbing Algorithm
 * https://adventofcode.com/2022/day/12
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseFileHandler
import extensions.whileLoop
import utils.grid.CardinalDirection
import utils.grid.CardinalDirection.*
import utils.grid.CardinalDirection as Direction

private class Day12 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 31
    println("=====")
    solveActual(1) // 472
    println("=====")
    solveSample(2) // 29
    println("=====")
    solveActual(2) // 465
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day12.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day12.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    HillClimbingPlanner.parse(
        input = input,
        startHillElevation = 'a',
        endHillElevation = 'z'
    )
        .getLengthOfShortestPossiblePathToEndFromStart()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    HillClimbingPlanner.parse(
        input = input,
        startHillElevation = 'a',
        endHillElevation = 'z',
        fixedStart = false
    )
        .getLengthOfShortestPossiblePathToEndFromStart()
        .also { println(it) }
}

private data class HillLocation(val x: Int, val y: Int) {
    override fun toString(): String = "($x, $y)"
}

private data class Hill(
    val location: HillLocation,
    val elevation: Char,
    val isStartHill: Boolean = false,
    val isEndHill: Boolean = false,
    var isVisited: Boolean = false
) {
    override fun toString(): String = "Hill at $location with elevation $elevation"
}

private interface IHillElevationGrid {
    fun getHillLocationOrNull(x: Int, y: Int): HillLocation?
    fun getHillLocation(x: Int, y: Int): HillLocation
    fun HillLocation.getNeighbour(direction: Direction): HillLocation?
    fun HillLocation.getAllNeighbours(): Collection<HillLocation>
    fun getFixedStartHillLocation(): HillLocation
    fun HillLocation.toHill(): Hill
    fun getAllStartHills(): List<Hill>
    fun getEndHill(): Hill
}

private class HillElevationGrid private constructor(
    private val rows: Int,
    private val columns: Int,
    elevationGridList: List<List<Char>>,
    private val startHillElevation: Char,
    private val endHillElevation: Char,
    private val fixedStart: Boolean
) : IHillElevationGrid {

    constructor(input: List<String>, startHillElevation: Char, endHillElevation: Char, fixedStart: Boolean) : this(
        rows = input.size,
        columns = input[0].length,
        elevationGridList = input.map { line -> line.map { it } },
        startHillElevation = startHillElevation,
        endHillElevation = endHillElevation,
        fixedStart = fixedStart
    )

    companion object {
        private const val START_HILL_IDENTIFIER = 'S'
        private const val END_HILL_IDENTIFIER = 'E'
    }

    private val hillLocationMap: Map<Int, List<HillLocation>> = (0 until rows).flatMap { x ->
        (0 until columns).map { y ->
            HillLocation(x, y)
        }
    }.groupBy { hillLocation -> hillLocation.x }

    private val hillLocationElevationMap: Map<HillLocation, Char> = elevationGridList.map { it.withIndex() }.withIndex()
        .flatMap { (x: Int, elevationIndexedValues: Iterable<IndexedValue<Char>>) ->
            elevationIndexedValues.map { (y: Int, elevationValue: Char) ->
                getHillLocation(x, y) to elevationValue
            }
        }.toMap()

    private val hillLocationToHillMap: Map<HillLocation, Hill> =
        hillLocationElevationMap.mapValues { (location, elevation) ->
            if (elevation == START_HILL_IDENTIFIER || (elevation == startHillElevation && !fixedStart)) {
                Hill(
                    location = location,
                    elevation = startHillElevation,
                    isStartHill = true
                )
            } else if (elevation == END_HILL_IDENTIFIER) {
                Hill(
                    location = location,
                    elevation = endHillElevation,
                    isEndHill = true,
                    isVisited = true    // Set to true since we are starting to trace from here
                )
            } else {
                Hill(location, elevation)
            }
        }

    operator fun get(hillLocation: HillLocation): Char = hillLocationElevationMap[hillLocation]!!

    override fun getHillLocationOrNull(x: Int, y: Int): HillLocation? = try {
        hillLocationMap[x]?.get(y)
    } catch (e: IndexOutOfBoundsException) {
        null
    }

    override fun getHillLocation(x: Int, y: Int): HillLocation =
        getHillLocationOrNull(x, y) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${HillLocation::class.simpleName} at the given coordinates ($x, $y)"
        )

    override fun HillLocation.getNeighbour(direction: CardinalDirection): HillLocation? =
        when (direction) {
            TOP -> getHillLocationOrNull(x - 1, y)
            BOTTOM -> getHillLocationOrNull(x + 1, y)
            RIGHT -> getHillLocationOrNull(x, y + 1)
            LEFT -> getHillLocationOrNull(x, y - 1)
        }

    override fun HillLocation.getAllNeighbours(): Collection<HillLocation> =
        Direction.entries.mapNotNull { direction -> getNeighbour(direction) }

    override fun getFixedStartHillLocation(): HillLocation = hillLocationElevationMap.filterValues { elevationChar ->
        elevationChar == START_HILL_IDENTIFIER
    }.keys.single()

    override fun HillLocation.toHill(): Hill = hillLocationToHillMap[this]!!

    override fun getAllStartHills(): List<Hill> = hillLocationToHillMap.values.filter { hill ->
        hill.isStartHill
    }

    override fun getEndHill(): Hill = hillLocationToHillMap.values.single { hill ->
        hill.isEndHill
    }

}

private interface IHillFinder {
    fun Hill.getNextPossibleHillsToClimb(): List<Hill>
    fun getAllStartHills(): List<Hill>
    fun getEndHill(): Hill
}

private class HillFinder(
    private val hillElevationGrid: HillElevationGrid,
) : IHillElevationGrid by hillElevationGrid, IHillFinder {

    override fun Hill.getNextPossibleHillsToClimb(): List<Hill> =
        getAllNeighbouringHills().filterNot { neighbouringHill ->
            elevation - neighbouringHill.elevation > 1
        }

    private fun Hill.getAllNeighbouringHills(): List<Hill> =
        location.getAllNeighbours().map { location -> location.toHill() }

}

private class HillTracer(
    private val hillFinder: IHillFinder,
    private val hillsTraced: List<Hill>
) : IHillFinder by hillFinder {

    val lastVisitedHill = if (hillsTraced.isEmpty()) {
        throw IllegalStateException("No Hill has been traced yet")
    } else {
        hillsTraced.last()
    }

    val isTraceFinished = lastVisitedHill in getAllStartHills()

    val isTraceDead =
        !isTraceFinished && lastVisitedHill.getNextPossibleHillsToClimb().filterNot { hill -> hill.isVisited }.isEmpty()

    val totalHillsTraced get() = hillsTraced.size

    operator fun plus(hill: Hill): HillTracer = HillTracer(hillFinder, hillsTraced + hill.also { it.isVisited = true })

    fun findNextHillsToClimb(): List<HillTracer> =
        lastVisitedHill.getNextPossibleHillsToClimb().filterNot { nextHill ->
            nextHill.isVisited
        }.map { nextHill -> this + nextHill }

    private fun getHillsTraced(): String = hillsTraced.joinToString { hill -> hill.toString() }

    override fun toString(): String =
        "Hills: ${getHillsTraced()} \n total: $totalHillsTraced, TraceDead: $isTraceDead, TraceFinished: $isTraceFinished \n"
}

private class HillClimbingPlanner private constructor(
    private val hillElevationGrid: HillElevationGrid
) : IHillFinder by HillFinder(hillElevationGrid) {

    companion object {
        fun parse(
            input: List<String>,
            startHillElevation: Char,
            endHillElevation: Char,
            fixedStart: Boolean = true
        ): HillClimbingPlanner =
            HillClimbingPlanner(
                HillElevationGrid(input, startHillElevation, endHillElevation, fixedStart)
            )

    }

    private fun getAllPossibleHillsToClimbFromStartToEnd(): List<HillTracer> =
        whileLoop(
            loopStartCounter = 0,
            initialResult = getEndHill().let { endHill ->
                endHill.getNextPossibleHillsToClimb().map { nextHill ->
                    HillTracer(this, listOf(endHill)) + nextHill
                }
            },
            exitCondition = { _: Int, lastIterationResult: List<HillTracer>? ->
                lastIterationResult != null && lastIterationResult.all { hillTracer -> hillTracer.isTraceFinished }
            }
        ) { loopCounter, lastIterationResult ->

            loopCounter to lastIterationResult.filterNot { hillTracer ->
                hillTracer.isTraceDead
            }.flatMap { hillTracer: HillTracer ->
                if (hillTracer.isTraceFinished) {
                    listOf(hillTracer)
                } else {
                    hillTracer.findNextHillsToClimb()
                }
            }
        }

    /**
     * [Solution for Part 1 & 2]
     * Returns the minimum number of steps required to reach the destination hill from the designated start hill.
     */
    fun getLengthOfShortestPossiblePathToEndFromStart(): Int =
        getAllPossibleHillsToClimbFromStartToEnd().minOf { hillTracer -> hillTracer.totalHillsTraced } - 1
}