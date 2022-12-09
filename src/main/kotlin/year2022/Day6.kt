/**
 * Problem: Day6: Tuning Trouble
 * https://adventofcode.com/2022/day/6
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseFileHandler

private class Day6 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)  // 7, 5, 6, 10, 11
    println("=====")
    solveActual(1) // 1848
    println("=====")
    solveSample(2) // 19, 23, 23, 29, 26
    println("=====")
    solveActual(2) // 2308
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day6.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day6.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    input.map { dataStreamBuffer -> DeviceSignalTuner(dataStreamBuffer) }
        .forEach { deviceSignalTuner ->
            println(deviceSignalTuner.getFirstPacketMarkerEndPosition())
        }
}

private fun doPart2(input: List<String>) {
    input.map { dataStreamBuffer -> DeviceSignalTuner(dataStreamBuffer) }
        .forEach { deviceSignalTuner ->
            println(deviceSignalTuner.getFirstMessageMarkerEndPosition())
        }
}

private class DeviceSignalTuner(
    val dataStreamBuffer: String
) {
    companion object {
        const val packetMarkerLength = 4
        const val messageMarkerLength = 14
    }

    private val packetMarker
        get() = dataStreamBuffer.windowedSequence(packetMarkerLength).filter { packet ->
            packet.toSet().size == packet.length
        }.first()

    private val messageMarker
        get() = dataStreamBuffer.windowedSequence(messageMarkerLength).filter { message ->
            message.toSet().size == message.length
        }.first()

    /**
     * [Solution for Part-1]
     * Returns the end position of the first detected start-of-packet marker
     */
    fun getFirstPacketMarkerEndPosition(): Int = dataStreamBuffer.indexOf(packetMarker) + packetMarkerLength

    /**
     * [Solution for Part-2]
     * Returns the end position of the first detected start-of-message marker
     */
    fun getFirstMessageMarkerEndPosition(): Int = dataStreamBuffer.indexOf(messageMarker) + messageMarkerLength
}