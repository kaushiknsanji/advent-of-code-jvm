/**
 * Problem: Day23: LAN Party
 * https://adventofcode.com/2024/day/23
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler
import extensions.distinctPairs
import extensions.generateRotations
import utils.Constants.COMMA_STRING
import utils.Constants.HYPHEN_STRING
import utils.Constants.T_SMALL_STRING

class Day23 : BaseProblemHandler() {

    /**
     * Returns the Package name of this problem class
     */
    override fun getCurrentPackageName(): String = this::class.java.`package`.name

    /**
     * Returns the Class name of this problem class
     */
    override fun getClassName(): String = this::class.java.simpleName

    /**
     * Executes "Part-1" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart1(input: List<String>, otherArgs: Array<out Any?>): Any =
        ComputerNetworkAnalyzer.parse(input)
            .getCountOfThreeInterConnectedSetsWithComputerT()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        ComputerNetworkAnalyzer.parse(input)
            .getPassword()

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, 7)
        solveActual(1, false, 0, 1344)
        solveSample(2, false, 0, "co,de,ka,ta")
        solveActual(2, false, 0, "ab,al,cq,cr,da,db,dr,fw,ly,mn,od,py,uh")
    }

}

fun main() {
    Day23().start()
}

/**
 * Class for a Computer Node in a Computer Network.
 *
 * @property name [String] value of the Computer Name.
 * @constructor Constructs a Computer Node.
 */
private class ComputerNode(
    val name: String
) {
    // List for all of its connections to other Computer Nodes in the Network
    private val connections: MutableList<ComputerNode> = mutableListOf()

    /**
     * Adds given [computerNode] to its list of [connections].
     */
    fun addConnection(computerNode: ComputerNode) {
        connections.add(computerNode)
    }

    /**
     * Returns this computer node's connections to other [ComputerNode]s in the Network
     */
    fun getConnections(): List<ComputerNode> = connections

    /**
     * Returns `true` if this computer node is connected to [other] computer node in the Network; `false` otherwise.
     */
    fun isConnectedTo(other: ComputerNode): Boolean = connections.any { it == other }

    /**
     * Returns a string representation of the object.
     */
    override fun toString(): String = name
}

private class ComputerNetworkAnalyzer private constructor(
    private val nameToNodeMap: Map<String, ComputerNode>
) {

    companion object {

        fun parse(input: List<String>): ComputerNetworkAnalyzer = buildMap<String, ComputerNode> {
            input.forEach { line ->
                val (nameOfNode1, nameOfNode2) = line.split(HYPHEN_STRING)
                val node1 = getOrPut(nameOfNode1) { ComputerNode(nameOfNode1) }
                val node2 = getOrPut(nameOfNode2) { ComputerNode(nameOfNode2) }
                // Add bidirectional connection
                node1.addConnection(node2)
                node2.addConnection(node1)
            }
        }.let { nameToNodeMap ->
            ComputerNetworkAnalyzer(nameToNodeMap)
        }
    }

    /**
     * Returns `true` if all three [ComputerNode]s in [this] are interconnected in a way
     * where every [ComputerNode] is connected to two other [ComputerNode]s present in [this].
     */
    private fun List<ComputerNode>.areThreeNodesInterConnected(): Boolean =
        this.generateRotations().any { computerNodes: List<ComputerNode> ->
            // Rotate list by [this.size - 1] number of times, to get different arrangements
            // when we take distinct pairs of the same

            // Returns true when distinct pairs of a particular arrangement
            // shows all three are interconnected as per requirement
            computerNodes.distinctPairs()
                .all { computerNodePairs ->
                    // For this arrangement of [ComputerNode]s, verify that all of its distinct pairs
                    // are connected.
                    computerNodePairs.first.isConnectedTo(computerNodePairs.second)
                }
        }

    /**
     * [Solution for Part-1]
     *
     * Returns count of the sets of Three Interconnected Computers having any computer starting with name 't'.
     */
    fun getCountOfThreeInterConnectedSetsWithComputerT(): Int =
        nameToNodeMap.keys.asSequence().filter { computerName: String ->
            // Select computer names starting with 't'
            computerName.startsWith(T_SMALL_STRING)
        }.map { tComputerName: String ->
            // Get their computer nodes
            nameToNodeMap[tComputerName]!!
        }.flatMap { tComputerNode: ComputerNode ->
            // Get Pairs of this 't'-computer node's connections and transform them into lists of three nodes
            // containing the pairs' nodes along with this 't'-computer node
            tComputerNode.getConnections().distinctPairs()
                .map { computerNodePairs ->
                    listOf(
                        tComputerNode,
                        computerNodePairs.first,
                        computerNodePairs.second
                    ).sortedBy { node ->
                        // Sort by their names, since such lists can contain more than one 't'-computer nodes leading
                        // to duplicates later while processing for another 't'-computer node
                        node.name
                    }
                }
        }.distinct().count { threeComputerNodes ->
            // Return count of sets of three interconnected computer nodes
            threeComputerNodes.areThreeNodesInterConnected()
        }

    /**
     * Recursive function to find all Maximal Cliques in the computer network using "The Bron-Kerbosch algorithm".
     *
     * @param clique A [Set] of [ComputerNode]s that are currently forming a Clique. Starts as an empty set and
     * grows as [ComputerNode]s are added during the recursive process.
     * @param candidates A [Set] of Potential [ComputerNode]s that can be added to the [clique]. These [ComputerNode]s
     * are connected to all [ComputerNode]s in [clique].
     * @param processedSet A [Set] of [ComputerNode]s that have been already processed and should not be added to
     * the [clique]. These [ComputerNode]s are connected to all [ComputerNode]s in [clique].
     * @param captureClique A Lambda to capture the [clique] found.
     */
    private fun executeBronKerbosch(
        clique: Set<ComputerNode>,
        candidates: MutableSet<ComputerNode>,
        processedSet: MutableSet<ComputerNode>,
        captureClique: (cliqueFound: Set<ComputerNode>) -> Unit
    ) {
        if (candidates.isEmpty() && processedSet.isEmpty()) {
            // Base Case: The [clique] formed will be a Maximal Clique when both [candidates] and [processedSet]
            // becomes empty, i.e., when no more computer nodes can be added to [clique].

            // Call the lambda to save the [clique] found
            captureClique(clique)
        } else {
            // Use a copy of the [candidates] while iterating
            candidates.toSet().forEach { node ->
                // For every computer node in [candidates] -
                // 1. Add current node to [clique]
                // 2. Rebuild [candidates] to contain only the neighbours of current node in [candidates]
                // 3. Rebuild [processedSet] to contain only the neighbours of current node in [processedSet]
                executeBronKerbosch(
                    clique + node,
                    candidates.intersect(node.getConnections().toSet()).toMutableSet(),
                    processedSet.intersect(node.getConnections().toSet()).toMutableSet(),
                    captureClique
                )

                // After processing the current node, remove it from [candidates] and
                // mark it as processed by adding it to [processedSet]
                candidates.remove(node)
                processedSet.add(node)
            }
        }
    }

    /**
     * Returns all Maximal Cliques found in the computer network using "The Bron-Kerbosch algorithm".
     *
     * A Clique in an undirected graph is a complete subgraph of the given graph. A complete subgraph is one where all
     * of its vertices are linked to all its other vertices. A Maximal Clique is a clique that cannot be extended by
     * including one more adjacent vertex. In other words, it is a complete subgraph that is not a subset of any
     * larger subgraph.
     *
     * In the current context of Computer Node network, a Maximal Clique will be a set of computer nodes
     * that are all interconnected to each other.
     *
     * The Largest set of all interconnected computers will be the Clique with most interconnected nodes.
     */
    private fun findMaximalCliques(): List<Set<ComputerNode>> = buildList {
        executeBronKerbosch(
            clique = mutableSetOf(),
            candidates = nameToNodeMap.values.toMutableSet(),
            processedSet = mutableSetOf(),
        ) { cliqueFound: Set<ComputerNode> ->
            add(cliqueFound)
        }
    }

    /**
     * [Solution for Part-2]
     *
     * Returns Password to get into the LAN party retrieved from the Largest Maximal Clique
     * found in the computer network.
     */
    fun getPassword(): String = findMaximalCliques().maxBy(Set<ComputerNode>::size)
        .sortedBy(ComputerNode::name)
        .joinToString(COMMA_STRING)

}