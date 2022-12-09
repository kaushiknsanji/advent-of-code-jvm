/**
 * Problem: Day4: Camp Cleanup
 * https://adventofcode.com/2022/day/4
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseFileHandler

private class Day4 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 2
    println("=====")
    solveActual(1) // 496
    println("=====")
    solveSample(2) // 4
    println("=====")
    solveActual(2) // 847
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day4.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day4.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    input.map { assignmentPairs -> AssignmentCleanup.parse(assignmentPairs) }
        .count { assignmentCleanup -> assignmentCleanup.hasRedundantAssignmentSections() }
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    input.map { assignmentPairs -> AssignmentCleanup.parse(assignmentPairs) }
        .count { assignmentCleanup -> assignmentCleanup.hasOverlappingAssignmentSections() }
        .also { println(it) }
}

private class CleanupSection(
    val firstSection: Int,
    val lastSection: Int
) {
    val sectionRange = firstSection..lastSection

    val sectionLength = lastSection - firstSection + 1

    override fun toString(): String {
        return "$firstSection - $lastSection"
    }
}

private class AssignmentCleanup private constructor(
    val firstCleanupGroup: CleanupSection,
    val secondCleanupGroup: CleanupSection,
) {
    companion object {
        private val assignmentPairRegex = """(\d+)-(\d+)""".toRegex()

        fun parse(assignmentPairs: String): AssignmentCleanup =
            assignmentPairRegex.findAll(assignmentPairs).map { matchResult ->
                CleanupSection(matchResult.groupValues[1].toInt(), matchResult.groupValues[2].toInt())
            }.let { cleanupSectionSeq ->
                AssignmentCleanup(cleanupSectionSeq.first(), cleanupSectionSeq.last())
            }
    }

    private val overlappingAssignmentSections = firstCleanupGroup.sectionRange intersect secondCleanupGroup.sectionRange

    /**
     * [Solution for Part-1]
     * Returns `true` if all sections assigned for a group is already assigned
     * as part of other group; `false` otherwise.
     */
    fun hasRedundantAssignmentSections(): Boolean =
        overlappingAssignmentSections.size == minOf(
            firstCleanupGroup.sectionLength,
            secondCleanupGroup.sectionLength
        )

    /**
     * [Solution for Part-2]
     * Returns `true` if any of the sections assigned for a group is already assigned
     * as part of other group; `false` otherwise.
     */
    fun hasOverlappingAssignmentSections(): Boolean = overlappingAssignmentSections.isNotEmpty()
}