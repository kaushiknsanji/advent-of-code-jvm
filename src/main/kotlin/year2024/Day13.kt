/**
 * Problem: Day13: Claw Contraption
 * https://adventofcode.com/2024/day/13
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import extensions.splitWhen
import utils.grid.Point2d

private class Day13 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 480
    println("=====")
    solveActual(1)      // 30413
    println("=====")
    solveSample(2)      // 875318608908
    println("=====")
    solveActual(2)      // 92827349540204
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day13.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day13.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    ClawMachineAnalyzer.parse(input)
        .getFewestTokensSpentToWin()
        .also(::println)
}

private fun doPart2(input: List<String>) {
    ClawMachineAnalyzer.parse(input)
        .getFewestTokensSpentToWin(prizeOffset = 10000000000000L)
        .also(::println)
}

private class ClawLocation(x: Long, y: Long) : Point2d<Long>(x, y)

private class ClawMachineAnalyzer private constructor(
    private val buttonBehaviors: List<Triple<ClawLocation, ClawLocation, ClawLocation>>
) {
    companion object {
        // Regular expression to capture numbers
        val numberRegex = """(\d+)""".toRegex()

        fun parse(input: List<String>): ClawMachineAnalyzer = input.splitWhen { it.isEmpty() || it.isBlank() }
            .map { lines: Iterable<String> ->
                lines.map { line: String ->
                    numberRegex.findAll(line).map { matchResult ->
                        matchResult.groupValues[1].toLong()
                    }.toList().let { values: List<Long> ->
                        ClawLocation(values[0], values[1])
                    }
                }.let { locations: List<ClawLocation> ->
                    check(locations.size == 3) {
                        "Error: Parsed count of locations are not 3, it is rather ${locations.size}"
                    }

                    Triple(locations[0], locations[1], locations[2])
                }
            }.let { buttonBehaviors: List<Triple<ClawLocation, ClawLocation, ClawLocation>> ->
                ClawMachineAnalyzer(buttonBehaviors)
            }
    }

    /**
     * Returns number of times Button B needs to be pushed to move the Claw using the locations
     * presented by [this] Claw machine's button behavior.
     *
     * @param prizeOffset [Long] value to be added to both X and Y coordinate values of [this.third] Prize location.
     */
    private fun Triple<ClawLocation, ClawLocation, ClawLocation>.getPushCountForButtonB(
        prizeOffset: Long
    ): Long =
        this.let { (buttonA, buttonB, prize) ->
            // Number of times Button-B needs to be pushed is solved by using below equation.
            // A stands for Button-A location, B stands for Button-B location and P stands for Prize location.
            // 1 means x-coordinate value and 2 means y-coordinate value.
            // btnCountB = (A2 * P1 - A1 * P2) / (A2 * B1 - A1 * B2)
            (buttonA.yPos * (prize.xPos + prizeOffset) - buttonA.xPos * (prize.yPos + prizeOffset)) / (buttonA.yPos * buttonB.xPos - buttonA.xPos * buttonB.yPos)
        }

    /**
     * Returns number of times Button A needs to be pushed to move the Claw using the locations presented
     * by [this] Claw machine's button behavior and the solved value
     * for the [Number of times Button B is pushed][pushCountForButtonB].
     *
     * @param prizeOffset [Long] value to be added to both X and Y coordinate values of [this.third] Prize location.
     */
    private fun Triple<ClawLocation, ClawLocation, ClawLocation>.getPushCountForButtonA(
        pushCountForButtonB: Long,
        prizeOffset: Long
    ): Long =
        this.let { (buttonA, buttonB, prize) ->
            // Number of times Button-A needs to be pushed is solved by using below equation.
            // A stands for Button-A location, B stands for Button-B location and P stands for Prize location.
            // 1 means x-coordinate value and 2 means y-coordinate value.
            // btnCountA = (P1 - B1 * btnCountB) / A1
            ((prize.xPos + prizeOffset) - buttonB.xPos * pushCountForButtonB) / buttonA.xPos
        }

    /**
     * Verifies whether the solved values for [Number of times Button A is pushed][pushCountForButtonA] and
     * [Number of times Button B is pushed][pushCountForButtonB] moves the claw right on top
     * of [this.third] Prize location using the button locations presented by [this] Claw machine's button behavior.
     *
     * @param prizeOffset [Long] value to be added to both X and Y coordinate values of [this.third] Prize location.
     */
    private fun Triple<ClawLocation, ClawLocation, ClawLocation>.verifyPrize(
        pushCountForButtonA: Long,
        pushCountForButtonB: Long,
        prizeOffset: Long
    ): Boolean =
        this.let { (buttonA, buttonB, prize) ->
            // Number of times Button-A and Button-B is pushed to move the claw right on top of Prize location is
            // verified using both the below equations.
            // A stands for Button-A location, B stands for Button-B location and P stands for Prize location.
            // 1 means x-coordinate value and 2 means y-coordinate value.
            // A1 * btnCountA + B1 * btnCountB = P1
            // A2 * btnCountA + B2 * btnCountB = P2
            buttonA.xPos * pushCountForButtonA + buttonB.xPos * pushCountForButtonB == (prize.xPos + prizeOffset) &&
                    buttonA.yPos * pushCountForButtonA + buttonB.yPos * pushCountForButtonB == (prize.yPos + prizeOffset)
        }

    /**
     * Returns fewest tokens that would have to be spent to win all prizes.
     *
     * @param prizeOffset [Long] value to be added to both X and Y coordinate values of every Prize location.
     * For Part-1, this will be 0 and is also the default value. In this case, the number of Button-A or Button-B
     * pushes cannot exceed 100. For Part-2, this will be 10000000000000. In this case, as it will take more
     * than 100 pushes for both buttons to move the Claw on top of the Prize, there are no constraints on this.
     */
    fun getFewestTokensSpentToWin(prizeOffset: Long = 0L): Long =
        buttonBehaviors.map { buttonBehavior: Triple<ClawLocation, ClawLocation, ClawLocation> ->
            // Number of times Button-B is pushed
            val pushCountForButtonB = buttonBehavior.getPushCountForButtonB(prizeOffset)

            // Number of times Button-A is pushed
            val pushCountForButtonA =
                buttonBehavior.getPushCountForButtonA(pushCountForButtonB, prizeOffset)

            // Verify with the solved number of pushes for both buttons
            val verification = buttonBehavior.verifyPrize(pushCountForButtonA, pushCountForButtonB, prizeOffset)

            // Return a triple of the above computed values
            Triple(pushCountForButtonA, pushCountForButtonB, verification)
        }.filterNot { (pushCountForButtonA, pushCountForButtonB, verification) ->
            if (prizeOffset == 0L) {
                // Part-1: Number of Button-A or Button-B pushes cannot exceed 100, and the number of pushes
                // should verify
                pushCountForButtonA > 100 || pushCountForButtonB > 100 || !verification
            } else {
                // Part-2: No constraints are there for button pushes. Only the number of pushes should verify
                !verification
            }
        }.sumOf { (pushCountForButtonA, pushCountForButtonB, _) ->
            // Return tokens needed to win a prize on the machine
            // 3 Tokens to push Button-A and 1 token to push Button-B
            pushCountForButtonA * 3 + pushCountForButtonB
        }

}