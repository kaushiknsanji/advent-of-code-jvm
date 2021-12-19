package utils.grid

open class Point2d<T>(val xPos: T, val yPos: T) where T : Comparable<T>, T : Number {

    fun toCoordinateList(): List<T> = listOf(xPos, yPos)

    override fun toString(): String = "($xPos, $yPos)"
}