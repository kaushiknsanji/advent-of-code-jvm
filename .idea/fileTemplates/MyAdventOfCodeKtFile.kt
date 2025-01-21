/**
 * Problem: ${NAME}: 
 * 
 *
 * @author ${USER}
 */

#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}
#end

import base.BaseProblemHandler

private class ${NAME} : BaseProblemHandler() {

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
        TODO("use input to solve the problem")

    /**
     * Executes "Part-2" of the problem with the [input] read and [other arguments][otherArgs] if any.
     *
     * @return Result of type [Any]
     */
    override fun doPart2(input: List<String>, otherArgs: Array<out Any?>): Any =
        TODO("use input to solve the problem")

}

fun main() {
    with(${NAME}()) {
        solveSample(1, false, 0, null)
//        solveActual(1, false, 0, null)
//        solveSample(2, false, 0, null)
//        solveActual(2, false, 0, null)
    }
}
