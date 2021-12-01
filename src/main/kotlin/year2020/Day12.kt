/**
 * Problem: Day12: Rain Risk
 * https://adventofcode.com/2020/day/12
 *
 * @author Kaushik N Sanji
 */

package year2020

import base.BaseFileHandler
import kotlin.math.absoluteValue

private class Day12 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)
    println("=====")
    solveActual(1)
    println("=====")
    solveSample(2)
    println("=====")
    solveActual(2)
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
    ShipNavigation(input)
        .processCommands()
        .getManhattanDistanceFromStart()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    ShipNavigation(input)
        .processCommands(isWaypointBased = true)
        .getManhattanDistanceFromStart()
        .also { println(it) }
}

private class ShipNavigation(
    val commands: List<String>
) {

    companion object {
        const val NORTH = "N"
        const val SOUTH = "S"
        const val EAST = "E"
        const val WEST = "W"
        const val LEFT = "L"
        const val RIGHT = "R"
        const val FORWARD = "F"

        private val pointingDirections get() = listOf(SOUTH, WEST, NORTH, EAST)

        private val rotatingDirections get() = listOf(LEFT, RIGHT)

        private val commandPattern = """([A-Z])(\d+)""".toRegex()
    }

    private val positionMap: MutableMap<String, Int> =
        pointingDirections.associateWith { 0 }.toMutableMap()

    private var currentPointingDirection = EAST

    private val currentWaypointPairs: MutableList<Pair<String, Int>> = mutableListOf(
        EAST to 10,
        NORTH to 1
    )

    private fun getEffectiveEastWestPositionValue(): Int =
        positionMap.filterKeys { it in listOf(EAST, WEST) }.values.reduce { acc, next -> (acc - next).absoluteValue }

    private fun getEffectiveNorthSouthPositionValue(): Int =
        positionMap.filterKeys { it in listOf(NORTH, SOUTH) }.values.reduce { acc, next -> (acc - next).absoluteValue }

    fun getManhattanDistanceFromStart(): Int =
        getEffectiveNorthSouthPositionValue() + getEffectiveEastWestPositionValue()

    fun processCommands(isWaypointBased: Boolean = false): ShipNavigation = this.apply {
        commands.mapNotNull { command: String ->
            commandPattern.find(command)?.destructured?.let { (commandKey: String, value: String) -> commandKey to value.toInt() }
        }.forEach { (commandKey: String, value: Int) ->
            when (commandKey) {
                FORWARD -> {
                    moveInGivenDirection(
                        isWaypointBased.takeIf { it }?.let { commandKey } ?: currentPointingDirection,
                        value,
                        isWaypointBased
                    )
                }
                in pointingDirections -> {
                    moveInGivenDirection(commandKey, value, isWaypointBased)
                }
                in rotatingDirections -> {
                    changePointingDirection(commandKey, value, isWaypointBased)
                }
            }
        }
    }

    private fun moveInGivenDirection(direction: String, units: Int, isWaypointBased: Boolean = false) {
        if (!isWaypointBased) {
            positionMap[direction] = positionMap[direction]!! + units
        } else {
            if (direction == FORWARD) {
                currentWaypointPairs.map { (waypointDirection, value) ->
                    waypointDirection to value * units
                }.forEach { (waypointDirection, value) ->
                    positionMap[waypointDirection] = positionMap.getOrElse(waypointDirection) { 0 } + value
                }
            } else {
                val waypointDirectionIndex: IndexedValue<Pair<String, Int>>? =
                    currentWaypointPairs.withIndex().firstOrNull { it.value.first == direction }
                if (waypointDirectionIndex != null) {
                    currentWaypointPairs[waypointDirectionIndex.index] =
                        direction to waypointDirectionIndex.value.second + units
                } else {
                    currentWaypointPairs.add(direction to units)
                }
            }
        }
    }

    private fun changePointingDirection(rotateDirection: String, rotateBy: Int, isWaypointBased: Boolean = false) {
        if (!isWaypointBased) {
            currentPointingDirection =
                pointingDirections[getNewPointingDirectionIndex(currentPointingDirection, rotateDirection, rotateBy)]
        } else {
            currentWaypointPairs.forEachIndexed { index, pair ->
                currentWaypointPairs[index] =
                    pointingDirections[getNewPointingDirectionIndex(
                        pair.first,
                        rotateDirection,
                        rotateBy
                    )] to pair.second
            }
        }
    }

    private fun getNewPointingDirectionIndex(
        pointingDirection: String,
        rotateDirection: String,
        rotateBy: Int
    ): Int {
        val rotateTimes90: Int = (rotateBy / 90) % 4
        return when (rotateDirection) {
            LEFT -> {
                (pointingDirections.indexOf(pointingDirection) - rotateTimes90).let {
                    if (it < 0) it + pointingDirections.size else it
                }
            }
            else -> {
                (pointingDirections.indexOf(pointingDirection) + rotateTimes90) % pointingDirections.size
            }
        }
    }

}