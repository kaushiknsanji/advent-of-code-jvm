/**
 * Problem: ${NAME}: 
 * 
 *
 * @author ${USER}
 */

#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}
#end

import base.BaseFileHandler

private class ${NAME} {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)
    println("=====")
//    solveActual(1)
//    println("=====")
//    solveSample(2)
//    println("=====")
//    solveActual(2)
//    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(${NAME}.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(${NAME}.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    TODO("use input to solve the problem")
}

private fun doPart2(input: List<String>) {
    TODO("use input to solve the problem")
}