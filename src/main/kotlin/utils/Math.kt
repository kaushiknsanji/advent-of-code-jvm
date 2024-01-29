package utils

import extensions.product

/**
 * Returns the [Int] result of the Greatest Common Divisor of [this] and [other] values.
 */
fun Int.gcd(other: Int): Int = computeGcd(this, other)

/**
 * Returns the [Long] result of the Greatest Common Divisor of [this] and [other] values.
 */
fun Long.gcd(other: Long): Long = computeGcd(this, other)

/**
 * Returns the [Int] result of the Least Common Multiple of [this] and [other] values.
 */
fun Int.lcm(other: Int): Int = computeLcm(this, other)

/**
 * Returns the [Long] result of the Least Common Multiple of [this] and [other] values.
 */
fun Long.lcm(other: Long): Long = computeLcm(this, other)

/**
 * Generates and returns an [IntArray] of Fibonacci series for the given [nth number][number].
 *
 * @throws IllegalArgumentException when [number] is negative.
 */
fun generateFibonacci(number: Int): IntArray = if (number < 0) {
    throw IllegalArgumentException(
        "(number=$number < 0)! Cannot find or generate Fibonacci of negative number."
    )
} else {
    IntArray(number + 1) { index ->
        if (index < 2) {
            // Fibonacci for 0 and 1 will be 0 and 1 respectively
            index
        } else {
            // Save -1 as a temporary Fibonacci value for indexes in the series yet to be computed
            -1
        }
    }.let { fibonacciArray: IntArray ->
        // Compute remaining values in the series when given nth number is greater than 1
        if (number > 1) {
            (2..number).forEach { index ->
                fibonacciArray[index] = fibonacciArray[index - 1] + fibonacciArray[index - 2]
            }
        }

        // Return the computed Fibonacci series
        fibonacciArray
    }
}

/**
 * Finds and returns the Fibonacci number for the given [nth number][number].
 */
fun findFibonacci(number: Int): Int = generateFibonacci(number)[number]

/**
 * Returns Factorial of the given [number].
 *
 * @throws IllegalArgumentException when [number] is negative.
 */
fun factorial(number: Int): Long = if (number < 0) {
    throw IllegalArgumentException("(number=$number < 0)! Cannot evaluate a Factorial of negative number.")
} else if (number == 0) {
    // Factorial of 0 is 1
    1L
} else {
    (1L..number).product()
}

/**
 * Returns total number of possible Permutations without repetition for [totalItems] to choose from
 * and [selectCount] of items chosen.
 *
 * @throws IllegalArgumentException when [selectCount] is greater than [totalItems].
 */
fun findTotalPermutationsWithoutRepetition(totalItems: Int, selectCount: Int): Long =
    if (selectCount > totalItems) {
        throw IllegalArgumentException(
            "(r=$selectCount > n=$totalItems)! Can only choose up to $totalItems items."
        )
    } else if (selectCount == totalItems) {
        // If all items are chosen, then the Factorial of total items gives the Permutations possible
        factorial(totalItems)
    } else {
        (totalItems.toLong() downTo (totalItems - selectCount + 1)).product()
    }

/**
 * Returns total number of possible Combinations without repetition for [totalItems] to choose from
 * and [selectCount] of items chosen.
 *
 * @throws IllegalArgumentException when [selectCount] is greater than [totalItems].
 */
fun findTotalCombinationsWithoutRepetition(totalItems: Int, selectCount: Int): Long =
    if (selectCount > totalItems) {
        throw IllegalArgumentException(
            "(r=$selectCount > n=$totalItems)! Can only choose up to $totalItems items."
        )
    } else if (selectCount == totalItems) {
        // If all items are chosen, then only 1 Combination is possible
        1L
    } else {
        findTotalPermutationsWithoutRepetition(totalItems, selectCount) / factorial(selectCount)
    }

/**
 * Function that computes and returns the [Int] result of the Greatest Common Divisor
 * of [number1] and [number2] values.
 */
private fun computeGcd(number1: Int, number2: Int): Int {
    var num1 = number1
    var num2 = number2

    while (num2 != 0) {
        val temp = num2
        num2 = num1.rem(num2)
        num1 = temp
    }

    return num1
}

/**
 * Function that computes and returns the [Long] result of the Greatest Common Divisor
 * of [number1] and [number2] values.
 */
private fun computeGcd(number1: Long, number2: Long): Long {
    var num1 = number1
    var num2 = number2

    while (num2 != 0L) {
        val temp = num2
        num2 = num1.rem(num2)
        num1 = temp
    }

    return num1
}

/**
 * Function that computes and returns the [Int] result of the Least Common Multiple of [number1] and [number2] values.
 * Least Common Multiple of two numbers is computed by using their Greatest Common Divisor.
 *
 * @throws ArithmeticException when there is [Int] overflow during multiplication.
 */
private fun computeLcm(number1: Int, number2: Int): Int =
    Math.multiplyExact(number1, number2) / computeGcd(number1, number2)

/**
 * Function that computes and returns the [Long] result of the Least Common Multiple of [number1] and [number2] values.
 * Least Common Multiple of two numbers is computed by using their Greatest Common Divisor.
 *
 * @throws ArithmeticException when there is [Long] overflow during multiplication.
 */
private fun computeLcm(number1: Long, number2: Long): Long =
    Math.multiplyExact(number1, number2) / computeGcd(number1, number2)
