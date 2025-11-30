/**
 * Kotlin file for working with Two Dimensional Grid Graphs.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package utils.grid

import utils.Constants.EMPTY

/**
 * Interface for Two Dimensional Grid Graph to initialize and get started with.
 *
 * @param P type of [Point2D] representing locations in the Grid.
 * @param V type of value stored in all locations of the Grid.
 */
interface IGrid2DGraph<P : Point2D<Int>, V> {
    /**
     * Returns location if present at given [row] and [column] in the grid; otherwise `null`.
     *
     * When location is not found in the grid, it will call [getExpandedLocationOrNull] to retrieve location
     * from the Expanded grid if the grid is expandable; otherwise `null`. Implement [getExpandedLocationOrNull]
     * when the grid is expandable.
     *
     * @see getExpandedLocationOrNull
     */
    fun getLocationOrNull(row: Int, column: Int): P?

    /**
     * Returns location if present at given [row] and [column] in the Expanded grid; otherwise `null`.
     *
     * Called by [getLocationOrNull] when location is not found in the original grid.
     *
     * Default implementation returns `null`. When implemented, this function must call [addLocation]
     * to save new location in the Expanded grid, and then must call `set(location, value)` operator to save the
     * computed value at this new location. If saving new location and computed value is costly, then
     * expansion can be implemented without saving by calling `provideLocation(row, column)` to return
     * new location and overriding `get(location)` operator to return computed value for this new location.
     * A hybrid approach can also be followed by saving only the required new location and its computed value and
     * overriding `get(location)` operator to return default value for those unsaved locations.
     */
    fun getExpandedLocationOrNull(row: Int, column: Int): P? = null

    /**
     * Returns location for given [row] and [column] in the grid
     *
     * @throws IllegalArgumentException when a location for given [row] and [column] does not exist in the grid.
     */
    fun getLocation(row: Int, column: Int): P

    /**
     * Adds location defined by given [row] and [column] to the grid, and then returns the location added.
     *
     * If the location already exists, then the location found will be returned.
     */
    fun addLocation(row: Int, column: Int): P

    /**
     * Adds given [location][newLocation] to the grid, and then returns the same [location][newLocation].
     *
     * If the [location][newLocation] already exists, then no update will be done
     * and the same [location][newLocation] will be returned.
     */
    fun addLocation(newLocation: P): P

    /**
     * Returns all locations in the grid as [Collection]
     */
    fun getAllLocations(): Collection<P>

    /**
     * Returns all locations found at the given [row] in the grid.
     *
     * If the given [row] does not exist, then the [Collection] returned will be empty.
     */
    fun getRowLocations(row: Int): Collection<P>

    /**
     * Returns value found at the given [location] if present in the grid; otherwise
     * returns the provided [defaultValue].
     */
    fun getOrDefault(location: P, defaultValue: V): V

    /**
     * Returns value found at [this] location in the grid
     */
    fun P.toValue(): V

    /**
     * Swaps value found at [location1] with value found at [location2] in the grid
     */
    fun swap(location1: P, location2: P)

    /**
     * Returns total number of rows found in the Expanded grid
     */
    fun getExpandedTotalRows(): Int

    /**
     * Returns total number of columns found in the Expanded grid
     */
    fun getExpandedTotalColumns(): Int

    /**
     * Returns this Grid Graph with current values as a [String]
     *
     * @param transform Lambda to transform value stored in type [V] to [Char]
     */
    fun gridToString(transform: (V) -> Char): String

    /**
     * Returns this Expanded Grid Graph with current values as a [String]
     *
     * @param defaultValue Default [Char] value to be used for locations not yet added to the Expanded grid
     * @param transform Lambda to transform value stored in type [V] to [Char]
     */
    fun expandedGridToString(defaultValue: Char, transform: (V) -> Char): String

    /**
     * Returns this Expanded Grid Graph with current values as a [String]
     *
     * @param defaultValue Lambda to provide default [Char] value to be used for locations not yet added
     * to the Expanded grid.
     * @param transform Lambda to transform value stored in type [V] to [Char]
     */
    fun expandedGridToString(defaultValue: (row: Int, column: Int) -> Char, transform: (V) -> Char): String
}

/**
 * Interface for Two Dimensional Lattice-4 to facilitate traversing the Grid in Cardinal directions.
 *
 * @param P type of [Point2D] representing locations in the Grid.
 * @param V type of value stored in all locations of the Grid.
 */
interface ILattice<P : Point2D<Int>, V> : IGrid2DGraph<P, V> {
    /**
     * Returns neighbouring location of [this] location in the given [direction] if present; otherwise `null`
     */
    fun P.getNeighbourOrNull(direction: CardinalDirection): P?

    /**
     * Returns a [Collection] of neighbouring locations of [this] location found in all [CardinalDirection]s possible
     */
    fun P.getAllNeighbours(): Collection<P>

    /**
     * Returns a [Map] of neighbouring locations of [this] location found in all [CardinalDirection]s possible.
     * [CardinalDirection]s will be the [Map.keys] of the [Map] returned.
     */
    fun P.getAllNeighboursWithDirection(): Map<CardinalDirection, P>

    /**
     * Returns [CardinalDirection] of travel from [this] location to the [next location][nextLocation].
     *
     * Can return `null` if [nextLocation] is NOT one of the neighbouring locations of [this] location.
     */
    fun P.getDirectionToNeighbourOrNull(nextLocation: P): CardinalDirection?

    /**
     * Returns a [Sequence] of all locations found from [this] location in the given [direction]
     */
    fun P.getLocationsInDirection(direction: CardinalDirection): Sequence<P>

    /**
     * Returns a [Map] of all locations found from [this] location in every possible [CardinalDirection].
     * [CardinalDirection]s will be the [Map.keys] of the [Map] returned.
     */
    fun P.getLocationsInAllDirections(): Map<CardinalDirection, Sequence<P>>
}

/**
 * Interface for Two Dimensional Diagonal Lattice to facilitate traversing the Grid in Ordinal directions only.
 *
 * @param P type of [Point2D] representing locations in the Grid.
 * @param V type of value stored in all locations of the Grid.
 */
interface IDiagonalLattice<P : Point2D<Int>, V> : IGrid2DGraph<P, V> {
    /**
     * Returns neighbouring location of [this] location in the given [direction] if present; otherwise `null`
     */
    fun P.getNeighbourOrNull(direction: OrdinalDirection): P?

    /**
     * Returns a [Collection] of neighbouring locations of [this] location found in all [OrdinalDirection]s possible
     */
    fun P.getAllNeighbours(): Collection<P>

    /**
     * Returns a [Map] of neighbouring locations of [this] location found in all [OrdinalDirection]s possible.
     * [OrdinalDirection]s will be the [Map.keys] of the [Map] returned.
     */
    fun P.getAllNeighboursWithDirection(): Map<OrdinalDirection, P>

    /**
     * Returns [OrdinalDirection] of travel from [this] location to the [next location][nextLocation].
     *
     * Can return `null` if [nextLocation] is NOT one of the diagonal neighbouring locations of [this] location.
     */
    fun P.getDirectionToNeighbourOrNull(nextLocation: P): OrdinalDirection?

    /**
     * Returns a [Sequence] of all locations found from [this] location in the given [direction]
     */
    fun P.getLocationsInDirection(direction: OrdinalDirection): Sequence<P>

    /**
     * Returns a [Map] of all locations found from [this] location in every possible [OrdinalDirection].
     * [OrdinalDirection]s will be the [Map.keys] of the [Map] returned.
     */
    fun P.getLocationsInAllDirections(): Map<OrdinalDirection, Sequence<P>>
}

/**
 * Interface for Two Dimensional Lattice-8 to facilitate traversing the Grid in both
 * Cardinal and Ordinal directions.
 *
 * @param P type of [Point2D] representing locations in the Grid.
 * @param V type of value stored in all locations of the Grid.
 */
interface IOmniLattice<P : Point2D<Int>, V> : IGrid2DGraph<P, V> {
    /**
     * Returns neighbouring location of [this] location in the given [direction] if present; otherwise `null`
     */
    fun P.getNeighbourOrNull(direction: OmniDirection): P?

    /**
     * Returns a [Collection] of neighbouring locations of [this] location found in all [OmniDirection]s possible
     */
    fun P.getAllNeighbours(): Collection<P>

    /**
     * Returns a [Collection] of neighbouring locations of [this] location found only in all
     * [Cardinal directions][OmniDirection.getCardinalDirections] possible
     */
    fun P.getCardinalNeighbours(): Collection<P>

    /**
     * Returns a [Collection] of neighbouring locations of [this] location found only in all
     * [Ordinal directions][OmniDirection.getOrdinalDirections] possible
     */
    fun P.getOrdinalNeighbours(): Collection<P>

    /**
     * Returns a [Map] of neighbouring locations of [this] location found in all [OmniDirection]s possible.
     * [OmniDirection]s will be the [Map.keys] of the [Map] returned.
     */
    fun P.getAllNeighboursWithDirection(): Map<OmniDirection, P>

    /**
     * Returns a [Map] of neighbouring locations of [this] location found only in all
     * [Cardinal directions][OmniDirection.getCardinalDirections] possible.
     * [OmniDirection]s will be the [Map.keys] of the [Map] returned.
     */
    fun P.getCardinalNeighboursWithDirection(): Map<OmniDirection, P>

    /**
     * Returns a [Map] of neighbouring locations of [this] location found only in all
     * [Ordinal directions][OmniDirection.getOrdinalDirections] possible.
     * [OmniDirection]s will be the [Map.keys] of the [Map] returned.
     */
    fun P.getOrdinalNeighboursWithDirection(): Map<OmniDirection, P>

    /**
     * Returns [OmniDirection] of travel from [this] location to the [next location][nextLocation].
     *
     * Can return `null` if [nextLocation] is NOT one of the neighbouring locations of [this] location.
     */
    fun P.getDirectionToNeighbourOrNull(nextLocation: P): OmniDirection?

    /**
     * Returns a [Sequence] of all locations found from [this] location in the given [direction]
     */
    fun P.getLocationsInDirection(direction: OmniDirection): Sequence<P>

    /**
     * Returns a [Map] of all locations found from [this] location in every possible [OmniDirection].
     * [OmniDirection]s will be the [Map.keys] of the [Map] returned.
     */
    fun P.getLocationsInAllDirections(): Map<OmniDirection, Sequence<P>>
}

/**
 * Abstract class for Two Dimensional Grid Graph to initialize and get started with.
 *
 * @param P type of [Point2D] representing locations in the Grid.
 * @param V type of value stored in all locations of the Grid.
 * @property rows [Int] number of rows in the Grid.
 * @property columns [Int] number of columns in the Grid.
 * @param pattern [List] of Strings having the input pattern for Two Dimensional Grid construction.
 *
 * @constructor Constructs [Grid2DGraph] using the provided grid `pattern` input.
 */
abstract class Grid2DGraph<P : Point2D<Int>, V> private constructor(
    val rows: Int,
    val columns: Int,
    pattern: List<String>
) : IGrid2DGraph<P, V> {

    constructor(pattern: List<String>) : this(
        pattern.size,
        pattern[0].length,
        pattern
    )

    // Map of Locations for each row index as key
    private val gridLocationMap: MutableMap<Int, MutableList<P>> = (0 until rows).associateWith { x ->
        (0 until columns).map { y ->
            provideLocation(x, y)
        }.toMutableList()
    }.toMutableMap()

    // Map of Values for each Location as key
    private val gridValueMap: MutableMap<P, V> = pattern.flatMapIndexed { x: Int, rowPattern: String ->
        rowPattern.mapIndexed { y: Int, locationChar: Char ->
            getLocation(x, y) to provideValue(locationChar)
        }
    }.toMap().toMutableMap()

    /**
     * Returns location if present at given [row] and [column] in the grid; otherwise `null`.
     *
     * When location is not found in the grid, it will call [getExpandedLocationOrNull] to retrieve location
     * from the Expanded grid if the grid is expandable; otherwise `null`. Implement [getExpandedLocationOrNull]
     * when the grid is expandable.
     *
     * @see getExpandedLocationOrNull
     */
    final override fun getLocationOrNull(row: Int, column: Int): P? = try {
        if (!gridLocationMap.containsKey(row)) {
            throw NoSuchElementException()
        } else {
            gridLocationMap[row]!!.single { location: P -> location.yPos == column }
        }
    } catch (e: NoSuchElementException) {
        getExpandedLocationOrNull(row, column)
    }

    /**
     * Returns location for given [row] and [column] in the grid
     *
     * @throws IllegalArgumentException when a location for given [row] and [column] does not exist in the grid.
     */
    override fun getLocation(row: Int, column: Int): P =
        getLocationOrNull(row, column) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${Point2D::class.simpleName} at the given location ($row, $column)"
        )

    /**
     * Adds location defined by given [row] and [column] to the grid, and then returns the location added.
     *
     * If the location already exists, then the location found will be returned.
     */
    override fun addLocation(row: Int, column: Int): P {
        val rowLocations = gridLocationMap.getOrPut(row) { mutableListOf() }

        return rowLocations.find { location: P ->
            location.yPos == column
        } ?: provideLocation(row, column).apply {
            rowLocations.add(this)
        }
    }

    /**
     * Adds given [location][newLocation] to the grid, and then returns the same [location][newLocation].
     *
     * If the [location][newLocation] already exists, then no update will be done
     * and the same [location][newLocation] will be returned.
     */
    override fun addLocation(newLocation: P): P {
        val rowLocations = gridLocationMap.getOrPut(newLocation.xPos) { mutableListOf() }

        return rowLocations.find { location: P ->
            location.yPos == newLocation.yPos
        } ?: newLocation.apply {
            rowLocations.add(this)
        }
    }

    /**
     * Returns all locations in the grid as [Collection]
     */
    override fun getAllLocations(): Collection<P> =
        gridLocationMap.values.flatten()

    /**
     * Returns all locations found at the given [row] in the grid.
     *
     * If the given [row] does not exist, then the [Collection] returned will be empty.
     */
    override fun getRowLocations(row: Int): Collection<P> =
        gridLocationMap.getOrElse(row) { emptyList() }

    /**
     * Returns value present in the grid at given [location]
     */
    open operator fun get(location: P): V = gridValueMap[location]!!

    /**
     * Updates value at the given [location] in the grid to new [value]
     */
    open operator fun set(location: P, value: V) {
        gridValueMap[location] = value
    }

    /**
     * Returns value found at the given [location] if present in the grid; otherwise
     * returns the provided [defaultValue].
     */
    override fun getOrDefault(location: P, defaultValue: V): V =
        get(location) ?: defaultValue

    /**
     * Returns value found at [this] location in the grid
     */
    override fun P.toValue(): V = get(this)

    /**
     * Swaps value found at [location1] with value found at [location2] in the grid
     */
    override fun swap(location1: P, location2: P) {
        val temp = location2.toValue()
        gridValueMap[location2] = location1.toValue()
        gridValueMap[location1] = temp
    }

    /**
     * Returns total number of rows found in the Expanded grid
     */
    override fun getExpandedTotalRows(): Int =
        with(getAllLocations()) {
            maxOf { location: P -> location.xPos } - minOf { location: P -> location.xPos } + 1
        }


    /**
     * Returns total number of columns found in the Expanded grid
     */
    override fun getExpandedTotalColumns(): Int =
        with(getAllLocations()) {
            maxOf { location: P -> location.yPos } - minOf { location: P -> location.yPos } + 1
        }

    /**
     * Returns this Grid Graph with current values as a [String]
     *
     * @param transform Lambda to transform value stored in type [V] to [Char]
     */
    override fun gridToString(transform: (V) -> Char): String =
        gridLocationMap.entries.joinToString(System.lineSeparator()) { (_: Int, rowLocations: List<P>) ->
            rowLocations.map { location: P ->
                transform(location.toValue())
            }.joinToString(EMPTY)
        }

    /**
     * Returns this Expanded Grid Graph with current values as a [String]
     *
     * @param defaultValue Default [Char] value to be used for locations not yet added to the Expanded grid
     * @param transform Lambda to transform value stored in type [V] to [Char]
     */
    override fun expandedGridToString(defaultValue: Char, transform: (V) -> Char): String =
        with(getAllLocations()) {
            val maxRow = maxOf { location: P -> location.xPos }
            val minRow = minOf { location: P -> location.xPos }
            val maxColumn = maxOf { location: P -> location.yPos }
            val minColumn = minOf { location: P -> location.yPos }

            (minRow..maxRow).joinToString(System.lineSeparator()) { row ->
                (minColumn..maxColumn).map { column ->
                    gridLocationMap[row]?.find { it.yPos == column }?.toValue()?.let(transform)
                        ?: defaultValue
                }.joinToString(EMPTY)
            }
        }

    /**
     * Returns this Expanded Grid Graph with current values as a [String]
     *
     * @param defaultValue Lambda to provide default [Char] value to be used for locations not yet added
     * to the Expanded grid.
     * @param transform Lambda to transform value stored in type [V] to [Char]
     */
    override fun expandedGridToString(
        defaultValue: (row: Int, column: Int) -> Char,
        transform: (V) -> Char
    ): String =
        with(getAllLocations()) {
            val maxRow = maxOf { location: P -> location.xPos }
            val minRow = minOf { location: P -> location.xPos }
            val maxColumn = maxOf { location: P -> location.yPos }
            val minColumn = minOf { location: P -> location.yPos }

            (minRow..maxRow).joinToString(System.lineSeparator()) { row ->
                (minColumn..maxColumn).map { column ->
                    gridLocationMap[row]?.find { it.yPos == column }?.toValue()?.let(transform)
                        ?: defaultValue(row, column)
                }.joinToString(EMPTY)
            }
        }

    /**
     * Returns location to be used in the grid.
     *
     * @param row [Int] value of location's row
     * @param column [Int] value of location's column
     */
    protected abstract fun provideLocation(row: Int, column: Int): P

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar [Char] found at a location in the input pattern
     */
    protected abstract fun provideValue(locationChar: Char): V

}

/**
 * Abstract Lattice-4 class to facilitate traversing the Grid in Cardinal directions.
 *
 * @param P type of [Point2D] representing locations in the Grid.
 * @param V type of value stored in all locations of the Grid.
 * @param pattern [List] of Strings having the input pattern for Two Dimensional Grid construction.
 *
 * @constructor Constructs [Lattice] using the provided grid `pattern` input.
 */
abstract class Lattice<P : Point2D<Int>, V>(
    pattern: List<String>
) : ILattice<P, V>, Grid2DGraph<P, V>(pattern) {

    /**
     * Returns neighbouring location of [this] location in the given [direction] if present; otherwise `null`
     */
    override fun P.getNeighbourOrNull(direction: CardinalDirection): P? = when (direction) {
        CardinalDirection.TOP -> getLocationOrNull(xPos - 1, yPos)
        CardinalDirection.BOTTOM -> getLocationOrNull(xPos + 1, yPos)
        CardinalDirection.RIGHT -> getLocationOrNull(xPos, yPos + 1)
        CardinalDirection.LEFT -> getLocationOrNull(xPos, yPos - 1)
    }

    /**
     * Returns a [Collection] of neighbouring locations of [this] location found in all [CardinalDirection]s possible
     */
    override fun P.getAllNeighbours(): Collection<P> =
        CardinalDirection.entries.mapNotNull { direction: CardinalDirection -> getNeighbourOrNull(direction) }

    /**
     * Returns a [Map] of neighbouring locations of [this] location found in all [CardinalDirection]s possible.
     * [CardinalDirection]s will be the [Map.keys] of the [Map] returned.
     */
    override fun P.getAllNeighboursWithDirection(): Map<CardinalDirection, P> =
        CardinalDirection.entries.filterNot { direction: CardinalDirection ->
            getNeighbourOrNull(direction) == null
        }.associateWith { direction: CardinalDirection ->
            getNeighbourOrNull(direction)!!
        }

    /**
     * Returns [CardinalDirection] of travel from [this] location to the [next location][nextLocation].
     *
     * Can return `null` if [nextLocation] is NOT one of the neighbouring locations of [this] location.
     */
    override fun P.getDirectionToNeighbourOrNull(nextLocation: P): CardinalDirection? =
        getAllNeighboursWithDirection().filterValues { location: P ->
            location == nextLocation
        }.keys.singleOrNull()

    /**
     * Returns a [Sequence] of all locations found from [this] location in the given [direction]
     */
    override fun P.getLocationsInDirection(direction: CardinalDirection): Sequence<P> =
        generateSequence(this) { previousLocation: P ->
            previousLocation.getNeighbourOrNull(direction)
        }

    /**
     * Returns a [Map] of all locations found from [this] location in every possible [CardinalDirection].
     * [CardinalDirection]s will be the [Map.keys] of the [Map] returned.
     */
    override fun P.getLocationsInAllDirections(): Map<CardinalDirection, Sequence<P>> =
        CardinalDirection.entries.associateWith { direction: CardinalDirection ->
            getLocationsInDirection(direction)
        }

}

/**
 * Abstract Diagonal Lattice class to facilitate traversing the Grid in Ordinal directions only.
 *
 * @param P type of [Point2D] representing locations in the Grid.
 * @param V type of value stored in all locations of the Grid.
 * @param pattern [List] of Strings having the input pattern for Two Dimensional Grid construction.
 *
 * @constructor Constructs [DiagonalLattice] using the provided grid `pattern` input.
 */
abstract class DiagonalLattice<P : Point2D<Int>, V>(
    pattern: List<String>
) : IDiagonalLattice<P, V>, Grid2DGraph<P, V>(pattern) {

    /**
     * Returns neighbouring location of [this] location in the given [direction] if present; otherwise `null`
     */
    override fun P.getNeighbourOrNull(direction: OrdinalDirection): P? = when (direction) {
        OrdinalDirection.TOP_LEFT -> getLocationOrNull(xPos - 1, yPos - 1)
        OrdinalDirection.TOP_RIGHT -> getLocationOrNull(xPos - 1, yPos + 1)
        OrdinalDirection.BOTTOM_LEFT -> getLocationOrNull(xPos + 1, yPos - 1)
        OrdinalDirection.BOTTOM_RIGHT -> getLocationOrNull(xPos + 1, yPos + 1)
    }

    /**
     * Returns a [Collection] of neighbouring locations of [this] location found in all [OrdinalDirection]s possible
     */
    override fun P.getAllNeighbours(): Collection<P> =
        OrdinalDirection.entries.mapNotNull { direction: OrdinalDirection ->
            getNeighbourOrNull(direction)
        }

    /**
     * Returns a [Map] of neighbouring locations of [this] location found in all [OrdinalDirection]s possible.
     * [OrdinalDirection]s will be the [Map.keys] of the [Map] returned.
     */
    override fun P.getAllNeighboursWithDirection(): Map<OrdinalDirection, P> =
        OrdinalDirection.entries.filterNot { direction: OrdinalDirection ->
            getNeighbourOrNull(direction) == null
        }.associateWith { direction: OrdinalDirection ->
            getNeighbourOrNull(direction)!!
        }

    /**
     * Returns [OrdinalDirection] of travel from [this] location to the [next location][nextLocation].
     *
     * Can return `null` if [nextLocation] is NOT one of the diagonal neighbouring locations of [this] location.
     */
    override fun P.getDirectionToNeighbourOrNull(nextLocation: P): OrdinalDirection? =
        getAllNeighboursWithDirection().filterValues { location: P ->
            location == nextLocation
        }.keys.singleOrNull()

    /**
     * Returns a [Sequence] of all locations found from [this] location in the given [direction]
     */
    override fun P.getLocationsInDirection(direction: OrdinalDirection): Sequence<P> =
        generateSequence(this) { previousLocation: P ->
            previousLocation.getNeighbourOrNull(direction)
        }

    /**
     * Returns a [Map] of all locations found from [this] location in every possible [OrdinalDirection].
     * [OrdinalDirection]s will be the [Map.keys] of the [Map] returned.
     */
    override fun P.getLocationsInAllDirections(): Map<OrdinalDirection, Sequence<P>> =
        OrdinalDirection.entries.associateWith { direction: OrdinalDirection ->
            getLocationsInDirection(direction)
        }

}

/**
 * Abstract Lattice-8 class to facilitate traversing the Grid in both
 * Cardinal and Ordinal directions.
 *
 * @param P type of [Point2D] representing locations in the Grid.
 * @param V type of value stored in all locations of the Grid.
 * @param pattern [List] of Strings having the input pattern for Two Dimensional Grid construction.
 *
 * @constructor Constructs [OmniLattice] using the provided grid `pattern` input.
 */
abstract class OmniLattice<P : Point2D<Int>, V>(
    pattern: List<String>
) : IOmniLattice<P, V>, Grid2DGraph<P, V>(pattern) {

    /**
     * Returns neighbouring location of [this] location in the given [direction] if present; otherwise `null`
     */
    override fun P.getNeighbourOrNull(direction: OmniDirection): P? = when (direction) {
        OmniDirection.TOP -> getLocationOrNull(xPos - 1, yPos)
        OmniDirection.BOTTOM -> getLocationOrNull(xPos + 1, yPos)
        OmniDirection.RIGHT -> getLocationOrNull(xPos, yPos + 1)
        OmniDirection.LEFT -> getLocationOrNull(xPos, yPos - 1)
        OmniDirection.TOP_LEFT -> getLocationOrNull(xPos - 1, yPos - 1)
        OmniDirection.TOP_RIGHT -> getLocationOrNull(xPos - 1, yPos + 1)
        OmniDirection.BOTTOM_LEFT -> getLocationOrNull(xPos + 1, yPos - 1)
        OmniDirection.BOTTOM_RIGHT -> getLocationOrNull(xPos + 1, yPos + 1)
    }

    /**
     * Returns a [Collection] of neighbouring locations of [this] location found in all [OmniDirection]s possible
     */
    override fun P.getAllNeighbours(): Collection<P> =
        OmniDirection.entries.mapNotNull { direction: OmniDirection ->
            getNeighbourOrNull(direction)
        }

    /**
     * Returns a [Collection] of neighbouring locations of [this] location found only in all
     * [Cardinal directions][OmniDirection.getCardinalDirections] possible
     */
    override fun P.getCardinalNeighbours(): Collection<P> =
        OmniDirection.getCardinalDirections().mapNotNull { direction: OmniDirection ->
            getNeighbourOrNull(direction)
        }

    /**
     * Returns a [Collection] of neighbouring locations of [this] location found only in all
     * [Ordinal directions][OmniDirection.getOrdinalDirections] possible
     */
    override fun P.getOrdinalNeighbours(): Collection<P> =
        OmniDirection.getOrdinalDirections().mapNotNull { direction: OmniDirection ->
            getNeighbourOrNull(direction)
        }

    /**
     * Returns a [Map] of neighbouring locations of [this] location found in all [OmniDirection]s possible.
     * [OmniDirection]s will be the [Map.keys] of the [Map] returned.
     */
    override fun P.getAllNeighboursWithDirection(): Map<OmniDirection, P> =
        OmniDirection.entries.filterNot { direction: OmniDirection ->
            getNeighbourOrNull(direction) == null
        }.associateWith { direction: OmniDirection ->
            getNeighbourOrNull(direction)!!
        }

    /**
     * Returns a [Map] of neighbouring locations of [this] location found only in all
     * [Cardinal directions][OmniDirection.getCardinalDirections] possible.
     * [OmniDirection]s will be the [Map.keys] of the [Map] returned.
     */
    override fun P.getCardinalNeighboursWithDirection(): Map<OmniDirection, P> =
        OmniDirection.getCardinalDirections().filterNot { direction: OmniDirection ->
            getNeighbourOrNull(direction) == null
        }.associateWith { direction: OmniDirection ->
            getNeighbourOrNull(direction)!!
        }

    /**
     * Returns a [Map] of neighbouring locations of [this] location found only in all
     * [Ordinal directions][OmniDirection.getOrdinalDirections] possible.
     * [OmniDirection]s will be the [Map.keys] of the [Map] returned.
     */
    override fun P.getOrdinalNeighboursWithDirection(): Map<OmniDirection, P> =
        OmniDirection.getOrdinalDirections().filterNot { direction: OmniDirection ->
            getNeighbourOrNull(direction) == null
        }.associateWith { direction: OmniDirection ->
            getNeighbourOrNull(direction)!!
        }

    /**
     * Returns [OmniDirection] of travel from [this] location to the [next location][nextLocation].
     *
     * Can return `null` if [nextLocation] is NOT one of the neighbouring locations of [this] location.
     */
    override fun P.getDirectionToNeighbourOrNull(nextLocation: P): OmniDirection? =
        getAllNeighboursWithDirection().filterValues { location: P ->
            location == nextLocation
        }.keys.singleOrNull()

    /**
     * Returns a [Sequence] of all locations found from [this] location in the given [direction]
     */
    override fun P.getLocationsInDirection(direction: OmniDirection): Sequence<P> =
        generateSequence(this) { previousLocation: P ->
            previousLocation.getNeighbourOrNull(direction)
        }

    /**
     * Returns a [Map] of all locations found from [this] location in every possible [OmniDirection].
     * [OmniDirection]s will be the [Map.keys] of the [Map] returned.
     */
    override fun P.getLocationsInAllDirections(): Map<OmniDirection, Sequence<P>> =
        OmniDirection.entries.associateWith { direction: OmniDirection ->
            getLocationsInDirection(direction)
        }

}