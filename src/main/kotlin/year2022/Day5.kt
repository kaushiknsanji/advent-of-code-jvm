/**
 * Problem: Day5: Supply Stacks
 * https://adventofcode.com/2022/day/5
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseFileHandler
import java.util.*

private class Day5 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // "CMZ"
    println("=====")
    solveActual(1) // "SHMSDGZVC"
    println("=====")
    solveSample(2) // "MCD"
    println("=====")
    solveActual(2) // "VRZGHDFBQ"
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day5.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day5.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    SupplyStackOps.parse(input)
        .restackWithCrateMover9000()
        .getTopOfAllStacksPostRearrangement()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    SupplyStackOps.parse(input)
        .restackWithCrateMover9001()
        .getTopOfAllStacksPostRearrangement()
        .also { println(it) }
}

private class CrateStacker private constructor(
    val arrangementMap: Map<Int, LinkedList<Char>>
) {
    companion object {
        fun create(arrangementInput: List<String>): CrateStacker = CrateStacker(
            arrangementInput.last().withIndex()
                .filterNot { stackIndexedValue -> stackIndexedValue.value.isWhitespace() }
                .map { stackIndexedValue ->
                    stackIndexedValue.value.digitToInt() to stackIndexedValue.index
                }
                .associate { stackNumberIndexPair ->
                    stackNumberIndexPair.first to LinkedList<Char>().apply {
                        (arrangementInput.lastIndex - 1 downTo 0).forEach { index ->
                            arrangementInput[index].getOrNull(stackNumberIndexPair.second)
                                ?.takeUnless { it.isWhitespace() }?.let { crateChar ->
                                    this.push(crateChar)
                                }
                        }
                    }
                }
        )
    }

    fun move9000(quantity: Int, fromStack: Int, toStack: Int) {
        repeat(quantity) {
            arrangementMap[fromStack]?.pop()?.let { fromStackTop ->
                arrangementMap[toStack]?.push(fromStackTop)
            }
        }
    }

    fun move9001(quantity: Int, fromStack: Int, toStack: Int) {
        (0 until quantity).forEach { destIndex ->
            arrangementMap[fromStack]?.pop()?.let { fromStackTop ->
                arrangementMap[toStack]?.add(destIndex, fromStackTop)
            }
        }
    }

}

private class SupplyStackOps private constructor(
    val crateStacker: CrateStacker,
    private val restackCommands: List<Triple<Int, Int, Int>>
) {
    companion object {
        val commandRegex = """move (\d+) from (\d+) to (\d+)""".toRegex()

        fun parse(input: List<String>): SupplyStackOps =
            input.indexOfFirst { it.isEmpty() || it.isBlank() }.let { emptyLineIndex ->
                SupplyStackOps(
                    crateStacker = CrateStacker.create(input.subList(0, emptyLineIndex)),
                    restackCommands = input.subList(emptyLineIndex + 1, input.lastIndex + 1)
                        .map { command ->
                            commandRegex.matchEntire(command)!!.let { matchResult ->
                                Triple(
                                    matchResult.groupValues[1].toInt(),
                                    matchResult.groupValues[2].toInt(),
                                    matchResult.groupValues[3].toInt()
                                )
                            }
                        }
                )
            }

    }

    /**
     * [Solution for Part-1]
     * Rearranges stacks with CrateMover 9000.
     */
    fun restackWithCrateMover9000(): SupplyStackOps = this.apply {
        restackCommands.forEach { triple ->
            crateStacker.move9000(triple.first, triple.second, triple.third)
        }
    }

    /**
     * [Solution for Part-2]
     * Rearranges stacks with CrateMover 9001.
     */
    fun restackWithCrateMover9001(): SupplyStackOps = this.apply {
        restackCommands.forEach { triple ->
            crateStacker.move9001(triple.first, triple.second, triple.third)
        }
    }

    fun getTopOfAllStacksPostRearrangement(): String =
        crateStacker.arrangementMap.values.mapNotNull { stack ->
            stack.peek()
        }.joinToString(separator = "")
}