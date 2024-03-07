/**
 * Problem: Day11: Monkey in the Middle
 * https://adventofcode.com/2022/day/11
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseFileHandler
import extensions.splitWhen
import utils.product
import java.math.BigInteger

private class Day11 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 10605
    println("=====")
    solveActual(1) // 119715
    println("=====")
    solveSample(2) // 2713310158
    println("=====")
    solveActual(2) // 18085004878
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day11.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day11.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    MonkeyBusiness.parse(
        input
    )
        .doMonkeyBusiness(20)
        .getMonkeyLevelBusinessCarried(2)
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    MonkeyBusiness.parse(
        input,
        worryLevelReducedBeingBoredNumber = 1
    )
        .doMonkeyBusiness(10000)
        .getMonkeyLevelBusinessCarried(2)
        .also { println(it) }
}

private class PeskyMonkey(
    val id: Int,
    private val itemsWithWorryLevels: MutableList<Long>,
    private val worryLevelAmplifyingNumber: Long?,
    private val worryLevelAmplifyingOperation: (Long, Long) -> Long,
    val itemDispatchDivisibilityTestNumber: Int,
    private val throwToMonkeyOnDivTestSuccess: Int,
    private val throwToMonkeyOnDivTestFailure: Int,
    private val itemDispatcher: MonkeyItemDispatcher,
    private val worryLevelReducedBeingBoredNumber: Long,
    private val worryLevelReducedBeingBoredOperation: (Long, Long) -> Long = Long::floorDiv
) {

    init {
        // Register with Item Dispatcher to grab their Divisibility Test Number
        registerWithItemDispatcher()
    }

    var itemsInspected: Long = 0
        private set

    fun registerWithItemDispatcher() {
        itemDispatcher.registerMonkey(this)
    }

    fun checkItems() {
        // Catch Items if any
        catchItems()

        // Check if there are any items to inspect
        if (itemsWithWorryLevels.isNotEmpty()) {
            // When Monkey has items to inspect

            // Start inspecting items
            itemsWithWorryLevels.forEach { itemWithWorryLevel ->
                checkItem(itemWithWorryLevel)
            }
        }

        // Clear items from list since the Monkey has finished his business for this round
        itemsWithWorryLevels.clear()
    }

    private fun checkItem(itemWithWorryLevel: Long) {
        // Amplify worry level as the Monkey begins inspecting
        var newWorryLevel =
            worryLevelAmplifyingOperation(itemWithWorryLevel, worryLevelAmplifyingNumber ?: itemWithWorryLevel)

        // Worry level changes as the Monkey gets bored
        newWorryLevel = worryLevelReducedBeingBoredOperation(newWorryLevel, worryLevelReducedBeingBoredNumber)

        // Normalize worry level by the modulo of the LCM of all the monkey's divisibility test numbers
        newWorryLevel %= itemDispatcher.allMonkeyDivTestNumbersLCM.toLong()

        // Monkey decides the next Monkey owner of this item
        decideNextMonkeyOwner(newWorryLevel)

        // Increment item inspected count
        itemsInspected++
    }

    private fun decideNextMonkeyOwner(itemWithNewWorryLevel: Long) {
        // Do the test to see who gets the item
        if (itemWithNewWorryLevel % itemDispatchDivisibilityTestNumber == 0L) {
            // When completely divisible
            throwItem(throwToMonkeyOnDivTestSuccess, itemWithNewWorryLevel)
        } else {
            // When NOT completely divisible
            throwItem(throwToMonkeyOnDivTestFailure, itemWithNewWorryLevel)
        }
    }

    private fun throwItem(toMonkey: Int, itemWithNewWorryLevel: Long) {
        itemDispatcher.throwItem(toMonkey, itemWithNewWorryLevel)
    }

    private fun catchItems() {
        itemsWithWorryLevels.addAll(itemDispatcher.catchItems(id))
    }
}

private class MonkeyItemDispatcher(
    private val itemsDispatched: MutableList<Pair<Int, Long>> = mutableListOf()
) {
    var allMonkeyDivTestNumbersLCM: BigInteger = BigInteger.ONE
        private set

    fun registerMonkey(monkey: PeskyMonkey) {
        calculateDivTestNumbersLCM(monkey.itemDispatchDivisibilityTestNumber.toBigInteger())
    }

    fun throwItem(toMonkey: Int, itemWithNewWorryLevel: Long) {
        itemsDispatched += toMonkey to itemWithNewWorryLevel
    }

    fun catchItems(forMonkey: Int): List<Long> = itemsDispatched.filter { (monkeyId: Int, _) ->
        monkeyId == forMonkey
    }.unzip().second.also {
        itemsDispatched.removeAll { (monkeyId: Int, _) ->
            monkeyId == forMonkey
        }
    }

    private fun calculateDivTestNumbersLCM(divTestNumber: BigInteger) {
        allMonkeyDivTestNumbersLCM =
            (allMonkeyDivTestNumbersLCM * divTestNumber) / divTestNumber.gcd(allMonkeyDivTestNumbersLCM)
    }
}

private class MonkeyBusiness private constructor(
    private val monkeys: List<PeskyMonkey>
) {

    companion object {
        private val monkeyIdentifierRegex = """Monkey (\d+):""".toRegex()
        private const val ITEMS_IDENTIFIER = "Starting items:"
        private const val AMPLIFY_OPERATION_IDENTIFIER = "Operation: new = old "
        private const val OLD_IDENTIFIER = "old"
        private const val DIV_TEST_IDENTIFIER = "Test: divisible by "
        private const val DIV_TEST_TRUE_IDENTIFIER = "If true: throw to monkey "
        private const val DIV_TEST_FALSE_IDENTIFIER = "If false: throw to monkey "
        private const val DETAILS_SEPARATOR = " | "

        val amplifyOperandMap: Map<String, (Long, Long) -> Long> = mapOf(
            "+" to Long::plus,
            "*" to Long::times
        )

        fun parse(input: List<String>, worryLevelReducedBeingBoredNumber: Long = 3): MonkeyBusiness =
            with(MonkeyItemDispatcher()) {
                MonkeyBusiness(
                    monkeys = input.splitWhen { line -> line.isEmpty() || line.isBlank() }
                        .map { monkeyInputList ->
                            monkeyInputList.joinToString(separator = DETAILS_SEPARATOR)
                        }
                        .map { monkeyInput ->
                            val monkeyId = monkeyIdentifierRegex.find(monkeyInput)!!.groupValues[1].toInt()
                            val items = monkeyInput.substringAfter(ITEMS_IDENTIFIER).substringBefore(DETAILS_SEPARATOR)
                                .split(",").map { worryLevelStrings -> worryLevelStrings.trim().toLong() }

                            val (amplifyOperand, amplifyNumber) = with(
                                monkeyInput.substringAfter(AMPLIFY_OPERATION_IDENTIFIER)
                                    .substringBefore(DETAILS_SEPARATOR)
                            ) {
                                this.split(" ").let { splitStrings ->
                                    val operand = amplifyOperandMap[splitStrings[0].trim()]!!
                                    val number: Long? = if (splitStrings[1].contains(OLD_IDENTIFIER)) {
                                        null
                                    } else {
                                        splitStrings[1].toLong()
                                    }

                                    operand to number
                                }
                            }

                            val divTestNumber = monkeyInput.substringAfter(DIV_TEST_IDENTIFIER)
                                .substringBefore(DETAILS_SEPARATOR).trim().toInt()

                            val divTestSuccessMonkeyId = monkeyInput.substringAfter(DIV_TEST_TRUE_IDENTIFIER)
                                .substringBefore(DETAILS_SEPARATOR).trim().toInt()

                            val divTestFalseMonkeyId =
                                monkeyInput.substringAfter(DIV_TEST_FALSE_IDENTIFIER).trim().toInt()

                            PeskyMonkey(
                                id = monkeyId,
                                itemsWithWorryLevels = items.toMutableList(),
                                worryLevelAmplifyingNumber = amplifyNumber,
                                worryLevelAmplifyingOperation = amplifyOperand,
                                itemDispatchDivisibilityTestNumber = divTestNumber,
                                throwToMonkeyOnDivTestSuccess = divTestSuccessMonkeyId,
                                throwToMonkeyOnDivTestFailure = divTestFalseMonkeyId,
                                itemDispatcher = this,
                                worryLevelReducedBeingBoredNumber = worryLevelReducedBeingBoredNumber
                            )
                        }
                )
            }
    }

    /**
     * Starts the Monkey Business for the given [number of rounds][noOfRounds].
     */
    fun doMonkeyBusiness(noOfRounds: Int): MonkeyBusiness = this.apply {
        repeat(noOfRounds) {
            monkeys.forEach { peskyMonkey -> peskyMonkey.checkItems() }
        }
    }

    /**
     * [Solution for Part 1 & 2]
     * Returns the level of Monkey Business determined by the product of top active [count][topActiveCount] of monkeys
     * after doing their business for several rounds.
     */
    fun getMonkeyLevelBusinessCarried(topActiveCount: Int): Long =
        monkeys.map { peskyMonkey -> peskyMonkey.itemsInspected }.sortedDescending().take(topActiveCount).product()
}