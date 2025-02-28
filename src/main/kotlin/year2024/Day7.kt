/**
 * Problem: Day7: Bridge Repair
 * https://adventofcode.com/2024/day/7
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler
import utils.Constants.COLON_CHAR
import utils.Constants.NO_0_CHAR
import utils.findAllLong
import kotlin.math.pow

class Day7 : BaseProblemHandler() {

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
        BridgeRepairProcessor.parse(input)
            .getTotalCalibratedResult()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        BridgeRepairProcessor.parse(input)
            .getTotalCalibratedResult(operatorTypesCount = 3)

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, 3749L)
        solveActual(1, false, 0, 3119088655389L)
        solveSample(2, false, 0, 11387L)
        solveActual(2, false, 0, 264184041398847L)
    }

}

fun main() {
    Day7().start()
}

private class BridgeRepairProcessor private constructor(
    private val calibrationMap: Map<Long, List<Long>>
) {

    companion object {

        fun parse(input: List<String>): BridgeRepairProcessor = BridgeRepairProcessor(
            calibrationMap = input.associate { line ->
                line.substringBefore(COLON_CHAR).toLong() to
                        line.substringAfter(COLON_CHAR).findAllLong()
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

            repeat(operatorCombinations) { decimalNumber ->
                yield(
                    // Get the representation of this number according to the number of operator types chosen
                    // and then convert each digit in the representation into its corresponding lambda based operator
                    decimalNumber.toString(operatorTypesCount).padStart(operatorCount, NO_0_CHAR)
                        .map(Char::digitToInt)
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