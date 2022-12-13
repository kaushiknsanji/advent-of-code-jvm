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

/**
 * Extension function on [Iterable] entity of type [T] to split the iterable collection
 * when determined by the [predicate]. If the split item needs to be retained, set [retainSplitItem] to `true`.
 *
 * @return [Collection] of split [Collection]s of type [T].
 */
fun <T> Iterable<T>.splitWhen(retainSplitItem: Boolean = false, predicate: (T) -> Boolean): Collection<Collection<T>> {
    val result = mutableListOf<MutableList<T>>()
    val appendItem: (T) -> Unit = { item ->
        try {
            result.last() += item
        } catch (e: NoSuchElementException) {
            result += mutableListOf(item)
        }
    }

    this.iterator().forEach { item: T ->
        if (predicate(item)) {
            if (retainSplitItem) {
                appendItem(item)
            }
            result += mutableListOf<T>()
        } else {
            appendItem(item)
        }
    }

    return result
}