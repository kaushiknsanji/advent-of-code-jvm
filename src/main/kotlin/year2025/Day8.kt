/**
 * Problem: Day8: Playground
 * https://adventofcode.com/2025/day/8
 *
 * @author <a href='mailto:kaushiknsanji@gmail.com'>Kaushik N Sanji</a>
 */

package year2025

import base.BaseProblemHandler
import extensions.distinctPairs
import utils.findAllPositiveInt
import utils.grid.Point3D
import utils.grid.euclideanDistance
import utils.product

class Day8 : BaseProblemHandler() {

    /**
     * Returns the Package name of this problem class
     */
    override fun getCurrentPackageName(): String = this::class.java.packageName

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
        JunctionCircuitAnalyzer.parse(input)
            .getProductOfTopThreeCircuitSizes(otherArgs[0] as Int)

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        JunctionCircuitAnalyzer.parse(input)
            .getXCoordinateProductOfLastJunctionPairToBuildCompleteCircuit()

    /**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, 40, 10)
        solveActual(1, false, 0, 129564, 1000)
        solveSample(2, false, 0, 25272)
        solveActual(2, false, 0, 42047840)
    }

}

fun main() {
    try {
        Day8().start()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

/**
 * [Point3D] subclass for the location of Junction Box.
 *
 * @param x [Int] X-coordinate value of the location
 * @param y [Int] Y-coordinate value of the location
 * @param z [Int] Z-coordinate value of the location
 */
private class JBoxLocus(x: Int, y: Int, z: Int) : Point3D<Int>(x, y, z)

private class JunctionCircuitAnalyzer private constructor(
    private val junctions: List<JBoxLocus>
) {

    companion object {

        fun parse(input: List<String>): JunctionCircuitAnalyzer = JunctionCircuitAnalyzer(
            junctions = input.map { coordinateString ->
                coordinateString.findAllPositiveInt().let { coordinates: List<Int> ->
                    Point3D.parse(coordinates, ::JBoxLocus)
                }
            }
        )
    }

    // Junctions as Distinct Pairs and sorted by its Euclidean Distance
    private val junctionPairsSortedByDistance: List<Pair<JBoxLocus, JBoxLocus>> by lazy {
        junctions.distinctPairs().sortedBy { junctionPair: Pair<JBoxLocus, JBoxLocus> ->
            junctionPair.first.euclideanDistance(junctionPair.second)
        }
    }

    // List of Circuits of Junctions where Junctions in each circuit is represented by its Hashcode
    private val circuits: MutableList<MutableList<Int>> = mutableListOf()

    /**
     * Returns the Junction [List] Circuit of the given Junction represented by its [Hashcode][JBoxLocus.hashCode].
     * When the given Junction is not found in any of the Circuits formed, it returns `null`.
     *
     * @param junctionHash The hash of the Junction's [JBoxLocus]
     */
    private fun findCircuit(junctionHash: Int): MutableList<Int>? =
        circuits.find { circuit: MutableList<Int> ->
            junctionHash in circuit
        }

    /**
     * Connects Junctions given as a [Pair][junctionPair] to form a Circuit.
     */
    private fun connectJunctions(junctionPair: Pair<JBoxLocus, JBoxLocus>) {
        if (circuits.isEmpty()) {
            // If this is the first Junction pair, just add the Junctions as a new Circuit List to the List of Circuits
            circuits.add(junctionPair.toList().map(JBoxLocus::hashCode).toMutableList())
        } else {
            // If this is NOT the first Junction pair, evaluate to find out corresponding circuits if any
            // and add to them; else create a new circuit.

            // Get Hashcode of first Junction
            val junctionOneHash = junctionPair.first.hashCode()
            // Get circuit of first Junction if any
            val circuitOne = findCircuit(junctionOneHash)

            // Get Hashcode of second Junction
            val junctionTwoHash = junctionPair.second.hashCode()
            // Get circuit of second Junction if any
            val circuitTwo = findCircuit(junctionTwoHash)

            if (circuitOne == null && circuitTwo == null) {
                // When no Circuits are found for the Junctions, just add the Junctions as a new Circuit List
                // to the List of Circuits
                circuits.add(junctionPair.toList().map(JBoxLocus::hashCode).toMutableList())
            } else if (circuitOne == null && circuitTwo != null) {
                // When Circuit of second Junction is found, add the first Junction to the same Circuit List
                // of second Junction as we are connecting these Junctions
                circuitTwo.add(junctionOneHash)
            } else if (circuitOne != null && circuitTwo == null) {
                // When Circuit of first Junction is found, add the second Junction to the same Circuit List
                // of first Junction as we are connecting these Junctions
                circuitOne.add(junctionTwoHash)
            } else {
                // When Circuits of both Junctions are found and are not part of the same Circuit,
                // then connect Circuit-Two to Circuit-One and remove Circuit-Two from the Circuit List
                if (circuitOne != circuitTwo) {
                    circuitOne!!.addAll(circuitTwo!!)
                    circuits.remove(circuitTwo)
                }

                // Nothing needs to be done when Circuits of both Junctions are same, which means these
                // Junction pairs are already connected
            }
        }
    }

    /**
     * Starts building [circuits] for the [required number of shortest connections][requiredCountOfShortestConnections]
     * to be made between Junctions.
     */
    private fun buildCircuits(
        requiredCountOfShortestConnections: Int
    ) {
        junctionPairsSortedByDistance.take(requiredCountOfShortestConnections)
            .forEach(::connectJunctions)
    }

    /**
     * Builds a single complete Circuit of all Junctions.
     *
     * @return The last shortest [Pair] of Junctions that completes the circuit.
     */
    private fun buildCompleteCircuit(): Pair<JBoxLocus, JBoxLocus> {
        // Stores reference to the last pair of Junctions that completes the circuit
        var lastJunctionPair: Pair<JBoxLocus, JBoxLocus>? = null

        // Iterate over all Junction pairs and connect them till we have one Circuit with all the Junctions in it
        for (junctionPair: Pair<JBoxLocus, JBoxLocus> in junctionPairsSortedByDistance) {
            connectJunctions(junctionPair)

            if (circuits.size == 1 && circuits.first().size == junctions.size) {
                lastJunctionPair = junctionPair
                break
            }
        }

        // Return the last pair of Junctions that completes the circuit
        return lastJunctionPair!!
    }

    /**
     * [Solution for Part-1]
     *
     * Returns the product of Top 3 Circuit sizes after building [circuits] for the
     * [required number of shortest connections][requiredCountOfShortestConnections] to be made between Junctions.
     */
    fun getProductOfTopThreeCircuitSizes(
        requiredCountOfShortestConnections: Int
    ): Int {
        // Start building circuits
        buildCircuits(requiredCountOfShortestConnections)

        // Return the product of Top 3 circuit sizes
        return circuits.map(MutableList<Int>::size)
            .sortedDescending()
            .take(3)
            .product()
    }

    /**
     * [Solution for Part-2]
     *
     * Returns product of the X-Coordinates of the last shortest pair of Junctions to complete the circuit
     * with all Junctions in it.
     */
    fun getXCoordinateProductOfLastJunctionPairToBuildCompleteCircuit(): Int =
        buildCompleteCircuit().let { (junctionOne: JBoxLocus, junctionTwo: JBoxLocus) ->
            junctionOne.xPos * junctionTwo.xPos
        }

}