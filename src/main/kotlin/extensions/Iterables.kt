/**
 * Kotlin file for `Iterable` extensions.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package extensions

/**
 * Extension function on [Iterable] entity of type [T] to split the iterable collection
 * when determined by the [predicate]. If the split item needs to be retained, set [retainSplitItem] to `true`.
 *
 * @return [Iterable] of split [Iterable]s of type [T].
 */
inline fun <T> Iterable<T>.splitWhen(
    retainSplitItem: Boolean = false,
    predicate: (item: T) -> Boolean
): Iterable<Iterable<T>> =
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

/**
 * Extension function on [Iterable] entity of type [T] to return a [Collection] of Distinct [Pair]s
 * of elements of type [T].
 *
 * If there are `n` elements, then a total of `n*(n-1)/2` distinct element pairs are returned.
 */
fun <T> Iterable<T>.distinctPairs(): Collection<Pair<T, T>> =
    flatMapIndexed { outerIndex: Int, currentElement: T ->
        filterIndexed { innerIndex: Int, _: T ->
            innerIndex > outerIndex
        }.map { nextElement: T ->
            currentElement to nextElement
        }
    }

/**
 * Extension function on [Iterable] entity of type [T] to return a [Collection] of All [Pair]s
 * of elements of type [T].
 *
 * If there are `n` elements, then a total of `n * n` element pairs are returned.
 */
fun <T> Iterable<T>.allPairs(): Collection<Pair<T, T>> =
    flatMap { currentElement: T ->
        map { nextElement: T ->
            currentElement to nextElement
        }
    }

/**
 * Extension function on [Iterable] of [Iterable] elements of type [T] to return a [List] of
 * combination of elements.
 */
fun <T> Iterable<Iterable<T>>.generateCombinations(): List<List<T>> =
    fold(listOf(listOf())) { acc: List<List<T>>, iterable: Iterable<T> ->
        acc.flatMap { combination: List<T> ->
            iterable.map { item: T ->
                combination + item
            }
        }
    }

/**
 * Extension function on [Iterable] of [Iterable] elements of type [T] to return a [List] of
 * unique combination of elements.
 *
 * [List] returned may contain duplicate combinations but each element of any combination
 * is ensured to be distinct by matching the [combiningPredicate], resulting in a unique combination of elements.
 */
inline fun <T> Iterable<Iterable<T>>.generateCombinations(
    combiningPredicate: (combination: List<T>, item: T) -> Boolean
): List<List<T>> =
    fold(listOf(listOf())) { acc: List<List<T>>, iterable: Iterable<T> ->
        acc.flatMap { combination: List<T> ->
            iterable.filter { item: T ->
                combiningPredicate(combination, item)
            }.map { item: T ->
                combination + item
            }
        }
    }

/**
 * Extension function on [Iterable] of [Iterable] elements of type [T] to return a [List] of
 * combination of elements of type [R] which is obtained by applying [transformCombination] function
 * on each combination.
 */
inline fun <T, R> Iterable<Iterable<T>>.generateCombinations(
    transformCombination: (combination: List<T>) -> R
): List<R> =
    generateCombinations().map(transformCombination)

/**
 * Extension function on [Iterable] of [Iterable] elements of type [T] to return a [List] of
 * unique combination of elements of type [R] which is obtained by applying [transformCombination] function
 * on each combination.
 *
 * [List] returned may contain duplicate combinations but each element of any combination
 * is ensured to be distinct by matching the [combiningPredicate], resulting in a unique combination of elements.
 */
inline fun <T, R> Iterable<Iterable<T>>.generateCombinations(
    combiningPredicate: (combination: List<T>, item: T) -> Boolean,
    transformCombination: (combination: List<T>) -> R
): List<R> =
    generateCombinations(combiningPredicate).map(transformCombination)

/**
 * Extension function on [Map] to return a [Map] with [this] map's `keys` as `values`
 * and [this] map's `values` as `keys`.
 *
 * @throws IllegalStateException when [this] map's `values` contain duplicates.
 */
fun <K, V> Map<K, V>.flipMap(): Map<V, K> = if (values.size == values.toSet().size) {
    this.map { (key: K, value: V) ->
        value to key
    }.toMap()
} else {
    throw IllegalStateException(
        "Map cannot be flipped since its 'values' are not distinct to be 'keys' when flipped"
    )
}
