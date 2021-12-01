package extensions

/**
 * Returns the product of all the integers in the Iterable entity.
 */
fun Iterable<Int>.product(): Int = reduce { acc: Int, next: Int -> acc * next }

fun Iterable<Long>.product(): Long = reduce { acc: Long, next: Long -> acc * next }