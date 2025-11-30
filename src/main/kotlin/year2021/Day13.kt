/**
 * Problem: Day13: Transparent Origami
 * https://adventofcode.com/2021/day/13
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2021

import base.BaseFileHandler
import utils.grid.Point2D

private class Day13 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)  // 17
    println("=====")
    solveActual(1)  // 850
    println("=====")
    solveSample(2)  // Code revealed is "0"
    println("=====")
    solveActual(2)  // Code revealed is "AHGCPGAU"
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
    OrigamiSolver.parse(input)
        .getCountOfDotsAfterFirstFold()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    OrigamiSolver.parse(input)
        .executeAndRevealCode()
}

private class DotLocus(val x: Int, val y: Int) : Point2D<Int>(x, y) {
    companion object {
        fun parse(coordinatesLine: String): DotLocus = coordinatesLine.split(",").let { coordinates: List<String> ->
            DotLocus(coordinates.first().toInt(), coordinates.last().toInt())
        }
    }
}

private class FoldOperation private constructor(val foldOpCode: String, val foldIndex: Int) {
    companion object {
        const val FOLD_HORIZONTAL = "y"
        const val FOLD_VERTICAL = "x"
        private val validFoldOps get() = listOf(FOLD_VERTICAL, FOLD_HORIZONTAL)

        fun create(foldOpCode: String, foldIndex: Int): FoldOperation = if (foldOpCode in validFoldOps) {
            FoldOperation(foldOpCode, foldIndex)
        } else {
            throw IllegalArgumentException("Provided $foldOpCode is an invalid Fold operation code")
        }

        fun parse(instructionLine: String): FoldOperation =
            instructionLine.substringAfter("fold along ").split("=").let { codeFoldIndexStrings ->
                create(codeFoldIndexStrings.first(), codeFoldIndexStrings.last().toInt())
            }
    }
}

private interface IDotGrid {
    fun getAllDotLocus(): Collection<DotLocus>
    fun getAllFilledDotLocus(): Collection<DotLocus>
    fun getDotLocusOrNull(x: Int, y: Int): DotLocus?
    fun getDotLocus(x: Int, y: Int): DotLocus
    fun executeFirstFoldOperation()
    fun executeAllFoldOperations()
}

private class OrigamiDotGrid private constructor(
    val dots: List<DotLocus>,
    val xMax: Int,
    val yMax: Int,
    val foldInstructions: List<FoldOperation>
) : IDotGrid {

    constructor(dots: List<DotLocus>, foldInstructions: List<FoldOperation>) : this(
        dots,
        xMax = dots.maxOf { dot -> dot.toCoordinateList().first() },
        yMax = dots.maxOf { dot -> dot.toCoordinateList().last() },
        foldInstructions
    )

    companion object {
        const val DOT = '#'
        const val EMPTY = '.'
    }

    private var xFoldMax = xMax
    private var yFoldMax = yMax

    private val dotLocusGridMap: Map<Int, List<DotLocus>> = (0..yMax).flatMap { y ->
        (0..xMax).map { x ->
            dots.firstOrNull { dotLocus -> dotLocus.x == x && dotLocus.y == y } ?: DotLocus(x, y)
        }
    }.groupBy { dotLocus -> dotLocus.y }

    private val dotLocusValueGridMap: MutableMap<DotLocus, Char> =
        getAllDotLocus().associateWith { EMPTY }.toMutableMap().apply {
            dots.forEach { dotLocus ->
                this[dotLocus] = DOT
            }
        }

    override fun getAllDotLocus(): Collection<DotLocus> = dotLocusGridMap.values.flatten().filterNot { dotLocus ->
        dotLocus.x > xFoldMax || dotLocus.y > yFoldMax
    }

    override fun getAllFilledDotLocus(): Collection<DotLocus> = getAllDotLocus().filter { dotLocus ->
        dotLocusValueGridMap[dotLocus] == DOT
    }

    operator fun set(dotLocus: DotLocus, value: Char) {
        dotLocusValueGridMap[dotLocus] = value
    }

    operator fun get(dotLocus: DotLocus): Char = dotLocusValueGridMap[dotLocus]!!

    override fun getDotLocusOrNull(x: Int, y: Int): DotLocus? = if (x > xFoldMax || y > yFoldMax) {
        null
    } else {
        try {
            dotLocusGridMap[y]?.get(x)
        } catch (e: IndexOutOfBoundsException) {
            null
        }
    }

    override fun getDotLocus(x: Int, y: Int): DotLocus =
        getDotLocusOrNull(x, y) ?: throw IllegalArgumentException(
            "${this::class.simpleName} does not have a ${DotLocus::class.simpleName} with a Dot at the given location ($x, $y)"
        )

    private fun Char.overlap(other: Char): Char = if (DOT in listOf(this, other)) {
        DOT
    } else {
        EMPTY
    }

    private fun executeFoldHorizontallyUp(foldIndex: Int) {
        val countOfRowsToMerge = yFoldMax - foldIndex
        (1..countOfRowsToMerge).forEach { mergeIndex ->
            val foldToIndex = foldIndex - mergeIndex
            val foldFromIndex = foldIndex + mergeIndex
            (0..xFoldMax).forEach { x ->
                this[getDotLocus(x, foldToIndex)] =
                    this[getDotLocus(x, foldToIndex)].overlap(this[getDotLocus(x, foldFromIndex)])
            }
        }

        yFoldMax = foldIndex - 1
    }

    private fun executeFoldVerticallyLeft(foldIndex: Int) {
        val countOfColumnsToMerge = xFoldMax - foldIndex
        (1..countOfColumnsToMerge).forEach { mergeIndex ->
            val foldToIndex = foldIndex - mergeIndex
            val foldFromIndex = foldIndex + mergeIndex
            (0..yFoldMax).forEach { y ->
                this[getDotLocus(foldToIndex, y)] =
                    this[getDotLocus(foldToIndex, y)].overlap(this[getDotLocus(foldFromIndex, y)])
            }
        }

        xFoldMax = foldIndex - 1
    }

    private fun executeFoldOperation(foldOperation: FoldOperation) = with(foldOperation) {
        when (foldOpCode) {
            FoldOperation.FOLD_VERTICAL -> executeFoldVerticallyLeft(foldIndex)
            FoldOperation.FOLD_HORIZONTAL -> executeFoldHorizontallyUp(foldIndex)
        }
    }

    override fun executeFirstFoldOperation() = executeFoldOperation(foldInstructions.first())

    override fun executeAllFoldOperations() = foldInstructions.forEach { executeFoldOperation(it) }

    override fun toString(): String =
        (0..yFoldMax).joinToString("\n") { y ->
            (0..xFoldMax).joinToString("") { x ->
                this[getDotLocus(x, y)].toString()
            }
        }
}

private class OrigamiSolver private constructor(
    private val origamiDotGrid: OrigamiDotGrid
) : IDotGrid by origamiDotGrid {
    companion object {
        fun parse(input: List<String>): OrigamiSolver =
            input.indexOfFirst { it.isEmpty() || it.isBlank() }.let { emptyLineIndex ->
                OrigamiSolver(
                    origamiDotGrid = OrigamiDotGrid(
                        dots = input.subList(0, emptyLineIndex)
                            .map { coordinatesLine -> DotLocus.parse(coordinatesLine) },
                        foldInstructions = input.subList(emptyLineIndex + 1, input.lastIndex + 1)
                            .map { line -> FoldOperation.parse(line) }
                    )
                )
            }
    }

    /**
     * [Solution for Part-1]
     * Returns the number of dots present in the Grid after the first fold operation.
     */
    fun getCountOfDotsAfterFirstFold(): Int = run {
        executeFirstFoldOperation()
    }.let {
        getAllFilledDotLocus().count()
    }

    /**
     * [Solution for Part-2]
     * Executes all fold operations on the Grid which then reveals a code when printed.
     */
    fun executeAndRevealCode() {
        executeAllFoldOperations()
        println("Origami code is:\n$origamiDotGrid")
    }
}