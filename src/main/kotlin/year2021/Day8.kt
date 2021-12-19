/**
 * Problem: Day8: Seven Segment Search
 * https://adventofcode.com/2021/day/8
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2021

import base.BaseFileHandler

private class Day8 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)  // 26
    println("=====")
    solveActual(1)  // 514
    println("=====")
    solveSample(2)  // 61229
    println("=====")
    solveActual(2)  // 1012272
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day8.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day8.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    SevenSegmentSearch.parse(input)
        .getCountOf1478DigitInstances()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    SevenSegmentSearch.parse(input)
        .getSumOfAllOutputNumbers()
        .also { println(it) }
}

private class SevenSegmentSearch private constructor(
    private val signalPatternOutputDigitMap: Map<List<String>, List<String>>
) {
    companion object {
        fun parse(input: List<String>): SevenSegmentSearch = SevenSegmentSearch(
            input.associate { line ->
                line.split("|").let { patternAndDigitStrings ->
                    patternAndDigitStrings[0].trim().split("""\s+""".toRegex())
                        .filterNot(String::isEmpty) to patternAndDigitStrings[1].trim().split("""\s+""".toRegex())
                        .filterNot(String::isEmpty)
                }
            }
        )
    }

    private val digitSegmentMap = mutableMapOf<Int, String>().apply {
        this[0] = "abcefg"
        this[1] = "cf"
        this[2] = "acdeg"
        this[3] = "acdfg"
        this[4] = "bcdf"
        this[5] = "abdfg"
        this[6] = "abdefg"
        this[7] = "acf"
        this[8] = "abcdefg"
        this[9] = "abcdfg"
    }

    private val digitSegmentCharsMap: Map<Int, Set<Char>> =
        digitSegmentMap.mapValues { (_, segment) -> segment.toSet() }

    private val digit1SegmentLength = digitSegmentMap.filterKeys { it == 1 }.values.single().length
    private val digit4SegmentLength = digitSegmentMap.filterKeys { it == 4 }.values.single().length
    private val digit7SegmentLength = digitSegmentMap.filterKeys { it == 7 }.values.single().length
    private val digit8SegmentLength = digitSegmentMap.filterKeys { it == 8 }.values.single().length

    private val digits1478SegmentLengths = listOf(
        digit1SegmentLength, digit4SegmentLength, digit7SegmentLength, digit8SegmentLength
    )

    /**
     * [Solution for Part-1]
     * Returns count of instances of unique digits 1,4,7 and 8 from all the output digits.
     */
    fun getCountOf1478DigitInstances(): Int =
        signalPatternOutputDigitMap.values.flatten().count { outputDigit: String ->
            outputDigit.length in digits1478SegmentLengths
        }

    private val digit1Segments: (commands: List<String>) -> List<Char> = { commands: List<String> ->
        commands.single { command -> command.length == digit1SegmentLength }.map { it }
    }

    private val digit4Segments: (commands: List<String>) -> List<Char> = { commands: List<String> ->
        commands.single { command -> command.length == digit4SegmentLength }.map { it }
    }

    private val digit7Segments: (commands: List<String>) -> List<Char> = { commands: List<String> ->
        commands.single { command -> command.length == digit7SegmentLength }.map { it }
    }

    private val digits235SegmentLength: Int =
        digitSegmentMap.filterKeys { it in listOf(2, 3, 5) }.mapValues { it.value.length }.values.distinct().single()

    private val digits235Segments: (commands: List<String>) -> List<List<Char>> = { commands: List<String> ->
        commands.filter { command -> command.length == digits235SegmentLength }.map { command -> command.map { it } }
    }

    private fun frameSevenSegmentMap(signalPatterns: List<String>): MutableMap<Char, Char> =
        digitSegmentMap[8]!!.map { it }.associateWith { 'x' }.toMutableMap().apply {
            // Decide the segment characters for digit 1: START
            val digit1NewSegmentChars = digit1Segments(signalPatterns)
            // Map according to the order they appear
            // (Segment characters 'c' and 'f' will be fixed after processing for digit 5)
            digitSegmentCharsMap[1]!!.forEachIndexed { index, originalSegmentChar ->
                this[originalSegmentChar] = digit1NewSegmentChars[index]
            }
            // Decide the segment characters for digit 1: END

            // Decide the segment characters for digit 7: START
            val digit7NewSegmentChars = digit7Segments(signalPatterns)
            val digit7RemainingOriginalSegmentChar = (digitSegmentCharsMap[7]!! - digitSegmentCharsMap[1]!!).single()
            this[digit7RemainingOriginalSegmentChar] =
                (digit7NewSegmentChars - this.values.filterNot { it == 'x' }.toSet()).single()
            // Decide the segment characters for digit 7: END

            // Decide the segment characters for digit 4: START
            val digit4NewSegmentChars = digit4Segments(signalPatterns)
            val digit4RemainingOriginalSegmentChars = digitSegmentCharsMap[4]!! - digitSegmentCharsMap[1]!!
            val digit4RemainingNewSegmentChars = digit4NewSegmentChars - this.values.filterNot { it == 'x' }.toSet()
            // Map according to the order they appear
            // (Segment characters 'b' and 'd' will be fixed after processing for digits 5 and 3)
            digit4RemainingOriginalSegmentChars.forEachIndexed { index, originalSegmentChar ->
                this[originalSegmentChar] = digit4RemainingNewSegmentChars[index]
            }
            // Decide the segment characters for digit 4: END

            // Get segment characters for digits - 2, 3, 5
            val digits235NewSegmentCharsList = digits235Segments(signalPatterns)

            // Decide the segment characters for digit 5: START
            // Provides a set of so far identified segment characters representing digit 5
            val findDigit5ContainingNewSegmentChars: () -> Set<Char> = {
                digitSegmentCharsMap[5]!!.map { originalSegmentChar -> this[originalSegmentChar]!! }
                    .filterNot { it == 'x' }.toSet()
            }
            val digit5UnidentifiedOriginalSegmentChar =
                digitSegmentCharsMap[5]!!.single { originalSegmentChar -> this[originalSegmentChar] == 'x' }
            // Provides the conflicting segment character that can be used to distinguish the segment characters
            // of digit 5 from digit 2
            val findDigit5ConflictingSegmentChar: () -> Char = {
                (digitSegmentCharsMap[1]!!.map { originalSegmentChar ->
                    this[originalSegmentChar]!!
                } - findDigit5ContainingNewSegmentChars()).single()
            }
            // Provides the most suitable segment characters representing digit 5
            val findDigit5NewSegmentChars: (List<List<Char>>) -> List<Char>? = {
                it.filterNot { digitSegmentChars ->
                    findDigit5ConflictingSegmentChar() in digitSegmentChars
                }.singleOrNull { digitSegmentChars ->
                    findDigit5ContainingNewSegmentChars().all { newSegmentChar -> newSegmentChar in digitSegmentChars }
                }
            }
            // Identify the most suitable segment characters representing digit 5
            var digit5NewSegmentChars: List<Char>? = findDigit5NewSegmentChars(digits235NewSegmentCharsList)
            if (digit5NewSegmentChars.isNullOrEmpty()) {
                // If digit 5 is not to be found, then the original segment characters 'c' and 'f'
                // of digit 1 needs to be swapped
                digitSegmentCharsMap[1]!!.let { digit1OriginalSegmentChars ->
                    this.swapSegmentChars(
                        digit1OriginalSegmentChars.first(),
                        digit1OriginalSegmentChars.last()
                    )
                }
                // Identify again and save the most suitable segment characters representing digit 5
                digit5NewSegmentChars = requireNotNull(findDigit5NewSegmentChars(digits235NewSegmentCharsList)) {
                    // Throw an exception when digit 5 could not be found again after the swap
                    "Digit 5 could not be found even after swapping original segment characters 'c' and 'f' of digit 1"
                }
            }
            // Map the unidentified segment character of digit 5
            this[digit5UnidentifiedOriginalSegmentChar] =
                (digit5NewSegmentChars - findDigit5ContainingNewSegmentChars()).single()
            // Decide the segment characters for digit 5: END

            // Decide the segment characters for digit 3: START
            // Provides the most suitable segment characters representing digit 3
            val findDigit3NewSegmentChars: (List<List<Char>>) -> List<Char>? = {
                it.singleOrNull { digitSegmentChars ->
                    digitSegmentCharsMap[3]!!.map { originalSegmentChar -> this[originalSegmentChar]!! }
                        .all { newSegmentChar -> newSegmentChar in digitSegmentChars }
                }
            }
            // Identify the most suitable segment characters representing digit 3
            val digit3NewSegmentChars: List<Char>? = findDigit3NewSegmentChars(digits235NewSegmentCharsList)
            if (digit3NewSegmentChars.isNullOrEmpty()) {
                // If digit 3 is not to be found, then the original segment characters 'b' and 'd'
                // of digit 4 needs to be swapped
                this.swapSegmentChars(
                    digit4RemainingOriginalSegmentChars.first(),
                    digit4RemainingOriginalSegmentChars.last()
                )
                // Identify again the most suitable segment characters representing digit 3
                requireNotNull(findDigit3NewSegmentChars(digits235NewSegmentCharsList)) {
                    // Throw an exception when digit 3 could not be found again after the swap
                    "Digit 3 could not be found even after swapping original segment characters 'b' and 'd' of digit 4"
                }
            }
            // Decide the segment characters for digit 3: END

            // Decide the segment characters for digit 2: START
            val digit2RemainingUnmappedSegmentChar =
                (digitSegmentCharsMap[8]!! - this.values.filterNot { it == 'x' }.toSet()).single()
            val digit2UnidentifiedOriginalSegmentChar =
                digitSegmentCharsMap[8]!!.single { originalSegmentChar -> this[originalSegmentChar] == 'x' }
            this[digit2UnidentifiedOriginalSegmentChar] = digit2RemainingUnmappedSegmentChar
            // Decide the segment characters for digit 2: END
        }

    private fun MutableMap<Char, Char>.swapSegmentChars(
        originalSegmentX: Char,
        originalSegmentY: Char
    ) {
        val temp = this[originalSegmentX]!!
        this[originalSegmentX] = this[originalSegmentY]!!
        this[originalSegmentY] = temp
    }

    private fun Map<Char, Char>.toSegmentCharsDigitMap(): Map<Set<Char>, Int> =
        digitSegmentCharsMap.mapValues { (_, originalSegmentChars) ->
            originalSegmentChars.map { originalSegmentChar ->
                this[originalSegmentChar]!!
            }.toSortedSet()
        }.entries.associate { (digit, newSegmentChars) -> newSegmentChars to digit }

    /**
     * [Solution for Part-2]
     * Returns sum of all output numbers after identifying all the output digits.
     */
    fun getSumOfAllOutputNumbers(): Int =
        signalPatternOutputDigitMap.mapValues { (commands: List<String>, digits: List<String>) ->
            val newSegmentCharsDigitMap = frameSevenSegmentMap(commands).toSegmentCharsDigitMap()
            digits.map { digit: String -> newSegmentCharsDigitMap[digit.toSortedSet()] }
                .joinToString(separator = "") { it.toString() }
                .toInt()
        }.values.sum()
}