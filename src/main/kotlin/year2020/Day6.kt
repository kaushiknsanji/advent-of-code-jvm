/**
 * Problem: Day6: Custom Customs
 * https://adventofcode.com/2020/day/6
 *
 * @author Kaushik N. Sanji
 */

package year2020

import base.BaseFileHandler

private class Day6 {
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
    execute(Day6.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day6.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    input.joinToString(separator = " ")
        .split("""\s{2,}""".toRegex())
        .map(CustomsDeclaration::parse)
        .sumOf(CustomsDeclaration::questionsRespondedCount)
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    input.joinToString(separator = " ")
        .split("""\s{2,}""".toRegex())
        .map(CustomsDeclaration::parse)
        .sumOf(CustomsDeclaration::commonQuestionsRespondedCount)
        .also { println(it) }
}

private class CustomsDeclaration private constructor(
    val groupResponses: String
) {
    companion object {
        fun parse(groupResponses: String): CustomsDeclaration = CustomsDeclaration(groupResponses)
    }

    val questionsRespondedCount: Int get() = groupResponses.filterNot(Char::isWhitespace).toSet().size

    val commonQuestionsRespondedCount: Int
        get() = groupResponses.split(" ").map(String::toSet).reduce(Set<Char>::intersect).size
}