/**
 * Problem: Day15: Rambunctious Recitation
 * https://adventofcode.com/2020/day/15
 *
 * @author Kaushik N Sanji
 */

package year2020

import base.BaseFileHandler

private class Day15 {
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
    MemoryGame.parse(input)
        .forEach { game: MemoryGame ->
            println(game.speakNumber(2020))
        }
}

private fun doPart2(input: List<String>) {
    MemoryGame.parse(input)
        .forEach { game: MemoryGame ->
            println(game.speakNumber(30000000))
        }
}

private class MemoryGame private constructor(
    val startingNumbers: List<Int>
) {
    companion object {
        fun parse(input: List<String>): List<MemoryGame> = mutableListOf<MemoryGame>().apply {
            input.forEach { str: String ->
                add(
                    MemoryGame(str.split(",").map { it.trim().toInt() })
                )
            }
        }
    }

    private val spokenNumberTurnsMap = mutableMapOf<Int, IntArray>()

    private fun IntArray.add(value: Int) {
        if (this.any { it == 0 }) {
            this[indexOfFirst { it == 0 }] = value
        } else {
            this[0] = this[1]
            this[1] = value
        }
    }

    private fun updateSpokenNumberTurnsMap(spokenNumber: Int, turnNumber: Int) {
        if (spokenNumberTurnsMap.containsKey(spokenNumber)) {
            spokenNumberTurnsMap[spokenNumber]!!.add(turnNumber)
        } else {
            spokenNumberTurnsMap[spokenNumber] = IntArray(2).apply { add(turnNumber) }
        }
    }

    private fun getLastTwoTurnNumbersDifference(spokenNumber: Int): Int =
        spokenNumberTurnsMap[spokenNumber]!!.reduce { acc, next -> next - acc }

    fun speakNumber(nthNumber: Int): Int {
        var lastSpokenNumber = startingNumbers[0]
        val startingNumbersSize = startingNumbers.size

        repeat(nthNumber) { index ->
            lastSpokenNumber = if (index < startingNumbersSize) {
                startingNumbers[index]
            } else {
                if (spokenNumberTurnsMap.containsKey(lastSpokenNumber) && spokenNumberTurnsMap[lastSpokenNumber]!!.any { it == 0 }) {
                    0
                } else {
                    getLastTwoTurnNumbersDifference(lastSpokenNumber)
                }
            }
            updateSpokenNumberTurnsMap(lastSpokenNumber, index + 1)
        }

        return lastSpokenNumber
    }
}