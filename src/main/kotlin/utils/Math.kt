/**
 * Kotlin file for functions on general mathematical computations.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package utils

/**
 * Returns the product of all [Int] values in the [Iterable] entity.
 *
 * @throws ArithmeticException when there is [Int] overflow during multiplication.
 */
fun Iterable<Int>.product(): Int = reduce(Math::multiplyExact)

/**
 * Returns the product of all [Long] values in the [Iterable] entity.
 *
 * @throws ArithmeticException when there is [Long] overflow during multiplication.
 */
fun Iterable<Long>.product(): Long = reduce(Math::multiplyExact)

/**
 * Returns the difference of all [Int] values in the [Iterable] entity.
 */
fun Iterable<Int>.difference(): Int = reduce(Int::minus)

/**
 * Returns the difference of all [Long] values in the [Iterable] entity.
 */
fun Iterable<Long>.difference(): Long = reduce(Long::minus)

/**
 * Returns the Greatest Common Divisor of all [Int] values in the [Iterable] entity.
 */
fun Iterable<Int>.gcd(): Int = reduce(::computeGcd)

/**
 * Returns the Least Common Multiple of all [Int] values in the [Iterable] entity.
 */
fun Iterable<Int>.lcm(): Int = reduce(::computeLcm)

/**
 * Returns the Greatest Common Divisor of all [Long] values in the [Iterable] entity.
 */
fun Iterable<Long>.gcd(): Long = reduce(::computeGcd)

/**
 * Returns the Least Common Multiple of all [Long] values in the [Iterable] entity.
 */
fun Iterable<Long>.lcm(): Long = reduce(::computeLcm)

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
 * Extension function on a [Collection] of [Int] to test whether [this] series is Quadratic. Returns `true`
 * when Quadratic; `false` otherwise.
 *
 * @throws IllegalArgumentException when the size of [this] series is less than 4 numbers. Minimum of
 * 4 numbers are required.
 */
fun Collection<Int>.isQuadratic(): Boolean = if (size < 4) {
    throw IllegalArgumentException(
        "To determine if the given series is Quadratic, minimum of 4 numbers are required. " +
                "Found only $size."
    )
} else {
    generateSequence(this) { series: Collection<Int> ->
        // Along with the given series, every next series of numbers in the sequence will be their
        // previous series' differences of adjacent numbers
        series.zipWithNext { currentNumber: Int, nextNumber: Int ->
            nextNumber - currentNumber
        }.takeIf { it.isNotEmpty() } // Generate differences at every stage till we have no more numbers
    }.takeWhile { numbers: Collection<Int> ->
        // Take all series of numbers except the last series of 0s
        !numbers.all { it == 0 }
    }.count() == 3 // Total number of series generated excluding the last series of 0s should be 3 for Quadratic series
}

/**
 * Extension function on a [Collection] of [Int] to extract and return the coefficients and constant of [this]
 * Quadratic series. Returns the result as a [List] of [Int] starting with the leading coefficient and ending with
 * the constant.
 *
 * @throws IllegalArgumentException when the size of [this] series is less than 4 numbers, or when [this] series is
 * NOT Quadratic.
 */
fun Collection<Int>.extractQuadraticCoefficients(): List<Int> = if (isQuadratic()) {
    generateSequence(this) { series: Collection<Int> ->
        // Along with the given Quadratic series, every next series of numbers in the sequence will be their
        // previous series' differences of adjacent numbers
        series.zipWithNext { currentNumber: Int, nextNumber: Int ->
            nextNumber - currentNumber
        }.takeIf { it.isNotEmpty() } // Generate differences at every stage till we have no more numbers
    }.takeWhile { numbers: Collection<Int> ->
        // Take all series of numbers except the last series of 0s
        !numbers.all { it == 0 }
    }.map { numbers: Collection<Int> ->
        // Take only the first number of all 3 series of numbers present
        numbers.first()
    }.toList().reversed() // Reverse the first numbers
        .let { firstNumbers: List<Int> ->
            buildList {
                // Solve for coefficient 'a' and save it in the result list
                val aCoefficient = (firstNumbers[0] / 2).also(::add)

                // Solve for coefficient 'b' and save it in the result list
                val bCoefficient = (firstNumbers[1] - 3 * aCoefficient).also(::add)

                // Solve for constant 'c' and save it in the result list
                (firstNumbers[2] - aCoefficient - bCoefficient).also(::add)
            }
        }
} else {
    throw IllegalArgumentException(
        "Given series is not Quadratic. Hence, cannot extract coefficients."
    )
}

/**
 * Extension function on a [List] of [Int] coefficients and constant of a Quadratic series, to compute and return
 * the [Int] Quadratic Number of the series for the given ['nth' term number][termNumber].
 *
 * @param termNumber Lambda that returns an [Int] 'nth' term number.
 *
 * @throws IllegalArgumentException when the total number of coefficients along with constant given in [this] is NOT 3.
 */
fun List<Int>.findQuadraticNumber(termNumber: () -> Int): Int = findQuadraticNumber(termNumber())

/**
 * Extension function on a [List] of [Int] coefficients and constant of a Quadratic series, to compute and return
 * the [Int] Quadratic Number of the series for the given ['nth' term number][termNumber].
 *
 * @throws IllegalArgumentException when the total number of coefficients along with constant given in [this] is NOT 3.
 */
fun List<Int>.findQuadraticNumber(termNumber: Int): Int = if (this.size != 3) {
    throw IllegalArgumentException(
        "Requires 3 Coefficients to find the Quadratic Number for Term number : $termNumber. Found $size."
    )
} else {
    this[0] * termNumber * termNumber + this[1] * termNumber + this[2]
}

/**
 * Extension function on a [List] of [Long] coefficients and constant of a Quadratic series, to compute and return
 * the [Long] Quadratic Number of the series for the given ['nth' term number][termNumber].
 *
 * @param termNumber Lambda that returns an [Int] 'nth' term number.
 *
 * @throws IllegalArgumentException when the total number of coefficients along with constant given in [this] is NOT 3.
 */
fun List<Long>.findQuadraticNumber(termNumber: () -> Int): Long = findQuadraticNumber(termNumber())

/**
 * Extension function on a [List] of [Long] coefficients and constant of a Quadratic series, to compute and return
 * the [Long] Quadratic Number of the series for the given ['nth' term number][termNumber].
 *
 * @throws IllegalArgumentException when the total number of coefficients along with constant given in [this] is NOT 3.
 */
fun List<Long>.findQuadraticNumber(termNumber: Int): Long = if (this.size != 3) {
    throw IllegalArgumentException(
        "Requires 3 Coefficients to find the Quadratic Number for Term number : $termNumber. Found $size."
    )
} else {
    this[0] * termNumber * termNumber + this[1] * termNumber + this[2]
}