/**
 * Problem: Day1: Secret Entrance
 * https://adventofcode.com/2025/day/1
 *
 * @author <a href='mailto:kaushiknsanji@gmail.com'>Kaushik N Sanji</a>
 */

package year2025

import base.BaseProblemHandler
import utils.Constants.LEFT_CHAR
import utils.Constants.RIGHT_CHAR
import kotlin.math.abs
import kotlin.math.sign

class Day1 : BaseProblemHandler() {

    /**
     * Returns the Package name of this problem class
     */
    override fun getCurrentPackageName(): String = this::class.java.packageName

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
        RotarySafeAnalyzer.parse(input)
            .getDoorPasswordUsingOldProtocol()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        RotarySafeAnalyzer.parse(input)
            .getDoorPasswordUsingNewProtocol()

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, 3)
        solveActual(1, false, 0, 984)
        solveSample(2, false, 0, 6)
        solveActual(2, false, 0, 5657)
    }

}

fun main() {
    Day1().start()
}

private class RotarySafeAnalyzer private constructor(
    private val dialStepsList: List<Int>,
    private val startDialPosition: Int
) {

    companion object {
        const val DIAL_SIZE = 100

        fun parse(input: List<String>, startDialPosition: Int = 50): RotarySafeAnalyzer = RotarySafeAnalyzer(
            dialStepsList = input.map { dialRotationString ->
                if (dialRotationString.startsWith(LEFT_CHAR)) {
                    -dialRotationString.substringAfter(LEFT_CHAR).toInt()
                } else {
                    dialRotationString.substringAfter(RIGHT_CHAR).toInt()
                }
            },
            startDialPosition = startDialPosition
        )
    }

    /**
     * Returns the number of times the dial passes through 0 position, including when it lands at 0 position.
     *
     * @param lastDialPosition [Int] value of the position the dial is currently pointing to
     * @param newDialPosition [Int] value of the position the dial is pointing to after rotating the dial
     * for the given number of [clicks][dialSteps] in the direction of the sign of [dialSteps].
     */
    private fun getZeroClickCount(
        lastDialPosition: Int,
        newDialPosition: Int,
        dialSteps: Int
    ): Int {
        // Rotary Dial is said to complete a Full rotation when it moves from the last position to the first (clockwise),
        // that is, from 99 to 0 (or 0 to 99 counterclockwise) in our case. Based on the direction of rotation
        // from `dialSteps`, we exclude the last step in the calculation of the number of circular rotations completed
        // because the last step can at times happen to point the dial at 0.
        val countOfCircularRotations = abs(lastDialPosition + dialSteps - dialSteps.sign) / DIAL_SIZE +
                if (lastDialPosition + dialSteps < 0 && lastDialPosition != 0) {
                    // Edge case correction to circular rotation count that occurs when starting away from 0 and
                    // rotating the dial counterclockwise which makes the new dial position go negative
                    // before modular wraparound. Modular wraparound hides the fact that we just went
                    // pass the 0 dial position.
                    1
                } else 0

        // With the proper count of circular rotations, we add one more to the count
        // if the new dial position happens to be at 0.
        return countOfCircularRotations + if (newDialPosition == 0) 1 else 0
    }

    /**
     * [Solution for Part-1]
     *
     * Returns the Password to open the door which is given by the number of times the dial points at 0
     * after applying a rotation in the given [rotation sequence][dialStepsList].
     */
    fun getDoorPasswordUsingOldProtocol(): Int =
        dialStepsList.runningFold(startDialPosition) { lastDialPosition: Int, dialSteps: Int ->
            (lastDialPosition + dialSteps).mod(DIAL_SIZE)
        }.count { newDialPosition -> newDialPosition == 0 }

    /**
     * [Solution for Part-2]
     *
     * Returns the Password to open the door which is given by the sum of the number of times the dial points at 0
     * after applying a rotation in the given [rotation sequence][dialStepsList], and also the number of times the dial
     * passes through 0 dial position during the rotation.
     */
    fun getDoorPasswordUsingNewProtocol(): Int =
        dialStepsList.runningFold(startDialPosition to 0) { (lastDialPosition: Int, _: Int), dialSteps: Int ->
            val newDialPosition = (lastDialPosition + dialSteps).mod(DIAL_SIZE)
            newDialPosition to getZeroClickCount(lastDialPosition, newDialPosition, dialSteps)
        }.unzip().second.sum()

}