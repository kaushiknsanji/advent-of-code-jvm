/**
 * Problem: Day23: A Long Walk
 * https://adventofcode.com/2023/day/23
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import utils.grid.CardinalDirection.*
import java.util.*
import utils.grid.CardinalDirection as Direction

private class Day23 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 94
    println("=====")
    solveActual(1)      // 2386
    println("=====")
    solveSample(2)      // 154
    println("=====")
    solveActual(2)      // 6246
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day23.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day23.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    HikingTrailAnalyzer(input).getTotalStepsOfLongestHike().also { println(it) }
}

private fun doPart2(input: List<String>) {
    HikingTrailAnalyzer(input).getTotalStepsOfLongestHike(isSlopeHikeable = true).also { println(it) }
}

private class TrailLocation(val x: Int, val y: Int)

private enum class TrailType(val type: Char) {
    PATH('.'),
    FOREST('#'),
    SLOPE_UP('^'),
    SLOPE_LEFT('<'),
    SLOPE_RIGHT('>'),
    SLOPE_DOWN('v')
}

private interface ITrailGrid {
    fun getTrailLocationOrNull(row: Int, column: Int): TrailLocation?
    fun getTrailLocation(row: Int, column: Int): TrailLocation
    fun getAllTrailLocations(): Collection<TrailLocation>
    fun getFirstRowLocations(): Collection<TrailLocation>
    fun getLastRowLocations(): Collection<TrailLocation>
    fun TrailLocation.getNeighbour(direction: Direction): TrailLocation?
    fun TrailLocation.getAllNeighbours(): Collection<TrailLocation>
}

private class TrailGrid private constructor(
    rows: Int,
    columns: Int,
    trailPatternList: List<String>
) : ITrailGrid {

    constructor(trailPatternList: List<String>) : this(
        rows = trailPatternList.size,
        columns = trailPatternList.first().length,
        trailPatternList = trailPatternList
    )

    private val trailGridMap: Map<Int, List<TrailLocation>> = (0 until rows).flatMap { x: Int ->
        (0 until columns).map { y: Int ->
            TrailLocation(x, y)
        }
    }.groupBy { trailLocation: TrailLocation -> trailLocation.x }

    private val trailGridValueMap: Map<TrailLocation, TrailType> =
        trailPatternList.flatMapIndexed { x: Int, rowPattern: String ->
            rowPattern.mapIndexed { y: Int, trailCharacter: Char ->
                getTrailLocation(x, y) to TrailType.entries.single { it.type == trailCharacter }
            }
        }.toMap()

    operator fun get(trailLocation: TrailLocation) = trailGridValueMap[trailLocation]!!

    override fun getTrailLocationOrNull(row: Int, column: Int): TrailLocation? = try {
        if (!trailGridMap.containsKey(row)) {
            throw NoSuchElementException()
        } else {
            trailGridMap[row]!!.single { it.y == column }
        }
    } catch (e: NoSuchElementException) {
        null
    }

    override fun getTrailLocation(row: Int, column: Int): TrailLocation =
        getTrailLocationOrNull(row, column) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${TrailLocation::class.simpleName} at given location ($row, $column)"
        )

    override fun getAllTrailLocations(): Collection<TrailLocation> =
        trailGridMap.values.flatten()

    override fun getFirstRowLocations(): Collection<TrailLocation> =
        trailGridMap.values.first()

    override fun getLastRowLocations(): Collection<TrailLocation> =
        trailGridMap.values.last()

    override fun TrailLocation.getNeighbour(direction: Direction): TrailLocation? = when (direction) {
        TOP -> getTrailLocationOrNull(x - 1, y)
        BOTTOM -> getTrailLocationOrNull(x + 1, y)
        RIGHT -> getTrailLocationOrNull(x, y + 1)
        LEFT -> getTrailLocationOrNull(x, y - 1)
    }

    override fun TrailLocation.getAllNeighbours(): Collection<TrailLocation> =
        Direction.entries.mapNotNull { direction -> getNeighbour(direction) }

}

private interface ITrailFinder {
    fun TrailLocation.toType(): TrailType
    fun getStartTrailLocation(): TrailLocation
    fun getEndTrailLocation(): TrailLocation
    fun TrailLocation.getNextNeighbours(isSlopeHikeable: Boolean): Collection<TrailLocation?>
}

private class TrailFinder(
    private val trailGrid: TrailGrid
) : ITrailGrid by trailGrid, ITrailFinder {

    override fun TrailLocation.toType(): TrailType = trailGrid[this]

    override fun getStartTrailLocation(): TrailLocation =
        getFirstRowLocations().single { it.toType() == TrailType.PATH }

    override fun getEndTrailLocation(): TrailLocation =
        getLastRowLocations().single { it.toType() == TrailType.PATH }

    /**
     * Extension function of [TrailLocation] to return its neighbouring [TrailLocation]s based on
     * [isSlopeHikeable] condition.
     *
     * @param isSlopeHikeable When all slopes are hikeable in the Trails, this will be set to `true`.
     * When `true` these are treated equivalent to [TrailType.PATH].
     */
    override fun TrailLocation.getNextNeighbours(isSlopeHikeable: Boolean): Collection<TrailLocation?> =
        if (isSlopeHikeable) {
            if (this.toType() == TrailType.FOREST) {
                listOf(null)
            } else {
                getAllNeighbours().filterNot { it.toType() == TrailType.FOREST }
            }
        } else {
            when (this.toType()) {
                TrailType.PATH -> getAllNeighbours().filterNot { it.toType() == TrailType.FOREST }
                TrailType.FOREST -> listOf(null)
                TrailType.SLOPE_UP -> listOf(getNeighbour(TOP))
                TrailType.SLOPE_LEFT -> listOf(getNeighbour(LEFT))
                TrailType.SLOPE_RIGHT -> listOf(getNeighbour(RIGHT))
                TrailType.SLOPE_DOWN -> listOf(getNeighbour(BOTTOM))
            }
        }

}

private class HikingTrailAnalyzer private constructor(
    private val trailGrid: TrailGrid
) : ITrailGrid by trailGrid, ITrailFinder by TrailFinder(trailGrid) {

    constructor(input: List<String>) : this(TrailGrid(input))

    /**
     * Generates a [Map] of [TrailLocation] to other [TrailLocation]s that can be reached along with their distance.
     * [TrailLocation]s chosen will have more than two next neighbouring [TrailLocation]s when all slopes
     * are treated equivalent to [TrailType.PATH]. [Map] returned will also include the [start][getStartTrailLocation]
     * and [end][getEndTrailLocation] Trail locations.
     *
     * Used for Part-2 only. Hence, this is initialized lazily.
     */
    private val locationToReachableLocationsDistanceMap: Map<TrailLocation, Map<TrailLocation, Int>> by lazy {
        getAllTrailLocations().filterNot { trailLocation ->
            trailLocation.toType() == TrailType.FOREST
        }.filter { trailLocation ->
            trailLocation.getNextNeighbours(true).count() > 2
        }.toMutableList().apply {
            add(0, getStartTrailLocation())
            add(getEndTrailLocation())
        }.let { locationsOfInterest ->
            locationsOfInterest.associateWith { source ->
                getReachableLocationsDistanceMap(source, locationsOfInterest.filterNot { it == source })
            }
        }
    }

    /**
     * Returns a [Map] of all [destinations] that can be reached along with their distance from the given
     * [source] Trail location.
     *
     * Uses BFS to find all [destinations] that are reachable from the [source] without revisiting any Trail location
     * traversed in the path to all reachable [destinations].
     */
    private fun getReachableLocationsDistanceMap(
        source: TrailLocation,
        destinations: List<TrailLocation>
    ): Map<TrailLocation, Int> {
        // Using two lists for Frontier instead of a Queue as it is faster for items that are already initialized
        // and since Queue would be just holding Trail Locations that are at a distance of 'd' and 'd+1' only.
        // Frontier list of Trail Locations that are at a distance of 'd'
        var currentFrontier: MutableList<TrailLocation> = mutableListOf(source)
        // Frontier list of Next Trail Locations that are at a distance of 'd + 1'
        val nextFrontier: MutableList<TrailLocation> = mutableListOf()

        // Set of Trail Locations already visited during traversal
        val visitedSet: MutableSet<TrailLocation> = mutableSetOf(source)

        // Result Map of Trail Locations reachable from the [source] along with their distance
        val distanceMap: MutableMap<TrailLocation, Int> = mutableMapOf()

        // Step counter
        var distance = 0

        // Repeat till the Frontier holding locations at distance of 'd' becomes empty
        while (currentFrontier.isNotEmpty()) {
            // Increment step counter
            distance++

            currentFrontier.forEach { current: TrailLocation ->
                current.getNextNeighbours(true)
                    .filterNotNull()
                    .filterNot { it in visitedSet }  // Exclude Trail locations already visited
                    .forEach { nextTrailLocation: TrailLocation ->
                        if (nextTrailLocation in destinations) {
                            // When this Trail location reached is mentioned in the [destinations],
                            // save its distance in the result Map
                            distanceMap[nextTrailLocation] = distance
                        } else {
                            // When this Trail location reached is NOT mentioned in the [destinations],
                            // just add for the Next Frontier and mark this Trail location as visited
                            nextFrontier.add(nextTrailLocation)
                            visitedSet.add(nextTrailLocation)
                        }
                    }
            }

            // Copy over to Current Frontier and clear Next Frontier
            currentFrontier = nextFrontier.toMutableList()
            nextFrontier.clear()
        }

        return distanceMap
    }

    /**
     * Returns the Longest distance to [end] from [start] Trail location, using [PriorityQueue].
     *
     * @param nextLocations A lambda that generates next [TrailLocation]s from the passed in `current` [TrailLocation]
     * and returns them along with their distance from `current` [TrailLocation] as [Pair]s.
     */
    private fun findLongestDistanceToEndFromStart(
        start: TrailLocation,
        end: TrailLocation,
        nextLocations: (current: TrailLocation) -> List<Pair<TrailLocation, Int>>
    ): Int {
        // A PriorityQueue based Frontier that inversely prioritizes
        // on the total distance so far, for maximizing the distance taken
        val frontier = PriorityQueue<Pair<TrailLocation, Int>>(
            compareByDescending { it.second }
        ).apply {
            // Begin with Starting Trail Location with a distance of 0
            add(start to 0)
        }

        // Map that saves which Trail Location we came from for the current Trail Location along with
        // their distances as Pairs. This facilitates to build traversed paths without the need for storing them in
        // a List of Lists for each trail path discovered.
        // Begin with a Pair of Starting Trail Location with a distance of 0 as both key and value.
        val cameFromMap: MutableMap<Pair<TrailLocation, Int>, Pair<TrailLocation, Int>> =
            mutableMapOf((start to 0) to (start to 0))

        // Saves maximum distance found when End is reached during traversal
        var maxDistanceToEnd = 0
        // Next Maximum search cutoff to exit searching for maximum distance after having found
        // the probable maximum distance to End. This will be set to twice the maximum distance found so far
        // which will act as a cutoff for the next maximum to be found.
        var nextMaxFindCutOff = 0

        // Generates a sequence of Trail locations traversed by backtracking
        // from the given pair of Trail Location and its distance. Sequence generated will be in the reverse direction
        // till the Start location. Start location will NOT be included.
        val pathSequence: (currentPair: Pair<TrailLocation, Int>) -> Sequence<TrailLocation> = {
            var currentPair: Pair<TrailLocation, Int> = it
            sequence {
                while (currentPair.first != start) {
                    yield(currentPair.first)
                    currentPair = cameFromMap[currentPair]!!
                }
            }
        }

        // Repeat till the PriorityQueue based Frontier becomes empty
        while (frontier.isNotEmpty()) {
            // Get the top Trail Location + Distance pair
            val current = frontier.poll()

            // Exit when the End is reached with elapsed next maximum search cutoff
            if (current.first == end && nextMaxFindCutOff == 0) break

            // When End is reached, decrement next maximum search cutoff and then continue finding the next maximum
            if (current.first == end) {
                nextMaxFindCutOff--
                continue
            }

            // Retrieve next Trail Locations along with their distance from the [nextLocations] lambda
            nextLocations(current.first)
                .filterNot { (nextTrailLocation: TrailLocation, _: Int) ->
                    // Exclude the Trail Location we are coming from
                    nextTrailLocation == cameFromMap[current]!!.first
                }
                .filterNot { (nextTrailLocation: TrailLocation, nextDistance: Int) ->
                    // For distances greater than 1, exclude Trail Locations already traversed
                    nextDistance > 1 && nextTrailLocation in pathSequence(current)
                }
                .forEach { (nextTrailLocation: TrailLocation, nextDistance: Int) ->
                    // Distance to the next Trail Location
                    val newDistance = current.second + nextDistance

                    // Save the current Trail Location Distance pair as the value of
                    // the Next Trail Location Distance pair in the Map
                    cameFromMap[nextTrailLocation to newDistance] = current
                    // Add Next Trail Location Distance pair to the Frontier
                    frontier.add(nextTrailLocation to newDistance)

                    // When End is reached and this distance to End is found to be the new maximum
                    if (nextTrailLocation == end && newDistance > maxDistanceToEnd) {
                        // Save the new maximum distance to End
                        maxDistanceToEnd = newDistance
                        // Reset the next maximum search cutoff to twice the maximum distance found.
                        // Twice maximum distance is just an overestimate made based on my test input.
                        // Actual estimate for my input is '1.6' times maximum distance.
                        // Nothing mathematical, just observational.
                        nextMaxFindCutOff = newDistance * 2
                    }
                }
        }

        return maxDistanceToEnd
    }

    /**
     * [Solution for Part-1 & Part-2]
     *
     * Returns the number of steps of the longest hike that can be taken through the Hiking trails provided.
     *
     * @param isSlopeHikeable When all slopes are hikeable in the Trails, this will be set to `true`.
     * When `true` these are treated equivalent to [TrailType.PATH]. This is set to `true` for Part-2.
     * Default is `false`.
     */
    fun getTotalStepsOfLongestHike(isSlopeHikeable: Boolean = false): Int =
        findLongestDistanceToEndFromStart(
            getStartTrailLocation(),
            getEndTrailLocation(),
            nextLocations = { current: TrailLocation ->
                if (isSlopeHikeable) {
                    // When slope is hikeable, return a list of Next Trail Location and Distance Pairs
                    // for the current Trail Location from the reachable locations distance map
                    locationToReachableLocationsDistanceMap[current]!!.map { it.key to it.value }
                } else {
                    // When slope is NOT hikeable, return a list of Next Trail Location and Distance Pairs
                    // each with a distance of 1, for the current Trail Location
                    current.getNextNeighbours(isSlopeHikeable)
                        .filterNotNull()
                        .map { it to 1 }
                }
            }
        )
}