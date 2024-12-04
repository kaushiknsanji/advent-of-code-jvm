package utils.grid

/**
 * Kotlin file for working with Two Dimensional Grid Graphs.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

/**
 * Interface for Two Dimensional Grid Graph to initialize and get started with.
 *
 * @param P type of [Point2d] representing locations in the Grid.
 * @param V type of value stored in all locations of the Grid.
 */
interface IGrid2dGraph<P : Point2d<Int>, V> {
    /**
     * Returns location if present at given [row] and [column] in the grid; otherwise `null`
     */
    fun getLocationOrNull(row: Int, column: Int): P?

    /**
     * Returns location for given [row] and [column] in the grid
     */
    fun getLocation(row: Int, column: Int): P

    /**
     * Returns all locations in the grid as [Collection]
     */
    fun getAllLocations(): Collection<P>

    /**
     * Returns value found at the given [location] if present in the grid; otherwise
     * returns the provided [defaultValue].
     */
    fun getOrDefault(location: P, defaultValue: V): V
}

/**
 * Interface for Two Dimensional Lattice to facilitate traversing the Grid in Cardinal directions.
 *
 * @param P type of [Point2d] representing locations in the Grid.
 * @param V type of value stored in all locations of the Grid.
 */
interface ILattice<P : Point2d<Int>, V> : IGrid2dGraph<P, V> {
    /**
     * Returns neighbouring location of [this] location in the given [direction] if present; otherwise `null`
     */
    fun P.getNeighbour(direction: TransverseDirection): P?

    /**
     * Returns a [Collection] of neighbouring locations of [this] location found in all [TransverseDirection]s possible
     */
    fun P.getAllNeighbours(): Collection<P>

    /**
     * Returns a [Sequence] of all locations found from [this] location in the given [direction]
     */
    fun P.getLocationsInDirection(direction: TransverseDirection): Sequence<P>

    /**
     * Returns a [Map] of all locations found from [this] location in every possible [TransverseDirection].
     * [TransverseDirection]s will be the [Map.keys] of the [Map] returned.
     */
    fun P.getLocationsInAllDirections(): Map<TransverseDirection, Sequence<P>>
}

/**
 * Interface for Two Dimensional Diagonal Lattice to facilitate traversing the Grid in both
 * Cardinal and Ordinal directions.
 *
 * @param P type of [Point2d] representing locations in the Grid.
 * @param V type of value stored in all locations of the Grid.
 */
interface IDiagonalLattice<P : Point2d<Int>, V> : IGrid2dGraph<P, V> {
    /**
     * Returns neighbouring location of [this] location in the given [direction] if present; otherwise `null`
     */
    fun P.getNeighbour(direction: OmniDirection): P?

    /**
     * Returns a [Collection] of neighbouring locations of [this] location found in all [OmniDirection]s possible
     */
    fun P.getAllNeighbours(): Collection<P>

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
 * @param P type of [Point2d] representing locations in the Grid.
 * @param V type of value stored in all locations of the Grid.
 * @property rows [Int] number of rows in the Grid.
 * @property columns [Int] number of columns in the Grid.
 * @param pattern [List] of Strings having the input pattern for Two Dimensional Grid construction.
 *
 * @constructor Constructs [Grid2dGraph] using the provided grid `pattern` input.
 */
abstract class Grid2dGraph<P : Point2d<Int>, V> private constructor(
    val rows: Int,
    val columns: Int,
    pattern: List<String>
) : IGrid2dGraph<P, V> {

    constructor(pattern: List<String>) : this(
        pattern.size,
        pattern[0].length,
        pattern
    )

    // Map of locations for each row index as key
    private val gridMap: Map<Int, List<P>> = (0 until rows).flatMap { x ->
        (0 until columns).map { y ->
            provideLocation(x, y)
        }
    }.groupBy { location: P -> location.xPos }

    // Map of Values for each Location as key
    private val gridValueMap: MutableMap<P, V> = pattern.flatMapIndexed { x: Int, rowPattern: String ->
        rowPattern.mapIndexed { y: Int, locationChar: Char ->
            getLocation(x, y) to provideValue(locationChar)
        }
    }.toMap().toMutableMap()

    /**
     * Returns location if present at given [row] and [column] in the grid; otherwise `null`
     */
    override fun getLocationOrNull(row: Int, column: Int): P? = try {
        if (!gridMap.containsKey(row)) {
            throw NoSuchElementException()
        } else {
            gridMap[row]!!.single { location: P -> location.yPos == column }
        }
    } catch (e: NoSuchElementException) {
        null
    }

    /**
     * Returns location for given [row] and [column] in the grid
     */
    override fun getLocation(row: Int, column: Int): P =
        getLocationOrNull(row, column) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${Point2d::class.simpleName} at the given location ($row, $column)"
        )

    /**
     * Returns all locations in the grid as [Collection]
     */
    override fun getAllLocations(): Collection<P> =
        gridMap.values.flatten()

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
     * Returns location to be used in the grid.
     *
     * @param row [Int] value location's row
     * @param column [Int] value location's column
     */
    protected abstract fun provideLocation(row: Int, column: Int): P

    /**
     * Returns value to be used in the grid.
     *
     * @param locationChar Char found at a location in the input pattern
     */
    protected abstract fun provideValue(locationChar: Char): V

}

/**
 * Abstract Lattice class to facilitate traversing the Grid in Cardinal directions.
 *
 * @param P type of [Point2d] representing locations in the Grid.
 * @param V type of value stored in all locations of the Grid.
 * @param pattern [List] of Strings having the input pattern for Two Dimensional Grid construction.
 *
 * @constructor Constructs [Lattice] using the provided grid `pattern` input.
 */
abstract class Lattice<P : Point2d<Int>, V>(
    pattern: List<String>
) : ILattice<P, V>, Grid2dGraph<P, V>(pattern) {

    /**
     * Returns neighbouring location of [this] location in the given [direction] if present; otherwise `null`
     */
    override fun P.getNeighbour(direction: TransverseDirection): P? = when (direction) {
        TransverseDirection.TOP -> getLocationOrNull(xPos - 1, yPos)
        TransverseDirection.BOTTOM -> getLocationOrNull(xPos + 1, yPos)
        TransverseDirection.RIGHT -> getLocationOrNull(xPos, yPos + 1)
        TransverseDirection.LEFT -> getLocationOrNull(xPos, yPos - 1)
    }

    /**
     * Returns a [Collection] of neighbouring locations of [this] location found in all [TransverseDirection]s possible
     */
    override fun P.getAllNeighbours(): Collection<P> =
        TransverseDirection.entries.mapNotNull { direction: TransverseDirection -> getNeighbour(direction) }

    /**
     * Returns a [Sequence] of all locations found from [this] location in the given [direction]
     */
    override fun P.getLocationsInDirection(direction: TransverseDirection): Sequence<P> =
        generateSequence(this) { previousLocation: P ->
            previousLocation.getNeighbour(direction)
        }

    /**
     * Returns a [Map] of all locations found from [this] location in every possible [TransverseDirection].
     * [TransverseDirection]s will be the [Map.keys] of the [Map] returned.
     */
    override fun P.getLocationsInAllDirections(): Map<TransverseDirection, Sequence<P>> =
        TransverseDirection.entries.associateWith { direction: TransverseDirection ->
            getLocationsInDirection(direction)
        }

}

/**
 * Abstract Diagonal Lattice class to facilitate traversing the Grid in both
 * Cardinal and Ordinal directions.
 *
 * @param P type of [Point2d] representing locations in the Grid.
 * @param V type of value stored in all locations of the Grid.
 * @param pattern [List] of Strings having the input pattern for Two Dimensional Grid construction.
 *
 * @constructor Constructs [DiagonalLattice] using the provided grid `pattern` input.
 */
abstract class DiagonalLattice<P : Point2d<Int>, V>(
    pattern: List<String>
) : IDiagonalLattice<P, V>, Grid2dGraph<P, V>(pattern) {

    /**
     * Returns neighbouring location of [this] location in the given [direction] if present; otherwise `null`
     */
    override fun P.getNeighbour(direction: OmniDirection): P? = when (direction) {
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
        OmniDirection.entries.mapNotNull { direction: OmniDirection -> getNeighbour(direction) }

    /**
     * Returns a [Sequence] of all locations found from [this] location in the given [direction]
     */
    override fun P.getLocationsInDirection(direction: OmniDirection): Sequence<P> =
        generateSequence(this) { previousLocation: P ->
            previousLocation.getNeighbour(direction)
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