/**
 * Problem: Day3: Toboggan Trajectory
 * https://adventofcode.com/2020/day/3
 *
 * @author Kaushik N. Sanji
 */

package year2020

import base.BaseFileHandler

private class Day3 {
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
    solveActual(2)
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
    val trajectory = Trajectory(3, 1).apply {
        input.map(this::buildGrid)
    }.also { it.trace() }

    println(trajectory.getTreeHitCount())
}

private fun doPart2(input: List<String>) {
    val trajectory11 = Trajectory(1, 1).apply {
        input.map(this::buildGrid)
    }.also { it.trace() }

    val trajectory31 = Trajectory(3, 1).apply {
        input.map(this::buildGrid)
    }.also { it.trace() }

    val trajectory51 = Trajectory(5, 1).apply {
        input.map(this::buildGrid)
    }.also { it.trace() }

    val trajectory71 = Trajectory(7, 1).apply {
        input.map(this::buildGrid)
    }.also { it.trace() }

    val trajectory12 = Trajectory(1, 2).apply {
        input.map(this::buildGrid)
    }.also { it.trace() }

    println(
        listOf(trajectory11, trajectory31, trajectory51, trajectory71, trajectory12)
            .fold(1) { acc: Long, trajectory: Trajectory ->
                acc * trajectory.getTreeHitCount()
            }
    )
}

private class Trajectory(
    val rightMoveSteps: Int,
    val downMoveSteps: Int,
    private val grid: MutableList<CharArray> = mutableListOf()
) {
    private val maxColumnCount: Int get() = grid[0].size
    private val maxRowCount: Int get() = grid.size

    private val pathBuilder: StringBuilder = StringBuilder()

    fun buildGrid(pattern: String) {
        grid.add(pattern.toCharArray())
    }

    fun trace() {
        var currentRowPosition = 0
        var currentColumnPosition = 0

        // Trace the path till we hit the last row
        while (currentRowPosition < maxRowCount) {
            // Trace the path after the first move
            if (currentRowPosition > 0 && currentColumnPosition > 0) {
                pathBuilder.append(grid[currentRowPosition][currentColumnPosition % maxColumnCount])
            }

            // Move right and then down
            currentColumnPosition += rightMoveSteps
            currentRowPosition += downMoveSteps
        }
    }

    private fun getPath(): String = pathBuilder.toString()

    fun getTreeHitCount(): Int = getPath().count { it == '#' }
}