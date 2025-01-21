/**
 * Kotlin file for extensions on Collections.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package extensions

/**
 * Swaps value found at [index1] with value found at [index2] in [this] Array
 */
fun <T> Array<T>.swap(index1: Int, index2: Int) {
    val temp = this[index2]
    this[index2] = this[index1]
    this[index1] = temp
}

/**
 * Swaps value found at [index1] with value found at [index2] in [this] List
 */
fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val temp = this[index2]
    this[index2] = this[index1]
    this[index1] = temp
}

/**
 * Rotates [this] List by `this.size - 1` number of times and returns every rotation as a [List].
 *
 * The first [List] of the resulting [List] of rotations will be the same as [this].
 */
fun <T> List<T>.generateRotations(): List<List<T>> =
    this.indices.map { index ->
        this.drop(index) + this.take(index)
    }

/**
 * Returns the rotated [List] of [this] List for the given [nth rotation number][number].
 *
 * If the given [nth rotation number][number] is larger than [this.size], then
 * the rotation found at its modulo is returned.
 */
fun <T> List<T>.findRotation(number: Int): List<T> = this.generateRotations()[number % this.size]