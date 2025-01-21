package base

import utils.Constants.DOT_CHAR
import utils.Constants.EMPTY
import utils.Constants.SLASH_CHAR
import utils.Constants.UNDERSCORE_CHAR
import java.io.File

/**
 * Abstract class to facilitate retrieval of input files for the problem classes that extend this.
 *
 * @author <a href="kaushiknsanji@gmail.com">Kaushik N Sanji</a>
 */
abstract class BaseFileHandler {

    companion object {
        private const val RESOURCES_PATH = "src/main/resources"
        private const val INPUT_FILE_EXT = ".txt"
        private const val SAMPLE_FILE_NAME = "sample"
        private const val ACTUAL_TEST_FILE_NAME = "test"
        private const val DAY_IN_PATH = "day"
    }

    /**
     * To be implemented by the problem class to retrieve its Package name
     *
     * Returns the Package name of this problem class
     */
    abstract fun getCurrentPackageName(): String

    /**
     * To be implemented by the problem class to retrieve its Class name
     *
     * Returns the Class name of this problem class
     */
    abstract fun getClassName(): String

    /**
     * Returns the [File] directory to the input file resources [RESOURCES_PATH]
     */
    private fun getResourcesDirectory(): File = File(RESOURCES_PATH)

    /**
     * Returns the Package specific [File] directory under the [input file resources][getResourcesDirectory]
     * directory using the Package name retrieved from [getCurrentPackageName]
     */
    private fun getResourcesDirectoryForPackage(): File =
        getResourcesDirectory()
            .resolve(
                // Replace all dots with slash in package name if any, which occurs when problem classes are kept
                // in their respective day package under the year package
                getCurrentPackageName().replace(DOT_CHAR, SLASH_CHAR)
            )

    /**
     * Returns [Package specific resources][getResourcesDirectoryForPackage] directory if problem classes
     * are kept in their respective day package under the year package; else returns the
     * Class name specific resources directory using the Class name retrieved from [getClassName]
     */
    private fun getResourcesDirectoryForClass(): File {
        // Package specific resources directory
        val packageFile = getResourcesDirectoryForPackage()

        // Class name with support for several problem classes of the same day with valid
        // class names of the format "Day1", "Day1_2", "Day1_3" and so on, all belonging to the
        // same day "Day1" as per the example, enabled by considering class name up till underscore if any
        val className = getClassName().lowercase().substringBefore(UNDERSCORE_CHAR)

        // Return package specific resources directory if problem classes are kept in their respective day package
        // under the year package; else return the class name specific resources directory
        return if (packageFile.absolutePath.contains(DAY_IN_PATH)) {
            packageFile
        } else {
            packageFile.resolve(className)
        }
    }

    /**
     * Returns the input sample [File] for the problem class. File should be named as `sample.txt`. If you have
     * several input sample files, then that can be managed by specifying the [suffix] like `_part2`
     * for a file with name as `sample_part2.txt`.
     *
     * [suffix] is defaulted to an empty string.
     */
    fun getSampleFile(suffix: String = EMPTY): File =
        getResourcesDirectoryForClass()
            .resolve("$SAMPLE_FILE_NAME$suffix$INPUT_FILE_EXT")

    /**
     * Returns the input test [File] for the problem class. File should be named as `test.txt`. If you have
     * several input test files, then that can be managed by specifying the [suffix] like `_part2`
     * for a file with name as `test_part2.txt`.
     *
     * [suffix] is defaulted to an empty string.
     */
    fun getActualTestFile(suffix: String = EMPTY): File =
        getResourcesDirectoryForClass()
            .resolve("$ACTUAL_TEST_FILE_NAME$suffix$INPUT_FILE_EXT")

}