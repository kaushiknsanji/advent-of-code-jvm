/**
 * Problem: Day8: Handheld Halting
 * https://adventofcode.com/2020/day/8
 *
 * @author Kaushik N Sanji
 */

package year2020

import base.BaseFileHandler

private class Day8 {
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
    HandheldGame.create(input)
        .getAccumulatorValuePriorToLoopRepeat()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    HandheldGame.create(input)
        .getAccumulatorValueAfterFix()
        .also { println(it) }
}

private class HandheldGame private constructor(
    val instructionList: List<String>,
    private val decodedInstructionPairs: MutableList<Pair<String, Int>> = mutableListOf()
) {
    companion object {
        const val KEY_NO_OP = "nop"
        const val KEY_ACCUMULATOR = "acc"
        const val KEY_JUMP = "jmp"

        val instructionPattern = """([\w]{3}) ([+|-][\d]+)""".toRegex()

        fun create(instructionList: List<String>): HandheldGame = HandheldGame(instructionList).apply { decode() }
    }

    private fun decode() {
        instructionList.mapTo(decodedInstructionPairs) { instruction: String ->
            instructionPattern.find(instruction)!!.destructured.let { (operation, argument) ->
                operation to argument.toInt()
            }
        }
    }

    fun getAccumulatorValuePriorToLoopRepeat(): Int {
        val totalCountOfInstructions = instructionList.size
        val traceAccumulatorPath = mutableListOf<Int>()
        var currentIndex = 0
        var accumulator = 0

        while (currentIndex < totalCountOfInstructions) {
            val (operation, argument) = decodedInstructionPairs[currentIndex]

            if (operation == KEY_NO_OP) {
                currentIndex++
            } else if (operation == KEY_JUMP) {
                currentIndex += argument
            } else if (operation == KEY_ACCUMULATOR) {
                if (traceAccumulatorPath.contains(currentIndex)) {
                    break
                }
                traceAccumulatorPath.add(currentIndex)
                accumulator += argument
                currentIndex++
            }
        }

        return accumulator
    }

    fun getAccumulatorValueAfterFix(): Int {
        val totalCountOfInstructions = instructionList.size
        val traceAccumulatorPath = mutableListOf<Int>()
        var currentIndex = 0
        var accumulator = 0
        var fixAccepted = false

        val correctedInstructionPairs = decodedInstructionPairs.toMutableList()

        while (currentIndex < totalCountOfInstructions) {
            val (operation, argument) = decodedInstructionPairs[currentIndex]

            if (operation == KEY_NO_OP || operation == KEY_JUMP) {
                if (!fixAccepted && testForLoop(correctedInstructionPairs, currentIndex, traceAccumulatorPath)) {
                    // Loop detected, try with another operation
                    if (testForLoop(
                            correctedInstructionPairs.toggleOperand(currentIndex),
                            currentIndex,
                            traceAccumulatorPath
                        )
                    ) {
                        // Loop again detected, reset the change done for testing loop
                        correctedInstructionPairs.toggleOperand(currentIndex)
                    } else {
                        // Loop fixed, keep the change done for testing loop and set the 'fixAccepted' flag
                        fixAccepted = true
                    }
                } else {
                    // Loop fixed without changing operation
                    // Set the 'fixAccepted' flag
                    fixAccepted = true
                }

                // Increment index based on the operation
                currentIndex += getNextIndexBasedOnOperand(correctedInstructionPairs[currentIndex])

            } else if (operation == KEY_ACCUMULATOR) {
                traceAccumulatorPath.add(currentIndex)
                accumulator += argument
                currentIndex++
            }
        }

        return accumulator
    }

    private fun getNextIndexBasedOnOperand(instructionPair: Pair<String, Int>): Int =
        if (instructionPair.first == KEY_NO_OP) {
            1
        } else {
            instructionPair.second
        }

    private fun MutableList<Pair<String, Int>>.toggleOperand(instructionIndexToModify: Int): MutableList<Pair<String, Int>> =
        this.apply {
            val (operation, argument) = this[instructionIndexToModify]

            val newOperation = if (operation == KEY_NO_OP) {
                KEY_JUMP
            } else {
                KEY_NO_OP
            }

            this[instructionIndexToModify] = newOperation to argument
        }

    private fun testForLoop(
        instructionPairs: MutableList<Pair<String, Int>>,
        instructionIndexToModify: Int,
        traceAccumulatorPath: List<Int>
    ): Boolean {
        val totalCountOfInstructions = instructionPairs.size
        val traceJumpPath = mutableListOf<Int>()
        var currentIndex = instructionIndexToModify
        var loopDetected = false

        while (currentIndex < totalCountOfInstructions) {
            val (operation, argument) = instructionPairs[currentIndex]

            if (operation == KEY_NO_OP) {
                currentIndex++
            } else if (operation == KEY_JUMP) {
                if (argument == 0 || traceJumpPath.contains(currentIndex)) {
                    loopDetected = true
                    break
                } else {
                    traceJumpPath.add(currentIndex)
                    currentIndex += argument
                }
            } else if (operation == KEY_ACCUMULATOR) {
                if (traceAccumulatorPath.contains(currentIndex)) {
                    loopDetected = true
                    break
                }
                currentIndex++
            }
        }

        return loopDetected
    }

}