/**
 * Problem: Day16: The Floor Will Be Lava
 * https://adventofcode.com/2023/day/16
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import extensions.whileLoop
import utils.grid.TransverseDirection.*
import utils.grid.TransverseDirection as Direction

private class Day16 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 46
    println("=====")
    solveActual(1)      // 8146
    println("=====")
    solveSample(2)      // 51
    println("=====")
    solveActual(2)      // 8358
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day16.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day16.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    ContraptionAnalyzer(input).getTotalTilesEnergized().also { println(it) }
}

private fun doPart2(input: List<String>) {
    ContraptionAnalyzer(input).getTotalTilesEnergized(measureFromAllEdgeCorner = true).also { println(it) }
}

private class ContraptionLocation(val x: Int, val y: Int) {

    private val visitedDirections = mutableSetOf<Direction>()

    fun setVisitedInDirection(inDirection: Direction) {
        visitedDirections.add(inDirection)
    }

    fun isVisitedInDirection(inDirection: Direction): Boolean =
        visitedDirections.any { it == inDirection }

    fun isVisited(): Boolean = visitedDirections.size > 0

    fun clearVisitedDirections() {
        visitedDirections.clear()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContraptionLocation) return false

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    override fun toString(): String = "($x, $y)"
}

private enum class ContraptionType(val type: Char) {
    EMPTY_SPACE('.'),
    MIRROR_ACUTE('/'),
    MIRROR_OBTUSE('\\'),
    SPLITTER_VERTICAL('|'),
    SPLITTER_HORIZONTAL('-')
}

private interface IContraptionGrid {
    fun getContraptionLocationOrNull(row: Int, column: Int): ContraptionLocation?
    fun getContraptionLocation(row: Int, column: Int): ContraptionLocation
    fun getAllContraptionLocations(): Collection<ContraptionLocation>
    fun getFirstRowContraptionLocations(): Collection<ContraptionLocation>
    fun getLastRowContraptionLocations(): Collection<ContraptionLocation>
    fun getFirstColumnContraptionLocations(): Collection<ContraptionLocation>
    fun getLastColumnContraptionLocations(): Collection<ContraptionLocation>
    fun clearAllVisitsToContraptionLocations()
    fun ContraptionLocation.getNeighbour(direction: Direction): ContraptionLocation?
    fun ContraptionLocation.getLocationsInDirection(direction: Direction): Sequence<ContraptionLocation>
    fun getStartLocation(): ContraptionLocation
    fun getAllEdgeCornerStartLocationsWithBeamDirection(): Collection<Pair<ContraptionLocation, Direction>>
}

private class ContraptionGrid private constructor(
    rows: Int, columns: Int, contraptionPatternList: List<String>
) : IContraptionGrid {

    constructor(contraptionPatternList: List<String>) : this(
        rows = contraptionPatternList.size,
        columns = contraptionPatternList.first().length,
        contraptionPatternList = contraptionPatternList
    )

    private val contraptionGridMap: Map<Int, List<ContraptionLocation>> = (0 until rows).flatMap { x: Int ->
        (0 until columns).map { y: Int ->
            ContraptionLocation(x, y)
        }
    }.groupBy { contraptionLocation: ContraptionLocation -> contraptionLocation.x }

    private val contraptionGridValueMap: Map<ContraptionLocation, ContraptionType> =
        contraptionPatternList.flatMapIndexed { x: Int, rowPattern: String ->
            rowPattern.mapIndexed { y: Int, type: Char ->
                getContraptionLocation(x, y) to ContraptionType.entries.single { it.type == type }
            }
        }.toMap()

    operator fun get(contraptionLocation: ContraptionLocation) = contraptionGridValueMap[contraptionLocation]!!

    override fun getContraptionLocationOrNull(row: Int, column: Int): ContraptionLocation? = try {
        if (!contraptionGridMap.containsKey(row)) {
            throw NoSuchElementException()
        }
        contraptionGridMap[row]!!.single { it.y == column }
    } catch (e: NoSuchElementException) {
        null
    }

    override fun getContraptionLocation(row: Int, column: Int): ContraptionLocation =
        getContraptionLocationOrNull(row, column) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${ContraptionLocation::class.simpleName} at the given location ($row, $column)"
        )

    override fun getAllContraptionLocations(): Collection<ContraptionLocation> =
        contraptionGridMap.values.flatten()

    override fun getFirstRowContraptionLocations(): Collection<ContraptionLocation> =
        contraptionGridMap.values.first()

    override fun getLastRowContraptionLocations(): Collection<ContraptionLocation> =
        contraptionGridMap.values.last()

    override fun getFirstColumnContraptionLocations(): Collection<ContraptionLocation> =
        getFirstRowContraptionLocations().first().y.let { firstColumnIndex ->
            getAllContraptionLocations().filter { contraptionLocation -> contraptionLocation.y == firstColumnIndex }
        }

    override fun getLastColumnContraptionLocations(): Collection<ContraptionLocation> =
        getFirstRowContraptionLocations().last().y.let { lastColumnIndex ->
            getAllContraptionLocations().filter { contraptionLocation -> contraptionLocation.y == lastColumnIndex }
        }

    override fun clearAllVisitsToContraptionLocations() {
        getAllContraptionLocations().filter { contraptionLocation -> contraptionLocation.isVisited() }
            .forEach { contraptionLocation -> contraptionLocation.clearVisitedDirections() }
    }

    override fun ContraptionLocation.getNeighbour(direction: Direction): ContraptionLocation? = when (direction) {
        TOP -> getContraptionLocationOrNull(x - 1, y)
        BOTTOM -> getContraptionLocationOrNull(x + 1, y)
        RIGHT -> getContraptionLocationOrNull(x, y + 1)
        LEFT -> getContraptionLocationOrNull(x, y - 1)
    }

    override fun ContraptionLocation.getLocationsInDirection(direction: Direction): Sequence<ContraptionLocation> =
        generateSequence(this) { contraptionLocation: ContraptionLocation ->
            contraptionLocation.getNeighbour(direction)
        }.drop(1)

    override fun getStartLocation(): ContraptionLocation = getAllContraptionLocations().first()

    override fun getAllEdgeCornerStartLocationsWithBeamDirection(): Collection<Pair<ContraptionLocation, Direction>> =
        getFirstRowContraptionLocations().map { contraptionLocation -> contraptionLocation to BOTTOM } +
                getLastRowContraptionLocations().map { contraptionLocation -> contraptionLocation to TOP } +
                getFirstColumnContraptionLocations().map { contraptionLocation -> contraptionLocation to RIGHT } +
                getLastColumnContraptionLocations().map { contraptionLocation -> contraptionLocation to LEFT }

    override fun toString(): String =
        contraptionGridMap.values.joinToString("\n") { contraptionLocations ->
            contraptionLocations.joinToString("\t") { get(it).type.toString() }
        }
}

private interface IBeamFinder {
    fun ContraptionLocation.toType(): ContraptionType
    fun ContraptionLocation.toNextDirections(fromDirection: Direction): List<Direction>
    fun ContraptionLocation.getNextLocationsInDirection(direction: Direction): List<ContraptionLocation?>
    fun ContraptionLocation.isContraptionVisitedInDirection(direction: Direction): Boolean
}

private class BeamFinder(
    private val contraptionGrid: ContraptionGrid
) : IContraptionGrid by contraptionGrid, IBeamFinder {

    override fun ContraptionLocation.toType(): ContraptionType = contraptionGrid[this]

    /**
     * Decides the next directions of beam traversal based on the contraption present in [ContraptionLocation]
     * and beam direction [fromDirection].
     */
    override fun ContraptionLocation.toNextDirections(fromDirection: Direction): List<Direction> =
        when (this.toType()) {
            ContraptionType.EMPTY_SPACE -> listOf(fromDirection)

            ContraptionType.MIRROR_ACUTE -> {
                when (fromDirection) {
                    TOP -> listOf(RIGHT)
                    BOTTOM -> listOf(LEFT)
                    LEFT -> listOf(BOTTOM)
                    RIGHT -> listOf(TOP)
                }
            }

            ContraptionType.MIRROR_OBTUSE -> {
                when (fromDirection) {
                    TOP -> listOf(LEFT)
                    BOTTOM -> listOf(RIGHT)
                    RIGHT -> listOf(BOTTOM)
                    LEFT -> listOf(TOP)
                }
            }

            ContraptionType.SPLITTER_VERTICAL -> {
                when (fromDirection) {
                    TOP, BOTTOM -> listOf(fromDirection)
                    LEFT, RIGHT -> listOf(TOP, BOTTOM)
                }
            }

            ContraptionType.SPLITTER_HORIZONTAL -> {
                when (fromDirection) {
                    TOP, BOTTOM -> listOf(LEFT, RIGHT)
                    LEFT, RIGHT -> listOf(fromDirection)
                }
            }
        }

    /**
     * Returns next empty spaces in the [direction] of traversal, till the last contiguous empty space that can be found
     * from the current [ContraptionLocation].
     */
    private fun ContraptionLocation.getNextEmptySpacesInDirection(direction: Direction): Sequence<ContraptionLocation> =
        getLocationsInDirection(direction).takeWhile { contraptionLocation: ContraptionLocation ->
            contraptionLocation.toType() == ContraptionType.EMPTY_SPACE
        }

    /**
     * Returns next [ContraptionLocation]s in the [direction] of traversal, which can be a contiguous list of
     * empty spaces or the next [ContraptionLocation] with a contraption,
     * or a `null` when the beam terminates at some edge.
     */
    override fun ContraptionLocation.getNextLocationsInDirection(direction: Direction): List<ContraptionLocation?> =
        getNextEmptySpacesInDirection(direction).toList().takeUnless { it.isEmpty() } ?: listOf(getNeighbour(direction))

    /**
     * Returns `true` if a contraption is visited in [direction].
     */
    override fun ContraptionLocation.isContraptionVisitedInDirection(direction: Direction): Boolean =
        when (this.toType()) {
            ContraptionType.EMPTY_SPACE -> false
            else -> this.isVisitedInDirection(direction)
        }

}

private class BeamTracer(
    private val beamFinder: IBeamFinder,
    private val lastLocationTraced: ContraptionLocation?,
    private var traceDirection: Direction
) : IBeamFinder by beamFinder {

    // Trace is finished when the beam terminates at some edge or
    // when the last traced location is en empty space, then the next location will have a contraption and
    // so it will be checked for being previously visited in current trace direction
    // and if it was then the trace is finished (since it is cyclic)
    val isTraceFinished = lastLocationTraced == null ||
            lastLocationTraced.toType() == ContraptionType.EMPTY_SPACE &&
            lastLocationTraced.getNextLocationsInDirection(traceDirection).lastOrNull()
                ?.isContraptionVisitedInDirection(traceDirection) ?: false

    operator fun plus(locationWithDirectionPair: Pair<ContraptionLocation?, Direction>): BeamTracer =
        BeamTracer(
            this,
            lastLocationTraced = locationWithDirectionPair.first,
            traceDirection = locationWithDirectionPair.second
        )

    fun findNextBeamLocations(): List<BeamTracer> =
        lastLocationTraced!!.toNextDirections(traceDirection).map { direction: Direction ->
            this + (lastLocationTraced.getNextLocationsInDirection(direction).onEach { contraptionLocation ->
                // Mark as visited in direction
                contraptionLocation?.setVisitedInDirection(direction)
            }.last() to direction)
        }

}

private class ContraptionAnalyzer private constructor(
    private val contraptionGrid: ContraptionGrid
) : IBeamFinder by BeamFinder(contraptionGrid), IContraptionGrid by contraptionGrid {

    constructor(input: List<String>) : this(
        contraptionGrid = ContraptionGrid(input)
    )

    /**
     * Generates [BeamTracer]s for all the paths traversed by the beam through the [contraption grid][contraptionGrid]
     * from the [startLocation] in the direction of [startDirection].
     */
    private fun getAllPossibleBeamPathsFromStart(
        startLocation: ContraptionLocation,
        startDirection: Direction
    ): List<BeamTracer> =
        whileLoop(
            loopStartCounter = 0,
            initialResult = listOf(
                BeamTracer(
                    beamFinder = this,
                    lastLocationTraced = startLocation.also { contraptionLocation ->
                        // Mark as visited in direction
                        contraptionLocation.setVisitedInDirection(startDirection)
                    },
                    traceDirection = startDirection
                )
            ),
            exitCondition = { _: Int, lastIterationResult: List<BeamTracer>? ->
                lastIterationResult?.all { beamTracer: BeamTracer -> beamTracer.isTraceFinished } ?: false
            }
        ) { loopCounter: Int, lastIterationResult: List<BeamTracer> ->

            loopCounter to lastIterationResult.flatMap { beamTracer: BeamTracer ->
                if (beamTracer.isTraceFinished) {
                    listOf(beamTracer)
                } else {
                    beamTracer.findNextBeamLocations()
                }
            }
        }

    /**
     * Computes and returns the total number of tiles energized by the beam sent from [startLocation]
     * in the direction of [startDirection].
     */
    private fun computeTotalTilesEnergized(startLocation: ContraptionLocation, startDirection: Direction): Int =
        getAllPossibleBeamPathsFromStart(startLocation, startDirection).let {
            getAllContraptionLocations().count { contraptionLocation -> contraptionLocation.isVisited() }
        }

    /**
     * [Solution for Part-1 & Part-2]
     *
     * Returns the total number of tiles being energized by the beam sent through the contraption grid.
     * For Part-1, beam will be sent in from the top left corner in the [RIGHT] direction.
     * For Part-2, beam will be sent in from all edges and corners in the direction towards the grid, and then the
     * maximum number of tiles energized is returned.
     *
     * @param measureFromAllEdgeCorner Used for Part-2 when set to `true`.
     */
    fun getTotalTilesEnergized(measureFromAllEdgeCorner: Boolean = false): Int =
        if (measureFromAllEdgeCorner) {
            getAllEdgeCornerStartLocationsWithBeamDirection()
                .maxOf { (startLocation: ContraptionLocation, startDirection: Direction) ->
                    computeTotalTilesEnergized(startLocation, startDirection).also {
                        clearAllVisitsToContraptionLocations()
                    }
                }
        } else {
            computeTotalTilesEnergized(getStartLocation(), RIGHT)
        }

}