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
        private const val YEAR_IN_PATH = "year"
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
     * Returns the [package][File] from [this] package specific [File] directory that
     * starts with the name [packageNameStart].
     *
     * @throws IllegalStateException when required package having name starting with [packageNameStart] could not
     * be found from [this] package.
     */
    private fun File.getPackage(packageNameStart: String): File {
        // Package reference for finding the required package
        var packageFile = this

        // Repeat till we find the required package
        while (!packageFile.name.startsWith(packageNameStart)) {
            // Move to parent package when required package is not yet found
            packageFile = packageFile.parentFile
        }

        // Ensure the required package is found
        check(packageFile.name.startsWith(packageNameStart)) {
            "Required package having name starting with '$packageNameStart' is not " +
                    "found in the path (${this.path})"
        }

        // Return package found
        return packageFile
    }

    /**
     * Returns the day package specific resources directory if problem classes are kept in their
     * respective day package under the year package; else returns the Class name specific resources directory
     * using the Class name retrieved from [getClassName].
     *
     * @throws IllegalStateException when problem classes are kept in their respective day package but not
     * directly found under the year package; or when problem classes are kept somewhere nested in the year package
     * but does not have a class name that starts as 'Day'.
     */
    private fun getResourcesDirectoryForClass(): File {
        // Package specific resources directory
        val packageFile = getResourcesDirectoryForPackage()

        // Get the year package from package specific resources directory
        val yearPackageFile = packageFile.getPackage(YEAR_IN_PATH)

        // Class name with support for several problem classes of the same day with valid
        // class names of the format "Day1", "Day1_2", "Day1_3" and so on, all belonging to the
        // same day "Day1" as per the example, enabled by considering class name up till underscore if any
        val className = getClassName().lowercase().substringBefore(UNDERSCORE_CHAR)

        // Return the day package specific resources directory if problem classes are kept in their
        // respective day package under the year package; else return the class name specific resources directory
        return if (packageFile.path.contains(DAY_IN_PATH)) {
            // When problem classes are kept in their respective day package

            // Get the day package from package specific resources directory
            val dayPackageFile = packageFile.getPackage(DAY_IN_PATH)

            // Ensure that the day package is the child of the year package
            check(dayPackageFile.parentFile == yearPackageFile) {
                "Day Package '${dayPackageFile.name}' should be directly present under " +
                        "Year Package '${yearPackageFile.name}'"
            }

            // Return the day package specific resources directory
            dayPackageFile
        } else {
            // When problem classes are kept somewhere nested in the year package

            // Ensure that the class name starts as 'Day'
            check(className.startsWith(DAY_IN_PATH)) {
                "Class Name should always start with '${DAY_IN_PATH.replaceFirstChar(Char::uppercaseChar)}', " +
                        "but was '${className.replaceFirstChar(Char::uppercaseChar)}'"
            }

            // Return the class name specific resources directory
            yearPackageFile.resolve(className)
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