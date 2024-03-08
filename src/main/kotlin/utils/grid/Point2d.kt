package utils.grid

/**
 * Class for a location in 2D coordinate grid system.
 *
 * Can be extended to suit the requirement of the problem.
 *
 * @param T [Comparable] and [Number] type
 * @property xPos Value of type [T] representing the location along X-plane.
 * @property yPos Value of type [T] representing the location along Y-plane.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */
open class Point2d<T>(val xPos: T, val yPos: T) where T : Comparable<T>, T : Number {

    fun toCoordinateList(): List<T> = listOf(xPos, yPos)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Point2d<*>) return false

        if (xPos != other.xPos) return false
        if (yPos != other.yPos) return false

        return true
    }

    override fun hashCode(): Int {
        var result = xPos.hashCode()
        result = 31 * result + yPos.hashCode()
        return result
    }

    override fun toString(): String = "($xPos, $yPos)"
}