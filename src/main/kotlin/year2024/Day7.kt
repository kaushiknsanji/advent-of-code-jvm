/**
 * Problem: Day7: Bridge Repair
 * https://adventofcode.com/2024/day/7
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import kotlin.math.pow

private class Day7 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 3749
    println("=====")
    solveActual(1)      // 3119088655389
    println("=====")
    solveSample(2)      // 11387
    println("=====")
    solveActual(2)      // 264184041398847
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day7.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day7.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    BridgeRepairProcessor.parse(input)
        .getTotalCalibratedResult()
        .also(::println)
}

private fun doPart2(input: List<String>) {
    BridgeRepairProcessor.parse(input)
        .getTotalCalibratedResult(operatorTypesCount = 3)
        .also(::println)
}

private class BridgeRepairProcessor private constructor(
    private val calibrationMap: Map<Long, List<Long>>
) {

    companion object {
        private const val COLON = ':'
        private const val SPACE = ' '

        fun parse(input: List<String>): BridgeRepairProcessor = BridgeRepairProcessor(
            calibrationMap = input.associate { line ->
                line.substringBefore(COLON).toLong() to
                        line.substringAfter(COLON).split(SPACE).filterNot { it.isEmpty() }.map { it.toLong() }
            }
        )
    }

    // Lambda for concat operation
    private val concatOperation: (x: Long, y: Long) -> Long = { x, y -> "$x$y".toLong() }

    // List of lambda based operators
    private val operators: List<(x: Long, y: Long) -> Long> = listOf(Long::plus, Long::times, concatOperation)

    /**
     * Generates sequence of lambda [operators] combination for the [operandCount] and chosen [operatorTypesCount].
     *
     * @param operandCount [Int] number of Operands being evaluated on
     * @param operatorTypesCount [Int] number of Operator Types chosen for evaluation
     */
    private fun generateOperatorCombinations(
        operandCount: Int,
        operatorTypesCount: Int
    ): Sequence<List<(x: Long, y: Long) -> Long>> =
        sequence {
            // Total number of operators required in a combination
            val operatorCount = operandCount - 1

            // Total number of possible combinations of operators for the number of operator types chosen
            // and the number of operators required in a combination
            val operatorCombinations = if (operatorTypesCount == 3) {
                // For 3 different operator types and [operatorCount] number of operators
                3.0.pow(operatorCount.toDouble()).toInt()
            } else {
                // For 2 different operator types and [operatorCount] number of operators
                1 shl operatorCount
            }

            (0 until operatorCombinations).forEach { decimalNumber ->
                yield(
                    // Get the representation of this number according to the number of operator types chosen
                    // and then convert each digit in the representation into its corresponding lambda based operator
                    decimalNumber.toString(operatorTypesCount).padStart(operatorCount, '0')
                        .map { it.digitToInt() }
                        .map { operatorTypeNumber ->
                            operators[operatorTypeNumber]
                        }
                )
            }
        }

    /**
     * Using each lambda [operators] combination generated for the chosen [operatorTypesCount], it computes
     * with all [operands] to verify if it results in [testValue].
     *
     * @return `true` when the computation using a particular lambda [operators] combination results in [testValue];
     * `false` otherwise.
     */
    private fun isCalibrated(testValue: Long, operands: List<Long>, operatorTypesCount: Int): Boolean =
        generateOperatorCombinations(
            operands.size,
            operatorTypesCount
        ).any { operators: List<(x: Long, y: Long) -> Long> ->
            operands.reduceIndexed { nextIndex: Int, acc: Long, next: Long ->
                operators[nextIndex - 1].invoke(acc, next)
            } == testValue
        }

    /**
     * [Solution for Part 1 and 2]
     *
     * Returns sum of the test values of properly calibrated equations.
     *
     * @param operatorTypesCount [Int] number of Operator Types chosen for evaluation.
     * This is 2 for Part-1 and 3 for Part-2.
     */
    fun getTotalCalibratedResult(operatorTypesCount: Int = 2): Long =
        calibrationMap.filter { (testValue: Long, operands: List<Long>) ->
            isCalibrated(testValue, operands, operatorTypesCount)
        }.keys.sum()

}