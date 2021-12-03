package extensions

/**
 * Inline function for While Loop directive.
 *
 * @param T [Comparable] and [Number] type to be used as Loop Counter.
 * @param R The type of result to be returned during and after 'while' loop completion.
 * @param loopStartCounter Value of type [T] that represents the start of Loop Counter.
 * @param exitCondition The lambda condition that decides when to exit the 'while' loop based
 * on the provided current Loop Counter of type [T] and/or the Last iteration result of type [R].
 * @param block The lambda action that will be executed inside the 'while' loop for every iteration based
 * on the provided current Loop Counter value of type [T]. Returns a [Pair] of Next Counter value of type [T]
 * and current iteration result of type nullable [R].
 * @return Returns a result of type [R].
 */
inline fun <T, R : Any> whileLoop(
    loopStartCounter: T,
    exitCondition: (loopCounter: T, lastIterationResult: R?) -> Boolean,
    block: (loopCounter: T) -> Pair<T, R?>
): R where T : Comparable<T>, T : Number {
    var counter: T = loopStartCounter
    var result: R? = null

    while (!exitCondition(counter, result)) {
        val (newIndex: T, newResult: R?) = block(counter)
        result = newResult
        counter = newIndex
    }

    return requireNotNull(result) {
        "While loop block did not yield any result!"
    }
}

/**
 * Inline function for While Loop directive.
 *
 * @param T [Comparable] and [Number] type to be used as Loop Counter.
 * @param R The type of result to be returned during and after 'while' loop completion.
 * @param loopStartCounter Value of type [T] that represents the start of Loop Counter.
 * @param initialResult Initial result of type [R] needed to compute the final result of 'while' loop operation.
 * @param exitCondition The lambda condition that decides when to exit the 'while' loop based
 * on the provided current Loop Counter of type [T] and/or the Last iteration result of type [R].
 * @param block The lambda action that will be executed inside the 'while' loop for every iteration based
 * on the provided current Loop Counter value of type [T] and Last iteration result of type [R]. Returns a [Pair] of
 * Next Counter value of type [T] and current iteration result of type nullable [R].
 * @return Returns a result of type [R].
 */
inline fun <T, R : Any> whileLoop(
    loopStartCounter: T,
    initialResult: R,
    exitCondition: (loopCounter: T, lastIterationResult: R?) -> Boolean,
    block: (loopCounter: T, lastIterationResult: R) -> Pair<T, R?>
): R where T : Comparable<T>, T : Number {
    var counter: T = loopStartCounter
    var result: R? = null

    while (!exitCondition(counter, result)) {
        val (newIndex: T, newResult: R?) = block(counter, result ?: initialResult)
        result = newResult
        counter = newIndex
    }

    return requireNotNull(result) {
        "While loop block did not yield any result!"
    }
}