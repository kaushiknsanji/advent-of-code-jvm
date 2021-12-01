/**
 * Problem: Day2: Password philosophy
 * https://adventofcode.com/2020/day/2
 *
 * @author Kaushik N. Sanji
 */

package year2020

import base.BaseFileHandler

private class Day2 {
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
    println(
        input.map(PasswordPolicyOld::parse)
            .count { policy: PasswordPolicyOld? ->
                policy?.isPasswordValid ?: false
            }
    )
}

private fun doPart2(input: List<String>) {
    println(
        input.map(PasswordPolicyNew::parse)
            .count { policy: PasswordPolicyNew? ->
                policy?.isPasswordValid ?: false
            }
    )
}

private class PasswordPolicyOld private constructor(
    val letter: Char,
    val maxAllowedCount: Int,
    val minAllowedCount: Int,
    val password: String
) {

    companion object {
        private val policyInputPattern = """(\d+)-(\d+) ([a-z]): ([a-z]+)""".toRegex()

        fun parse(policyString: String): PasswordPolicyOld? =
            policyInputPattern.find(policyString)?.groupValues?.run {
                PasswordPolicyOld(
                    this[3].single(),
                    this[2].toInt(),
                    this[1].toInt(),
                    this[4]
                )
            }
    }

    val isPasswordValid: Boolean
        get() = password.count { testChar: Char -> testChar == letter } in minAllowedCount..maxAllowedCount

}

private class PasswordPolicyNew private constructor(
    val letter: Char,
    val position1: Int,
    val position2: Int,
    val password: String
) {

    companion object {
        private val policyInputPattern = """(\d+)-(\d+) ([a-z]): ([a-z]+)""".toRegex()

        fun parse(policyString: String): PasswordPolicyNew? =
            policyInputPattern.find(policyString)?.groupValues?.run {
                PasswordPolicyNew(
                    this[3].single(),
                    this[2].toInt() - 1,
                    this[1].toInt() - 1,
                    this[4]
                )
            }
    }

    val isPasswordValid: Boolean
        get() = (password[position1] == letter) xor (password[position2] == letter)

}