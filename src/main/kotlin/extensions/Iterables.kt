package extensions

/**
 * Returns the product of all [Int] values in the [Iterable] entity.
 */
fun Iterable<Int>.product(): Int = reduce(Int::times)

/**
 * Returns the product of all [Long] values in the [Iterable] entity.
 */
fun Iterable<Long>.product(): Long = reduce(Long::times)

/**
 * Returns the difference of all [Int] values in the [Iterable] entity.
 */
fun Iterable<Int>.difference(): Int = reduce(Int::minus)

/**
 * Extension function on [Iterable] entity of type [T] to split the iterable collection
 * when determined by the [predicate]. If the split item needs to be retained, set [retainSplitItem] to `true`.
 *
 * @return [Iterable] of split [Iterable]s of type [T].
 */
fun <T> Iterable<T>.splitWhen(retainSplitItem: Boolean = false, predicate: (T) -> Boolean): Iterable<Iterable<T>> =
    this.fold(mutableListOf(mutableListOf())) { acc: MutableList<MutableList<T>>, item: T ->
        acc.apply {
            if (predicate(item)) {
                if (retainSplitItem) {
                    this.last().add(item)
                }
                this.add(mutableListOf())
            } else {
                this.last().add(item)
            }
        }
    }