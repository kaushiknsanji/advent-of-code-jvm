/**
 * Problem: Day3: Mull It Over
 * https://adventofcode.com/2024/day/3
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler

class Day3 : BaseProblemHandler() {

    /**
     * Returns the Package name of this problem class
     */
    override fun getCurrentPackageName(): String = this::class.java.`package`.name

    /**
     * Returns the Class name of this problem class
     */
    override fun getClassName(): String = this::class.java.simpleName

    /**
     * Executes "Part-1" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart1(input: List<String>, otherArgs: Array<out Any?>): Any =
        CorruptComputer.parse(input)
            .getSumOfProducts()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        CorruptComputer.parse(input)
            .getSumOfProducts(processConditions = true)

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, true, 0, 161L)
        solveActual(1, false, 0, 174960292L)
        solveSample(2, true, 0, 48L)
        solveActual(2, false, 0, 56275602L)
    }

}

fun main() {
    Day3().start()
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