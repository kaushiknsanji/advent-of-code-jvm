/**
 * Problem: Day9: Rope Bridge
 * https://adventofcode.com/2022/day/9
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseFileHandler
import utils.grid.DiagonalDirection
import utils.grid.TransverseDirection

private class Day9 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 13
    println("=====")
    solveActual(1) // 6486
    println("=====")
    solveSample(2) // 1
    println("=====")
    solvePart2Sample1() // 36
    println("=====")
    solveActual(2) // 2678
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day9.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day9.getActualTestFile().readLines(), executeProblemPart)
}

private fun solvePart2Sample1() {
    execute(Day9.getSampleFile("_part2_1").readLines(), 2)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    RopeBridgeAnalyzer(headMotionList = input)
        .process()
        .getTotalOfDistinctTailPositionsVisited()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    RopeBridgeAnalyzer(headMotionList = input, ropeKnotProcessor = RopeKnotProcessor(10))
        .process()
        .getTotalOfDistinctTailPositionsVisited()
        .also { println(it) }
}

private data class RopeKnotsPosition(val x: Int, val y: Int)

private interface IRopeKnotGrid {
    fun RopeKnotsPosition.getNeighbour(direction: TransverseDirection): RopeKnotsPosition
    fun RopeKnotsPosition.getNeighbour(direction: DiagonalDirection): RopeKnotsPosition
    fun RopeKnotsPosition.getAllNeighboursInTransverseDirection(): Collection<RopeKnotsPosition>
    fun RopeKnotsPosition.getAllNeighboursInDiagonalDirection(): Collection<RopeKnotsPosition>
    fun RopeKnotsPosition.getAllNeighbours(): Collection<RopeKnotsPosition>
    fun RopeKnotsPosition.isInVicinity(other: RopeKnotsPosition): Boolean
}

private interface IRopeKnotMovement {
    fun getKnotPosition(): RopeKnotsPosition
    fun updateKnotPosition(newKnotPosition: RopeKnotsPosition)
}

private class HeadKnotMovement(
    private var knotPosition: RopeKnotsPosition = RopeKnotsPosition(0, 0),
    private var previousKnotPosition: RopeKnotsPosition = RopeKnotsPosition(0, 0)
) : IRopeKnotMovement {

    override fun getKnotPosition(): RopeKnotsPosition = knotPosition

    override fun updateKnotPosition(newKnotPosition: RopeKnotsPosition) {
        previousKnotPosition = knotPosition
        knotPosition = newKnotPosition
    }

    fun getPreviousPosition(): RopeKnotsPosition = previousKnotPosition
}

private class AdditionalKnotMovement(
    private var knotPosition: RopeKnotsPosition = RopeKnotsPosition(0, 0),
    private val isTailKnot: Boolean = false
) : IRopeKnotMovement {

    private val positionTracer = mutableListOf(knotPosition)

    override fun getKnotPosition(): RopeKnotsPosition = knotPosition

    override fun updateKnotPosition(newKnotPosition: RopeKnotsPosition) {
        knotPosition = newKnotPosition
        updatePositionTracer()
    }

    private fun updatePositionTracer() {
        if (isTailKnot) {
            positionTracer.add(knotPosition)
        }
    }

    fun getPositionsVisited(): List<RopeKnotsPosition> = positionTracer
}

private class RopeKnotProcessor(
    private val noOfRopeKnots: Int = 2
) : IRopeKnotGrid {

    private val knotsList: List<IRopeKnotMovement>

    init {
        require(noOfRopeKnots >= 2) {
            "${this::class.simpleName} requires at least 2 knots to work as intended"
        }

        knotsList = mutableListOf<IRopeKnotMovement>().apply {
            (0 until noOfRopeKnots).forEach { index ->
                when (index) {
                    0 -> add(HeadKnotMovement())
                    noOfRopeKnots - 1 -> add(AdditionalKnotMovement(isTailKnot = true))
                    else -> add(AdditionalKnotMovement())
                }
            }
        }
    }

    private val head = knotsList[0] as HeadKnotMovement

    fun getTail(): AdditionalKnotMovement = knotsList[knotsList.lastIndex] as AdditionalKnotMovement

    fun moveHead(toDirection: TransverseDirection, steps: Int) {
        repeat(steps) {
            // update head-knot
            head.updateKnotPosition(head.getKnotPosition().getNeighbour(toDirection))
            // refresh additional knots in the rope
            refreshAdditionalKnots()
        }
    }

    private fun refreshAdditionalKnots() {
        knotsList.zipWithNext()
            .forEach { knotPair ->
                if (knotPair.first is HeadKnotMovement) {
                    refreshHeadFollowingKnot(knotPair.second as AdditionalKnotMovement)
                } else {
                    refreshFollowingKnot(
                        knotPair.first as AdditionalKnotMovement,
                        knotPair.second as AdditionalKnotMovement
                    )
                }
            }
    }

    private fun refreshHeadFollowingKnot(nextKnot: AdditionalKnotMovement) {
        if (!head.getKnotPosition().isInVicinity(nextKnot.getKnotPosition())) {
            nextKnot.updateKnotPosition(head.getPreviousPosition())
        }
    }

    private fun refreshFollowingKnot(processedKnot: AdditionalKnotMovement, nextKnot: AdditionalKnotMovement) {
        if (!processedKnot.getKnotPosition().isInVicinity(nextKnot.getKnotPosition())) {
            nextKnot.updateKnotPosition(
                try {
                    (processedKnot.getKnotPosition()
                        .getAllNeighboursInTransverseDirection() intersect nextKnot.getKnotPosition()
                        .getAllNeighbours()).single()
                } catch (e: NoSuchElementException) {
                    (processedKnot.getKnotPosition()
                        .getAllNeighboursInDiagonalDirection() intersect nextKnot.getKnotPosition()
                        .getAllNeighbours()).single()
                }
            )
        }
    }

    override fun RopeKnotsPosition.getNeighbour(direction: TransverseDirection): RopeKnotsPosition =
        when (direction) {
            TransverseDirection.TOP -> RopeKnotsPosition(x - 1, y)
            TransverseDirection.BOTTOM -> RopeKnotsPosition(x + 1, y)
            TransverseDirection.RIGHT -> RopeKnotsPosition(x, y + 1)
            TransverseDirection.LEFT -> RopeKnotsPosition(x, y - 1)
        }

    override fun RopeKnotsPosition.getNeighbour(direction: DiagonalDirection): RopeKnotsPosition =
        when (direction) {
            DiagonalDirection.TOP_LEFT -> RopeKnotsPosition(x - 1, y - 1)
            DiagonalDirection.TOP_RIGHT -> RopeKnotsPosition(x - 1, y + 1)
            DiagonalDirection.BOTTOM_LEFT -> RopeKnotsPosition(x + 1, y - 1)
            DiagonalDirection.BOTTOM_RIGHT -> RopeKnotsPosition(x + 1, y + 1)
        }

    override fun RopeKnotsPosition.getAllNeighboursInTransverseDirection(): Collection<RopeKnotsPosition> =
        TransverseDirection.values().map { direction -> getNeighbour(direction) }

    override fun RopeKnotsPosition.getAllNeighboursInDiagonalDirection(): Collection<RopeKnotsPosition> =
        DiagonalDirection.values().map { direction -> getNeighbour(direction) }

    override fun RopeKnotsPosition.getAllNeighbours(): Collection<RopeKnotsPosition> =
        getAllNeighboursInTransverseDirection() + getAllNeighboursInDiagonalDirection()

    override fun RopeKnotsPosition.isInVicinity(other: RopeKnotsPosition): Boolean =
        this in other.getAllNeighbours() || this == other

}

private class RopeBridgeAnalyzer(
    private val headMotionList: List<String>,
    private val ropeKnotProcessor: RopeKnotProcessor = RopeKnotProcessor()
) {
    companion object {
        private const val LEFT = "L"
        private const val RIGHT = "R"
        private const val UP = "U"
        private const val DOWN = "D"
    }

    private val keyToDirectionMap = mapOf(
        LEFT to TransverseDirection.LEFT,
        RIGHT to TransverseDirection.RIGHT,
        UP to TransverseDirection.TOP,
        DOWN to TransverseDirection.BOTTOM
    )

    fun process(): RopeBridgeAnalyzer = this.apply {
        headMotionList.forEach { line ->
            line.split(" ").let { splitStrings ->
                ropeKnotProcessor.moveHead(
                    keyToDirectionMap[splitStrings[0]]!!,
                    splitStrings[1].toInt()
                )
            }
        }
    }

    /**
     * [Solution for Part 1 & 2]
     * Returns the number of distinct positions visited by the Tail Rope Knot during all Head Knot motions.
     */
    fun getTotalOfDistinctTailPositionsVisited(): Int = ropeKnotProcessor.getTail().getPositionsVisited().toSet().size

}