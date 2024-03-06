/**
 * Problem: Day12: Hot Springs
 * https://adventofcode.com/2023/day/12
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler

private class Day12 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 21
    println("=====")
    solveActual(1)      // 6488
    println("=====")
    solveSample(2)      // 525152
    println("=====")
    solveActual(2)      // 815364548481
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day12.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day12.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    input.map { line ->
        HotSpringsStateAnalyzer.parse(line)
    }.sumOf { hotSpringsStateAnalyzer: HotSpringsStateAnalyzer ->
        hotSpringsStateAnalyzer.getTotalPossibleArrangements()
    }.also(::println)
}

private fun doPart2(input: List<String>) {
    input.map { line ->
        HotSpringsStateAnalyzer.parse(line)
    }.sumOf { hotSpringsStateAnalyzer: HotSpringsStateAnalyzer ->
        hotSpringsStateAnalyzer.getTotalPossibleArrangements(unfoldedCopies = 5)
    }.also(::println)
}

private class HotSpringsStateAnalyzer private constructor(
    private val springStateRecord: List<Char>,
    private val damagedGroupsReport: List<Int>
) {

    companion object {
        private const val OPERATIONAL = '.'
        private const val DAMAGED = '#'
        private const val UNKNOWN = '?'

        fun parse(input: String): HotSpringsStateAnalyzer = HotSpringsStateAnalyzer(
            springStateRecord = input.substringBefore(' ').map { it },
            damagedGroupsReport = input.substringAfter(' ').split(',').map(String::toInt)
        )
    }

    // Hot springs that are either Operational or Unknown
    private val operationalOrUnknown = listOf(OPERATIONAL, UNKNOWN)

    // Hot springs that are either Damaged or Unknown
    private val damagedOrUnknown = listOf(DAMAGED, UNKNOWN)

    // Dynamic Programming Cache needed for arriving at possible arrangements of damaged groups of Hot springs.
    // The Key of the Cache is a Triple of -
    // 1. the index of Hot spring being checked,
    // 2. index of damaged group being scanned for and
    // 3. the damaged springs accounted so far for the damaged group being scanned.
    // Value of the Cache will be the total valid arrangements found for the Key combination.
    private val arrangementResultCacheMap: MutableMap<Triple<Int, Int, Int>, Long> = mutableMapOf()

    /**
     * Finds and returns the total valid arrangements of damaged groups of Hot springs recursively.
     *
     * @param springStateRecord [List] of [Char] representing the Hot springs arrangement.
     * @param damagedGroupsReport [List] of [Int] representing the lengths of damaged groups of Hot springs
     * required in order.
     * @param springStateIndex [Int] value of Hot spring character position in [springStateRecord] to be checked.
     * @param damagedGroupsIndex [Int] value of the damaged group position in [damagedGroupsReport] being scanned for.
     * @param damagedCounter [Int] value of the damaged springs accounted so far for the damaged group being scanned.
     */
    private fun findPossibleArrangementsRecursively(
        springStateRecord: List<Char>,
        damagedGroupsReport: List<Int>,
        springStateIndex: Int = 0,
        damagedGroupsIndex: Int = 0,
        damagedCounter: Int = 0
    ): Long =
        arrangementResultCacheMap.getOrPut(Triple(springStateIndex, damagedGroupsIndex, damagedCounter)) {
            // If not in Cache, evaluate for the current Hot spring character and damaged group

            if (springStateIndex == springStateRecord.size) {
                // After scanning the entire Hot springs arrangement, if we have arrived at an equal number of
                // damaged springs for the last damaged group, then return 1 for valid arrangement; otherwise
                // return 0 for invalid arrangement.
                if (damagedGroupsIndex == damagedGroupsReport.lastIndex
                    && damagedCounter == damagedGroupsReport[damagedGroupsIndex]
                ) {
                    1
                } else {
                    0
                }
            } else {
                // For Total valid arrangements found
                var arrangementCount = 0L

                if (springStateRecord[springStateIndex] in operationalOrUnknown) {
                    // When the current Hot spring character is either Operational or Unknown
                    // If Unknown, it will be treated as Operational

                    if (damagedCounter == damagedGroupsReport[damagedGroupsIndex]) {
                        // When we have found an equal number of damaged springs for the damaged group
                        // being scanned

                        if (damagedGroupsIndex == damagedGroupsReport.lastIndex) {
                            // If the damaged group being scanned is the last group, then
                            // only move to the next Hot spring character with the same damaged count and its group
                            arrangementCount += findPossibleArrangementsRecursively(
                                springStateRecord,
                                damagedGroupsReport,
                                springStateIndex + 1,
                                damagedGroupsIndex,
                                damagedCounter
                            )
                        } else {
                            // If the damaged group being scanned is NOT the last group yet, then
                            // move to the next Hot spring character and the next damaged group along with
                            // the damaged counter reset to 0
                            arrangementCount += findPossibleArrangementsRecursively(
                                springStateRecord,
                                damagedGroupsReport,
                                springStateIndex + 1,
                                damagedGroupsIndex + 1,
                                0
                            )
                        }
                    }

                    if (damagedCounter == 0) {
                        // When we are yet to find a damaged spring for the damaged group being scanned, move over
                        // to the next Hot spring character and continue
                        arrangementCount += findPossibleArrangementsRecursively(
                            springStateRecord,
                            damagedGroupsReport,
                            springStateIndex + 1,
                            damagedGroupsIndex,
                            damagedCounter
                        )
                    }
                }

                if (springStateRecord[springStateIndex] in damagedOrUnknown) {
                    // When the current Hot spring character is either Damaged or Unknown
                    // If Unknown, it will be treated as Damaged

                    if (damagedCounter < damagedGroupsReport[damagedGroupsIndex]) {
                        // When we are in the process of finding an equal number of damaged springs
                        // for the damaged group, increment the damaged counter and move over
                        // to the next Hot spring character
                        arrangementCount += findPossibleArrangementsRecursively(
                            springStateRecord,
                            damagedGroupsReport,
                            springStateIndex + 1,
                            damagedGroupsIndex,
                            damagedCounter + 1
                        )
                    }
                }

                // Save and return the updated total valid arrangements found
                arrangementCount
            }
        }

    /**
     * [Solution for Part 1 & 2]
     *
     * Returns total valid arrangements possible for [springStateRecord] and [damagedGroupsReport].
     *
     * @param unfoldedCopies When records are unfolded for Part-2, this will be greater than the default 0.
     */
    fun getTotalPossibleArrangements(unfoldedCopies: Int = 0): Long =
        if (unfoldedCopies > 0) {
            // Part-2: For unfolded records
            findPossibleArrangementsRecursively(
                springStateRecord = springStateRecord + List(unfoldedCopies - 1) {
                    listOf(UNKNOWN) + springStateRecord
                }.flatten(),
                damagedGroupsReport = List(unfoldedCopies) { damagedGroupsReport }.flatten()
            )
        } else {
            // Part-1: For folded records
            findPossibleArrangementsRecursively(springStateRecord, damagedGroupsReport)
        }

    override fun toString(): String = "Record: $springStateRecord \t Report: $damagedGroupsReport"
}