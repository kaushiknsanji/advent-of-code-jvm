/**
 * Problem: Day3: Lobby
 * https://adventofcode.com/2025/day/3
 *
 * @author <a href='mailto:kaushiknsanji@gmail.com'>Kaushik N Sanji</a>
 */

package year2025

import base.BaseProblemHandler
import java.util.*

class Day3 : BaseProblemHandler() {

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
        LobbyEscalatorAnalyzer.parse(input)
            .getTotalOutputJoltage(otherArgs[0] as Int)

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        LobbyEscalatorAnalyzer.parse(input)
            .getTotalOutputJoltage(otherArgs[0] as Int)

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, 357L, 2)
        solveActual(1, false, 0, 17107L, 2)
        solveSample(2, false, 0, 3121910778619L, 12)
        solveActual(2, false, 0, 169349762274117L, 12)
    }

}

fun main() {
    Day3().start()
}

private class LobbyEscalatorAnalyzer private constructor(
    private val batteryBanks: List<List<Char>>
) {

    companion object {

        fun parse(input: List<String>): LobbyEscalatorAnalyzer = LobbyEscalatorAnalyzer(
            batteryBanks = input.map { batteryBankString ->
                batteryBankString.map { it }
            }
        )
    }

    /**
     * Returns Maximum possible Joltage that can be obtained from the given [Battery Bank][batteryBank] when
     * the given [number of batteries are turned on][poweredOnBatteryCount]. Solution is obtained by following
     * Monotonic Subsequence Greedy algorithm.
     */
    private fun getMaxPossibleJoltage(batteryBank: List<Char>, poweredOnBatteryCount: Int): Long {
        // Stack to hold the batteries that may need to be turned on
        val poweredOnBatteryStack = LinkedList<Char>()

        // Number of batteries that can be popped from the Stack till we have enough batteries
        // as per requirement to be turned on
        var removableItemCount = batteryBank.size - poweredOnBatteryCount

        // Build the stack by going through each battery joltage
        batteryBank.indices.forEach { index ->
            // Keep backtracking when we have found a better candidate compared to the recent and not yet fully
            // exhausted the number of possible removals
            while (poweredOnBatteryStack.isNotEmpty() &&
                batteryBank[index].digitToInt() > poweredOnBatteryStack.peek().digitToInt() &&
                removableItemCount > 0
            ) {
                poweredOnBatteryStack.pop()
                removableItemCount--
            }

            // Update the Stack with the chosen battery
            poweredOnBatteryStack.push(batteryBank[index])
        }

        // When more batteries have been selected (stacked), pop till we have the correct number of batteries
        while (poweredOnBatteryStack.size > poweredOnBatteryCount) {
            poweredOnBatteryStack.pop()
        }

        // Return the Max joltage from the Stack of batteries chosen
        return poweredOnBatteryStack.reversed().joinToString("").toLong()
    }

    /**
     * [Solution for Part 1 & 2]
     *
     * Returns Total output Joltage obtained from each Battery bank when
     * the given [number of batteries are turned on][poweredOnBatteryCount].
     */
    fun getTotalOutputJoltage(poweredOnBatteryCount: Int): Long =
        batteryBanks.sumOf { batteryBank: List<Char> ->
            getMaxPossibleJoltage(batteryBank, poweredOnBatteryCount)
        }

}