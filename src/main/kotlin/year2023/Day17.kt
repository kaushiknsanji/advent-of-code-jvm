/**
 * Problem: Day17: Clumsy Crucible
 * https://adventofcode.com/2023/day/17
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import extensions.whileLoop
import utils.grid.TransverseDirection.*
import java.util.*
import utils.grid.TransverseDirection as Direction

private class Day17 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 102
    println("=====")
    solveActual(1)      // 638
    println("=====")
    solveSample(2)      // 94
    println("=====")
    solveSamplePart2Type2()             // 71
    println("=====")
    solveActual(2)      // 748
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day17.getSampleFile().readLines(), executeProblemPart)
}

private fun solveSamplePart2Type2(executeProblemPart: Int = 2) {
    execute(Day17.getSampleFile("_part2_2").readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day17.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    CrucibleFlowAnalyzer(input)
        .getLeastHeatLossInTransport(minStepsInDirection = 1, maxStepsInDirection = 3)
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    CrucibleFlowAnalyzer(input)
        .getLeastHeatLossInTransport(minStepsInDirection = 4, maxStepsInDirection = 10)
        .also { println(it) }
}

private class CityBlock(val x: Int, val y: Int) {

    // Set of Pairs of Directions with Step Count used to track which City Blocks were visited while traversing
    private val visitedDirectionsWithStepCount = mutableSetOf<Pair<Direction, Int>>()

    /**
     * Marks this [CityBlock] as visited in the [inDirection] of traversal along with [stepCount].
     */
    fun setVisitedInDirectionWithStepCount(inDirection: Direction, stepCount: Int) {
        visitedDirectionsWithStepCount.add(inDirection to stepCount)
    }

    /**
     * Marks this [CityBlock] as visited in all possible [Direction]s along with a step count of `1`. Used only for the
     * starting [CityBlock] during traversal.
     */
    fun setVisitedInAllDirections() {
        visitedDirectionsWithStepCount.addAll(Direction.entries.map { it to 1 })
    }

    /**
     * Returns `true` if the [CityBlock] had been visited in the [inDirection] of traversal along with [stepCount].
     */
    fun isVisitedInDirectionWithStepCount(inDirection: Direction, stepCount: Int): Boolean =
        visitedDirectionsWithStepCount.any { it.first == inDirection && it.second == stepCount }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CityBlock) return false

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

private interface ICityBlockGrid {
    fun getCityBlockOrNull(row: Int, column: Int): CityBlock?
    fun getCityBlock(row: Int, column: Int): CityBlock
    fun getAllCityBlocks(): Collection<CityBlock>
    fun getStartCityBlock(): CityBlock
    fun getEndCityBlock(): CityBlock
    fun CityBlock.getNeighbour(direction: Direction): CityBlock?
    fun CityBlock.getAllNeighbours(): Map<Direction, CityBlock?>
}

private class CityBlockGrid private constructor(
    rows: Int, columns: Int, gridPatternList: List<String>
) : ICityBlockGrid {

    constructor(gridPatternList: List<String>) : this(
        rows = gridPatternList.size,
        columns = gridPatternList.first().length,
        gridPatternList = gridPatternList
    )

    private val cityGridMap: Map<Int, List<CityBlock>> = (0 until rows).flatMap { x: Int ->
        (0 until columns).map { y: Int ->
            CityBlock(x, y)
        }
    }.groupBy { cityBlock: CityBlock -> cityBlock.x }

    private val cityGridHeatMap: Map<CityBlock, Int> =
        gridPatternList.flatMapIndexed { x: Int, rowHeatPattern: String ->
            rowHeatPattern.mapIndexed { y: Int, heatChar: Char ->
                getCityBlock(x, y) to heatChar.toString().toInt()
            }
        }.toMap()

    operator fun get(cityBlock: CityBlock): Int = cityGridHeatMap[cityBlock]!!

    override fun getCityBlockOrNull(row: Int, column: Int): CityBlock? = try {
        if (!cityGridMap.containsKey(row)) {
            throw NoSuchElementException()
        }
        cityGridMap[row]!!.single { it.y == column }
    } catch (e: NoSuchElementException) {
        null
    }

    override fun getCityBlock(row: Int, column: Int): CityBlock =
        getCityBlockOrNull(row, column) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${CityBlock::class.simpleName} at the given location ($row, $column)"
        )

    override fun getAllCityBlocks(): Collection<CityBlock> =
        cityGridMap.values.flatten()

    override fun getStartCityBlock(): CityBlock =
        getAllCityBlocks().first()

    override fun getEndCityBlock(): CityBlock =
        getAllCityBlocks().last()

    override fun CityBlock.getNeighbour(direction: Direction): CityBlock? = when (direction) {
        TOP -> getCityBlockOrNull(x - 1, y)
        BOTTOM -> getCityBlockOrNull(x + 1, y)
        RIGHT -> getCityBlockOrNull(x, y + 1)
        LEFT -> getCityBlockOrNull(x, y - 1)
    }

    override fun CityBlock.getAllNeighbours(): Map<Direction, CityBlock?> =
        Direction.entries.associateWith { direction -> getNeighbour(direction) }

}

private interface ICrucibleFlowFinder {
    fun Direction.flipDirection(): Direction
    fun CityBlock.getNextNeighbours(inDirection: Direction): Map<Direction, CityBlock?>
    fun CityBlock.toHeat(): Int
}

private class CrucibleFlowFinder(
    private val cityBlockGrid: CityBlockGrid
) : ICityBlockGrid by cityBlockGrid, ICrucibleFlowFinder {

    /**
     * Extension function of [Direction] which flips it by 180 degrees to return its opposite [Direction].
     */
    override fun Direction.flipDirection(): Direction = when (this) {
        TOP -> BOTTOM
        BOTTOM -> TOP
        RIGHT -> LEFT
        LEFT -> RIGHT
    }

    /**
     * Extension function of [CityBlock] to return its neighbouring [CityBlock]s along with the
     * key [Direction] of traversal as a [Map].
     *
     * Result will exclude previous [CityBlock] already traversed based on [inDirection].
     *
     * @param inDirection Current [Direction] of traversal
     */
    override fun CityBlock.getNextNeighbours(inDirection: Direction): Map<Direction, CityBlock?> =
        getAllNeighbours().filterKeys { nextDirection ->
            // Exclude previous City Block already traversed in the direction of [inDirection] by comparing
            // with its flip direction
            nextDirection != inDirection.flipDirection()
        }

    override fun CityBlock.toHeat(): Int = cityBlockGrid[this]

}

private class CrucibleFlowTracer(
    private val crucibleFlowFinder: ICrucibleFlowFinder,
    private val lastBlockTraced: CityBlock?,
    private val destination: CityBlock,
    val heatLoss: Int,
    private val traceDirection: Direction,
    private val traceCountInDirection: Int,
    private val minStepsInDirection: Int,
    private val maxStepsInDirection: Int
) : ICrucibleFlowFinder by crucibleFlowFinder {

    // Trace is finished when [destination] is reached along with the current step count in direction being within
    // the range of [minStepsInDirection] to [maxStepsInDirection]
    val isTraceFinished = lastBlockTraced == destination
            && traceCountInDirection in minStepsInDirection..maxStepsInDirection

    // Trace is dead when the last city block traced is not a city block or when the current step count in direction
    // is greater than the allowed [maxStepsInDirection]
    val isTraceDead = lastBlockTraced == null || traceCountInDirection > maxStepsInDirection

    /**
     * Provides the next step count based on [nextDirection] and current [traceDirection].
     */
    private fun getNextStepCount(nextDirection: Direction): Int = if (traceDirection != nextDirection) {
        // When direction of traversal has changed, then restart step count from 1
        1
    } else {
        // When next direction of traversal is same as current, then increment step count
        traceCountInDirection + 1
    }

    /**
     * Converts a [Map] of [Direction] and [CityBlock] to a [List] of [CrucibleFlowTracer]s.
     */
    private fun Map<Direction, CityBlock?>.toCrucibleFlowTracers(): List<CrucibleFlowTracer> =
        map { (nextDirection: Direction, nextCityBlock: CityBlock?) ->
            this@CrucibleFlowTracer + (nextDirection to nextCityBlock)
        }

    operator fun plus(nextDirectionCityBlockPair: Pair<Direction, CityBlock?>): CrucibleFlowTracer =
        getNextStepCount(nextDirectionCityBlockPair.first).let { nextStepCount ->
            CrucibleFlowTracer(
                this,
                lastBlockTraced = nextDirectionCityBlockPair.second?.apply {
                    // Mark the new city block as visited in direction of traversal along with step count
                    setVisitedInDirectionWithStepCount(nextDirectionCityBlockPair.first, nextStepCount)
                },
                destination = destination,
                heatLoss = heatLoss + (nextDirectionCityBlockPair.second?.toHeat() ?: 0), // Accumulate heat loss
                traceDirection = nextDirectionCityBlockPair.first,
                traceCountInDirection = nextStepCount,
                minStepsInDirection, maxStepsInDirection
            )
        }

    /**
     * Returns a [List] of [CrucibleFlowTracer]s from the [lastBlockTraced] based on the current [traceDirection],
     * [traceCountInDirection] and the [minStepsInDirection] step count restriction.
     */
    fun findNextCityBlocks(): List<CrucibleFlowTracer> =
        lastBlockTraced!!.getNextNeighbours(traceDirection)
            .filterNot { (nextDirection: Direction, nextCityBlock: CityBlock?) ->
                // Exclude next City Block if already visited in the same [nextDirection] and next step count, in
                // order to avoid evaluating repeated sub-problem
                nextCityBlock != null && nextCityBlock.isVisitedInDirectionWithStepCount(
                    nextDirection,
                    getNextStepCount(nextDirection)
                )
            }.let { nextDirectionCityBlockMap: Map<Direction, CityBlock?> ->
                // If current step count in direction is less than [minStepsInDirection], then only pick those entries
                // whose direction is same as the current and generate Flow tracers for the same. Otherwise, generate
                // Flow tracers for all the entries.
                nextDirectionCityBlockMap.filterKeys { nextDirection ->
                    traceCountInDirection < minStepsInDirection && nextDirection == traceDirection
                }.takeUnless {
                    it.isEmpty()
                }?.toCrucibleFlowTracers() ?: nextDirectionCityBlockMap.toCrucibleFlowTracers()
            }

    override fun toString(): String =
        "TraceDirection: $traceDirection, " +
                "TraceCount: $traceCountInDirection, " +
                "TraceDead: $isTraceDead, " +
                "TraceFinished: $isTraceFinished, " +
                "HeatLoss: $heatLoss, " +
                "lastBlockTraced: $lastBlockTraced"
}

private class CrucibleFlowAnalyzer private constructor(
    private val cityBlockGrid: CityBlockGrid
) : ICityBlockGrid by cityBlockGrid, ICrucibleFlowFinder by CrucibleFlowFinder(cityBlockGrid) {

    constructor(input: List<String>) : this(
        cityBlockGrid = CityBlockGrid(input)
    )

    /**
     * Generates a [PriorityQueue] of [CrucibleFlowTracer]s for all shortest paths to [end] from [start] whilst
     * following the restrictions of [minStepsInDirection] and [maxStepsInDirection].
     *
     * Result is obtained by following [Dijkstra](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm) algorithm
     * using [PriorityQueue].
     */
    private fun getAllPathsToEndFromStart(
        start: CityBlock,
        end: CityBlock,
        minStepsInDirection: Int,
        maxStepsInDirection: Int
    ): PriorityQueue<CrucibleFlowTracer> =
        whileLoop(
            loopStartCounter = 0,
            initialResult = PriorityQueue<CrucibleFlowTracer>(
                // Set Priority on accumulated Heat loss
                compareBy { it.heatLoss }
            ).apply {
                // Since start city block is the top left corner always, valid start directions are Bottom and Right
                listOf(BOTTOM, RIGHT).forEach { startDirection ->
                    // Add a flow tracer for each start direction into the Priority Queue with initial heat loss as 0
                    // and a step count of 1
                    add(
                        CrucibleFlowTracer(
                            this@CrucibleFlowAnalyzer,
                            lastBlockTraced = start,
                            destination = end,
                            heatLoss = 0,
                            traceDirection = startDirection,
                            traceCountInDirection = 1,
                            minStepsInDirection, maxStepsInDirection
                        )
                    )
                }
            },
            exitCondition = { _: Int, lastIterationResult: PriorityQueue<CrucibleFlowTracer>? ->
                // Terminate when we have found the path at the head of the Priority Queue
                lastIterationResult?.peek()?.isTraceFinished ?: false
            }
        ) { loopCounter: Int, lastIterationResult: PriorityQueue<CrucibleFlowTracer> ->

            loopCounter to lastIterationResult.apply {
                // Find next city blocks from the flow tracer
                // having the least accumulated heat loss (i.e., from the head of the Priority Queue)
                poll().findNextCityBlocks().forEach { crucibleFlowTracer: CrucibleFlowTracer ->
                    if (!crucibleFlowTracer.isTraceDead) {
                        // Load the next flow tracers to the Priority Queue only when their trace is still active
                        add(crucibleFlowTracer)
                    }
                }
            }

        }

    /**
     * [Solution for Part-1 & Part-2]
     *
     * Returns minimum heat loss incurred in transporting lava crucibles from [start][getStartCityBlock]
     * to [end][getEndCityBlock].
     *
     * @param minStepsInDirection Minimum number of steps to be taken in a direction prior to changing direction.
     * This is `1` for Part-1 and `4` for Part-2.
     * @param maxStepsInDirection Maximum number of steps that can be taken in a direction prior to being
     * forced to change direction. This is `3` for Part-1 and `10` for Part-2.
     */
    fun getLeastHeatLossInTransport(minStepsInDirection: Int, maxStepsInDirection: Int): Int =
        getAllPathsToEndFromStart(
            start = getStartCityBlock().apply {
                // Mark the start city block as visited from all directions with a step count of 1
                setVisitedInAllDirections()
            },
            end = getEndCityBlock(),
            minStepsInDirection,
            maxStepsInDirection
        ).peek().heatLoss

}