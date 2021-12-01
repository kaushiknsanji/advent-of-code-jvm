/**
 * Problem: Day7: Handy Haversacks
 * https://adventofcode.com/2020/day/7
 *
 * @author Kaushik N. Sanji
 */

package year2020

import base.BaseFileHandler
import java.util.*

private class Day7 {
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
    solvePart2Sample()
    println("=====")
    solveActual(2)
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day7.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day7.getActualTestFile().readLines(), executeProblemPart)
}

private fun solvePart2Sample() {
    execute(Day7.getSampleFile("_part2").readLines(), 2)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    BagRules(input)
        .buildRulesEngine()
        .findCountOfCompatibleOuterMostBagsByTabulation("shiny gold")
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    BagRules(input)
        .buildRulesEngine()
        .findTotalBagsInsideByTabulation("shiny gold")
        .also { println(it) }
}

private class BagRules(
    val rulesList: List<String>,
    private val rulesMap: MutableMap<String, Map<String, Int>> = mutableMapOf()
) {

    companion object {
        const val WORD_CONTAIN_WITH_SPACE = " contain "
        const val STRING_NO_OTHER = "no other"

        private val keyPattern get() = """([\w\s]+) bags""".toRegex()
        private val valuePattern get() = """([\d]+) ([\w\s]+) (?:bags|bag)""".toRegex()
    }

    fun buildRulesEngine(): BagRules = this.apply {
        rulesList.associateTo(rulesMap) { rule: String ->
            rule.substringBefore(WORD_CONTAIN_WITH_SPACE).toRulesMapKey() to
                    rule.substringAfter(WORD_CONTAIN_WITH_SPACE).toRulesMapValue()
        }
    }

    private fun String.toRulesMapKey(): String = keyPattern.find(this)!!.groupValues[1]

    private fun String.toRulesMapValue(): Map<String, Int> = if (this.startsWith(STRING_NO_OTHER)) {
        emptyMap()
    } else {
        valuePattern.findAll(this).associate { it.groupValues[2] to it.groupValues[1].toInt() }
    }

    fun findCountOfCompatibleOuterMostBagsByTabulation(innerBagColor: String): Int {
        // Stack to save outer bags for deep processing
        val outerBagsStackInProcess: LinkedList<String> = LinkedList()
        // List to save the outer bags already processed from the stack
        // that are found to contain the inner bag of given color either directly or indirectly
        val outerBagsProcessed = mutableListOf<String>()

        // First, find direct outer-most bags containing the inner bag of given color
        // and load them on to a stack for processing further
        stackOuterMostBags(innerBagColor, outerBagsStackInProcess)

        // Then, find the outer bags of the bags present in the stack that can contain these direct outer-most bags,
        // and recursively in the same way, which eventually will contain the inner bag of given color
        while (outerBagsStackInProcess.isNotEmpty()) {
            // Pop a bag from the stack and assume it to be the inner bag of some outer bag that we are yet to find out
            val currentInnerBagColor = outerBagsStackInProcess.pop()

            // Already processed! Then, pop the next bag from the stack
            if (outerBagsProcessed.contains(currentInnerBagColor)) continue

            // Find and load the outer bags containing this new inner bag on to the stack for processing further
            stackOuterMostBags(currentInnerBagColor, outerBagsStackInProcess)

            // Save this inner bag into the processed list
            outerBagsProcessed.add(currentInnerBagColor)
        }

        // Return the number of outer bags found (from the processed list)
        return outerBagsProcessed.size
    }

    private fun stackOuterMostBags(
        innerBagColor: String,
        outerBagsStackInProcess: LinkedList<String>
    ) {
        rulesMap.forEach { (outerBagColor, innerBagMap) ->
            if (innerBagMap.containsKey(innerBagColor)) {
                outerBagsStackInProcess.push(outerBagColor)
            }
        }
    }

    fun findCountOfCompatibleOuterMostBagsByRecursion(innerBagColor: String): Int {
        val outerBagsSet = mutableSetOf<String>()
        searchAndSaveCompatibleOuterBagsByRecursion(innerBagColor, outerBagsSet)
        return outerBagsSet.size
    }

    private fun searchAndSaveCompatibleOuterBagsByRecursion(
        innerBagColor: String,
        outerBagsSet: MutableSet<String>
    ) {
        rulesMap.forEach { (outerBagColor, innerBagMap) ->
            if (innerBagMap.containsKey(innerBagColor)) {
                outerBagsSet.add(outerBagColor)
                searchAndSaveCompatibleOuterBagsByRecursion(outerBagColor, outerBagsSet)
            }
        }
    }

    fun findTotalBagsInsideByTabulation(bagColor: String): Int {
        val explorationStack: LinkedList<Map.Entry<String, Int>> = LinkedList()
        val evaluationStack: LinkedList<Map.Entry<String, Int>> = LinkedList()
        val innerBagsTotalCountMap = mutableMapOf<String, Int>()

        rulesMap[bagColor]?.forEach { explorationStack.push(it) }

        while (explorationStack.isNotEmpty()) {
            val currentBagEntry = explorationStack.pop()
            rulesMap[currentBagEntry.key]?.takeUnless { it.isEmpty() }?.forEach { explorationStack.push(it) }
            evaluationStack.push(currentBagEntry)
        }

        while (evaluationStack.isNotEmpty()) {
            val currentBagEntry = evaluationStack.pop()
            computeAndSaveBagInclusiveCount(
                currentBagEntry.key,
                currentBagEntry.value,
                innerBagsTotalCountMap
            )
        }

        return getInnerBagsTotalCount(bagColor, innerBagsTotalCountMap)
    }

    private fun computeAndSaveBagInclusiveCount(
        bagColor: String,
        bagCount: Int,
        innerBagsTotalCountMap: MutableMap<String, Int>
    ) {
        if (innerBagsTotalCountMap.containsKey("${bagColor}_$bagCount")) return

        innerBagsTotalCountMap["${bagColor}_$bagCount"] =
            if (innerBagsTotalCountMap.containsKey("${bagColor}_1")) {
                bagCount * innerBagsTotalCountMap["${bagColor}_1"]!!
            } else {
                bagCount * (1 + getInnerBagsTotalCount(
                    bagColor,
                    innerBagsTotalCountMap
                )).also { singleUnitInclusiveCount ->
                    innerBagsTotalCountMap["${bagColor}_1"] = singleUnitInclusiveCount
                }
            }
    }

    private fun getInnerBagsTotalCount(
        bagColor: String,
        innerBagsTotalCountMap: MutableMap<String, Int>
    ): Int = rulesMap[bagColor]
        ?.takeUnless { it.isEmpty() }
        ?.map { (innerBagColor: String, innerBagCount: Int) ->
            innerBagsTotalCountMap["${innerBagColor}_$innerBagCount"]!!
        }?.sum() ?: 0

    fun findTotalBagsInsideByRecursion(bagColor: String): Int {
        return getBagInclusiveCountByRecursion(bagColor) - 1
    }

    private fun getBagInclusiveCountByRecursion(
        bagColor: String
    ): Int {
        var count = 1

        rulesMap[bagColor]?.takeUnless { it.isEmpty() }?.forEach { (innerBagColor: String, innerBagCount: Int) ->
            count += innerBagCount * getBagInclusiveCountByRecursion(innerBagColor)
        }

        return count
    }

}