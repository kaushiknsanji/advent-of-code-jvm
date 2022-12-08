/**
 * Problem: Day2: Rock Paper Scissors
 * https://adventofcode.com/2022/day/2
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseFileHandler

private class Day2 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 15
    println("=====")
    solveActual(1) // 10595
    println("=====")
    solveSample(2) // 12
    println("=====")
    solveActual(2) // 9541
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
    RPSGame.parse(
        opponentCodeMap = mapOf(
            'A' to RPSGameScorer.ROCK,
            'B' to RPSGameScorer.PAPER,
            'C' to RPSGameScorer.SCISSORS
        ),
        playerCodeMap = mapOf(
            'X' to RPSGameScorer.ROCK,
            'Y' to RPSGameScorer.PAPER,
            'Z' to RPSGameScorer.SCISSORS
        ),
        input
    )
        .getTotalScore()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    RPSGame.parse(
        opponentCodeMap = mapOf(
            'A' to RPSGameScorer.ROCK,
            'B' to RPSGameScorer.PAPER,
            'C' to RPSGameScorer.SCISSORS
        ),
        playerCodeMap = mapOf(
            'X' to RPSGameScorer.KEY_LOSE,
            'Y' to RPSGameScorer.KEY_DRAW,
            'Z' to RPSGameScorer.KEY_WIN
        ),
        input,
        isGameRigged = true
    )
        .getTotalScore()
        .also { println(it) }
}

private interface RPSGameScoringInterface {
    fun getShapeSelectedScore(shape: String): Int
    fun getRoundOutcomeScore(opponentShape: String, playerShape: String): Int
    fun getSingleRoundScore(opponentCode: Char, playerCode: Char): Int
    fun getMatchFixedPlayerShapeScore(opponentShape: String, playOutcome: String): Int
}

private class RPSGameScorer(
    val opponentCodeMap: Map<Char, String>,
    val playerCodeMap: Map<Char, String>,
    val isGameRigged: Boolean
) : RPSGameScoringInterface {
    companion object {
        const val ROCK = "Rock"
        const val PAPER = "Paper"
        const val SCISSORS = "Scissors"

        const val LOSE = 0
        const val WIN = 6
        const val DRAW = 3

        const val KEY_LOSE = "Lose"
        const val KEY_WIN = "Win"
        const val KEY_DRAW = "Draw"
    }

    private val shapeToScoreMap
        get() = mapOf(
            ROCK to 1,
            PAPER to 2,
            SCISSORS to 3
        )

    private val gameRuleMap
        get() = mapOf(
            listOf(ROCK, SCISSORS) to ROCK,
            listOf(PAPER, SCISSORS) to SCISSORS,
            listOf(PAPER, ROCK) to PAPER
        )

    private val outcomeScoreMap
        get() = mapOf(
            KEY_LOSE to LOSE,
            KEY_WIN to WIN,
            KEY_DRAW to DRAW
        )

    override fun getShapeSelectedScore(shape: String): Int = shapeToScoreMap[shape]!!

    override fun getRoundOutcomeScore(opponentShape: String, playerShape: String): Int =
        if (opponentShape == playerShape) {
            DRAW
        } else {
            val gameResult = gameRuleMap[listOf(opponentShape, playerShape).sorted()]!!
            if (opponentShape == gameResult) {
                LOSE
            } else {
                WIN
            }
        }

    override fun getSingleRoundScore(opponentCode: Char, playerCode: Char): Int =
        if (isGameRigged) {
            getMatchFixedPlayerShapeScore(
                opponentCodeMap[opponentCode]!!,
                playerCodeMap[playerCode]!!
            ) + outcomeScoreMap[playerCodeMap[playerCode]!!]!!
        } else {
            getShapeSelectedScore(playerCodeMap[playerCode]!!) + getRoundOutcomeScore(
                opponentCodeMap[opponentCode]!!,
                playerCodeMap[playerCode]!!
            )
        }

    override fun getMatchFixedPlayerShapeScore(opponentShape: String, playOutcome: String): Int =
        when (playOutcome) {
            KEY_LOSE -> {
                // Opponent Wins
                gameRuleMap.filter { entry -> entry.value == opponentShape }
                    .keys.flatten()
                    .filterNot { shape -> shape == opponentShape }.first().let { playerShape ->
                        getShapeSelectedScore(playerShape)
                    }
            }

            KEY_WIN -> {
                // Opponent Loses
                gameRuleMap.filterNot { entry -> entry.value == opponentShape }
                    .filterKeys { shapes -> opponentShape in shapes }
                    .keys.flatten()
                    .filterNot { shape -> shape == opponentShape }.first().let { playerShape ->
                        getShapeSelectedScore(playerShape)
                    }
            }

            else -> {
                // Draw
                getShapeSelectedScore(opponentShape)
            }
        }

}

private class RPSGame private constructor(
    opponentCodeMap: Map<Char, String>,
    playerCodeMap: Map<Char, String>,
    val gameInputs: List<Set<Char>>,
    isGameRigged: Boolean = false
) : RPSGameScoringInterface by RPSGameScorer(opponentCodeMap, playerCodeMap, isGameRigged) {
    companion object {
        fun parse(
            opponentCodeMap: Map<Char, String>,
            playerCodeMap: Map<Char, String>,
            input: List<String>,
            isGameRigged: Boolean = false
        ): RPSGame = RPSGame(
            opponentCodeMap,
            playerCodeMap,
            input.map { gameRoundStr ->
                gameRoundStr.filterNot(Char::isWhitespace).toSet()
            },
            isGameRigged
        )
    }

    fun getTotalScore(): Int =
        gameInputs.sumOf { gameRoundCodeSet -> getSingleRoundScore(gameRoundCodeSet.first(), gameRoundCodeSet.last()) }
}