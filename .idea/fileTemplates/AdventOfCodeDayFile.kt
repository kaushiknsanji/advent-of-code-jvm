#set($author = "${USER}")
#if (${Email} && ${Email.trim()} != "")
	#set($author = "<a href='mailto:${Email}'>${USER}</a>")
#end
/**
 * Problem: ${NAME}: 
 * 
 *
 * @author ${author}
 */

#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}
#end

import base.BaseProblemHandler

class ${NAME} : BaseProblemHandler() {

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
        TODO("use input to solve the problem")

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        TODO("use input to solve the problem")
		
	/**
     * Called by the `main` function of the problem class to begin solving problem parts
     * with various problem inputs.
     *
     * Call to [solveSample] for sample inputs and [solveActual] for actual inputs, to start solving problem parts.
     *
     * @throws org.opentest4j.AssertionFailedError when any result of execution is not the same as its expected result
     */
    override fun start() {
        solveSample(1, false, 0, null)
//        solveActual(1, false, 0, null)
//        solveSample(2, false, 0, null)
//        solveActual(2, false, 0, null)
    }

}

fun main() {
    ${NAME}().start()
}
