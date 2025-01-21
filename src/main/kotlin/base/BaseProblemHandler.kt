package base

import org.junit.jupiter.api.Assertions.assertEquals
import utils.Constants.EMPTY
import kotlin.time.TimedValue
import kotlin.time.measureTimedValue

/**
 * Abstract [BaseFileHandler] class to facilitate the setup of calling different problem parts
 * for the problem classes that extend this to solve problem inputs read from various input files.
 *
 * Following are valid input filename formats for the problem classes that extend [BaseProblemHandler] -
 * ```
 * 1. sample.txt
 * 2. test.txt
 * 3. sample_1.txt, sample_2.txt, sample_3.txt ... sample_{%d}.txt
 * 4. test_1.txt, test_2.txt, test_3.txt ... test_{%d}.txt
 * 5. sample_part1.txt, sample_part2.txt ... sample_part{%d}.txt
 * 6. test_part1.txt, test_part2.txt ... test_part{%d}.txt
 * 7. sample_part1_1.txt, sample_part1_2.txt, sample_part2_1.txt, sample_part2_2.txt ... sample_part{%d}_{%d}.txt
 * 8. test_part1_1.txt, test_part1_2.txt, test_part2_1.txt, test_part2_2.txt ... test_part{%d}_{%d}.txt
 * ```
 * The values of `includeProblemPartInFileName` and `inputVariantId` parameters passed to
 * [solveSample] and [solveActual] vary as per the input filename format needed -
 * * For 1 & 2 filename formats, `includeProblemPartInFileName` = false and `inputVariantId` = 0
 * * For 3 & 4 filename formats, `includeProblemPartInFileName` = false and `inputVariantId` > 0
 * * For 5 & 6 filename formats, `includeProblemPartInFileName` = true and `inputVariantId` = 0
 * * For 7 & 8 filename formats, `includeProblemPartInFileName` = true and `inputVariantId` > 0
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */
abstract class BaseProblemHandler : BaseFileHandler() {

    companion object {
        // Line printed out after every problem part variant execution
        private const val PROBLEM_PART_VARIANT_DELINEATION = "======"
    }

    /**
     * Enum class for different parts of a problem to solve.
     *
     * @property fileNameSuffix [String] used as suffix or as part of the suffix in the input file name.
     */
    private enum class ProblemPart(val fileNameSuffix: String) {
        PART_1("_part1"),
        PART_2("_part2"),
        PART_3("_part3");

        companion object {
            // Map of Problem Part identifiers to enum
            private val partIdMap: Map<Int, ProblemPart> = entries.associateBy { part ->
                part.name.filter(Char::isDigit).toInt()
            }

            /**
             * Returns [ProblemPart] identified by the given [partId]
             */
            fun fromPartId(partId: Int): ProblemPart = partIdMap[partId]!!
        }
    }

    /**
     * Generates suffix used in the input file name.
     *
     * @param inputVariantId [Int] identifier of the input file variant. When there is no identifier used
     * in the input file name, value passed to this identifier should be 0.
     * @param problemPart [ProblemPart] to solve. Should be `null` if [ProblemPart.fileNameSuffix] need Not be
     * included in the input file name.
     */
    private fun generateSuffix(inputVariantId: Int, problemPart: ProblemPart? = null): String =
        if (problemPart != null) {
            // When Problem part needs to be included in the input file name

            if (inputVariantId > 0) {
                // Suffix generated will be like "_part1_1", "_part2_1" where "_1" is the input variant-Id
                "${problemPart.fileNameSuffix}_$inputVariantId"
            } else {
                // Suffix generated will be like "_part1", "_part2"
                problemPart.fileNameSuffix
            }
        } else {
            // When Problem part need Not be included in the input file name

            if (inputVariantId > 0) {
                // Suffix generated will be like "_1", "_2", "_3" etc.
                "_$inputVariantId"
            } else {
                EMPTY
            }
        }

    /**
     * Calls [execute] with required parameters after reading the given input file.
     *
     * @param executeSample [Boolean] to decide if Sample input file needs to be read and executed or
     * the Actual test input file needs to be read and executed. `true` to read and execute Sample input file;
     * `false` to read and execute Actual test input file.
     * @param executeProblemPart [Int] identifier of the Problem part to be solved.
     * @param includeProblemPartInFileName [Boolean] to indicate if the input file name has Problem part suffix.
     * `true` to include; `false` to exclude.
     * @param inputVariantId [Int] identifier of the input file variant. When there is no identifier used
     * in the input file name, value passed to this identifier should be 0.
     * @param otherArgs [Array] of arguments of nullable type [Any] needed to solve the problem.
     *
     * @return Result of type [Any]
     */
    private fun solve(
        executeSample: Boolean,
        executeProblemPart: Int,
        includeProblemPartInFileName: Boolean,
        inputVariantId: Int,
        otherArgs: Array<out Any?>
    ): Any = ProblemPart.fromPartId(executeProblemPart).let { problemPart ->

        // Generate input file name suffix to be used
        val fileNameSuffix = generateSuffix(
            inputVariantId,
            if (includeProblemPartInFileName) {
                problemPart
            } else {
                null
            }
        )

        // Call `execute` with the input read
        execute(
            input = if (executeSample) {
                getSampleFile(fileNameSuffix).readLines()
            } else {
                getActualTestFile(fileNameSuffix).readLines()
            },
            problemPart = problemPart,
            otherArgs
        )

    }

    /**
     * Calls [solve] with given parameters and then verifies the result of execution.
     *
     * @param executeProblemPart [Int] identifier of the Problem part to be solved.
     * @param includeProblemPartInFileName [Boolean] to indicate if the input file name has Problem part suffix.
     * `true` to include; `false` to exclude.
     * @param inputVariantId [Int] identifier of the input file variant. When there is no identifier used
     * in the input file name, value passed to this identifier should be 0.
     * @param expectedResult Expected result of execution of nullable type [Any]. Can be `null` when unknown.
     * @param otherArgs Vararg list of arguments of nullable type [Any] needed to solve the problem.
     */
    fun solveSample(
        executeProblemPart: Int,
        includeProblemPartInFileName: Boolean,
        inputVariantId: Int,
        expectedResult: Any?,
        vararg otherArgs: Any?
    ) {
        // Result obtained after execution
        val actualResult = solve(
            executeSample = true,
            executeProblemPart,
            includeProblemPartInFileName,
            inputVariantId,
            otherArgs
        ).also(::println)

        // Verify the result
        verify(expectedResult, actualResult)

        // Print delineation
        println(PROBLEM_PART_VARIANT_DELINEATION)
    }

    /**
     * Calls [solve] with given parameters and then verifies the result of execution.
     *
     * @param executeProblemPart [Int] identifier of the Problem part to be solved.
     * @param includeProblemPartInFileName [Boolean] to indicate if the input file name has Problem part suffix.
     * `true` to include; `false` to exclude.
     * @param inputVariantId [Int] identifier of the input file variant. When there is no identifier used
     * in the input file name, value passed to this identifier should be 0.
     * @param expectedResult Expected result of execution of nullable type [Any]. Can be `null` when unknown.
     * @param otherArgs Vararg list of arguments of nullable type [Any] needed to solve the problem.
     */
    fun solveActual(
        executeProblemPart: Int,
        includeProblemPartInFileName: Boolean,
        inputVariantId: Int,
        expectedResult: Any?,
        vararg otherArgs: Any?
    ) {
        // Result obtained after execution
        val actualResult = solve(
            executeSample = false,
            executeProblemPart,
            includeProblemPartInFileName,
            inputVariantId,
            otherArgs
        ).also(::println)

        // Verify the result
        verify(expectedResult, actualResult)

        // Print delineation
        println(PROBLEM_PART_VARIANT_DELINEATION)
    }

    /**
     * Executes the given function [block] inside [measureTimedValue], prints resulting [TimedValue] instance data
     * and returns the [result of the action][TimedValue.value].
     *
     * @param T type of value returned by the given function [block].
     */
    protected inline fun <T> printTimedValue(block: () -> T): T =
        measureTimedValue(block).also(::println).value

    /**
     * Verifies the [actual result][actualResult] of execution with
     * the [expected result][expectedResult] when provided.
     */
    private fun verify(expectedResult: Any?, actualResult: Any) {
        if (expectedResult != null) {
            // When expected result is available, verify the actual result
            assertEquals(expectedResult, actualResult)
        }
    }

    /**
     * Executes required [problemPart] with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    private fun execute(
        input: List<String>,
        problemPart: ProblemPart,
        otherArgs: Array<out Any?>
    ): Any =
        when (problemPart) {
            ProblemPart.PART_1 -> doPart1(input, otherArgs)
            ProblemPart.PART_2 -> doPart2(input, otherArgs)
            ProblemPart.PART_3 -> doPart3(input, otherArgs)
        }

    /**
     * Executes "Part-1" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    abstract fun doPart1(
        input: List<String>,
        otherArgs: Array<out Any?>
    ): Any

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    open fun doPart2(
        input: List<String>,
        otherArgs: Array<out Any?>
    ): Any = {}

    /**
     * Executes "Part-3" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    open fun doPart3(
        input: List<String>,
        otherArgs: Array<out Any?>
    ): Any = {}

}