/**
 * Kotlin file for Enum classes and utilities on Directions used in coordinate grid traversal.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package utils.grid

import utils.Constants.CARET_CHAR
import utils.Constants.GREATER_CHAR
import utils.Constants.LESSER_CHAR
import utils.Constants.V_SMALL_CHAR

enum class VerticalDirection {
    TOP, BOTTOM
}

enum class HorizontalDirection {
    RIGHT, LEFT
}

enum class TransverseDirection {
    TOP, BOTTOM, RIGHT, LEFT
}

enum class OrdinalDirection {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}

enum class OmniDirection {
    TOP, BOTTOM, RIGHT, LEFT, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;

    companion object {
        /**
         * Returns a [List] of Cardinal Directions only
         */
        fun getCardinalDirections(): List<OmniDirection> =
            listOf(TOP, BOTTOM, RIGHT, LEFT)

        /**
         * Returns a [List] of Ordinal Directions only
         */
        fun getOrdinalDirections(): List<OmniDirection> =
            listOf(TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT)
    }
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
        TransverseDirection.TOP -> CARET_CHAR
        TransverseDirection.BOTTOM -> V_SMALL_CHAR
        TransverseDirection.RIGHT -> GREATER_CHAR
        TransverseDirection.LEFT -> LESSER_CHAR
    }

/**
 * Converts [this] directional [Char] representation to [TransverseDirection].
 */
fun Char.toTransverseDirection(): TransverseDirection =
    when (this) {
        CARET_CHAR -> TransverseDirection.TOP
        V_SMALL_CHAR -> TransverseDirection.BOTTOM
        GREATER_CHAR -> TransverseDirection.RIGHT
        LESSER_CHAR -> TransverseDirection.LEFT
        else -> throw Error("Unrecognized Directional character : $this")
    }
