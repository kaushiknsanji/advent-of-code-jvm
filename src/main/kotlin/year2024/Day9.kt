/**
 * Problem: Day9: Disk Fragmenter
 * https://adventofcode.com/2024/day/9
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2024

import base.BaseFileHandler

private class Day9 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1)      // 1928
    println("=====")
    solveActual(1)      // 6154342787400
    println("=====")
    solveSample(2)      // 2858
    println("=====")
    solveActual(2)      // 6183632723350
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day9.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day9.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    DiskDefragmenter.parse(input)
        .getFilesystemChecksum()
        .also(::println)
}

private fun doPart2(input: List<String>) {
    DiskDefragmenter.parse(input)
        .getFilesystemChecksum(moveWholeBlocks = true)
        .also(::println)
}

private class DiskDefragmenter private constructor(
    private val denseDiskLayout: List<Int>
) {
    companion object {
        private const val FREE_SPACE = "."

        fun parse(input: List<String>): DiskDefragmenter = DiskDefragmenter(input.single().map { it.digitToInt() })
    }

    /**
     * Generates a sparse representation of the given [dense disk layout][denseDiskLayout].
     */
    private fun generateSparseDiskLayout(): List<String> = buildList {
        var fileIdNumber = 0

        denseDiskLayout.forEachIndexed { denseIndex: Int, blockCount: Int ->
            if (denseIndex.rem(2) == 0) {
                // File block
                repeat(blockCount) {
                    add(fileIdNumber.toString())
                }
                fileIdNumber++
            } else {
                // Free space block
                repeat(blockCount) {
                    add(FREE_SPACE)
                }
            }
        }
    }

    /**
     * Compacts File system for the given [sparse representation of disk layout][sparseDiskLayout] and returns
     * result in an [Array].
     *
     * @param moveWholeBlocks [Boolean] that dictates the way file blocks are moved during defragmentation.
     * For Part-1, this will be `false`, which means the process will move file blocks individually. For Part-2,
     * this will be `true`, which means the process will move file blocks of the same ID together as a segment.
     */
    private fun compactFilesystem(sparseDiskLayout: List<String>, moveWholeBlocks: Boolean): Array<String> =
        sparseDiskLayout.toTypedArray().apply {

            // Lambda to swap values at the given indices
            val swap: (index1: Int, index2: Int) -> Unit = { index1, index2 ->
                val temp = this[index1]
                this[index1] = this[index2]
                this[index2] = temp
            }

            if (moveWholeBlocks) {
                // Part-2: Moves file blocks of the same ID together

                // Map containing starting sparse Index of File ID Block with its Block count for each File ID as key
                val fileIdMetaMap: Map<Int, Pair<Int, Int>> = denseDiskLayout.asSequence()
                    .withIndex()
                    .filter { (denseIndex: Int, _: Int) ->
                        // Filter for the blocks that contains files
                        denseIndex.rem(2) == 0
                    }.map { (denseIndex: Int, blockCount: Int) ->
                        // Converting start dense index to start sparse index by calculating sum of all dense indices
                        // till current one, and then pairing it with current Block count
                        if (denseIndex == 0) {
                            0   // Start sparse index is same as start dense index of 0
                        } else {
                            denseDiskLayout.subList(0, denseIndex).sum()
                        } to blockCount
                    }.withIndex().associate { (fileId: Int, startSparseIndexBlockCountPair: Pair<Int, Int>) ->
                        fileId to startSparseIndexBlockCountPair
                    }

                // Iterating over all File IDs starting from last till ID 1 and not 0, since block of File ID 0
                // will already be in the required place
                ((denseDiskLayout.size shr 1) downTo 1).forEach { fileId: Int ->

                    // Get start index and count of blocks for the File with [fileId]
                    val (firstFileBlockIndex, countOfBlocksNeeded) = fileIdMetaMap[fileId]!!

                    // Index to iterate over the array
                    var index = 0
                    // To count contiguous free space blocks found
                    var contiguousFreeSpaceBlockCount = 0
                    // Flag to bail out when required amount of free space blocks are found for moving whole blocks
                    var contiguousFreeSpaceFound = false

                    // Search for free space blocks till the start index of file blocks
                    while (index < firstFileBlockIndex) {
                        if (this[index++] != FREE_SPACE) {
                            // Reset count of free space blocks when we encounter an occupied block
                            contiguousFreeSpaceBlockCount = 0
                        } else {
                            // Increment count of free space blocks when free space is found
                            contiguousFreeSpaceBlockCount++
                        }

                        // When required amount of contiguous free space blocks are found, update flag and bail out
                        if (contiguousFreeSpaceBlockCount == countOfBlocksNeeded) {
                            contiguousFreeSpaceFound = true
                            break
                        }
                    }

                    if (contiguousFreeSpaceFound) {
                        // When required amount of contiguous free space blocks are found, start moving
                        // whole file blocks to this free space

                        // Readjust index to start of the free space blocks
                        index -= countOfBlocksNeeded
                        var fileBlockIndex = firstFileBlockIndex

                        // Swap blocks
                        repeat(countOfBlocksNeeded) {
                            swap(index++, fileBlockIndex++)
                        }
                    }

                }
            } else {
                // Part-1: Moves file blocks individually

                // Indices to iterate over the array from both ends
                var leftIndex = 0
                var rightIndex = this.size - 1

                // Iterate till left index meets right index
                while (leftIndex <= rightIndex) {
                    if (this[leftIndex] != FREE_SPACE) {
                        // When block is occupied, increment to find a free space block
                        leftIndex++
                    } else {
                        // When free space is found at left index, iterate from right to left
                        // to find an index that has a file block
                        while (this[rightIndex] == FREE_SPACE) {
                            rightIndex--
                        }

                        // Swap individual blocks pointed by left index and right index when both indices
                        // have not yet crossed over
                        if (leftIndex <= rightIndex) {
                            swap(leftIndex++, rightIndex--)
                        }
                    }
                }

            }
        }

    /**
     * [Solution for Part 1 and 2]
     *
     * Returns Filesystem checksum after defragmentation.
     *
     * @param moveWholeBlocks [Boolean] that dictates the way file blocks are moved during defragmentation.
     * For Part-1, this will be `false`, which means the process will move file blocks individually. For Part-2,
     * this will be `true`, which means the process will move file blocks of the same ID together as a segment.
     */
    fun getFilesystemChecksum(moveWholeBlocks: Boolean = false): Long =
        compactFilesystem(generateSparseDiskLayout(), moveWholeBlocks)
            .withIndex()
            .filterNot { (_, valueString) -> valueString == FREE_SPACE }
            .sumOf { (index, numberString) ->
                index * numberString.toLong()
            }

}