/**
 * Problem: Day4: Passport Processing
 * https://adventofcode.com/2020/day/4
 *
 * @author Kaushik N. Sanji
 */

package year2020

import base.BaseFileHandler

private class Day4 {
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
    solvePart2SampleValid()
    println("=====")
    solvePart2SampleInvalid()
    println("=====")
    solveActual(2)
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day4.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day4.getActualTestFile().readLines(), executeProblemPart)
}

private fun solvePart2SampleValid() {
    execute(Day4.getSampleFile("_part2_valid").readLines(), 2)
}

private fun solvePart2SampleInvalid() {
    execute(Day4.getSampleFile("_part2_invalid").readLines(), 2)
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
        .map(Passport::parse)
        .map(Passport::isValidForPart1)
        .count { it }
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    input.joinToString(separator = " ")
        .split("""\s{2,}""".toRegex())
        .map(Passport::parse)
        .map(Passport::isValidForPart2)
        .count { it }
        .also { println(it) }
}

private class Passport private constructor(
    private val dataMap: MutableMap<String, String> = mutableMapOf()
) {
    companion object {
        // Key fields in the Passport information
        private const val KEY_BIRTH_YEAR = "byr"
        private const val KEY_ISSUE_YEAR = "iyr"
        private const val KEY_EXPIRATION_YEAR = "eyr"
        private const val KEY_HEIGHT = "hgt"
        private const val KEY_HAIR_COLOR = "hcl"
        private const val KEY_EYE_COLOR = "ecl"
        private const val KEY_PASSPORT_ID = "pid"

        // Fields for Part1 validations
        private val reqdPassportDataFields: List<String>
            get() = listOf(
                KEY_BIRTH_YEAR,
                KEY_ISSUE_YEAR,
                KEY_EXPIRATION_YEAR,
                KEY_HEIGHT,
                KEY_HAIR_COLOR,
                KEY_EYE_COLOR,
                KEY_PASSPORT_ID
            )

        // Fields for Part2 validations
        private val validBirthYearRange get() = 1920..2002
        private val validIssueYearRange get() = 2010..2020
        private val validExpirationYearRange get() = 2020..2030
        private val validHeightRangeCM get() = 150..193
        private val validHeightRangeINCH get() = 59..76
        private val validHairColorPattern = """#[\da-f]{6}""".toRegex()
        private val validEyeColors: List<String>
            get() = listOf(
                "amb", "blu", "brn", "gry", "grn", "hzl", "oth"
            )
        private val validPassportIdPattern = """\d{9}""".toRegex()
        private const val unitHeightCM = "cm"
        private const val unitHeightINCH = "in"

        // Regex Pattern for parsing the Passport information from input
        private val passportLineDataPattern = """([a-z]{3}):([\w#]+)""".toRegex()

        /**
         * Parses the provided [passportLineData] to create and build a [Passport] instance, and then returns it.
         */
        fun parse(passportLineData: String): Passport = Passport().apply { buildInfo(passportLineData) }
    }

    val isValidForPart1: Boolean get() = dataMap.keys.containsAll(reqdPassportDataFields)

    val isValidForPart2: Boolean
        get() =
            isValidForPart1 &&
                    isBirthYearValid() &&
                    isIssueYearValid() &&
                    isExpirationYearValid() &&
                    isHeightValid() &&
                    isHairColorValid() &&
                    isEyeColorValid() &&
                    isPassportIdValid()

    private fun isBirthYearValid(): Boolean = dataMap[KEY_BIRTH_YEAR]?.toInt()?.let { byr: Int ->
        byr in validBirthYearRange
    } ?: false

    private fun isIssueYearValid(): Boolean = dataMap[KEY_ISSUE_YEAR]?.toInt()?.let { iyr: Int ->
        iyr in validIssueYearRange
    } ?: false

    private fun isExpirationYearValid(): Boolean = dataMap[KEY_EXPIRATION_YEAR]?.toInt()?.let { eyr: Int ->
        eyr in validExpirationYearRange
    } ?: false

    private fun isHeightValid(): Boolean = dataMap[KEY_HEIGHT]?.let { hgt: String ->
        if (hgt.endsWith(unitHeightCM)) {
            hgt.substringBefore(unitHeightCM).toInt() in validHeightRangeCM
        } else if (hgt.endsWith(unitHeightINCH)) {
            hgt.substringBefore(unitHeightINCH).toInt() in validHeightRangeINCH
        } else false
    } ?: false

    private fun isHairColorValid(): Boolean = dataMap[KEY_HAIR_COLOR]?.let { hcl: String ->
        validHairColorPattern matches hcl
    } ?: false

    private fun isEyeColorValid(): Boolean = dataMap[KEY_EYE_COLOR]?.let { ecl: String ->
        ecl in validEyeColors
    } ?: false

    private fun isPassportIdValid(): Boolean = dataMap[KEY_PASSPORT_ID]?.let { pid: String ->
        validPassportIdPattern matches pid
    } ?: false

    fun buildInfo(passportLineData: String) {
        passportLineDataPattern.findAll(passportLineData)
            .associateTo(dataMap) { it.groupValues[1] to it.groupValues[2] }
    }
}