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
 * Extension function on [Iterable] entity of type [T] to return a [Collection] of distinct [Pair]s
 * of the same elements of type [T].
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