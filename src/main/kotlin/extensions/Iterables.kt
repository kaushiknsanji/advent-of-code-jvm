package extensions

import utils.gcd
import utils.lcm

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
 * Returns the difference of all [Long] values in the [Iterable] entity.
 */
fun Iterable<Long>.difference(): Long = reduce(Long::minus)

/**
 * Returns the Greatest Common Divisor of all [Int] values in the [Iterable] entity.
 */
fun Iterable<Int>.gcd(): Int = reduce(Int::gcd)

/**
 * Returns the Least Common Multiple of all [Int] values in the [Iterable] entity.
 */
fun Iterable<Int>.lcm(): Int = reduce(Int::lcm)

/**
 * Returns the Greatest Common Divisor of all [Long] values in the [Iterable] entity.
 */
fun Iterable<Long>.gcd(): Long = reduce(Long::gcd)

/**
 * Returns the Least Common Multiple of all [Long] values in the [Iterable] entity.
 */
fun Iterable<Long>.lcm(): Long = reduce(Long::lcm)

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