package extensions

inline fun <T, R : Any> whileLoop(
    loopCounter: T,
    exitCondition: (loopCounter: T, lastIterationResult: R?) -> Boolean,
    block: (loopCounter: T) -> Pair<T, R?>
): R where T : Comparable<T>, T : Number {
    var counter: T = loopCounter
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