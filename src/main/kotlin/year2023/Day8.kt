/**
 * Problem: Day8: Haunted Wasteland
 * https://adventofcode.com/2023/day/8
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2023

import base.BaseFileHandler
import extensions.splitWhen
import extensions.whileLoop
import utils.lcm

private class Day8 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 2
    println("=====")
    solvePart1Sample2()                // 6
    println("=====")
    solveActual(1)      // 19099
    println("=====")
    solvePart2Sample()                 // 6
    println("=====")
    solveActual(2)      // 17099847107071
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day8.getSampleFile().readLines(), executeProblemPart)
}

private fun solvePart1Sample2() {
    execute(Day8.getSampleFile("_part1_2").readLines(), 1)
}

private fun solvePart2Sample() {
    execute(Day8.getSampleFile("_part2").readLines(), 2)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day8.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    WastelandRouteAnalyzer.parse(input)
        .getStepCountToReachZZZ()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    WastelandRouteAnalyzer.parse(input)
        .getStepCountToReachNodesEndingWithZAtSameStep()
        .also { println(it) }
}

private class WastelandTree private constructor(
    private val nodes: Set<WastelandTreeNode>
) {
    companion object {
        private const val AAA = "AAA"
        private const val A = 'A'
        private val nodeDataRegex = """([A-Z0-9]{3})""".toRegex()

        fun parse(input: Iterable<String>): WastelandTree = with(mutableMapOf<String, WastelandTreeNode>()) {
            input.forEach { line ->
                val (current, left, right) = nodeDataRegex.findAll(line).map { it.groupValues[1] }.toList()

                // Create or get the Left child node
                val leftChildNode = getOrPut(left) {
                    WastelandTreeNode(data = left)
                }

                // Create or get the Right child node
                val rightChildNode = getOrPut(right) {
                    WastelandTreeNode(data = right)
                }

                // Create or get the Parent node
                getOrPut(current, defaultValue = {
                    WastelandTreeNode(current, leftChildNode, rightChildNode)
                }).apply {
                    // Update Parent with Left and Right child node as it might not be present yet for the iteration
                    setLeftChildNode(leftChildNode)
                    setRightChildNode(rightChildNode)
                }
            }

            // Create and return the Tree for all the Nodes setup
            WastelandTree(
                nodes = this.values.toSet()
            )
        }
    }

    // Node with "AAA"
    val nodeAAA: WastelandTreeNode get() = nodes.single { node -> node.data == AAA }

    // All Nodes ending with 'A'
    val nodesEndingWithA: List<WastelandTreeNode> get() = nodes.filter { node -> node.data.endsWith(A) }

}

private class WastelandTreeNode(
    val data: String,
    private var leftChildNode: WastelandTreeNode? = null,
    private var rightChildNode: WastelandTreeNode? = null
) {

    companion object {
        private const val LEFT = 'L'
    }

    fun setLeftChildNode(node: WastelandTreeNode) {
        leftChildNode = node
    }

    fun setRightChildNode(node: WastelandTreeNode) {
        rightChildNode = node
    }

    private fun toLeftChild(): WastelandTreeNode =
        leftChildNode ?: throw IllegalStateException("There is no Left child for the Node with data $data")

    private fun toRightChild(): WastelandTreeNode =
        rightChildNode ?: throw IllegalStateException("There is no Right child for the Node with data $data")

    fun toNextNode(move: Char): WastelandTreeNode = if (move == LEFT) {
        toLeftChild()
    } else {
        toRightChild()
    }

}

private class WastelandRouteAnalyzer private constructor(
    private val moves: List<Char>,
    private val navigator: WastelandTree
) {
    companion object {
        private const val ZZZ = "ZZZ"
        private const val Z = 'Z'

        fun parse(input: List<String>): WastelandRouteAnalyzer =
            input.splitWhen { it.isEmpty() || it.isBlank() }
                .partition { it.any { str: String -> str.contains("=") } }
                .let { (nodesList: List<Iterable<String>>, movesList: List<Iterable<String>>) ->
                    WastelandRouteAnalyzer(
                        moves = movesList.single().single().map { it },
                        navigator = WastelandTree.parse(nodesList.single())
                    )
                }
    }

    /**
     * Calculates and returns the numbers of steps in [Long] required to reach the destination node provided by
     * the conditional selector [tillNodeCondition].
     */
    private fun getStepCountToReachNode(
        fromNode: WastelandTreeNode,
        tillNodeCondition: (nodeSelector: WastelandTreeNode) -> Boolean
    ): Pair<Long, WastelandTreeNode> = whileLoop(
        loopStartCounter = 0,
        initialResult = 0L to fromNode,
        exitCondition = { _: Int, lastIterationResult: Pair<Long, WastelandTreeNode>? ->
            lastIterationResult?.second?.let(tillNodeCondition) ?: false
        }
    ) { loopCounter: Int, (stepCount: Long, node: WastelandTreeNode) ->
        (loopCounter + 1).rem(moves.size) to (stepCount + 1 to node.toNextNode(moves[loopCounter]))
    }

    /**
     * [Solution for Part-1]
     * Returns the number of steps required to reach the destination node [ZZZ].
     */
    fun getStepCountToReachZZZ(): Long =
        getStepCountToReachNode(fromNode = navigator.nodeAAA) { nodeSelector: WastelandTreeNode ->
            nodeSelector.data == ZZZ
        }.first

    /**
     * [Solution for Part-2]
     * Returns the number of steps required to reach each of the destination nodes ending with [Z] at the same step.
     *
     * Calculated using the Least Common Multiple of all the number of steps required to reach each of the
     * destination nodes ending with [Z].
     */
    fun getStepCountToReachNodesEndingWithZAtSameStep(): Long =
        navigator.nodesEndingWithA.associateWith { wastelandTreeNode ->
            getStepCountToReachNode(wastelandTreeNode) { nodeSelector: WastelandTreeNode ->
                nodeSelector.data.endsWith(Z)
            }.first
        }.values.lcm()

}