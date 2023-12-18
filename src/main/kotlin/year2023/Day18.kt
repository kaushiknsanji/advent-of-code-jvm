/**
 * Problem: Day18: Lavaduct Lagoon
 * https://adventofcode.com/2023/day/18
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import utils.grid.Point2d
import utils.grid.toTotalPointsEnclosedByPolygon

private class Day18 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 62
    println("=====")
    solveActual(1)      // 46359
    println("=====")
    solveSample(2)      // 952408144115
    println("=====")
    solveActual(2)      // 59574883048274
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day18.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day18.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    LagoonAnalyzer.parse(input)
        .getTotalLagoonCapacityToHoldLava()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    LagoonAnalyzer.parse(input)
        .getTotalLagoonCapacityToHoldLava(hexCodesAreInstructions = true)
        .also { println(it) }
}

private class DigPoint(val x: Int, val y: Int) : Point2d<Int>(x, y)

private enum class DigDirection(val type: String) {
    RIGHT("R"), DOWN("D"), LEFT("L"), UP("U")
}

private class LagoonAnalyzer private constructor(
    private val digPlanList: List<Triple<DigDirection, Int, Int>>
) {
    companion object {
        private val spaceRegex = """\s+""".toRegex()
        private const val HEX_HASH_CHAR = "#"
        private const val CLOSE_BRACE = ")"

        fun parse(input: List<String>): LagoonAnalyzer = input.map { line ->
            line.split(spaceRegex).let { splitStrings ->
                Triple(
                    DigDirection.entries.single { it.type == splitStrings[0] },
                    splitStrings[1].toInt(),
                    splitStrings[2].substringBefore(CLOSE_BRACE).substringAfter(HEX_HASH_CHAR).toInt(16)
                )
            }
        }.let { digPlanList: List<Triple<DigDirection, Int, Int>> ->
            LagoonAnalyzer(digPlanList)
        }
    }

    /**
     * Extension function on a [Sequence] of [Pair]s of [DigDirection] and [Int] count (for digging) that
     * generates a [Sequence] of [DigPoint]s representing the trench edges of digging
     */
    private fun Sequence<Pair<DigDirection, Int>>.toTrenchEdges(): Sequence<DigPoint> =
        this.runningFold(
            sequenceOf(
                DigPoint(
                    0,
                    0
                )
            )
        ) { acc: Sequence<DigPoint>, (direction: DigDirection, digCount: Int) ->
            val lastDigPoint = acc.last()
            when (direction) {
                DigDirection.UP -> generateSequence(lastDigPoint) {
                    DigPoint(it.x - 1, it.y)
                }

                DigDirection.DOWN -> generateSequence(lastDigPoint) {
                    DigPoint(it.x + 1, it.y)
                }

                DigDirection.LEFT -> generateSequence(lastDigPoint) {
                    DigPoint(it.x, it.y - 1)
                }

                DigDirection.RIGHT -> generateSequence(lastDigPoint) {
                    DigPoint(it.x, it.y + 1)
                }
            }.drop(1).take(digCount)
        }.flatten().drop(1)

    /**
     * [Solution for Part-1 & Part-2]
     *
     * Returns total cubic meters of lava the dug up lagoon can hold.
     *
     * @param hexCodesAreInstructions This is `true` for Part-2 where those hex codes in the test input are considered
     * as actual instructions for digging.
     */
    fun getTotalLagoonCapacityToHoldLava(hexCodesAreInstructions: Boolean = false): Long =
        if (hexCodesAreInstructions) {
            digPlanList.asSequence().map { (_: DigDirection, _: Int, instructionHex: Int) ->
                // Remainder gives the ordinal for Direction since last digit in the hex code represents Direction,
                // while Dividing gives the actual count for digging in each direction
                // which excludes the last digit in the hex code.
                DigDirection.entries[instructionHex.rem(16)] to instructionHex / 16
            }
        } else {
            digPlanList.asSequence().map { (direction: DigDirection, digCount: Int, _: Int) ->
                direction to digCount
            }
        }.toTrenchEdges().let { trenchEdges: Sequence<DigPoint> ->
            // Boundary Points + Enclosed Points
            trenchEdges.count() + trenchEdges.toTotalPointsEnclosedByPolygon()
        }

}