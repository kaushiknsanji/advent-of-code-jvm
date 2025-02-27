/**
 * Kotlin file for Enum classes and utilities on Directions used in coordinate grid traversal.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package utils.grid

import utils.Constants.CARET_CHAR
import utils.Constants.DOWN_CHAR
import utils.Constants.GREATER_CHAR
import utils.Constants.LEFT_CHAR
import utils.Constants.LESSER_CHAR
import utils.Constants.RIGHT_CHAR
import utils.Constants.UP_CHAR
import utils.Constants.V_SMALL_CHAR

/**
 * Enum class for Vertical Directions
 */
enum class VerticalDirection {
    TOP, BOTTOM
}

/**
 * Enum class for Horizontal Directions
 */
enum class HorizontalDirection {
    RIGHT, LEFT
}

/**
 * Enum class for Directions having both Vertical and Horizontal Directions, which are known as Cardinal Directions
 */
enum class CardinalDirection {
    TOP, BOTTOM, RIGHT, LEFT;

    /**
     * Returns a [CardinalDirection] at 180-degree angle to `this`.
     */
    fun toHalfTurn(): CardinalDirection = when (this) {
        TOP -> BOTTOM
        BOTTOM -> TOP
        RIGHT -> LEFT
        LEFT -> RIGHT
    }

    /**
     * Returns a [CardinalDirection] at right angle with clockwise rotation to `this`.
     */
    fun toRightQuarterTurn(): CardinalDirection = when (this) {
        TOP -> RIGHT
        BOTTOM -> LEFT
        RIGHT -> BOTTOM
        LEFT -> TOP
    }

    /**
     * Returns a [CardinalDirection] at right angle with anticlockwise rotation to `this`.
     */
    fun toLeftQuarterTurn(): CardinalDirection = when (this) {
        TOP -> LEFT
        BOTTOM -> RIGHT
        RIGHT -> TOP
        LEFT -> BOTTOM
    }

    /**
     * Verifies whether `this` direction is in Quarter rotation to given [other direction][otherDirection].
     *
     * @return `true` when `this` and [otherDirection] are at right angles to each other; `false` otherwise.
     */
    fun isQuarterTurnTo(otherDirection: CardinalDirection): Boolean =
        toLeftQuarterTurn() == otherDirection || toRightQuarterTurn() == otherDirection

    /**
     * Verifies whether `this` direction is in Half rotation to given [other direction][otherDirection].
     *
     * @return `true` when `this` and [otherDirection] are at 180-degree angles to each other; `false` otherwise.
     */
    fun isHalfTurnTo(otherDirection: CardinalDirection): Boolean =
        toHalfTurn() == otherDirection

    /**
     * Converts `this` direction to its [Directional character][Char] representation form.
     */
    fun toDirectionalChar(): Char =
        when (this) {
            TOP -> CARET_CHAR
            BOTTOM -> V_SMALL_CHAR
            RIGHT -> GREATER_CHAR
            LEFT -> LESSER_CHAR
        }
}

/**
 * Enum class for Directions in-between Cardinal Directions, which are known as Ordinal Directions
 */
enum class OrdinalDirection {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
}

/**
 * Enum class for Directions having both Cardinal and Ordinal Directions
 */
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
 * Converts [this] Character representation to [CardinalDirection]
 */
fun Char.toCardinalDirection(): CardinalDirection =
    when (this) {
        CARET_CHAR, UP_CHAR -> CardinalDirection.TOP
        V_SMALL_CHAR, DOWN_CHAR -> CardinalDirection.BOTTOM
        GREATER_CHAR, RIGHT_CHAR -> CardinalDirection.RIGHT
        LESSER_CHAR, LEFT_CHAR -> CardinalDirection.LEFT
        else -> throw Error("Unrecognized character : $this")
    }
