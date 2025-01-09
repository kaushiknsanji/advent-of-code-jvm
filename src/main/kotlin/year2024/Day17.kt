/**
 * Problem: Day17: Chronospatial Computer
 * https://adventofcode.com/2024/day/17
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler
import extensions.createRange
import extensions.splitWhen
import org.junit.jupiter.api.Assertions.assertEquals

private class Day17 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    listOf(
        ::solveSample to arrayOf<Any?>(1, "4,6,3,5,6,3,5,2,1,0"),
        ::solveActual to arrayOf<Any?>(1, "6,5,4,7,1,6,0,3,1"),
        ::solveSamplePart2 to arrayOf<Any?>(2, 117440L),
        ::solveActual to arrayOf<Any?>(2, 106086382266778L)
    ).forEach { (solver, args: Array<Any?>) ->
        val result = solver(args[0] as Int).also(::println)

        // Last argument should be the expected value. If unknown, it will be `null`. When known, following statement
        // asserts the `result` with the expected value.
        if (args.last() != null) {
            assertEquals(args.last(), result)
        }
        println("=====")
    }
}

private fun solveSample(executeProblemPart: Int): Any =
    execute(Day17.getSampleFile().readLines(), executeProblemPart)

private fun solveSamplePart2(executeProblemPart: Int): Any =
    execute(Day17.getSampleFile("_part2").readLines(), executeProblemPart)

private fun solveActual(executeProblemPart: Int): Any =
    execute(Day17.getActualTestFile().readLines(), executeProblemPart)

private fun execute(input: List<String>, executeProblemPart: Int): Any =
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
        else -> throw Error("Unexpected Problem Part: $executeProblemPart")
    }

private fun doPart1(input: List<String>): Any =
    ComputerDebugger.parse(input)
        .getOutputAsString()

private fun doPart2(input: List<String>): Any =
    ComputerDebugger.parse(input)
        .getProgramReplicatingLowestRegisterA()


private class ComputerDebugger private constructor(
    private var registerA: Long,
    private var registerB: Long,
    private var registerC: Long,
    private val instructions: List<Int>
) {

    companion object {
        // Regular expression to capture numbers
        private val numberRegex = """(\d+)""".toRegex()

        fun parse(input: List<String>): ComputerDebugger = input.splitWhen { line ->
            line.isEmpty() || line.isBlank()
        }.let { splitBlocks: Iterable<Iterable<String>> ->
            splitBlocks.flatMap { lines ->
                lines.flatMap { line ->
                    numberRegex.findAll(line).map { matchResult ->
                        matchResult.groupValues[1].toInt()
                    }
                }
            }.let { numbers: List<Int> ->
                ComputerDebugger(
                    registerA = numbers[0].toLong(),
                    registerB = numbers[1].toLong(),
                    registerC = numbers[2].toLong(),
                    instructions = numbers.subList(3, numbers.size)
                )
            }
        }
    }

    // Array of Combo operands
    private val comboArray: Array<() -> Long> = arrayOf(
        { 0 },
        { 1 },
        { 2 },
        { 3 },
        { registerA },
        { registerB },
        { registerC },
        { throw Error("Error: RESERVED") }
    )

    // 'adv' instruction
    private val opcodeZero: (combo: Int) -> Int = { combo: Int ->
        registerA = registerA shr comboArray[combo]().toInt()

        // Increment pointer to next instruction
        2
    }

    // 'bxl' instruction
    private val opcodeOne: (literal: Int) -> Int = { literal: Int ->
        registerB = registerB xor literal.toLong()

        // Increment pointer to next instruction
        2
    }

    // 'bst' instruction
    private val opcodeTwo: (combo: Int) -> Int = { combo: Int ->
        registerB = comboArray[combo]() % 8

        // Increment pointer to next instruction
        2
    }

    // 'jnz' Jump instruction
    private val opcodeThree: (literal: Int) -> Int = { literal: Int ->
        if (registerA != 0L) {
            literal
        } else {
            -1
        }
    }

    // 'bxc' instruction
    private val opcodeFour: (literal: Int) -> Int = {
        registerB = registerB xor registerC

        // Increment pointer to next instruction
        2
    }

    // List for numbers output by 'out' instruction opcode
    private val outputNumbers: MutableList<Int> = mutableListOf()

    // 'out' instruction
    private val opcodeFive: (combo: Int) -> Int = { combo: Int ->
        outputNumbers.add((comboArray[combo]() % 8).toInt())

        // Increment pointer to next instruction
        2
    }

    // 'bdv' instruction
    private val opcodeSix: (combo: Int) -> Int = { combo: Int ->
        registerB = registerA shr comboArray[combo]().toInt()

        // Increment pointer to next instruction
        2
    }

    // 'cdv' instruction
    private val opcodeSeven: (combo: Int) -> Int = { combo: Int ->
        registerC = registerA shr comboArray[combo]().toInt()

        // Increment pointer to next instruction
        2
    }

    // Array of Instruction Opcodes
    private val opcodeArray: Array<(Int) -> Int> = arrayOf(
        opcodeZero, opcodeOne, opcodeTwo, opcodeThree, opcodeFour, opcodeFive, opcodeSix, opcodeSeven
    )

    /**
     * Executes all [instructions]
     */
    private fun processInstructions() {
        // Instruction pointer
        var pointer = 0

        // Execute until all instructions are processed
        while (pointer < instructions.size) {
            if (pointer % 2 == 0) {
                // Instruction Opcodes are always present at even number

                // Get the Instruction Opcode
                val instructionOpcode = instructions[pointer]

                if (instructionOpcode == 3) {
                    // When the Instruction Opcode is for Jump instruction

                    // Execute instruction and get the next instruction to jump to
                    val jump = opcodeArray[instructionOpcode].invoke(instructions[pointer + 1])

                    if (jump > -1) {
                        // When Register-A value is NOT 0, pointer will jump
                        pointer = jump
                    } else {
                        // When Register-A value is 0, pointer is updated to next Instruction Opcode
                        pointer += 2
                    }
                } else {
                    // When the Instruction Opcode is other than for Jump instruction

                    // Execute instruction and update pointer to next Instruction Opcode
                    pointer += opcodeArray[instructionOpcode].invoke(instructions[pointer + 1])
                }
            } else {
                // Throw an Error if pointer incorrectly got updated to an odd number
                throw Error("Pointer moved to odd number")
            }
        }
    }

    /**
     * Loads [registerA] with given [testValue], [registerB] to 0, [registerC] to 0, clears [outputNumbers]
     * and then executes all [instructions].
     */
    private fun testRegisterA(testValue: Long) {
        registerA = testValue
        registerB = 0L
        registerC = 0L
        outputNumbers.clear()
        processInstructions()
    }

    /**
     * [Solution for Part-1]
     *
     * Returns [outputNumbers] filled by 'out' instruction, in a comma-separated format.
     */
    fun getOutputAsString(): String =
        processInstructions().let {
            outputNumbers.joinToString(",")
        }

    /**
     * [Solution for Part-2]
     *
     * Returns [instructions] replicating lowest [registerA] value.
     */
    fun getProgramReplicatingLowestRegisterA(): Long =
        instructions.reversed() // Reverse to start replicating output from last instruction
            .fold(listOf(0L)) { candidates: List<Long>, instruction: Int ->
                // A Candidate is a Register-A value that resulted in the output of last iteration's `instruction` value
                // Initial list of Candidates will be just a list containing 0
                candidates.flatMap { candidate: Long ->
                    // As we are backtracking, we reverse whatever Register-A was going through in order to find the
                    // previous Register-A value. So, we multiply the candidate value by 8 (i.e., binary shift left by 3)
                    // and take a range of 8 numbers (3-bit computer) starting from it and test each as Register-A value.
                    // If the first output from 'out' instruction matches the current iteration's `instruction` value,
                    // then this Register-A value becomes the candidate for the next iteration's `instruction`
                    (candidate shl 3).createRange(8).mapNotNull { testValue: Long ->
                        // Load Register-A with the test value and process all instructions
                        testRegisterA(testValue)
                        // Consider this test value as the next iteration's candidate if the first output
                        // from 'out' instruction matches the current iteration's `instruction` value
                        testValue.takeIf { outputNumbers.first() == instruction }
                    }
                }
            }
            .first() // Take the first candidate as this will be the lowest Register-A value to replicate `instructions`

}