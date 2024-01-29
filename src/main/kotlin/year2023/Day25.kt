/**
 * Problem: Day25: Snowverload
 * https://adventofcode.com/2023/day/25
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import extensions.distinctPairs
import extensions.reversed
import utils.findTotalCombinationsWithoutRepetition
import java.util.*

private class Day25 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample()      // 54
    println("=====")
    solveActual()      // 596376
    println("=====")
}

private fun solveSample(executeProblemPart: Int = 1) {
    execute(Day25.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int = 1) {
    execute(Day25.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
    }
}

private fun doPart1(input: List<String>) {
    ComponentNetworkAnalyzer.parse(input)
        .getProductOfTotalComponentsInDisconnectedGroups(countOfDisconnectsRequiredToDivide = 3)
        .also { println(it) }
}

/**
 * Class for network of components.
 */
private class ComponentNetwork {
    // Graph/network Map of components connected to other components
    private val graph: MutableMap<String, MutableList<String>> = mutableMapOf()

    /**
     * Returns Maximum number of connections, i.e., Maximum degree found in the component network
     */
    private val maximumDegreeOfConnection: Int by lazy {
        graph.maxOf { (_: String, connections: List<String>) -> connections.size }
    }

    operator fun get(component: String): List<String> = graph[component]!!

    /**
     * Adds [sourceComponent] with its connecting components as given by [connectingComponents] into the network.
     * Also, takes care of connecting [connectingComponents] components bidirectionally with [sourceComponent].
     */
    fun addConnection(
        sourceComponent: String,
        connectingComponents: List<String>
    ) {
        // Add connections for Source component
        graph[sourceComponent] =
            (graph.getOrDefault(sourceComponent, emptyList()) + connectingComponents).toMutableList()

        // Include bidirectional connection for every connecting component with the Source component
        connectingComponents.forEach { connectingComponent: String ->
            graph[connectingComponent] =
                (graph.getOrDefault(connectingComponent, emptyList()) + sourceComponent).toMutableList()
        }
    }

    /**
     * Returns Components having maximum degree, i.e., maximum number of connections.
     */
    fun getMaximumDegreeComponents(): Set<String> =
        graph.filter { (_: String, connections: List<String>) ->
            connections.size == maximumDegreeOfConnection
        }.keys

}

private class ComponentNetworkAnalyzer(
    private val componentNetwork: ComponentNetwork
) {

    companion object {
        private val componentsRegex = """([a-z]{3})""".toRegex()

        fun parse(input: List<String>): ComponentNetworkAnalyzer = ComponentNetworkAnalyzer(
            componentNetwork = ComponentNetwork().apply {
                input.forEach { line ->
                    componentsRegex.findAll(line).map { matchResult ->
                        matchResult.groupValues[1]
                    }.toList().let { components: List<String> ->
                        addConnection(components[0], components.drop(1))
                    }
                }
            }
        )
    }

    /**
     * Returns a [List] of [Triple] having the shortest distance value between distinct pairs of components
     * having maximum number of connections.
     */
    private val maxDegreeComponentsWithDistance: List<Triple<String, String, Int>> by lazy {
        componentNetwork.getMaximumDegreeComponents().distinctPairs().map { componentPair: Pair<String, String> ->
            Triple(
                componentPair.first,
                componentPair.second,
                getShortestDistanceBetweenTwoComponents(
                    componentPair.first,
                    componentPair.second
                )
            )
        }
    }

    /**
     * Returns maximum value of the shortest distance found between distinct pairs of components
     * having maximum number of connections.
     */
    private val maxOfShortestDistanceBetweenMaxDegreeComponents: Int by lazy {
        maxDegreeComponentsWithDistance.maxOf { it.third }
    }

    /**
     * Returns [Int] value of the shortest distance found between [sourceComponent] and [destinationComponent].
     *
     * Result is obtained by following [Dijkstra](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm) algorithm
     * using [PriorityQueue].
     */
    private fun getShortestDistanceBetweenTwoComponents(
        sourceComponent: String,
        destinationComponent: String
    ): Int {
        // A PriorityQueue based Frontier that prioritizes on the distance so far, for minimizing the distance taken
        val frontier = PriorityQueue<Pair<String, Int>>(
            compareBy { it.second }
        ).apply {
            // Begin with [sourceComponent] with a distance of 0
            add(sourceComponent to 0)
        }

        // Map of Components reached from the [sourceComponent] along with their distance
        val distanceMap: MutableMap<String, Int> = mutableMapOf(sourceComponent to 0)

        // Repeat till the PriorityQueue based Frontier becomes empty
        while (frontier.isNotEmpty()) {
            // Get the top component with distance pair
            val current = frontier.poll()

            // Exit when [destinationComponent] is reached
            if (current.first == destinationComponent) break

            // Explore next components connected to the current
            componentNetwork[current.first].forEach { nextComponent: String ->
                // Distance to the next component
                val newDistance = current.second + 1

                if (newDistance < distanceMap.getOrDefault(nextComponent, Int.MAX_VALUE)) {
                    // If the next component is a newly explored component or the distance to this already explored
                    // next component is smaller than previously found, then only update the same onto the distance map
                    // as well as the Frontier to explore further
                    distanceMap[nextComponent] = newDistance
                    frontier.add(nextComponent to newDistance)
                }
            }
        }

        // Return distance to [destinationComponent] from the distance map
        return distanceMap[destinationComponent]!!
    }

    /**
     * Returns a [List] of components that can be reached for every path traversed
     * from [sourceComponent] to [destinationComponent].
     *
     * @param excludeConnections [List] of connections denoted as component [Pair]s that needs to be excluded
     * while exploring paths to [destinationComponent] from [sourceComponent]. Defaulted to [emptyList].
     */
    private fun getAllComponentsConnectingTwoComponents(
        sourceComponent: String,
        destinationComponent: String,
        excludeConnections: List<Pair<String, String>> = emptyList()
    ): List<List<String>> {
        // Using two lists for Frontier instead of a Queue as it is faster for items that are already initialized
        // and since Queue would be just holding Components that are at a distance of 'd' and 'd+1' only.
        // Frontier list of Components that are at a distance of 'd'
        var currentFrontier: MutableList<String> = mutableListOf(sourceComponent)
        // Frontier list of Next Components that are at a distance of 'd + 1'
        val nextFrontier: MutableList<String> = mutableListOf()

        // List of components that can be reached for every path
        // traversed from [sourceComponent] to [destinationComponent]
        val pathsToDestination: MutableList<List<String>> = mutableListOf()

        // Map that saves the Component we came from for the current Component. This facilitates to build
        // traversed paths without the need for storing them in a List of Lists for each Component explored.
        val cameFromMap: MutableMap<String, String> = mutableMapOf(sourceComponent to sourceComponent)

        // Generates a sequence of Components traversed by backtracking from the given Component. Sequence generated
        // will be in the reverse direction till the [sourceComponent]. [sourceComponent] is also included.
        val pathSequence: (component: String) -> Sequence<String> = {
            var currentComponent: String = it
            sequence {
                while (currentComponent != sourceComponent) {
                    yield(currentComponent)
                    currentComponent = cameFromMap[currentComponent]!!
                }
                yield(currentComponent)
            }
        }

        // Repeat till the Frontier holding Components at distance of 'd' becomes empty
        while (currentFrontier.isNotEmpty()) {
            currentFrontier.forEach { currentComponent: String ->
                componentNetwork[currentComponent].filterNot { nextComponent: String ->
                    // Exclude next component if already visited
                    nextComponent in cameFromMap
                }.filterNot { nextComponent: String ->
                    // Exclude next component if it is part of the connections that needs to be excluded
                    excludeConnections.isNotEmpty() && (currentComponent to nextComponent) in excludeConnections
                }.forEach { nextComponent: String ->
                    if (nextComponent == destinationComponent) {
                        // When [destinationComponent] is reached, build the path traversed
                        // to the current component along with this [destinationComponent] and include it
                        // in the list of paths explored towards [destinationComponent]
                        pathsToDestination.add(
                            pathSequence(currentComponent).reversed().toMutableList().apply { add(nextComponent) }
                        )
                    } else {
                        // When [destinationComponent] is NOT yet reached, save the current component as the value
                        // of this next component in the Map and add next component for the Next Frontier
                        cameFromMap[nextComponent] = currentComponent
                        nextFrontier.add(nextComponent)
                    }
                }
            }

            // Copy over to Current Frontier and clear Next Frontier
            currentFrontier = nextFrontier.toMutableList()
            nextFrontier.clear()
        }

        // Return all paths traversed to [destinationComponent]
        return pathsToDestination
    }

    /**
     * Extension function on a [List] of paths of components connecting two distant Maximum degree components to return
     * a [Set] of Probable disconnections represented by components in [Pair]s, which may allow to divide the main
     * network into two properly disconnected groups of network.
     */
    private fun List<List<String>>.getProbableDisconnections(): Set<Pair<String, String>> =
        buildSet {
            forEach { path: List<String> ->
                // Index of the Articulation point/component that may have a bridge/connection to nearby components
                // whose disconnection may aid in dividing the network
                val index = path.lastIndex shr 1

                // Add connections to adjacent components found from the traversed path
                add(path[index - 1] to path[index])
                add(path[index] to path[index + 1])

                // If traversed path size is even, then we might have
                // another Articulation point/component at 'index + 1'
                if (path.size % 2 == 0) {
                    // If path size is even, then add connection to only the next adjacent component
                    // found from the traversed path, since connection to previous adjacent component is already added
                    add(path[index + 1] to path[index + 2])
                }
            }
        }

    /**
     * Finds and returns the required disconnections in the paths connecting two distant Maximum degree components,
     * that is [sourceComponent] and [destinationComponent], in order to divide the main network
     * comprising such components into two properly disconnected groups of network having
     * either of the components but not both under one group.
     *
     * @param countOfDisconnectsRequiredToDivide Number of wires that needs to be disconnected to divide
     * the main group into two disconnected groups.
     *
     * @return [Triple] containing [sourceComponent], [destinationComponent] and then
     * the [List] of required disconnections represented by components in [Pair]s. Can be `null` when
     * required disconnections could not be derived for the paths connecting [sourceComponent] to [destinationComponent].
     */
    private fun findRequiredDisconnections(
        sourceComponent: String,
        destinationComponent: String,
        countOfDisconnectsRequiredToDivide: Int
    ): Triple<String, String, List<Pair<String, String>>>? {
        // Get all paths connecting [sourceComponent] to [destinationComponent]
        var connectingPaths = getAllComponentsConnectingTwoComponents(
            sourceComponent,
            destinationComponent
        )

        // Get all probable disconnections
        val probableDisconnections = connectingPaths.getProbableDisconnections()
        // List to save the required disconnections found
        var requiredDisconnections: List<Pair<String, String>> = emptyList()
        // List to keep track of the combinations of disconnections tried
        val alreadyTestedDisconnections: MutableList<List<Pair<String, String>>> = mutableListOf()

        // Counter to bail out when all combinations of disconnections have been tested to no avail
        var breakCounter: Long = findTotalCombinationsWithoutRepetition(
            probableDisconnections.size,
            countOfDisconnectsRequiredToDivide
        )

        // Comparator to sort disconnections by components in order to just keep track of the
        // combinations instead of the permutations of disconnections
        val componentPairSorter = compareBy<Pair<String, String>> { it.first }.thenBy { it.second }

        // Run till we find the network disconnected or when all combinations of disconnections are tested to no avail
        while (connectingPaths.isNotEmpty()) {
            // Randomly pick disconnections equal to [countOfDisconnectsRequiredToDivide] after shuffling the
            // source list of probable disconnections
            requiredDisconnections = probableDisconnections.shuffled()
                .take(countOfDisconnectsRequiredToDivide)
                .sortedWith(componentPairSorter)  // Sort disconnections to avoid permutations of the same

            // Exit when all combinations of disconnections are tested
            if (breakCounter == 0L) break

            if (requiredDisconnections in alreadyTestedDisconnections) {
                // If current disconnections are already tested, then continue finding the next
                // appropriate list of disconnections
                continue
            } else {
                // If current disconnections are NOT yet tested, then decrement to reflect the tally of
                // remaining combinations that can be tested and track the current combination of
                // disconnections being tested
                breakCounter--
                alreadyTestedDisconnections.add(requiredDisconnections)
            }

            // With the randomly picked disconnections,
            // test connectivity between [sourceComponent] and [destinationComponent]
            connectingPaths = getAllComponentsConnectingTwoComponents(
                sourceComponent,
                destinationComponent,
                requiredDisconnections
            )
        }

        return if (connectingPaths.isEmpty()) {
            // If there is NO connectivity between [sourceComponent] and [destinationComponent], then
            // we have successfully divided the main group into two disconnected groups. So, return
            // its Triple result having the required disconnections.
            Triple(sourceComponent, destinationComponent, requiredDisconnections)
        } else {
            // If there is still connectivity between [sourceComponent] and [destinationComponent], then return null.
            null
        }
    }

    /**
     * Returns [Int] value of the count of components present in the network of the given [component]
     * when certain connections between components as given by [excludeConnections] are to be
     * excluded or treated as disconnected / non-existing.
     */
    private fun getComponentCountInComponentTree(
        component: String,
        excludeConnections: List<Pair<String, String>>
    ): Int {
        // Using two lists for Frontier instead of a Queue as it is faster for items that are already initialized
        // and since Queue would be just holding Components that are at a distance of 'd' and 'd+1' only.
        // Frontier list of Components that are at a distance of 'd'
        var currentFrontier: MutableList<String> = mutableListOf(component)
        // Frontier list of Next Components that are at a distance of 'd + 1'
        val nextFrontier: MutableList<String> = mutableListOf()

        // Map that saves the Component we came from for the current Component
        val cameFromMap: MutableMap<String, String> = mutableMapOf(component to component)

        // Repeat till the Frontier holding Components at distance of 'd' becomes empty
        while (currentFrontier.isNotEmpty()) {
            currentFrontier.forEach { currentComponent: String ->
                componentNetwork[currentComponent].filterNot { nextComponent: String ->
                    // Exclude next component if already visited
                    nextComponent in cameFromMap
                }.filterNot { nextComponent: String ->
                    // Exclude next component if it is part of the connections that needs to be excluded.
                    // Check bidirectionally as we may be arriving or leaving this next component, which may be
                    // one of the connections that needs to be excluded.
                    (currentComponent to nextComponent) in excludeConnections
                            || (nextComponent to currentComponent) in excludeConnections
                }.forEach { nextComponent: String ->
                    // Save the current component as the value of this next component in the Map
                    // and add next component for the Next Frontier
                    cameFromMap[nextComponent] = currentComponent
                    nextFrontier.add(nextComponent)
                }
            }

            // Copy over to Current Frontier and clear Next Frontier
            currentFrontier = nextFrontier.toMutableList()
            nextFrontier.clear()
        }

        // Return the number of components visited
        return cameFromMap.size
    }

    /**
     * [Solution for Part-1]
     *
     * Returns the product of the number of components found in each disconnected groups
     * when the main group was divided into two by disconnecting a number of wires as
     * given by [countOfDisconnectsRequiredToDivide].
     *
     * Following is the algorithm followed-
     * 1. Find Components having maximum number of connections (also known as Degree in Graph terminology).
     * 2. Find the shortest distance between distinct pairs of Maximum degree components from step(1).
     * 3. Find Component pairs having the maximum of shortest distance between them. Such component pairs
     * can be imagined as clusters that needs to be separated, hence the above steps were followed.
     * 4. For each Maximum degree distant component pair found from step(3)-
     *    4a. We find all paths between these components.
     *    4b. From those paths, we determine probable component pairs that can be disconnected which would appear
     *    somewhere in the middle of those paths connecting the current Maximum degree components.
     *    4c. From the probable disconnections found in step(4b), we randomly consider [countOfDisconnectsRequiredToDivide]
     *    pairs to be excluded while finding paths between the Maximum degree components as we did before in step(4a).
     *    4d. Step(4c) gets repeated until we have either exhausted testing all combinations or have found
     *    the right one which will be indicated by no paths found between Maximum degree components.
     *    4e. If Step(4d) did not yield the result we needed, then we repeat all the steps(4a-4d) for the next pair
     *    of Maximum degree components.
     * 5. Once we have the required disconnections for the pair of Maximum degree components, we count the components
     * present in the network of each of the Maximum degree components, and then return their product.
     *
     * @param countOfDisconnectsRequiredToDivide Number of wires that needs to be disconnected to divide
     * the main group into two disconnected groups.
     */
    fun getProductOfTotalComponentsInDisconnectedGroups(countOfDisconnectsRequiredToDivide: Int) =
        maxDegreeComponentsWithDistance.filter { (_: String, _: String, distance: Int) ->
            // Do steps 1 to 3 - Find Maximum degree distant component pairs
            distance == maxOfShortestDistanceBetweenMaxDegreeComponents
        }.asSequence().mapNotNull { (sourceComponent: String, destinationComponent: String, _: Int) ->
            // Do step 4 - Find required disconnections between Maximum degree distant component pair
            findRequiredDisconnections(sourceComponent, destinationComponent, countOfDisconnectsRequiredToDivide)
        }.first().let { (sourceComponent: String,
                            destinationComponent: String,
                            requiredDisconnections: List<Pair<String, String>>) ->
            // Do step 5 - Return the product of the total components found in each of the disconnected groups
            getComponentCountInComponentTree(
                sourceComponent,
                requiredDisconnections
            ) * getComponentCountInComponentTree(
                destinationComponent,
                requiredDisconnections
            )
        }

}