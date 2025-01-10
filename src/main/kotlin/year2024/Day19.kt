/**
 * Problem: Day19: Linen Layout
 * https://adventofcode.com/2024/day/19
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import extensions.splitWhen
import org.junit.jupiter.api.Assertions.assertEquals

private class Day19 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    listOf(
        ::solveSample to arrayOf<Any?>(1, 6),
        ::solveActual to arrayOf<Any?>(1, 365),
        ::solveSample to arrayOf<Any?>(2, 16L),
        ::solveActual to arrayOf<Any?>(2, 730121486795169L)
    ).forEach { (solver, args: Array<Any?>) ->
        val result = solver(args[0] as Int).also(::println)

        // Last argument should be the expected value. If unknown, it will be `null`. When known, following statement
        // asserts the `result` with the expected value.
        if (args.last() != null) {
            assertEquals(args.last(), result)
        }
        println("=====")
    }
}

private fun solveSample(executeProblemPart: Int): Any =
    execute(Day19.getSampleFile().readLines(), executeProblemPart)

private fun solveActual(executeProblemPart: Int): Any =
    execute(Day19.getActualTestFile().readLines(), executeProblemPart)

private fun execute(input: List<String>, executeProblemPart: Int): Any =
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
        else -> throw Error("Unexpected Problem Part: $executeProblemPart")
    }

private fun doPart1(input: List<String>): Any =
    TowelPatternAnalyzer.parse(input)
        .getCountOfPossibleDesigns()

private fun doPart2(input: List<String>): Any =
    TowelPatternAnalyzer.parse(input)
        .getTotalCountOfArrangementsOfAllPossibleDesigns()


private class TowelPatternAnalyzer private constructor(
    private val towelPatterns: List<String>,
    private val desiredDesigns: List<String>
) {

    companion object {
        private const val COMMA = ','
        private const val UNDERSCORE = '_'

        fun parse(input: List<String>): TowelPatternAnalyzer = input.splitWhen { line ->
            line.isEmpty() || line.isBlank()
        }.let { splitBlocks ->
            TowelPatternAnalyzer(
                towelPatterns = splitBlocks.first().single()
                    .split(COMMA)
                    .map { pattern -> pattern.trim() },
                desiredDesigns = splitBlocks.last().toList()
            )
        }
    }

    /**
     * Returns `true` if towels from [towelPatterns] can be arranged as per the
     * given [design][desiredDesign]; `false` otherwise.
     */
    private fun isPossibleToDesign(desiredDesign: String): Boolean {
        // ArrayDeque to process and arrange towels as per the design iteratively. Begin with the given `desiredDesign`.
        val processingQueue = ArrayDeque<String>().apply { add(desiredDesign) }

        // Set to keep track of iterative designs that have already been processed
        val visitedSet: MutableSet<String> = mutableSetOf()

        // Boolean to keep track of the possibility of arranging the given design using the available towel patterns
        var isDesignPossible = false

        // List of Towel patterns which can be used for the given design
        val usableTowelPatterns = towelPatterns.filter { pattern ->
            pattern in desiredDesign
        }

        // Repeat till the ArrayDeque of design iterations becomes empty
        while (processingQueue.isNotEmpty()) {
            // Get the first iterative design
            val current = processingQueue.removeFirst()

            // When the current iterative design is full of underscores, it means it is completely processed
            // and arranged successfully. Mark the given design as possible to be arranged, and then exit.
            if (current.all { it == UNDERSCORE }) {
                isDesignPossible = true
                break
            }

            // If the current iterative design is already processed, then continue
            // to the next design iteration in ArrayDeque
            if (current in visitedSet) continue

            // Get the start index of the next pattern to be arranged
            val startIndex = current.indexOfFirst { it != UNDERSCORE }

            usableTowelPatterns.forEach { pattern ->
                // When the next pattern at start index begins with any of the usable towel patterns, mark this
                // matching towel pattern on the copy of current iterative design with underscores, and then add
                // the result to the ArrayDeque for further processing
                if (current.substring(startIndex).startsWith(pattern)) {
                    processingQueue.add(
                        StringBuilder(current).replace(
                            startIndex,
                            startIndex + pattern.length,
                            List(pattern.length) { UNDERSCORE }.joinToString("")
                        ).toString()
                    )
                }
            }

            // Mark the current iterative design as processed
            visitedSet.add(current)
        }

        // Return the possibility of arranging the given design with the towel patterns
        return isDesignPossible
    }

    /**
     * [Solution for Part-1]
     *
     * Returns the number of designs from [desiredDesigns] that are possible to be arranged
     * using [towel patterns][towelPatterns].
     */
    fun getCountOfPossibleDesigns(): Int =
        desiredDesigns.count { design -> isPossibleToDesign(design) }

    /**
     * [Solution for Part-2]
     *
     * Returns the total of the number of arrangements found for each of the designs from [desiredDesigns] that can be
     * arranged using [towel patterns][towelPatterns].
     */
    fun getTotalCountOfArrangementsOfAllPossibleDesigns(): Long =
        desiredDesigns.filter { design ->
            // Pick only the designs that can be arranged with the towel patterns
            isPossibleToDesign(design)
        }.sumOf { design ->
            // Return the sum of all possible arrangements found for each design that can be arranged

            // List of Towel patterns which can be used for the given design
            val usableTowelPatterns = towelPatterns.filter { pattern ->
                pattern in design
            }

            // Dynamic Programming (DP) Array for capturing the number of possible arrangements occurring
            // at every length of the design. DP Array size will be one more than the length of the given design,
            // with initial value at each design length set to 0.
            val solutionArray = Array(design.length + 1) { 0L }

            // At design length 0, number of possible arrangements will always be 1
            solutionArray[0]++

            design.indices.forEach { index ->
                usableTowelPatterns.forEach { pattern ->
                    // When the next pattern starting at each `index` of the design begins with any of the
                    // usable towel patterns, extend the number of possible arrangements found at current `index`
                    // to the index pointed to by the length of the matching towel pattern from the current `index`
                    if (design.substring(index).startsWith(pattern)) {
                        solutionArray[index + pattern.length] += solutionArray[index]
                    }
                }
            }

            // Return the total number of possible arrangements for this design
            // which is given by the last element of DP Array
            solutionArray.last()
        }

}