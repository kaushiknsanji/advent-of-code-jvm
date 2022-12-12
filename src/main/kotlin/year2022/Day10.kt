/**
 * Problem: Day10: Cathode-Ray Tube
 * https://adventofcode.com/2022/day/10
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseFileHandler

private class Day10 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 13140
    println("=====")
    solveActual(1) // 14860
    println("=====")
    solveSample(2)
    /**
     * ##..##..##..##..##..##..##..##..##..##..
     * ###...###...###...###...###...###...###.
     * ####....####....####....####....####....
     * #####.....#####.....#####.....#####.....
     * ######......######......######......####
     * #######.......#######.......#######.....
     */
    println("=====")
    solveActual(2) // RGZEHURK
    /**
     * ###...##..####.####.#..#.#..#.###..#..#.
     * #..#.#..#....#.#....#..#.#..#.#..#.#.#..
     * #..#.#......#..###..####.#..#.#..#.##...
     * ###..#.##..#...#....#..#.#..#.###..#.#..
     * #.#..#..#.#....#....#..#.#..#.#.#..#.#..
     * #..#..###.####.####.#..#..##..#..#.#..#.
     */
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day10.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day10.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    DeviceSignalTester(instructions = input)
        .readInstructions()
        .getTotalSignalStrength()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    DeviceSignalTester(instructions = input)
        .readInstructions()
        .printCRT()
}

private class DeviceProcessor(
    noOfCycleTests: Int = 6,
    private var register: Int = 1,
    startCycleTest: Int = 20,
    private val cycleTestStep: Int = 40,
    private val crtPixelWidth: Int = 40
) {

    companion object {
        private const val OP_CODE_NOOP = "noop"
        private const val OP_CODE_ADD = "addx"
        private const val CYCLE_NOOP = 1
        private const val CYCLE_ADD = 2
        private const val CRT_PIXEL_LIT = "#"
        private const val CRT_PIXEL_DARK = "."
    }

    private val cycleSignalStrengthMap: MutableMap<Int, Int?> = generateSequence(startCycleTest) { lastCycle ->
        lastCycle + cycleTestStep
    }.take(noOfCycleTests).associateWith { null }.toMutableMap()

    private var cycleCount = 0
    private val cycleSignalStrengthMapSeq = cycleSignalStrengthMap.asSequence()

    private val nextSignalStrengthTest get() = cycleSignalStrengthMapSeq.firstOrNull { signalEntry -> signalEntry.value == null }?.key

    private val spriteList get() = listOf(register - 1, register, register + 1)

    private val crtBuilder: StringBuilder = StringBuilder()

    fun readInstruction(instruction: String) {
        instruction.split(" ").let { splitStrings ->
            when (splitStrings[0]) {
                OP_CODE_NOOP -> doNoop()
                OP_CODE_ADD -> doAdd(splitStrings[1].toInt())
            }
        }
    }

    fun getAllSignalStrengths(): List<Int> = cycleSignalStrengthMap.values.filterNotNull()

    private fun doNoop() {
        testSignalStrength(CYCLE_NOOP, nextSignalStrengthTest)
        updateCycleCount(CYCLE_NOOP)
    }

    private fun doAdd(number: Int) {
        testSignalStrength(CYCLE_ADD, nextSignalStrengthTest)
        updateCycleCount(CYCLE_ADD)
        register += number
    }

    private fun updateCycleCount(opCycleCount: Int) {
        repeat(opCycleCount) {
            drawCRT()
            cycleCount++
        }
    }

    private fun testSignalStrength(opCycleCount: Int, nextSignalStrengthTest: Int?) {
        if (nextSignalStrengthTest != null && cycleCount + opCycleCount >= nextSignalStrengthTest) {
            cycleSignalStrengthMap[nextSignalStrengthTest] = nextSignalStrengthTest * register
        }
    }

    private fun drawCRT() {
        if (cycleCount % crtPixelWidth in spriteList) {
            crtBuilder.append(CRT_PIXEL_LIT)
        } else {
            crtBuilder.append(CRT_PIXEL_DARK)
        }
    }

    fun printCRT() {
        crtBuilder.toString().chunked(crtPixelWidth).forEach { pixelsOnLine ->
            println(pixelsOnLine)
        }
    }
}

private class DeviceSignalTester(
    private val instructions: List<String>,
    private val processor: DeviceProcessor = DeviceProcessor()
) {
    fun readInstructions(): DeviceSignalTester = this.apply {
        instructions.forEach { instruction ->
            processor.readInstruction(instruction)
        }
    }

    /**
     * [Solution for Part-1]
     * Returns the total signal strength of all the signal strengths determined at 6 different clock cycles.
     */
    fun getTotalSignalStrength(): Int = processor.getAllSignalStrengths().sum()

    /**
     * [Solution for Part-2]
     * Draws the message appearing on CRT after evaluating clock cycles with the sprite controlled by the register.
     */
    fun printCRT() {
        processor.printCRT()
    }
}