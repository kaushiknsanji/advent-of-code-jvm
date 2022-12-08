/**
 * Problem: Day1: Calorie Counting
 * https://adventofcode.com/2022/day/1
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseFileHandler

private class Day1 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 24000
    println("=====")
    solveActual(1) // 70720
    println("=====")
    solveSample(2) // 45000
    println("=====")
    solveActual(2) // 207148
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day1.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day1.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    input.joinToString(separator = " ")
        .split("""\s{2,}""".toRegex())
        .maxOf { calorieValuesPerElf: String ->
            calorieValuesPerElf.split(" ").sumOf(String::toInt)
        }
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    input.joinToString(separator = " ")
        .split("""\s{2,}""".toRegex())
        .map { calorieValuesPerElf: String ->
            calorieValuesPerElf.split(" ").sumOf(String::toInt)
        }
        .sorted()
        .takeLast(3)
        .sum()
        .also { println(it) }
}