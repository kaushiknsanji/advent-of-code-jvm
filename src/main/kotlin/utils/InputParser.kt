/**
 * Kotlin file for utilities on parsing input.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package utils

import extensions.splitWhen

// Regular expression to capture numbers
private val numberRegex = """(-?\d+)""".toRegex()

// Regular expression to capture positive numbers only (including zero)
private val positiveNumberRegex = """(\d+)""".toRegex()

// Regular expression for one or more whitespaces
private val whitespacesRegex = """\s+""".toRegex()

/**
 * Splits [this] into [Iterable] of [Iterable] lines of [String] at all [lines][String]
 * that are either Blank or Empty
 */
fun List<String>.splitWhenLineBlankOrEmpty(): Iterable<Iterable<String>> =
    splitWhen { line: String -> line.isBlank() || line.isEmpty() }

/**
 * Extracts and returns a [List] of [Int] found in [this] input line
 */
fun String.findAllInt(): List<Int> =
    numberRegex.findAll(this).map { matchResult ->
        matchResult.groupValues[1].toInt()
    }.toList()

/**
 * Extracts and returns a [List] of Positive [Int] found in [this] input line
 */
fun String.findAllPositiveInt(): List<Int> =
    positiveNumberRegex.findAll(this).map { matchResult ->
        matchResult.groupValues[1].toInt()
    }.toList()

/**
 * Extracts and returns a [List] of [Long] found in [this] input line
 */
fun String.findAllLong(): List<Long> =
    numberRegex.findAll(this).map { matchResult ->
        matchResult.groupValues[1].toLong()
    }.toList()

/**
 * Extracts and returns a [List] of Positive [Long] found in [this] input line
 */
fun String.findAllPositiveLong(): List<Long> =
    positiveNumberRegex.findAll(this).map { matchResult ->
        matchResult.groupValues[1].toLong()
    }.toList()

/**
 * Splits [this] line by one or more whitespaces found and returns a [List] of split [String]s
 */
fun String.splitContentByWhitespaces(): List<String> = split(whitespacesRegex)