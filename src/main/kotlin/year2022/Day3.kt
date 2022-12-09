/**
 * Problem: Day3: Rucksack Reorganization
 * https://adventofcode.com/2022/day/3
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseFileHandler

private class Day3 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 157
    println("=====")
    solveActual(1) // 7850
    println("=====")
    solveSample(2) // 70
    println("=====")
    solveActual(2) // 2581
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day3.getSampleFile().readLines(), executeProblemPart)
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
    input.map { itemStr -> Rucksack.parse(itemStr) }
        .sumOf { rucksack -> Rucksack.getItemPriority(rucksack.commonItemInCompartments) }
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    input.map { itemStr -> Rucksack.parse(itemStr) }
        .chunked(3) { rucksackGroup ->
            Rucksack.getItemPriority(
                rucksackGroup.asSequence().map { rucksack -> rucksack.items.toSet() }
                    .zipWithNext { itemSet1, itemSet2 -> itemSet1 intersect itemSet2 }
                    .zipWithNext { itemSet1, itemSet2 -> itemSet1 intersect itemSet2 }
                    .flatten()
                    .first()
            )
        }
        .sum()
        .also { println(it) }
}

private class Rucksack private constructor(
    val items: String,
    val leftCompartmentItems: String,
    val rightCompartmentItems: String
) {
    companion object {
        private val lowerItemPriorities
            get() = generateSequence('a') { previous ->
                if (previous == 'z') {
                    null
                } else {
                    previous + 1
                }
            }

        private val upperItemPriorities
            get() = generateSequence('A') { previous ->
                if (previous == 'Z') {
                    null
                } else {
                    previous + 1
                }
            }

        private val itemPriorities get() = lowerItemPriorities + upperItemPriorities

        fun getItemPriority(item: Char) = itemPriorities.indexOf(item) + 1

        fun parse(input: String): Rucksack = Rucksack(
            input,
            leftCompartmentItems = input.substring(0, input.length.ushr(1)),
            rightCompartmentItems = input.substring(input.length.ushr(1))
        )
    }

    val commonItemInCompartments get() = (leftCompartmentItems.toSet() intersect rightCompartmentItems.toSet()).first()

}