/**
 * Problem: Day5: Binary Boarding
 * https://adventofcode.com/2020/day/5
 *
 * @author Kaushik N. Sanji
 */

package year2020

import base.BaseFileHandler

private class Day5 {
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
    input.map(BoardingPass::parse)
        .onEach { println(it) }
        .maxOf(BoardingPass::seatId)
        .also { println("Max Seat ID: $it") }
}

private fun doPart2(input: List<String>) {
    input.map(BoardingPass::parse)
        .onEach { println(it) }
        .map(BoardingPass::seatId)
        .apply {
            val yourSeatId: Int = (this.minOrNull()!!..this.maxOrNull()!!).first { it !in this }
            println("Max Seat ID: ${this.maxOrNull()!!}\nYour Seat ID: $yourSeatId")
        }
}

private class BoardingPass private constructor(
    val seatCode: String,
    val seatRowCode: String,
    val seatColumnCode: String
) {
    companion object {
        private const val upperBoundLiterals = "BR"

        fun parse(locationCode: String): BoardingPass = BoardingPass(
            seatCode = locationCode,
            seatRowCode = locationCode.substring(0, 6),
            seatColumnCode = locationCode.substring(7)
        )
    }

    val row: Int get() = locationCodeToInt(seatRowCode)

    val column: Int get() = locationCodeToInt(seatColumnCode)

    val seatId: Int get() = locationCodeToInt(seatCode)

    private fun locationCodeToInt(locationCode: String): Int = locationCode.map { code: Char ->
        if (code in upperBoundLiterals) {
            "1"
        } else {
            "0"
        }
    }.joinToString("").toInt(2)

    override fun toString(): String =
        "$seatRowCode$seatColumnCode: row $row, column $column, seat ID $seatId"
}