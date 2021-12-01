/**
 * Problem: Day14: Docking Data
 * https://adventofcode.com/2020/day/14
 *
 * @author Kaushik N Sanji
 */

package year2020

import base.BaseFileHandler
import java.util.*

private class Day14 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)
    println("=====")
    solveActual(1)
    println("=====")
    solvePart2Sample()
    println("=====")
    solveActual(2)
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day14.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day14.getActualTestFile().readLines(), executeProblemPart)
}

private fun solvePart2Sample() {
    execute(Day14.getSampleFile("_part2").readLines(), 2)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    ValueDecoder.parseToDecoders(input)
        .map { (it as ValueDecoder).processSavingValuesToAddresses() }
        .map { it.finalAddressValueMap }
        .reduce { accMap, nextMap -> nextMap.mapValuesTo(accMap) { it.value } }
        .values
        .sum()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    MemoryAddressDecoder.parseToDecoders(input)
        .map { (it as MemoryAddressDecoder).processSavingValuesToAddresses() }
        .map { it.finalAddressValueMap }
        .reduce { accMap, nextMap -> nextMap.mapValuesTo(accMap) { it.value } }
        .values
        .sum()
        .also { println(it) }
}

private interface Decoder {

    companion object {
        const val BIT_FLOATING = 'X'
        const val BIT_ONE = '1'
        const val BIT_ZERO = '0'
        const val BIT_MASK_SIZE = 36
    }

    val maskPattern: Regex
        get() = """mask = ([X10]+)""".toRegex()

    val addressValuePattern: Regex
        get() = """mem\[(\d+)] = (\d+)""".toRegex()

    fun decode(value: Long, mask: List<IndexedValue<Char>>): LongBitVector {
        val maskedValue = LongBitVector(value, BIT_MASK_SIZE)

        mask.forEach { (index, char: Char) ->
            when (char) {
                BIT_ONE -> maskedValue.set(index)
                BIT_ZERO -> maskedValue.clear(index)
                BIT_FLOATING -> maskedValue.setFloating(index)
            }
        }

        return maskedValue
    }

}

private interface DecoderCreator : Decoder {

    fun createDecoder(input: List<String>): Decoder

    fun parseToDecoders(input: List<String>): List<Decoder> = mutableListOf<Decoder>().apply {
        val addressValueBlockInput: MutableList<String> = mutableListOf()
        input.forEach { line: String ->
            if (line.matches(maskPattern)) {
                addressValueBlockInput.takeUnless { it.isEmpty() }?.let {
                    add(createDecoder(it))
                    it.clear()
                }
            }
            addressValueBlockInput.add(line)
        }
        add(createDecoder(addressValueBlockInput))
    }

    fun getMask(input: String): List<IndexedValue<Char>> =
        maskPattern.find(input)!!.groupValues[1].reversed().withIndex().map { it }

    fun getAddressValueMap(input: List<String>): Map<Long, Long> =
        addressValuePattern.findAll(input.joinToString())
            .associate { it.groupValues[1].toLong() to it.groupValues[2].toLong() }

}

private class ValueDecoder private constructor(
    val mask: List<IndexedValue<Char>>,
    val addressValueMap: Map<Long, Long>
) : Decoder {

    companion object : DecoderCreator {
        override fun createDecoder(input: List<String>): Decoder = ValueDecoder(
            mask = getMask(input[0]),
            addressValueMap = getAddressValueMap(input.slice(1 until input.size))
        )

        override fun getMask(input: String): List<IndexedValue<Char>> {
            return super.getMask(input).filterNot { it.value == Decoder.BIT_FLOATING }
        }
    }

    val finalAddressValueMap = mutableMapOf<Long, Long>()

    private fun maskedValueOf(value: Long): Long = decode(value, mask).toLong(2)

    fun processSavingValuesToAddresses(): ValueDecoder = this.apply {
        addressValueMap.forEach { (address, value) ->
            finalAddressValueMap[address] = maskedValueOf(value)
        }
    }

}

private class MemoryAddressDecoder private constructor(
    val mask: List<IndexedValue<Char>>,
    val addressValueMap: Map<Long, Long>
) : Decoder {

    companion object : DecoderCreator {
        override fun createDecoder(input: List<String>): Decoder = MemoryAddressDecoder(
            mask = getMask(input[0]),
            addressValueMap = getAddressValueMap(input.slice(1 until input.size))
        )

        override fun getMask(input: String): List<IndexedValue<Char>> {
            return super.getMask(input).filterNot { it.value == Decoder.BIT_ZERO }
        }
    }

    val finalAddressValueMap = mutableMapOf<Long, Long>()

    private fun decodedAddressesOf(address: Long): List<Long> {
        val addressVector = decode(address, mask)
        val decodedAddresses = mutableListOf(addressVector)

        addressVector.getBitValueVector().withIndex().filter { it.value == Decoder.BIT_FLOATING }
            .forEach { (bitIndex, _) ->
                val tempAddresses = LinkedList(decodedAddresses)
                decodedAddresses.clear()

                while (!tempAddresses.isEmpty()) {
                    val fluctuatingAddress = tempAddresses.pop()

                    repeat(2) { bit ->
                        decodedAddresses.add(
                            element = fluctuatingAddress.deepCopy().apply {
                                if (bit == 0) {
                                    clear(bitIndex)
                                } else {
                                    set(bitIndex)
                                }
                            }
                        )
                    }
                }
            }

        return decodedAddresses.map { it.toLong(2) }
    }

    fun processSavingValuesToAddresses(): MemoryAddressDecoder = this.apply {
        addressValueMap.forEach { (address, value) ->
            decodedAddressesOf(address).forEach { decodedAddress ->
                finalAddressValueMap[decodedAddress] = value
            }
        }
    }
}

private data class LongBitVector(
    val value: Long,
    val size: Int,
    private val valueVector: CharArray
) {

    constructor(value: Long, size: Int) : this(
        value,
        size,
        CharArray(size) { Decoder.BIT_ZERO }.apply { value.toString(2).reversed().toCharArray(this) }
    )

    fun set(bitIndex: Int) {
        valueVector[bitIndex] = Decoder.BIT_ONE
    }

    fun setFloating(bitIndex: Int) {
        valueVector[bitIndex] = Decoder.BIT_FLOATING
    }

    fun clear(bitIndex: Int) {
        valueVector[bitIndex] = Decoder.BIT_ZERO
    }

    fun getBitValueVector(): CharArray = valueVector

    private fun getBitValueVectorAsString(): String = valueVector.reverseJoinToString()

    fun toLong(radix: Int = 10): Long = getBitValueVectorAsString().toLong(radix)

    fun deepCopy(): LongBitVector = copy(valueVector = valueVector.copyOf())

    private fun CharArray.reverseJoinToString(): String = StringBuilder().apply {
        this@reverseJoinToString.forEach { char: Char ->
            insert(0, char)
        }
    }.toString()

    override fun toString(): String {
        return "$value = ${getBitValueVectorAsString()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LongBitVector

        if (value != other.value) return false
        if (size != other.size) return false
        if (!valueVector.contentEquals(other.valueVector)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + size
        result = 31 * result + valueVector.contentHashCode()
        return result
    }

}