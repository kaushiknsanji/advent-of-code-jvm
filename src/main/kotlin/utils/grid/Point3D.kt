package utils.grid

import utils.grid.Point3D.Companion.parse
import utils.grid.Point3D.Companion.parseAll


/**
 * Class for a location in 3D coordinate grid system.
 *
 * Can be extended to suit the requirement of the problem.
 *
 * @param T [Comparable] and [Number] type
 * @property xPos Value of type [T] representing the location along X-plane.
 * @property yPos Value of type [T] representing the location along Y-plane.
 * @property zPos Value of type [T] representing the location along Z-plane.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */
open class Point3D<T>(val xPos: T, val yPos: T, val zPos: T) where T : Comparable<T>, T : Number {

    companion object {

        /**
         * Generic parser with [factory].
         *
         * @param T [Comparable] and [Number] type of coordinate value
         * @param P type of [Point3D]
         * @param coordinates [List] of coordinate [T] values
         * @param factory Lambda to create the [Point3D] subclass
         *
         * @throws IllegalArgumentException when the number of provided [coordinates] are not 3.
         */
        fun <T, P : Point3D<T>> parse(
            coordinates: List<T>,
            factory: (x: T, y: T, z: T) -> P
        ): P where T : Comparable<T>, T : Number {
            require(coordinates.size == 3) {
                "${this::class.simpleName} requires exactly 3 coordinates, but got ${coordinates.size}"
            }

            return factory(
                coordinates[0],
                coordinates[1],
                coordinates[2]
            )
        }

        /**
         * Convenience overload for plain [Point3D] that calls [parse] with [Point3D] constructor.
         *
         * @param T [Comparable] and [Number] type of coordinate value
         * @param coordinates [List] of coordinate [T] values
         *
         * @throws IllegalArgumentException when the number of provided [coordinates] are not 3.
         */
        fun <T> parse(
            coordinates: List<T>
        ): Point3D<T> where T : Comparable<T>, T : Number = parse(coordinates, ::Point3D)

        /**
         * Generic Batch parser with [factory].
         *
         * @param T [Comparable] and [Number] type of coordinate value
         * @param P type of [Point3D]
         * @param coordinatesList Batch [List] of coordinate [T] values [List]
         * @param factory Lambda to create the [Point3D] subclass
         *
         * @throws IllegalArgumentException when the number of provided coordinate values in any
         * of the Batch [coordinatesList] items are not 3.
         */
        fun <T, P : Point3D<T>> parseAll(
            coordinatesList: List<List<T>>,
            factory: (x: T, y: T, z: T) -> P
        ): List<P> where T : Comparable<T>, T : Number = coordinatesList.map { coordinates: List<T> ->
            require(coordinates.size == 3) {
                "${this::class.simpleName} requires exactly 3 coordinates, but got ${coordinates.size}"
            }

            factory(
                coordinates[0],
                coordinates[1],
                coordinates[2]
            )
        }

        /**
         * Convenience overload for plain [Point3D] batch [List] that calls [parseAll] with [Point3D] constructor.
         *
         * @param T [Comparable] and [Number] type of coordinate value
         * @param coordinatesList Batch [List] of coordinate [T] values [List]
         *
         * @throws IllegalArgumentException when the number of provided coordinate values in any
         * of the Batch [coordinatesList] items are not 3.
         */
        fun <T> parseAll(
            coordinatesList: List<List<T>>
        ): List<Point3D<T>> where T : Comparable<T>, T : Number = parseAll(coordinatesList, ::Point3D)

    }

    /**
     * Returns [List] of coordinate [T] values
     */
    fun toCoordinateList(): List<T> = listOf(xPos, yPos, zPos)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Point3D<*>) return false

        if (xPos != other.xPos) return false
        if (yPos != other.yPos) return false
        if (zPos != other.zPos) return false

        return true
    }

    override fun hashCode(): Int {
        var result = xPos.hashCode()
        result = 31 * result + yPos.hashCode()
        result = 31 * result + zPos.hashCode()
        return result
    }

    override fun toString(): String = "($xPos, $yPos, $zPos)"

}