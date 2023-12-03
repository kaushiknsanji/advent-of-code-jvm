/**
 * Problem: Day2: Cube Conundrum
 * https://adventofcode.com/2023/day/2
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler

private class Day2 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 8
    println("=====")
    solveActual(1)      // 2369
    println("=====")
    solveSample(2)      // 2286
    println("=====")
    solveActual(2)      // 66363
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day2.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day2.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    with(TripleCube(red = 12, green = 13, blue = 14)) {
        input.map { game ->
            TripleCubeGameProcessor.create(
                bagConfiguration = this,
                inputGame = game
            )
        }
    }.filter { tripleCubeGameProcessor ->
        tripleCubeGameProcessor.isGamePossible()
    }.sumOf { tripleCubeGameProcessor ->
        tripleCubeGameProcessor.gameId
    }.also {
        println(it)
    }
}

private fun doPart2(input: List<String>) {
    with(TripleCube(red = 12, green = 13, blue = 14)) {
        input.map { game ->
            TripleCubeGameProcessor.create(
                bagConfiguration = this,
                inputGame = game
            )
        }
    }.sumOf { tripleCubeGameProcessor ->
        tripleCubeGameProcessor.possibleGameWithFewerCubes().power()
    }.also {
        println(it)
    }
}

private class TripleCube(
    val red: Int,
    val green: Int,
    val blue: Int
) {
    /**
     * Returns the power of cubes
     */
    fun power() = red * green * blue
}

private class TripleCubeGameProcessor private constructor(
    val bagConfiguration: TripleCube,
    val gameId: Int,
    val revealedCubesList: List<TripleCube>
) {
    companion object {
        private const val GAME = "Game"
        private const val RED = "red"
        private const val GREEN = "green"
        private const val BLUE = "blue"
        private const val COLON = ":"
        private const val SEMICOLON = ";"
        private const val COMMA = ","

        fun create(bagConfiguration: TripleCube, inputGame: String): TripleCubeGameProcessor =
            TripleCubeGameProcessor(
                bagConfiguration = bagConfiguration,
                gameId = inputGame.substringBefore(COLON).substringAfter(GAME).trim().toInt(),
                revealedCubesList = inputGame.substringAfter(COLON).split(SEMICOLON).map { revealedCubesString ->
                    revealedCubesString.split(COMMA).let { revealedCubes ->
                        TripleCube(
                            red = revealedCubes.singleOrNull { it.endsWith(RED) }
                                ?.substringBefore(RED)?.trim()?.toInt()
                                ?: 0,
                            green = revealedCubes.singleOrNull { it.endsWith(GREEN) }
                                ?.substringBefore(GREEN)?.trim()?.toInt()
                                ?: 0,
                            blue = revealedCubes.singleOrNull { it.endsWith(BLUE) }
                                ?.substringBefore(BLUE)?.trim()?.toInt()
                                ?: 0
                        )
                    }
                }
            )

    }

    /**
     * [Solution for Part-1]
     * Returns if the Game is possible based on the [bagConfiguration] of cubes.
     */
    fun isGamePossible(): Boolean = revealedCubesList.all { revealedCubes ->
        revealedCubes.red <= bagConfiguration.red
                && revealedCubes.green <= bagConfiguration.green
                && revealedCubes.blue <= bagConfiguration.blue
    }

    /**
     * [Solution for Part-2]
     * Returns a possible [TripleCube] game configuration that can be played
     * for the current game [revealedCubesList] by selecting fewer cubes.
     */
    fun possibleGameWithFewerCubes(): TripleCube = TripleCube(
        red = revealedCubesList.maxByOrNull { tripleCube -> tripleCube.red }?.red ?: 0,
        green = revealedCubesList.maxByOrNull { tripleCube -> tripleCube.green }?.green ?: 0,
        blue = revealedCubesList.maxByOrNull { tripleCube -> tripleCube.blue }?.blue ?: 0
    )
}