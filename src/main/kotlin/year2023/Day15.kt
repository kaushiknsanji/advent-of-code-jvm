/**
 * Problem: Day15: Lens Library
 * https://adventofcode.com/2023/day/15
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler

private class Day15 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 1320
    println("=====")
    solveActual(1)      // 517965
    println("=====")
    solveSample(2)      // 145
    println("=====")
    solveActual(2)      // 267372
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day15.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day15.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    InitSequenceAnalyzer.parse(input)
        .getTotalHashOfInputSequence()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    InitSequenceAnalyzer.parse(input)
        .getTotalFocusingPowerOfLensConfig()
        .also { println(it) }
}

private class LensBox(
    private val id: Int,
    private val lensArray: MutableList<Pair<String, Int>> = mutableListOf()
) {
    companion object {
        const val OP_CHAR_REPLACE = '='
        const val OP_CHAR_REMOVE = '-'
    }

    /**
     * Arranges lens in the [LensBox] as per the [command]
     */
    fun execute(command: String) {
        if (OP_CHAR_REPLACE in command) {
            doInsertOrReplaceOperation(command.split(OP_CHAR_REPLACE).let { it.first() to it.last().toInt() })
        } else {
            doRemoveOperation(command.substringBefore(OP_CHAR_REMOVE))
        }
    }

    /**
     * Returns the total focal power of the [LensBox]
     */
    fun getBoxPower(): Int = lensArray.withIndex()
        .fold(0) { acc: Int, (slotIndex: Int, lensLabelFocalLengthPair: Pair<String, Int>) ->
            acc + (slotIndex + 1) * lensLabelFocalLengthPair.second
        } * (id + 1)

    private fun doRemoveOperation(lensLabel: String) {
        // Remove lens with the [lensLabel] if found in the Box
        lensArray.removeIf { (label: String, _: Int) ->
            label == lensLabel
        }
    }

    private fun doInsertOrReplaceOperation(lensLabelFocalLengthPair: Pair<String, Int>) =
        lensArray.indexOfFirst { (label: String, _: Int) -> label == lensLabelFocalLengthPair.first }
            .let { lensPositionIfFound ->
                if (lensPositionIfFound > -1) {
                    // Replace when an existing lens is found in the Box
                    lensArray.removeAt(lensPositionIfFound)
                    lensArray.add(lensPositionIfFound, lensLabelFocalLengthPair)
                } else {
                    // Just add the new lens to the Box when not already present
                    lensArray.add(lensLabelFocalLengthPair)
                }
            }

    override fun toString(): String =
        "Box ${id + 1}: ${lensArray.joinToString(" ") { (label: String, focalLength: Int) -> "[$label $focalLength]" }}"

}

private class InitSequenceAnalyzer private constructor(
    private val initCommands: List<String>
) {
    companion object {
        fun parse(input: List<String>): InitSequenceAnalyzer = InitSequenceAnalyzer(
            initCommands = input.single().split(",")
        )
    }

    /**
     * Extension function on a [List] of ASCII values to convert them into their Hash value.
     */
    private fun List<Int>.toHash(): Int = this.fold(0) { acc: Int, nextAscii: Int ->
        ((acc + nextAscii) * 17).rem(256)
    }

    /**
     * [Solution for Part-1]
     *
     * Returns a total of the Hash of each of the [initCommands].
     */
    fun getTotalHashOfInputSequence(): Int =
        initCommands.sumOf { command: String ->
            command.map { commandChar -> commandChar.code }.toHash()
        }

    /**
     * [Solution for Part-2]
     *
     * Returns the total focal power of the entire lens configuration arranged as per [initCommands].
     */
    fun getTotalFocusingPowerOfLensConfig() = mutableMapOf<Int, LensBox>().apply {
        initCommands.map { command: String ->
            // Convert to a pair of Command and Label
            command to if (command.contains(LensBox.OP_CHAR_REPLACE)) {
                command.substringBefore(LensBox.OP_CHAR_REPLACE)
            } else {
                command.substringBefore(LensBox.OP_CHAR_REMOVE)
            }
        }.map { (command: String, label: String) ->
            // Convert Command-Label pair to a pair of Command and Label Hash
            command to label.map { labelChar -> labelChar.code }.toHash()
        }.forEach { (command: String, labelHash: Int) ->
            // For each Label Hash, create or reuse the corresponding LensBox and execute the [command]
            getOrPut(labelHash) {
                LensBox(labelHash)
            }.execute(command)
        }
    }.values.sumOf { lensBox: LensBox -> lensBox.getBoxPower() }

}