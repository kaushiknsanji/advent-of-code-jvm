/**
 * Kotlin file for Enum classes and utilities on Directions used in coordinate grid traversal.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package utils.grid

enum class VerticalDirection {
    TOP, BOTTOM
}

enum class HorizontalDirection {
    RIGHT, LEFT
}

enum class TransverseDirection {
    TOP, BOTTOM, RIGHT, LEFT
}

enum class DiagonalDirection {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}

enum class OmniDirection {
    TOP, BOTTOM, RIGHT, LEFT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}

/**
 * Returns a [TransverseDirection] at 180-degree angle to [this].
 */
fun TransverseDirection.toHalfTurn(): TransverseDirection = when (this) {
    TransverseDirection.TOP -> TransverseDirection.BOTTOM
    TransverseDirection.BOTTOM -> TransverseDirection.TOP
    TransverseDirection.RIGHT -> TransverseDirection.LEFT
    TransverseDirection.LEFT -> TransverseDirection.RIGHT
}

/**
 * Returns a [TransverseDirection] at right angle with clockwise rotation to [this].
 */
fun TransverseDirection.toRightQuarterTurn(): TransverseDirection = when (this) {
    TransverseDirection.TOP -> TransverseDirection.RIGHT
    TransverseDirection.BOTTOM -> TransverseDirection.LEFT
    TransverseDirection.RIGHT -> TransverseDirection.BOTTOM
    TransverseDirection.LEFT -> TransverseDirection.TOP
}

/**
 * Returns a [TransverseDirection] at right angle with anticlockwise rotation to [this].
 */
fun TransverseDirection.toLeftQuarterTurn(): TransverseDirection = when (this) {
    TransverseDirection.TOP -> TransverseDirection.LEFT
    TransverseDirection.BOTTOM -> TransverseDirection.RIGHT
    TransverseDirection.RIGHT -> TransverseDirection.TOP
    TransverseDirection.LEFT -> TransverseDirection.BOTTOM
}

/**
 * Verifies whether [this] direction is in Quarter rotation to given [current direction][currentDirection].
 *
 * @return `true` when [this] and [currentDirection] are at right angles to each other; `false` otherwise.
 */
fun TransverseDirection.isQuarterTurnTo(currentDirection: TransverseDirection): Boolean =
    toLeftQuarterTurn() == currentDirection || toRightQuarterTurn() == currentDirection

/**
 * Verifies whether [this] direction is in Half rotation to given [current direction][currentDirection].
 *
 * @return `true` when [this] and [currentDirection] are at 180-degree angles to each other; `false` otherwise.
 */
fun TransverseDirection.isHalfTurnTo(currentDirection: TransverseDirection): Boolean =
    toHalfTurn() == currentDirection

/**
 * Converts [this] direction to its [character][Char] representation form.
 */
fun TransverseDirection.toDirectionalChar(): Char =
    when (this) {
        TransverseDirection.TOP -> '^'
        TransverseDirection.BOTTOM -> 'v'
        TransverseDirection.RIGHT -> '>'
        TransverseDirection.LEFT -> '<'
    }
