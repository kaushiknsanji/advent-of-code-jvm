/**
 * Problem: Day1: Sonar Sweep
 * https://adventofcode.com/2021/day/1
 *
 * @author Kaushik N Sanji (kaushiknsanji@gmail.com)
 */

package year2021

import base.BaseFileHandler

private class Day1 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 7
    println("=====")
    solveActual(1) // 1288
    println("=====")
    solveSample(2) // 5
    println("=====")
    solveActual(2) // 1311
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day1.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day1.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    SonarKeyFinder.create(input)
        .getCountOfIncreasedFloorDepths()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    SonarKeyFinder.create(input)
        .getCountOfIncreasedThreeWindowedFloorDepths()
        .also { println(it) }
}

private class SonarKeyFinder private constructor(
    val floorDepths: List<Int>
) {
    companion object {
        fun create(input: List<String>): SonarKeyFinder = SonarKeyFinder(input.map { it.toInt() })
    }

    /**
     * [Solution for Part-1]
     * Returns count of increasing floor depths.
     */
    fun getCountOfIncreasedFloorDepths(aggregatedFloorDepths: List<Int> = floorDepths): Int =
        aggregatedFloorDepths.zipWithNext { previousDepth, nextDepth -> nextDepth - previousDepth }.count { it > 0 }

    /**
     * [Solution for Part-2]
     * Returns count of increasing floor depths based on three-measurement window.
     */
    fun getCountOfIncreasedThreeWindowedFloorDepths(): Int = getCountOfIncreasedFloorDepths(
        floorDepths.windowed(size = 3).map { window -> window.sum() }
    )
}