/**
 * Problem: Day1: Trebuchet?!
 * https://adventofcode.com/2023/day/1
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler

private class Day1 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)       // 142
    println("=====")
    solveActual(1)       // 54877
    println("=====")
    solvePart2Sample()                  // 281
    println("=====")
    solveActual(2)      // 54100
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day1.getSampleFile().readLines(), executeProblemPart)
}

private fun solvePart2Sample() {
    execute(Day1.getSampleFile("_part2").readLines(), 2)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day1.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    input.sumOf { line ->
        Trebuchet.parse(line).decodeCalibrationValue()
    }
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    input.sumOf { line ->
        Trebuchet.parse(line, true).decodeCalibrationValue()
    }
        .also { println(it) }
}

private class Trebuchet private constructor(
    private val calibrationCode: String
) {
    companion object {
        private val digitMap = mapOf(
            "one" to "1",
            "two" to "2",
            "three" to "3",
            "four" to "4",
            "five" to "5",
            "six" to "6",
            "seven" to "7",
            "eight" to "8",
            "nine" to "9"
        )

        fun parse(calibrationCodeLine: String, includeSpelledDigits: Boolean = false): Trebuchet = Trebuchet(
            buildString {
                append(calibrationCodeLine)

                if (includeSpelledDigits) {
                    // Spelled digits to be included for Part-2
                    // For all spelled digits, this inserts corresponding number at the beginning of spelled digit
                    digitMap.filterKeys { digitString ->
                        digitString in this
                    }.forEach { (digitString, digit) ->
                        var currentIndex = 0
                        while (this.indexOf(digitString.dropLast(1), currentIndex) > -1) {
                            // While finding next index, drop the last character of a spelled digit
                            // as it can tie with a following spelled digit if any
                            val nextIndex = this.indexOf(digitString.dropLast(1), currentIndex)
                            // Insert corresponding number at the beginning of spelled digit
                            this.insert(nextIndex, digit)
                            // Add 1 to the currentIndex as we just inserted a digit
                            currentIndex = nextIndex + digitString.length + 1
                        }
                    }

                    // Another approach which inserts corresponding number for only the first and last spelled digits.
                    // Slightly slower by 5-10ms
                    /*digitMap.filterKeys { digitString ->
                        digitString in this
                    }.takeUnless { it.isEmpty() }?.flatMap { (digitString, _) ->
                        setOf(this.indexOf(digitString) to digitString, this.lastIndexOf(digitString) to digitString)
                    }?.let { indexDigitStringPairs ->
                        indexDigitStringPairs.singleOrNull()?.let { (index, digitString) ->
                            this.insert(index, digitMap[digitString])
                        }?: run {
                            indexDigitStringPairs.minBy { it.first }.let { (index, digitString) ->
                                // Insert a digit for the first spelled digit
                                this.insert(index, digitMap[digitString])
                            }
                            indexDigitStringPairs.maxBy { it.first }.let { (index, digitString) ->
                                // Add 1 to the index of last spelled digit as we just
                                // inserted a digit for the first spelled digit
                                this.insert(index + 1, digitMap[digitString])
                            }
                        }
                    }*/
                }

            }
        )
    }

    /**
     * [Solution for Part 1 & 2]
     * Returns the calibration value determined for the [calibrationCode] in the document.
     */
    fun decodeCalibrationValue(): Int =
        "${calibrationCode.first { it.isDigit() }}${calibrationCode.last { it.isDigit() }}".toInt()

}