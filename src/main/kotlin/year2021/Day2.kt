/**
 * Problem: Day2: Dive!
 * https://adventofcode.com/2021/day/2
 *
 * @author Kaushik N Sanji (kaushiknsanji@gmail.com)
 */

package year2021

import base.BaseFileHandler

private class Day2 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 150
    println("=====")
    solveActual(1) // 1690020
    println("=====")
    solveSample(2) // 900
    println("=====")
    solveActual(2) // 1408487760
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day2.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day2.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    SubmarineControllerType1.parse(input)
        .processCommands()
        .getHorizontalPositionAndDepthProduct()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    SubmarineControllerType2.parse(input)
        .processCommands()
        .getHorizontalPositionAndDepthProduct()
        .also { println(it) }
}

/**
 * [Solution for Part-1]
 * Class for computing the Horizontal Position and Depth of Submarine after processing the commands.
 */
private class SubmarineControllerType1 private constructor(
    private val commandValues: List<Pair<String, Int>>
) : SubmarineController() {

    companion object {
        const val COMMAND_INC_DEPTH = COMMAND_DOWN
        const val COMMAND_DEC_DEPTH = COMMAND_UP

        fun parse(input: List<String>): SubmarineControllerType1 = SubmarineControllerType1(
            commandValues = input.toCommandValuePairs()
        )
    }

    fun processCommands(): SubmarineControllerType1 = this.apply {
        commandValues.forEach { (command: String, value: Int) ->
            when (command) {
                COMMAND_FORWARD -> updateHorizontalPosition(value)
                COMMAND_INC_DEPTH -> updateDepth(value)
                COMMAND_DEC_DEPTH -> updateDepth(-value)
            }
        }
    }

}

/**
 * [Solution for Part-2]
 * Class for computing the Horizontal Position and Depth of Submarine after processing the commands, based on Aim.
 */
private class SubmarineControllerType2 private constructor(
    private val commandValues: List<Pair<String, Int>>
) : SubmarineController() {

    companion object {
        const val COMMAND_INC_AIM = COMMAND_DOWN
        const val COMMAND_DEC_AIM = COMMAND_UP

        fun parse(input: List<String>): SubmarineControllerType2 = SubmarineControllerType2(
            commandValues = input.toCommandValuePairs()
        )
    }

    private var aim: Int = 0

    private fun updateAim(value: Int) {
        aim += value
    }

    private fun updateHorizontalPositionAndDepth(value: Int) {
        // Update Horizontal Position
        updateHorizontalPosition(value)
        // Update Depth based on Aim
        updateDepth(aim * value)
    }

    fun processCommands(): SubmarineControllerType2 = this.apply {
        commandValues.forEach { (command: String, value: Int) ->
            when (command) {
                COMMAND_FORWARD -> updateHorizontalPositionAndDepth(value)
                COMMAND_INC_AIM -> updateAim(value)
                COMMAND_DEC_AIM -> updateAim(-value)
            }
        }
    }

}

/**
 * Abstract class for controlling the Submarine's Horizontal Position and Depth.
 */
private abstract class SubmarineController {

    companion object {
        const val COMMAND_FORWARD = "forward"
        const val COMMAND_DOWN = "down"
        const val COMMAND_UP = "up"

        fun List<String>.toCommandValuePairs(): List<Pair<String, Int>> = map { commandValueStr ->
            with(commandValueStr.split(" ")) {
                first() to last().toInt()
            }
        }
    }

    private var horizontalPosition: Int = 0
    private var depth: Int = 0

    protected fun updateHorizontalPosition(value: Int) {
        horizontalPosition += value
    }

    protected fun updateDepth(value: Int) {
        depth += value
    }

    fun getHorizontalPositionAndDepthProduct(): Int = horizontalPosition * depth
}