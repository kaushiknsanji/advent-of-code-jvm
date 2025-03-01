package utils.test

import base.BaseProblemHandler
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Utility Object for testing Day Problem Classes.
 *
 * @author <a href='mailto:kaushiknsanji@gmail.com'>Kaushik N Sanji</a>
 */
object ProblemClassTester {

    /**
     * Calls the specified function [block] and returns its encapsulated [Result] if invocation was successful,
     * catching any [Throwable] exception that was thrown from the [block] function execution and encapsulating
     * it as a failure.
     *
     * Returned [Result] also prints exception message and stack trace [on Failure][Result.onFailure].
     */
    private inline fun runCatchingPrintFailure(block: () -> Unit): Result<Unit> = runCatching { block() }.apply {
        // Print exception message and stack trace on Failure
        this.onFailure { exception: Throwable ->
            println("Test failed with exception: ${exception.message}")
            exception.printStackTrace()
        }
    }

    /**
     * Tests given [Day Problem Class][dayProblem]
     */
    fun testDay(dayProblem: BaseProblemHandler) {
        println("\n*** ${dayProblem.getClassName()} ***")
        assertTrue(runCatchingPrintFailure { dayProblem.start() }.isSuccess)
    }

}