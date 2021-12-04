/**
 * Problem: Day4: Giant Squid
 * https://adventofcode.com/2021/day/4
 *
 * @author Kaushik N Sanji (kaushiknsanji@gmail.com)
 */

package year2021

import base.BaseFileHandler
import extensions.whileLoop

private class Day4 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 4512
    println("=====")
    solveActual(1) // 39984
    println("=====")
    solveSample(2) // 1924
    println("=====")
    solveActual(2) // 8468
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
    BingoGame.parse(input)
        .getBoardWonWithLastDrawnNumber()
        .also { (bingoBoard: BingoBoard, lastNumberDrawn: Int) ->
            println(bingoBoard.getAllUnmarkedNumbers().sum() * lastNumberDrawn)
        }
}

private fun doPart2(input: List<String>) {
    BingoGame.parse(input)
        .getLastBoardWonWithLastDrawnNumber()
        .also { (bingoBoard: BingoBoard, lastNumberDrawn: Int) ->
            println(bingoBoard.getAllUnmarkedNumbers().sum() * lastNumberDrawn)
        }
}

private data class BingoNumber(val number: Int, var markedState: Boolean = false)

private class BingoBoard(input: List<String>) {
    private val boardGrid: List<List<BingoNumber>> = input.map { rowString ->
        rowString.split("""\s+""".toRegex()).map { rowNumberString -> BingoNumber(rowNumberString.toInt()) }
    }

    private val transposedBoardGridMap: Map<Int, List<BingoNumber>> =
        boardGrid.flatMap { bingoNumbers -> bingoNumbers.withIndex() }
            .groupBy { indexedBingoNumbers -> indexedBingoNumbers.index }
            .mapValues { (_, indexedBingoNumbersAtSameColumn) -> indexedBingoNumbersAtSameColumn.map { it.value } }

    private val isAnyEntireRowMarked: () -> Boolean = {
        boardGrid.any { rowBingoNumbers ->
            rowBingoNumbers.all { bingoNumber: BingoNumber -> bingoNumber.markedState }
        }
    }

    private val isAnyEntireColumnMarked: () -> Boolean = {
        transposedBoardGridMap.any { (_, columnBingoNumbers) ->
            columnBingoNumbers.all { bingoNumber: BingoNumber -> bingoNumber.markedState }
        }
    }

    operator fun set(number: Int, newMarkedState: Boolean) {
        boardGrid.flatten().firstOrNull { bingoNumber: BingoNumber ->
            bingoNumber.number == number
        }?.markedState = newMarkedState
    }

    fun getAllUnmarkedNumbers(): List<Int> = boardGrid.flatten().filter { bingoNumber: BingoNumber ->
        !bingoNumber.markedState
    }.map { bingoNumber: BingoNumber -> bingoNumber.number }

    fun hasBoardWon(): Boolean = isAnyEntireRowMarked() || isAnyEntireColumnMarked()
}

private class BingoGame private constructor(
    private val numbersDrawn: List<Int>,
    private val bingoBoards: List<BingoBoard>
) {
    companion object {
        fun parse(input: List<String>): BingoGame {
            val numbersDrawn = mutableListOf<Int>()
            val bingoBoards = mutableListOf<BingoBoard>()
            val currentBoardData = mutableListOf<String>()

            val buildBingoBoardList: (bingoBoardData: List<String>) -> Unit = { bingoBoardData ->
                if (bingoBoardData.isNotEmpty()) {
                    bingoBoards.add(BingoBoard(bingoBoardData))
                }
            }

            input.forEach { line ->
                if (line.contains(",")) {
                    line.split(",").mapTo(numbersDrawn, String::toInt)
                } else if (line.isBlank() || line.isEmpty()) {
                    buildBingoBoardList(currentBoardData)
                    currentBoardData.clear()
                } else {
                    currentBoardData.add(line.trim())
                }
            }

            buildBingoBoardList(currentBoardData)

            return BingoGame(numbersDrawn, bingoBoards)
        }
    }

    /**
     * [Solution for Part-1]
     * Returns a [BingoBoard] that won with the last number drawn.
     */
    fun getBoardWonWithLastDrawnNumber(): Pair<BingoBoard, Int> = whileLoop(
        loopStartCounter = 0,
        exitCondition = { _, lastIterationResult: Pair<BingoBoard?, Int>? ->
            lastIterationResult?.first != null
        }
    ) { loopCounter: Int ->
        // Draw the corresponding number
        val numberDrawn = numbersDrawn[loopCounter]
        // Mark the drawn number on the Bingo Boards
        bingoBoards.forEach { bingoBoard -> bingoBoard[numberDrawn] = true }
        // Find the Bingo Board that has won
        val bingoBoardWon: BingoBoard? = bingoBoards.firstOrNull { bingoBoard -> bingoBoard.hasBoardWon() }
        // Return the current iteration result
        (loopCounter + 1) to (bingoBoardWon to numberDrawn)
    }.let { it.first!! to it.second }

    /**
     * [Solution for Part-2]
     * Returns the last [BingoBoard] that won with the last number drawn.
     */
    fun getLastBoardWonWithLastDrawnNumber(): Pair<BingoBoard, Int> = whileLoop(
        loopStartCounter = 0,
        exitCondition = { loopCounter: Int, _ ->
            loopCounter == numbersDrawn.size || bingoBoards.all { bingoBoard -> bingoBoard.hasBoardWon() }
        }
    ) { loopCounter: Int ->
        // Draw the corresponding number
        val numberDrawn = numbersDrawn[loopCounter]
        // Read all Bingo Boards that have already won
        val bingoBoardsWon = bingoBoards.filter { bingoBoard -> bingoBoard.hasBoardWon() }
        // Mark the drawn number on the Bingo Boards that have not yet won
        bingoBoards.filterNot { it in bingoBoardsWon }.forEach { bingoBoard -> bingoBoard[numberDrawn] = true }
        // Find the Bingo Board that has currently won
        val bingoBoardWon: BingoBoard? =
            bingoBoards.filterNot { it in bingoBoardsWon }.firstOrNull { bingoBoard -> bingoBoard.hasBoardWon() }
        // Return the current iteration result
        (loopCounter + 1) to (bingoBoardWon to numberDrawn)
    }.let { it.first!! to it.second }
}