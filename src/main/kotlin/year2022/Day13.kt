/**
 * Problem: Day13: Distress Signal
 * https://adventofcode.com/2022/day/13
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseFileHandler
import extensions.splitWhen
import kotlinx.serialization.json.*
import utils.product
import java.util.*

private class Day13 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 13
    println("=====")
    solveActual(1) // 5843
    println("=====")
    solveSample(2) // 140
    println("=====")
    solveActual(2) // 26289
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day13.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day13.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    PacketAnalyzer.parse(input)
        .getSumOfCorrectIndicesOfPacketPairs()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    PacketAnalyzer.parse(input)
        .getDecoderKeyOfDistressSignal(2, 6)
        .also { println(it) }
}

private data class Packet(
    val content: JsonElement,
    val isDivider: Boolean = false
) : Comparable<Packet> {

    companion object {
        fun createDividerPacket(number: Int) = Packet(Json.parseToJsonElement("[[$number]]"), true)
    }

    fun isCorrectOrder(other: Packet): Boolean = this < other

    private fun contentAsJsonArray(): Packet = if (content is JsonArray) {
        this
    } else {
        Packet(JsonArray(listOf(content)))
    }

    fun zip(other: Packet): List<Pair<Packet, Packet>> = contentAsJsonArray().zipInternal(other.contentAsJsonArray())

    fun zipAsStack(other: Packet): LinkedList<Pair<Packet, Packet>> = LinkedList<Pair<Packet, Packet>>(zip(other))

    fun zipAsStackTo(destination: LinkedList<Pair<Packet, Packet>>, other: Packet) = destination.apply {
        addAll(0, zip(other))
    }

    private fun zipInternal(other: Packet): List<Pair<Packet, Packet>> =
        (0 until maxOf(content.jsonArray.size, other.content.jsonArray.size)).map { index ->
            content.jsonArray.getOrNull(index) to other.content.jsonArray.getOrNull(index)
        }.map { pair ->
            Packet(pair.first ?: JsonNull) to Packet(pair.second ?: JsonNull)
        }

    override fun compareTo(other: Packet): Int {
        val packetStack = zipAsStack(other)

        while (packetStack.isNotEmpty()) {
            val (left, right) = packetStack.pop()

            when {
                left.content is JsonNull && right.content is JsonNull -> continue
                left.content is JsonNull && right.content !is JsonNull -> return -1
                left.content !is JsonNull && right.content is JsonNull -> return 1
                left.content is JsonPrimitive && right.content is JsonPrimitive -> return if (left.content.int < right.content.int) {
                    -1
                } else if (left.content.int > right.content.int) {
                    1
                } else {
                    continue
                }

                else -> left.zipAsStackTo(packetStack, right)
            }
        }

        return 0
    }

    override fun toString(): String = content.toString()
}

private class PacketAnalyzer private constructor(
    private val packetPairs: List<Pair<Packet, Packet>>
) {

    companion object {
        fun parse(input: List<String>): PacketAnalyzer = PacketAnalyzer(
            packetPairs = input.splitWhen { it.isEmpty() || it.isBlank() }
                .map { collectionOfMessages ->
                    Packet(Json.parseToJsonElement(collectionOfMessages.first())) to Packet(
                        Json.parseToJsonElement(
                            collectionOfMessages.last()
                        )
                    )
                }
        )
    }

    /**
     * [Solution for Part-1]
     * Returns the sum of indices of those Packet pairs that were in required order.
     */
    fun getSumOfCorrectIndicesOfPacketPairs(): Int = packetPairs.mapIndexed { index, (left, right) ->
        index to left.isCorrectOrder(right)
    }.filter { (_, compareResult) ->
        compareResult
    }.sumOf { (index, _) -> index + 1 }

    /**
     * [Solution for Part-2]
     * Returns the Decoder Key for Distress signal derived from the product of the indices
     * of divider packets constructed for the [dividers] given.
     */
    fun getDecoderKeyOfDistressSignal(vararg dividers: Int): Int =
        (packetPairs.flatMap { it.toList() } + dividers.map { Packet.createDividerPacket(it) }).sortedWith(
            Packet::compareTo
        ).withIndex().filter { (_, packet) ->
            packet.isDivider
        }.map { (index, _) ->
            index + 1
        }.product()
}