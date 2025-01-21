/**
 * Problem: Day24: Crossed Wires
 * https://adventofcode.com/2024/day/24
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseProblemHandler
import extensions.toIntRanges
import utils.Constants.AND_GATE
import utils.Constants.COLON_STRING
import utils.Constants.COMMA_STRING
import utils.Constants.EMPTY
import utils.Constants.NO_0_CHAR
import utils.Constants.NO_1_CHAR
import utils.Constants.OR_GATE
import utils.Constants.RIGHT_ARROW
import utils.Constants.XOR_GATE
import utils.Constants.X_SMALL_STRING
import utils.Constants.Y_SMALL_STRING
import utils.Constants.Z_SMALL_STRING
import utils.splitContentByWhitespaces
import utils.splitWhenLineBlankOrEmpty
import java.math.BigInteger

private class Day24 : BaseProblemHandler() {

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
        MonitoringDeviceAnalyzer.parse(input)
            .getZWiresDecimalNumber()

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        MonitoringDeviceAnalyzer.parse(input)
            .getOrderedNamesOfWiresInvolvedInASwap()

}

fun main() {
    with(Day24()) {
        solveSample(1, false, 0, BigInteger.valueOf(2024L))
        solveActual(1, false, 0, BigInteger.valueOf(51107420031718L))
        solveActual(2, false, 0, "cpm,ghp,gpr,krs,nks,z10,z21,z33")
    }
}

/**
 * Class for Logic Gate
 *
 * @property wire1Name [String] Name of one input wire
 * @property wire2Name [String] Name of other input wire
 * @property gateName [String] Name of basic Gate (i.e., OR / XOR / AND)
 * @property outputWireName [String] Name of output wire
 *
 * @constructor Constructs a [Gate] with input and output wire names
 */
private data class Gate(
    val wire1Name: String,
    val wire2Name: String,
    val gateName: String,
    val outputWireName: String
) {

    /**
     * Executes Logic Gate and returns its bit result as [Int]
     *
     * @param getValue Lambda for obtaining the bit [Int] value present on input wire with name `wireName`.
     */
    fun execute(getValue: (wireName: String) -> Int): Int =
        when (gateName) {
            OR_GATE -> getValue(wire1Name) or getValue(wire2Name)
            XOR_GATE -> getValue(wire1Name) xor getValue(wire2Name)
            AND_GATE -> getValue(wire1Name) and getValue(wire2Name)
            else -> throw Error("Unidentified Gate '$gateName' found!")
        }

    /**
     * Returns [List] of input wires of this [Gate]
     */
    fun getInputWires(): List<String> = listOf(wire1Name, wire2Name)
}

private class MonitoringDeviceAnalyzer private constructor(
    private val wireMap: MutableMap<String, Int>,
    private val gates: List<Gate>
) {

    companion object {
        private const val WIRE_X = X_SMALL_STRING
        private const val WIRE_Y = Y_SMALL_STRING
        private const val WIRE_Z = Z_SMALL_STRING

        fun parse(input: List<String>): MonitoringDeviceAnalyzer =
            input.splitWhenLineBlankOrEmpty().let { splitBlocks: Iterable<Iterable<String>> ->
                MonitoringDeviceAnalyzer(
                    wireMap = splitBlocks.first().associate { line ->
                        val (wireName, wireStringValue) = line.split(COLON_STRING)
                        wireName to wireStringValue.trim().toInt()
                    }.toMutableMap(),

                    gates = splitBlocks.last().map { line ->
                        val (operationString, outputWireName) = line.split(RIGHT_ARROW)
                        val (wire1Name, gateName, wire2Name) = operationString.splitContentByWhitespaces()
                        Gate(
                            wire1Name,
                            wire2Name,
                            gateName,
                            outputWireName.trim()
                        )
                    }
                )
            }

    }

    /**
     * Executes all [gates] as and when its input values are updated and made available on [wireMap].
     */
    private fun execute() {
        // ArrayDeque to process all gates based on their input availability
        val gateQueue: ArrayDeque<Gate> = ArrayDeque<Gate>().apply { addAll(gates) }

        // Verifies whether both the input wires of a Gate has input values or not
        val wiresHaveValues: (wire1Name: String, wire2Name: String) -> Boolean =
            { wire1Name: String, wire2Name: String ->

                listOf(wire1Name, wire2Name)
                    .map { wireName ->
                        wireMap.getOrDefault(wireName, -1)
                    }.all { value ->
                        value > -1
                    }
            }

        // Execute till the queue becomes empty
        while (gateQueue.isNotEmpty()) {
            // Get the first gate
            val current = gateQueue.removeFirst()

            if (wiresHaveValues(current.wire1Name, current.wire2Name)) {
                // If input wires have values, then execute the gate and update [wireMap] with the
                // resulting value on output wire
                wireMap[current.outputWireName] = current.execute { wireName ->
                    wireMap[wireName]!!
                }
            } else {
                // If input wires are yet to receive their values, then add it back to the queue to process it later
                gateQueue.add(current)
            }
        }
    }

    /**
     * Returns [BigInteger] of the binary number formed by the set of wires identified by their first letter
     * given in [wireIdentifier].
     */
    private fun getWiresDecimalNumber(wireIdentifier: String): BigInteger =
        wireMap.keys.filter { wireName ->
            wireName.startsWith(wireIdentifier)
        }.sortedDescending().map { wireName ->
            wireMap[wireName]!!
        }.joinToString(EMPTY).let { binaryNumberString ->
            BigInteger(binaryNumberString, 2)
        }

    /**
     * Returns [Pair]s of [String] Names of the wires that needs to be swapped so that the logic circuit presented
     * in the input of Part-2 works correctly as a Full Adder.
     *
     * Full Adder is implemented using two Half Adders. Each Half Adder has an XOR Gate for the Sum and
     * an AND gate for the Carry. For every bit after the first bit addition, carry needs to be propagated.
     * This carry from previous bit goes into the current bit's second Half Adder's XOR gate, along with the output
     * of the first Half Adder's XOR gate to generate the Full Sum for the current bit. Previous carry also goes into
     * the second Half Adder's AND gate to generate the second Half Carry, which is combined with the
     * first Half Carry generated by the first Half Adder's AND gate, using an OR Gate to generate the Full Carry
     * for the next bit.
     *
     * @param incorrectIndices [List] of [Int] indices where bits are incorrectly set based on the expected output of
     * a Full Adder.
     */
    private fun getPairsOfWiresToBeSwapped(incorrectIndices: List<Int>): List<Pair<String, String>> {
        // List to store the pairs of wires that needs to be swapped
        val pairsOfWiresToSwap: MutableList<Pair<String, String>> = mutableListOf()

        // Amount of zero padding that will be needed for the names of the bit wires of a bit being checked
        val padSize = wireMap.keys.filter { wireName ->
            wireName.startsWith(WIRE_Z)
        }.maxOf { it }.length - 1

        // Map of input wires ordered by name to the List of Gates having the same input wires
        val orderedInputWiresToGatesMap: Map<Pair<String, String>, List<Gate>> =
            gates.groupBy { gate: Gate ->
                gate.getInputWires().sorted().let { sortedWireNames: List<String> ->
                    sortedWireNames[0] to sortedWireNames[1]
                }
            }

        // Lambda that returns a Gate for the given Gate Name with the names of its input wires
        val getGate: (gateName: String, wire1Name: String, wire2Name: String) -> Gate =
            { gateName: String, wire1Name: String, wire2Name: String ->
                listOf(wire1Name, wire2Name).sorted().let { sortedWireNames: List<String> ->
                    orderedInputWiresToGatesMap[sortedWireNames[0] to sortedWireNames[1]]!!
                }.single { gate ->
                    gate.gateName == gateName
                }
            }

        // Lambda that gets the name of the other input wire for a Gate with its other input wire name
        val getOtherInputWireForGateWithInput: (
            gateName: String,
            inputWireName: String
        ) -> String = { gateName: String, inputWireName: String ->

            gates.filter { gate ->
                gate.gateName == gateName
            }.single { selectedGate ->
                selectedGate.getInputWires().any { wireName -> wireName == inputWireName }
            }.getInputWires().filterNot { wireName ->
                wireName == inputWireName
            }.single()
        }

        // Resolve only the bits at indices where the problem starts and gets carried over to its immediate next bits
        // In other words, convert incorrect indices to ranges and take the first index of each range
        for (indexToResolve in incorrectIndices.toIntRanges().map { range -> range.first }) {
            // Bit identifier of the index being resolved
            val idString = indexToResolve.toString().padStart(padSize, NO_0_CHAR)

            // X, Y and Z wire names of the current bit being resolved
            val xWireName = "$WIRE_X$idString"
            val yWireName = "$WIRE_Y$idString"
            val zWireName = "$WIRE_Z$idString"

            // AND Gate of the First Half Adder
            val xyAndGate = getGate(AND_GATE, xWireName, yWireName)
            // XOR Gate of the First Half Adder
            val xyXorGate = getGate(XOR_GATE, xWireName, yWireName)

            // List to save the wires that needs to be swapped for the current bit
            val incorrectWires: MutableSet<String> = mutableSetOf()

            // The Carry-In wire name for the Carry coming in from the previous bit addition
            val carryInWireName = try {
                // Get the name of the carry-in wire
                getOtherInputWireForGateWithInput(AND_GATE, xyXorGate.outputWireName)
            } catch (e: NoSuchElementException) {
                // Exception occurs when the output wire of First Half Adder XOR Gate was not one of the inputs of
                // the second Half Adder's AND Gate. Mark this as one of the pair of incorrect wires causing problems.
                incorrectWires.add(xyXorGate.outputWireName)

                // In order to find the other wire involved in the swap, we need to find the Carry-in wire from
                // the previous bit addition which does not have any such problems

                // Previous Bit identifier
                val idPreviousString = (indexToResolve - 1).toString().padStart(padSize, NO_0_CHAR)
                // X and Y wire names of the previous bit
                val xPreviousWireName = "$WIRE_X$idPreviousString"
                val yPreviousWireName = "$WIRE_Y$idPreviousString"

                // AND Gate of the previous bit's First Half Adder
                val xyPreviousAndGate = getGate(AND_GATE, xPreviousWireName, yPreviousWireName)
                // XOR Gate of the previous bit's First Half Adder
                val xyPreviousXorGate = getGate(XOR_GATE, xPreviousWireName, yPreviousWireName)

                // The Carry-In wire name for the Carry coming in from the previous to previous bit addition
                val previousCarryInWireName = getOtherInputWireForGateWithInput(
                    AND_GATE,
                    xyPreviousXorGate.outputWireName
                )

                // Half Carry AND Gate of the second Half Adder of the previous bit
                val previousOtherHalfAdderCarryOutAndGate = getGate(
                    AND_GATE,
                    xyPreviousXorGate.outputWireName,
                    previousCarryInWireName
                )

                // Full Carry OR Gate of the previous bit
                val previousCarryOutOrGate = getGate(
                    OR_GATE,
                    xyPreviousAndGate.outputWireName,
                    previousOtherHalfAdderCarryOutAndGate.outputWireName
                )

                // With the Carry-in coming from the previous bit addition, get the other input wire causing problems
                // on the second Half Adder's AND Gate
                val otherWire = getOtherInputWireForGateWithInput(
                    AND_GATE,
                    previousCarryOutOrGate.outputWireName
                )

                // Mark the other input wire found as one of the other pair of incorrect wires causing problems
                incorrectWires.add(otherWire)

                if (incorrectWires.size == 2) {
                    // When we have found a pair of incorrect wires, add it to the main list of incorrect wire pairs
                    pairsOfWiresToSwap.add(incorrectWires.first() to incorrectWires.last())
                    // Continue to the next incorrect bit as we have already found a pair of incorrect wires
                    // causing the problem for the current bit addition
                    continue
                } else {
                    // Throw an Error when incorrect wires could not be determined as we cannot proceed further
                    // in such a peculiar case
                    throw Error("Unknown condition: Incorrect wires could not be determined")
                }
            }

            // Full Sum XOR Gate
            val sumOutXorGate = getGate(XOR_GATE, xyXorGate.outputWireName, carryInWireName)

            // Half Carry AND Gate of the second Half Adder
            val otherHalfAdderCarryOutAndGate = getGate(AND_GATE, xyXorGate.outputWireName, carryInWireName)

            // Full Carry OR Gate
            // Incorrect input wires to this OR Gate can only be caused by the output wires of both the
            // Half Adder's Carry AND Gates which can only be swapped with the current bit's Z-wire
            // as there is no other detectable way it can be messed.
            val carryOutOrGate = if (xyAndGate.outputWireName == zWireName) {
                // When the output wire of the first Half Adder's AND Gate is the current bit's Z-wire

                // Get the actual output wire of first Half Adder's AND Gate using the output wire
                // of second Half Adder's AND Gate which is one of the correct input wire to this Full Carry OR Gate
                val actualWire = getOtherInputWireForGateWithInput(
                    OR_GATE,
                    otherHalfAdderCarryOutAndGate.outputWireName
                )

                // Mark this actual output wire along with the current bit's Z-wire as the pair of
                // incorrect wires causing problems
                incorrectWires.add(actualWire)
                incorrectWires.add(zWireName)

                // Get the Full Carry OR Gate using the above actual wire
                getGate(OR_GATE, actualWire, otherHalfAdderCarryOutAndGate.outputWireName)

            } else if (otherHalfAdderCarryOutAndGate.outputWireName == zWireName) {
                // When the output wire of the second Half Adder's AND Gate is the current bit's Z-wire

                // Get the actual output wire of second Half Adder's AND Gate using the output wire
                // of first Half Adder's AND Gate which is one of the correct input wire to this Full Carry OR Gate
                val actualWire = getOtherInputWireForGateWithInput(OR_GATE, xyAndGate.outputWireName)

                // Mark this actual output wire along with the current bit's Z-wire as the pair of
                // incorrect wires causing problems
                incorrectWires.add(actualWire)
                incorrectWires.add(zWireName)

                // Get the Full Carry OR Gate using the above actual wire
                getGate(OR_GATE, xyAndGate.outputWireName, actualWire)

            } else {
                // When both input wires are correct, get the Full Carry OR Gate using those input wires
                getGate(OR_GATE, xyAndGate.outputWireName, otherHalfAdderCarryOutAndGate.outputWireName)
            }

            // When the output wire of Full Sum XOR Gate is not the required current bit's Z-wire, then mark it as
            // one of the pair of incorrect wires causing problems
            if (sumOutXorGate.outputWireName != zWireName) {
                incorrectWires.add(sumOutXorGate.outputWireName)
            }

            // When the output wire of Full Carry OR Gate is incorrectly the current bit's Z-wire, then mark it as
            // one of the pair of incorrect wires causing problems
            if (carryOutOrGate.outputWireName == zWireName) {
                incorrectWires.add(carryOutOrGate.outputWireName)
            }

            // Throw an Error when exactly two incorrect wires are NOT detected for the
            // current bit resolution
            if (incorrectWires.size != 2) {
                throw Error("Exactly two wires are NOT found to have problems at bit $indexToResolve : $incorrectWires")
            }

            // When exactly two incorrect wires are detected for the current bit resolution, add it to the main list
            // of incorrect wire pairs
            pairsOfWiresToSwap.add(incorrectWires.first() to incorrectWires.last())
        }

        // Return list of incorrect wire pairs found
        return pairsOfWiresToSwap
    }


    /**
     * [Solution for Part-1]
     *
     * Returns [BigInteger] of the binary number formed by the set of wires starting with 'z'.
     */
    fun getZWiresDecimalNumber(): BigInteger =
        execute().let { getWiresDecimalNumber(WIRE_Z) }

    /**
     * [Solution for Part-2]
     *
     * Returns the alpha-numerically ordered names of eight wires involved in a swap, in a comma-separated format.
     *
     * Using the binary numbers formed by the set of wires starting with 'x' and that of 'y', we get their
     * addition result which will be the expected value of 'z'. This result is XOR-ed with binary number formed
     * by the set of wires starting with 'z' in order to find bits that are incorrect, as XOR result is 1 when the bits
     * compared don't match. We reverse this number and get the indices of incorrect bits to find and return
     * the pairs of wires involved in a swap. Pairs of wires found are converted to a list, sorted and joined to return
     * required result in a comma-separated format.
     */
    fun getOrderedNamesOfWiresInvolvedInASwap(): String =
        ((getWiresDecimalNumber(WIRE_X) + getWiresDecimalNumber(WIRE_Y)) xor getZWiresDecimalNumber())
            .toString(2)
            .reversed()
            .withIndex()
            .filter { (_, bitChar) ->
                bitChar == NO_1_CHAR
            }.map { (index, _) ->
                index
            }.let { incorrectIndices: List<Int> ->
                getPairsOfWiresToBeSwapped(incorrectIndices).flatMap { wirePairs ->
                    wirePairs.toList()
                }.sorted().joinToString(COMMA_STRING)
            }

}