package extensions

/**
 * Returns the product of all [Int] values in the [Iterable] entity.
 */
fun Iterable<Int>.product(): Int = reduce { acc: Int, next: Int -> acc * next }

/**
 * Returns the product of all [Long] values in the [Iterable] entity.
 */
fun Iterable<Long>.product(): Long = reduce { acc: Long, next: Long -> acc * next }

/**
 * Returns the difference of all [Int] values in the [Iterable] entity.
 */
fun Iterable<Int>.difference(): Int = reduce { acc: Int, next: Int -> acc - next }