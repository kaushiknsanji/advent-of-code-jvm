/**
 * Problem: Day13: Point of Incidence
 * https://adventofcode.com/2023/day/13
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import extensions.splitWhen

private class Day13 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 405
    println("=====")
    solveActual(1)      // 31265
    println("=====")
    solveSample(2)      // 400
    println("=====")
    solveActual(2)      // 39359
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
    ReflectionPatternAnalyzer.parse(input)
        .getReflectionSummaryOfAllPatterns()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    ReflectionPatternAnalyzer.parse(input)
        .getReflectionSummaryOfAllPatterns(smudgeFixAllowed = true)
        .also { println(it) }
}

private enum class ReflectionType {
    VERTICAL, HORIZONTAL
}

private class MirrorReflection(
    val reflectionLine: Int,
    val reflectionType: ReflectionType,
    val trueReflection: Boolean = false,
    val totalSmudgeCount: Int = 0
) {
    override fun toString(): String =
        "reflectionLine: $reflectionLine, " +
                "reflectionType: $reflectionType, " +
                "trueReflection: $trueReflection, " +
                "totalSmudgeCount: $totalSmudgeCount"
}

private class ReflectionPatternAnalyzer private constructor(
    private val patternGroups: List<List<List<Char>>>
) {
    companion object {

        fun parse(input: List<String>): ReflectionPatternAnalyzer = ReflectionPatternAnalyzer(
            patternGroups = input.splitWhen { it.isEmpty() || it.isBlank() }.map { groupedStrings: Iterable<String> ->
                groupedStrings.map { string ->
                    string.map { it }
                }
            }
        )

    }

    /**
     * Extracts and returns the number of characters that are not same between the lists [this] and [other].
     * Used for the smudge fix operation to test if they can mirror one another.
     */
    private fun List<Char>.extractSmudgeCount(other: List<Char>): Int =
        this.zip(other).count { it.first != it.second }

    /**
     * Default [MirrorReflection] data used when no result is available for a particular plane of Reflection.
     */
    private fun defaultReflectionData(type: ReflectionType): MirrorReflection = MirrorReflection(
        reflectionLine = -1,
        reflectionType = type
    )

    /**
     * Extension function on list of [MirrorReflection] data to obtain the best [MirrorReflection] data if any based on
     * [smudgeFixAllowed] condition and [MirrorReflection.totalSmudgeCount] and [MirrorReflection.trueReflection] properties.
     *
     * Returns `null` when no [MirrorReflection] data could be found based on the condition and properties.
     */
    private fun List<MirrorReflection>.selectBestReflectionDataOrNull(smudgeFixAllowed: Boolean): MirrorReflection? =
        singleOrNull { mirrorReflection ->
            if (smudgeFixAllowed) {
                mirrorReflection.totalSmudgeCount == 1
            } else {
                mirrorReflection.trueReflection
            }
        }

    /**
     * Extension function on a single [List] group of [Char]s representing the reflection pattern under study,
     * to find and return the [MirrorReflection] data present in Vertical Plane.
     */
    private fun List<List<Char>>.getReflectionDataInVerticalPlane(smudgeFixAllowed: Boolean): MirrorReflection {
        // List of Pairs to store the Reflection plane/line along with the count of smudge fixes
        val reflectionLineSmudgeCountPairs = mutableListOf<Pair<Int, Int>>()

        // Map of Column indexes with their values
        val columnBasedValuesMap: Map<Int, List<Char>> = this.map { it.withIndex() }.withIndex()
            .flatMap { (rowIndex: Int, indexedColumnValues: Iterable<IndexedValue<Char>>) ->
                // With columns and then rows being indexed, we turn them into a Triple with their column value
                indexedColumnValues.map { (columnIndex: Int, value: Char) ->
                    Triple(rowIndex, columnIndex, value)
                }
            }.groupBy { it.second } // Group by the column index
            .mapValues { (_: Int, columnTripleValues: List<Triple<Int, Int, Char>>) ->
                // Turn Triple into just column values
                columnTripleValues.map { it.third }
            }

        val totalColumns = columnBasedValuesMap.keys.size

        (0 until totalColumns - 1).forEach { columnKey ->
            // Check immediate columns for reflection plane/line
            if (columnBasedValuesMap[columnKey] == columnBasedValuesMap[columnKey + 1]) {
                // When reflection is found, add reflection plane for further analysis
                reflectionLineSmudgeCountPairs.add(columnKey + 1 to 0)
            }

            if (smudgeFixAllowed) {
                // When smudge fix is allowed, find immediate columns that can be made to have reflection
                // with a single character fix
                if (columnBasedValuesMap[columnKey] != columnBasedValuesMap[columnKey + 1]
                    && columnBasedValuesMap[columnKey]!!.extractSmudgeCount(columnBasedValuesMap[columnKey + 1]!!) == 1
                ) {
                    // When reflection is found with a single character fix, add reflection plane
                    // along with fix count for further analysis
                    reflectionLineSmudgeCountPairs.add(columnKey + 1 to 1)
                }
            }
        }

        return reflectionLineSmudgeCountPairs.map { (reflectionLine, reflectionLineSmudgeCount) ->
            // Get the Left and Right planes along the reflection plane/line
            var leftPlane = reflectionLine - 1
            var rightPlane = reflectionLine

            // Reflection is true when the patterns along the reflection plane is same till
            // one of the edges of the given pattern
            var trueReflection = true

            // When smudge fix is allowed, store the count of further fixes that
            // may be required along the reflection plane.
            // Initialize to the smudge count found for the reflection plane.
            var smudgeCount = reflectionLineSmudgeCount

            while (leftPlane > 0 && rightPlane < totalColumns - 1) {
                // Scan till one of the edges of the pattern
                if (columnBasedValuesMap[--leftPlane] != columnBasedValuesMap[++rightPlane]) {
                    // When the planes compared are not same, the pattern is not truly reflective
                    trueReflection = false

                    if (smudgeFixAllowed) {
                        // When smudge fix is allowed, extract the number of smudge fixes required to make the planes
                        // compared to have reflection
                        smudgeCount += columnBasedValuesMap[leftPlane]!!.extractSmudgeCount(columnBasedValuesMap[rightPlane]!!)
                    } else {
                        // When smudge fix is NOT allowed, just break out of the loop since we have found out that
                        // the pattern is not truly reflective.
                        break
                    }
                }
            }

            // Return the Reflection data for the pattern
            MirrorReflection(
                reflectionLine,
                ReflectionType.VERTICAL,
                trueReflection,
                smudgeCount
            )
        }.selectBestReflectionDataOrNull(smudgeFixAllowed) ?: defaultReflectionData(ReflectionType.VERTICAL)

    }

    /**
     * Extension function on a single [List] group of [Char]s representing the reflection pattern under study,
     * to find and return the [MirrorReflection] data present in Horizontal Plane.
     */
    private fun List<List<Char>>.getReflectionDataInHorizontalPlane(smudgeFixAllowed: Boolean): MirrorReflection {
        // List of Pairs to store the Reflection plane/line along with the count of smudge fixes
        val reflectionLineSmudgeCountPairs = mutableListOf<Pair<Int, Int>>()

        (0 until lastIndex).forEach { rowIndex ->
            // Check immediate rows for reflection plane/line
            if (this[rowIndex] == this[rowIndex + 1]) {
                // When reflection is found, add reflection plane for further analysis
                reflectionLineSmudgeCountPairs.add(rowIndex + 1 to 0)
            }

            if (smudgeFixAllowed) {
                // When smudge fix is allowed, find immediate rows that can be made to have reflection
                // with a single character fix
                if (this[rowIndex] != this[rowIndex + 1] && this[rowIndex].extractSmudgeCount(this[rowIndex + 1]) == 1) {
                    // When reflection is found with a single character fix, add reflection plane
                    // along with fix count for further analysis
                    reflectionLineSmudgeCountPairs.add(rowIndex + 1 to 1)
                }
            }
        }

        return reflectionLineSmudgeCountPairs.map { (reflectionLine, reflectionLineSmudgeCount) ->
            // Get the Top and Bottom planes along the reflection plane/line
            var topPlane = reflectionLine - 1
            var bottomPlane = reflectionLine

            // Reflection is true when the patterns along the reflection plane is same till
            // one of the edges of the given pattern
            var trueReflection = true

            // When smudge fix is allowed, store the count of further fixes that
            // may be required along the reflection plane.
            // Initialize to the smudge count found for the reflection plane.
            var smudgeCount = reflectionLineSmudgeCount

            while (topPlane > 0 && bottomPlane < lastIndex) {
                // Scan till one of the edges of the pattern
                if (this[--topPlane] != this[++bottomPlane]) {
                    // When the planes compared are not same, the pattern is not truly reflective
                    trueReflection = false

                    if (smudgeFixAllowed) {
                        // When smudge fix is allowed, extract the number of smudge fixes required to make the planes
                        // compared to have reflection
                        smudgeCount += this[topPlane].extractSmudgeCount(this[bottomPlane])
                    } else {
                        // When smudge fix is NOT allowed, just break out of the loop since we have found out that
                        // the pattern is not truly reflective.
                        break
                    }
                }
            }

            // Return the Reflection data for the pattern
            MirrorReflection(
                reflectionLine,
                ReflectionType.HORIZONTAL,
                trueReflection,
                smudgeCount
            )
        }.selectBestReflectionDataOrNull(smudgeFixAllowed) ?: defaultReflectionData(ReflectionType.HORIZONTAL)
    }

    /**
     * [Solution for Part-1 & Part-2]
     *
     * Returns a Numeric representation for the Reflection summary of all reflection patterns in [patternGroups].
     * For Part-2, when smudge fix is allowed, [smudgeFixAllowed] will be passed as `true`, which allows a single fix on
     * a reflection pattern being summarized.
     */
    fun getReflectionSummaryOfAllPatterns(smudgeFixAllowed: Boolean = false): Int =
        patternGroups.sumOf { patternGroup: List<List<Char>> ->
            val selectedReflectionData =
                listOf(
                    patternGroup.getReflectionDataInHorizontalPlane(smudgeFixAllowed),
                    patternGroup.getReflectionDataInVerticalPlane(smudgeFixAllowed)
                ).selectBestReflectionDataOrNull(smudgeFixAllowed)!!

            when (selectedReflectionData.reflectionType) {
                ReflectionType.VERTICAL -> selectedReflectionData.reflectionLine
                ReflectionType.HORIZONTAL -> selectedReflectionData.reflectionLine * 100
            }
        }

}