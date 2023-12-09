package utils

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
