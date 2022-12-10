/**
 * Problem: Day7: No Space Left On Device
 * https://adventofcode.com/2022/day/7
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */

package year2022

import base.BaseFileHandler
import extensions.whileLoop
import java.util.*

private class Day7 {
    companion object : BaseFileHandler() {
        override fun getCurrentPackageName(): String = this::class.java.`package`.name
        override fun getClassName(): String = this::class.java.declaringClass.simpleName
    }
}

fun main() {
    solveSample(1) // 95437
    println("=====")
    solveActual(1) // 1792222
    println("=====")
    solveSample(2) // 24933642
    println("=====")
    solveActual(2) // 1112963
    println("=====")
}

private fun solveSample(executeProblemPart: Int) {
    execute(Day7.getSampleFile().readLines(), executeProblemPart)
}

private fun solveActual(executeProblemPart: Int) {
    execute(Day7.getActualTestFile().readLines(), executeProblemPart)
}

private fun execute(input: List<String>, executeProblemPart: Int) {
    when (executeProblemPart) {
        1 -> doPart1(input)
        2 -> doPart2(input)
    }
}

private fun doPart1(input: List<String>) {
    DeviceSpaceAnalyzer.create(input)
        .getTotalSizeOfDirsWithMaxSize100000()
        .also { println(it) }
}

private fun doPart2(input: List<String>) {
    DeviceSpaceAnalyzer.create(input)
        .getTotalSizeOfDirToDeleteForReclaimingSpace()
        .also { println(it) }
}

private class DeviceDirectory(
    val dirName: String,
    val parentDir: DeviceDirectory?,
) {
    var size: Long = 0
        private set

    private var oldSize: Long = 0

    val subDirectories: ArrayList<DeviceDirectory> = arrayListOf()
    val files: ArrayList<DeviceFile> = arrayListOf()

    fun add(file: DeviceFile) {
        files.add(file)
        updateSize(file.size)
    }

    fun add(dir: DeviceDirectory) {
        subDirectories.add(dir)
        updateSize(dir.size)
    }

    private fun updateSize(fileDirSize: Long) {
        if (fileDirSize > 0) {
            oldSize = size
            size += fileDirSize
            parentDir?.rebuildSize(oldSize, size)
        }
    }

    fun rebuildSize(subDirOldSize: Long, subDirNewSize: Long) {
        oldSize = size
        size += (subDirNewSize - subDirOldSize)
        parentDir?.rebuildSize(oldSize, size)
    }
}

private class DeviceFile(
    val size: Long
)

private class DeviceFileSystem(
    val rootDir: DeviceDirectory,
    val diskSize: Long = 70000000L
) {
    val usedSpace: Long get() = rootDir.size
    val freeSpace: Long get() = diskSize - usedSpace

    fun getSubDirectory(currentDir: DeviceDirectory, toSubDirName: String): DeviceDirectory =
        if (toSubDirName in currentDir.subDirectories.map { dir -> dir.dirName }) {
            currentDir.subDirectories.single { dir -> dir.dirName == toSubDirName }
        } else throw IllegalArgumentException(
            "${currentDir.dirName} does not contain any subdirectory with the name '${toSubDirName}'"
        )

    fun getSubDirectories(): List<DeviceDirectory> =
        mutableListOf<DeviceDirectory>().apply {
            whileLoop(
                loopStartCounter = 0,
                initialResult = LinkedList<DeviceDirectory>().apply {
                    addAll(rootDir.subDirectories)
                },
                exitCondition = { _: Int, lastIterationResult: LinkedList<DeviceDirectory>? ->
                    lastIterationResult?.size == 0
                }
            ) { loopCounter: Int, dirsToProcess: LinkedList<DeviceDirectory> ->
                val dirToProcess = dirsToProcess.pop().also { add(it) }
                (loopCounter to dirsToProcess.apply { addAll(dirToProcess.subDirectories) })
            }
        }

    fun getAllDirectoriesWithRoot(): List<DeviceDirectory> = mutableListOf(rootDir) + getSubDirectories()
}

private class DeviceShellProcessor private constructor(
    val fileSystem: DeviceFileSystem,
    private var currentDir: DeviceDirectory
) {
    companion object {
        private const val ROOT_DIR_NAME = "/"
        private const val DIR_IDENTIFIER = "dir "
        private const val PROMPT_IDENTIFIER = "$ "
        private const val CHANGE_DIR_CMD = "cd "
        private const val UP_ONE_DIR_CMD_VAL = ".."

        fun init(command: String): DeviceShellProcessor = if (command.startsWith(PROMPT_IDENTIFIER)
            && command.contains(CHANGE_DIR_CMD)
            && command.endsWith(ROOT_DIR_NAME)
        ) {
            DeviceDirectory(
                dirName = ROOT_DIR_NAME,
                parentDir = null
            ).let { rootDir ->
                DeviceShellProcessor(
                    fileSystem = DeviceFileSystem(rootDir),
                    currentDir = rootDir
                )
            }
        } else throw IllegalArgumentException(
            "${DeviceShellProcessor::class.simpleName} needs to be initialized with the command for the root directory, i.e., '$ROOT_DIR_NAME'"
        )
    }

    fun repl(lines: List<String>) {
        lines.forEach { line ->
            process(line)
        }
    }

    private fun process(line: String) {
        if (line.startsWith(PROMPT_IDENTIFIER)) {
            execute(line.substringAfter(PROMPT_IDENTIFIER))
        } else if (line.startsWith(DIR_IDENTIFIER)) {
            // Create and Add SubDirectory
            currentDir.add(
                DeviceDirectory(
                    dirName = line.substringAfter(DIR_IDENTIFIER),
                    parentDir = currentDir
                )
            )
        } else {
            // Create and Add Files
            currentDir.add(
                DeviceFile(
                    size = line.split(" ")[0].toLong()
                )
            )
        }
    }

    private fun execute(command: String) {
        if (command.startsWith(CHANGE_DIR_CMD)) {
            changeDirectory(command.substringAfter(CHANGE_DIR_CMD))
        }
        // No action needed for "ls" command
    }

    private fun changeDirectory(changeTo: String) {
        if (changeTo == UP_ONE_DIR_CMD_VAL) {
            // Traverse to parent directory
            currentDir = currentDir.parentDir!!
        } else {
            // Traverse to the given subdirectory
            currentDir = fileSystem.getSubDirectory(currentDir, changeTo)
        }
    }
}

private class DeviceSpaceAnalyzer private constructor(
    val fileSystem: DeviceFileSystem
) {
    companion object {
        private const val UNUSED_SPACE_NEEDED = 30000000

        fun create(input: List<String>): DeviceSpaceAnalyzer =
            DeviceShellProcessor.init(input[0]).let { processor ->
                // Process all commands
                processor.repl(input.drop(1))
                // Create the Space analyzer
                DeviceSpaceAnalyzer(
                    fileSystem = processor.fileSystem
                )
            }
    }

    /**
     * [Solution for Part-1]
     * Returns the sum of the size of all the directories with a directory size of at most 100000.
     */
    fun getTotalSizeOfDirsWithMaxSize100000(): Long =
        fileSystem.getAllDirectoriesWithRoot().filter { dir -> dir.size <= 100000L }.map { dir -> dir.size }.sum()

    /**
     * [Solution for Part-2]
     * Returns a directory size that can reclaimed to free up the space needed.
     */
    fun getTotalSizeOfDirToDeleteForReclaimingSpace(): Long =
        fileSystem.getAllDirectoriesWithRoot().sortedBy { dir -> dir.size }
            .first { dir -> dir.size >= (UNUSED_SPACE_NEEDED - fileSystem.freeSpace) }
            .size

}