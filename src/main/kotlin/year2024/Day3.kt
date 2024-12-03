/**
 * Problem: Day3: Mull It Over
 * https://adventofcode.com/2024/day/3
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler

private class Day3 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSamplePart1()      // 161
    println("=====")
    solveActual(1)      // 174960292
    println("=====")
    solveSamplePart2()      // 48
    println("=====")
    solveActual(2)      // 56275602
    println("=====")
}

private fun solveSamplePart1() {
    execute(Day3.getSampleFile("_part1").readLines(), 1)
}

private fun solveSamplePart2() {
    execute(Day3.getSampleFile("_part2").readLines(), 2)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day3.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    CorruptComputer.parse(input)
        .getSumOfProducts()
        .also(::println)
}

private fun doPart2(input: List<String>) {
    CorruptComputer.parse(input)
        .getSumOfProducts(processConditions = true)
        .also(::println)
}

private class CorruptComputer private constructor(
    private val operandTriples: List<Triple<Boolean, Long, Long>>
) {
    companion object {
        // Regular expression to capture "do()" instruction or "don't() instruction or numbers
        private val mulConditionsRegex = """(don't\(\))|(do\(\))|mul\((\d+),(\d+)\)""".toRegex()

        // Part-2: Conditional multiplication is enabled at the beginning
        private var mulEnabled = true

        fun parse(input: List<String>): CorruptComputer = CorruptComputer(
            operandTriples = input.flatMap { commandLine ->
                mulConditionsRegex.findAll(commandLine).map { mulMatchResult ->
                    when {
                        // For "don't()" instruction captured
                        mulMatchResult.groupValues[1].isNotEmpty() -> {
                            // Disable multiplication
                            mulEnabled = false
                            null
                        }

                        // For "do()" instruction captured
                        mulMatchResult.groupValues[2].isNotEmpty() -> {
                            // Enable multiplication
                            mulEnabled = true
                            null
                        }

                        // For numbers captured
                        else -> Triple(
                            mulEnabled,
                            mulMatchResult.groupValues[3].toLong(),
                            mulMatchResult.groupValues[4].toLong()
                        )
                    }
                }.filterNotNull()
            }
        )
    }

    /**
     * [Solution for Part 1 and 2]
     *
     * Returns the Total of Products for all pairs of numbers found in the corrupted input.
     *
     * For Part 2, multiplication is done only when a recent "do()" instruction found in the corrupted input,
     * (re)enables the multiplication operation, while a "don't()" instruction disables the same. Product will be 0
     * for all pairs of numbers when disabled. Enabled or disabled state is present in [Triple.first] of all [Triple]s
     * in [operandTriples] that contains the two numbers to be multiplied.
     *
     * @param processConditions Boolean that allows "do()" and "don't()" instructions found
     * in the corrupted input to control the multiplication operation accordingly. Set to `true` for Part-2,
     * defaulted to `false` for Part-1.
     */
    fun getSumOfProducts(processConditions: Boolean = false): Long = operandTriples.sumOf { (operate, first, second) ->
        if (processConditions && !operate) 0 else first * second
    }

}