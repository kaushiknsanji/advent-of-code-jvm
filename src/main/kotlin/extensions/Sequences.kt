/**
 * Kotlin file for `Sequence` extensions.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package extensions

/**
 * Returns a [Sequence] that yields elements of type [T] of [this] sequence in reverse order.
 *
 * The operation is _intermediate_ and _stateful_.
 */
fun <T> Sequence<T>.reversed(): Sequence<T> {
    return object : Sequence<T> {
        override fun iterator(): Iterator<T> {
            val reversedList = this@reversed.toMutableList().apply { reverse() }
            return reversedList.iterator()
        }
    }
}