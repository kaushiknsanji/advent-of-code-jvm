/**
 * Kotlin file for Enum classes on Directions used in coordinate grid traversal.
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