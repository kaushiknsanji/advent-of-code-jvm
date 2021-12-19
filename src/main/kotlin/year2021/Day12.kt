/**
 * Problem: Day12: Passage Pathing
 * https://adventofcode.com/2021/day/12
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2021

import base.BaseFileHandler
import extensions.whileLoop

private class Day12 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample1(1) // 10
    println("=====")
    solveSample2(1) // 19
    println("=====")
    solveSample3(1) // 226
    println("=====")

    solveActual(1)  // 3708
    println("=====")

    solveSample1(2) // 36
    println("=====")
    solveSample2(2) // 103
    println("=====")
    solveSample3(2) // 3509
    println("=====")

    solveActual(2)  // 93858
    println("=====")
}

private fun solveSample1(executeProblemPart: Int) {
    execute(Day12.getSampleFile("1").readLines(), executeProblemPart)
}

private fun solveSample2(executeProblemPart: Int) {
    execute(Day12.getSampleFile("2").readLines(), executeProblemPart)
}

private fun solveSample3(executeProblemPart: Int) {
    execute(Day12.getSampleFile("3").readLines(), executeProblemPart)
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
    PathPlanner.parse(input)
        .getCountOfPossiblePathsToEndFromStart()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    PathPlanner.parse(input)
        .getCountOfPossiblePathsToEndFromStart(true)
        .also { println(it) }
}

private class Cave(val id: String, totalConnections: Int) {
    companion object {
        const val CAVE_START = "start"
        const val CAVE_END = "end"
    }

    val isCaveStart = id == CAVE_START
    val isCaveEnd = id == CAVE_END
    val isReservedCave = isCaveStart || isCaveEnd

    val isCaveSmall = !isReservedCave && id.map { it }.all { it.isLowerCase() }
    val isCaveBig = id.map { it }.all { it.isUpperCase() }
    val isCaveLast = totalConnections == 1 && !isReservedCave
}

private fun List<Cave>.hasStartOrEndCaves() = any { cave ->
    cave.id in listOf(
        Cave.CAVE_START,
        Cave.CAVE_END
    )
}

private class Path private constructor(val cave1: Cave, val cave2: Cave) {
    companion object {
        fun create(cave1: Cave, cave2: Cave): Path = listOf(cave1, cave2).hasStartOrEndCaves()
            .takeIf { it }?.let {
                if (cave2.isCaveStart || cave1.isCaveEnd) {
                    Path(cave2, cave1)
                } else {
                    Path(cave1, cave2)
                }
            } ?: Path(cave1, cave2)
    }

    fun getCavesInDirection(): List<Cave> = listOf(cave1, cave2)
}

private interface IPathFinder {
    fun getStartPaths(): List<Path>
    fun Cave.getContainingPaths(): List<Path>
    fun Cave.getConnectingCaves(): List<Cave>
}

private class PathFinder(val paths: List<Path>) : IPathFinder {

    override fun getStartPaths(): List<Path> = paths.filter { path ->
        path.getCavesInDirection().any { cave -> cave.isCaveStart }
    }

    override fun Cave.getContainingPaths(): List<Path> = paths.filter { path ->
        path.getCavesInDirection().any { cave -> cave.id == this.id }
    }

    override fun Cave.getConnectingCaves(): List<Cave> = getContainingPaths().flatMap { path ->
        path.getCavesInDirection().filterNot { cave -> cave.id == this.id }
    }

}

private class PathTracer(
    val pathFinder: IPathFinder,
    val tracedCaves: List<Cave>,
    val enableVisitingOneSmallCaveTwice: Boolean = false
) : IPathFinder by pathFinder {

    val lastVisitedCave = if (tracedCaves.isEmpty()) {
        throw IllegalStateException("No Cave has been traced yet")
    } else {
        tracedCaves.last()
    }

    val previousToLastVisitedCave = if (tracedCaves.size < 2) {
        throw IllegalStateException("No Path has been traced yet")
    } else {
        tracedCaves[tracedCaves.lastIndex - 1]
    }

    val isTraceTerminated =
        lastVisitedCave.isCaveLast && (
                previousToLastVisitedCave.isCaveSmall && (!enableVisitingOneSmallCaveTwice
                        || (enableVisitingOneSmallCaveTwice && hasVisitedAnySmallCaveTwice()))
                )

    val isTraceFinished = lastVisitedCave.isCaveEnd

    fun Cave.isCaveVisited(): Boolean = tracedCaves.any { cave -> cave.id == this.id }

    fun Cave.isCaveVisitedTwice(): Boolean = tracedCaves.count { cave -> cave.id == this.id } == 2

    fun hasVisitedAnySmallCaveTwice(): Boolean = tracedCaves.groupingBy { it }.eachCount()
        .filterKeys { cave: Cave -> cave.isCaveSmall }
        .any { (_, visitedCount) -> visitedCount == 2 }

    fun append(cave: Cave): PathTracer =
        PathTracer(this.pathFinder, this.tracedCaves + cave, enableVisitingOneSmallCaveTwice)

    fun findNextPathTraces(): List<PathTracer> = lastVisitedCave.getConnectingCaves()
        .filterNot { cave ->
            cave.isCaveStart || cave.isCaveSmall && (
                    (!enableVisitingOneSmallCaveTwice && cave.isCaveVisited())
                            || (enableVisitingOneSmallCaveTwice && (
                            cave.isCaveVisitedTwice() || (cave.isCaveVisited() && hasVisitedAnySmallCaveTwice())
                            )
                            )
                    )
        }
        .map { cave -> this.append(cave) }

    fun getPathTraced(): String = tracedCaves.joinToString { cave -> cave.id }

    override fun toString(): String = getPathTraced()
}

private class PathPlanner private constructor(
    private val paths: List<Path>
) : IPathFinder by PathFinder(paths) {

    companion object {
        fun parse(input: List<String>): PathPlanner = with(input.map { line -> line.split("-") }) {
            val nameCaveMap: Map<String, Cave> = this.flatten().groupingBy { it }.eachCount()
                .mapValues { (id: String, connectionsCount: Int) ->
                    Cave(id, connectionsCount)
                }

            PathPlanner(
                paths = this.map { caveStrings ->
                    Path.create(
                        nameCaveMap[caveStrings.first()]!!,
                        nameCaveMap[caveStrings.last()]!!
                    )
                }
            )
        }
    }

    private fun getAllPossiblePathsToEndFromStart(enableVisitingOneSmallCaveTwice: Boolean): List<PathTracer> =
        whileLoop(
            loopStartCounter = 0,
            initialResult = getStartPaths().map { path ->
                PathTracer(this, path.getCavesInDirection(), enableVisitingOneSmallCaveTwice)
            },
            exitCondition = { _, lastIterationResult: List<PathTracer>? ->
                lastIterationResult != null && lastIterationResult.all { tracer: PathTracer -> tracer.isTraceFinished }
            }
        ) { loopCounter: Int, lastIterationResult: List<PathTracer> ->
            loopCounter to lastIterationResult.filterNot { tracer: PathTracer ->
                tracer.isTraceTerminated
            }.flatMap { tracer: PathTracer ->
                if (tracer.isTraceFinished) {
                    listOf(tracer)
                } else {
                    tracer.findNextPathTraces()
                }
            }
        }

    /**
     * [Solution for Part-1 and Part-2]
     * Returns the count of all possible paths to "end" cave from "start" cave.
     * @param enableVisitingOneSmallCaveTwice [Boolean] flag to enable visiting only one small cave twice.
     * Added for Part-2. Defaulted to `false` for Part-1 which allows all small caves to be visited only once.
     */
    fun getCountOfPossiblePathsToEndFromStart(enableVisitingOneSmallCaveTwice: Boolean = false): Int =
        getAllPossiblePathsToEndFromStart(enableVisitingOneSmallCaveTwice).count()

}