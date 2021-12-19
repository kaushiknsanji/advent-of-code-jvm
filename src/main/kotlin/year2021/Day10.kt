/**
 * Problem: Day10: Syntax Scoring
 * https://adventofcode.com/2021/day/10
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2021

import base.BaseFileHandler

private class Day10 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)  // 26397
    println("=====")
    solveActual(1)  // 321237
    println("=====")
    solveSample(2)  // 288957
    println("=====")
    solveActual(2)  // 2360030859
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day10.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day10.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    SyntaxAnalyzer.parse(input)
        .getTotalSyntaxErrorScore()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    SyntaxAnalyzer.parse(input)
        .getMiddleScoreOfCompletionStrings()
        .also { println(it) }
}

private class SyntaxAnalyzer private constructor(
    private val bracePatterns: List<List<Char>>
) {
    companion object {
        const val OPEN_BRACES = "([{<"
        const val CLOSE_BRACES = ")]}>"

        fun parse(input: List<String>): SyntaxAnalyzer = SyntaxAnalyzer(
            input.map { pattern -> pattern.map { it } }
        )
    }

    private val closeOpenBraceMap: Map<Char, Char> = mutableMapOf<Char, Char>().apply {
        this[')'] = '('
        this[']'] = '['
        this['}'] = '{'
        this['>'] = '<'
    }

    private val closeBraceErrorPointsMap: Map<Char, Int> = mutableMapOf<Char, Int>().apply {
        this[')'] = 3
        this[']'] = 57
        this['}'] = 1197
        this['>'] = 25137
    }

    /**
     * [Solution for Part-1]
     * Returns a Total score of the Errors identified in all syntax lines.
     */
    fun getTotalSyntaxErrorScore(): Int = bracePatterns.mapNotNull { syntaxLine ->
        val bracesStack = mutableListOf<Char>()
        var errorBrace: Char? = null
        for (braceChar in syntaxLine) {
            when (braceChar) {
                in OPEN_BRACES -> bracesStack.add(0, braceChar) // Stack open braces
                in CLOSE_BRACES -> if (bracesStack[0] == closeOpenBraceMap[braceChar]!!) {
                    // When there is an open-close brace match, remove the corresponding open brace from the stack
                    bracesStack.removeFirst()
                } else {
                    // When there is a close brace error, save the mismatching close brace and bail out
                    errorBrace = braceChar
                    break
                }
            }
        }
        // Return the mismatched closing brace
        errorBrace
    }.sumOf { errorBrace -> closeBraceErrorPointsMap[errorBrace]!! }

    private val closeBraceCompletionPointsMap: Map<Char, Int> = CLOSE_BRACES.map { it }.withIndex()
        .associate { indexedBrace ->
            indexedBrace.value to indexedBrace.index + 1
        }

    private val openCloseBraceMap: Map<Char, Char> = closeOpenBraceMap.entries.associate { (closeBrace, openBrace) ->
        openBrace to closeBrace
    }

    private fun List<Long>.middle(): Long = this.sorted().let { sortedList ->
        sortedList[sortedList.lastIndex.ushr(1)]
    }

    /**
     * [Solution for Part-2]
     * Returns a middle score of the Completion strings derived from each incomplete syntax lines.
     */
    fun getMiddleScoreOfCompletionStrings(): Long = bracePatterns.mapNotNull { syntaxLine ->
        val bracesStack = mutableListOf<Char>()
        var errorOccurred = false
        for (braceChar in syntaxLine) {
            when (braceChar) {
                in OPEN_BRACES -> bracesStack.add(0, braceChar) // Stack open braces
                in CLOSE_BRACES -> if (bracesStack[0] == closeOpenBraceMap[braceChar]!!) {
                    // When there is an open-close brace match, remove the corresponding open brace from the stack
                    bracesStack.removeFirst()
                } else {
                    // When there is a close brace error, set the error flag and bail out
                    errorOccurred = true
                    break
                }
            }
        }

        if (!errorOccurred) {
            // When there is no error in the syntax line, then it is incomplete
            // Map the remaining open braces to the corresponding close braces in the same stack order
            bracesStack.map { braceChar -> openCloseBraceMap[braceChar]!! }
        } else {
            // When there is an error in the syntax line, return null to not consider this syntax line
            null
        }
    }.map { closeBraces ->
        // Derive score for each completed string
        closeBraces.indices.fold(0) { score: Long, index: Int ->
            score * 5 + closeBraceCompletionPointsMap[closeBraces[index]]!!
        }
    }.middle() // Return the middle score after sorting internally

}