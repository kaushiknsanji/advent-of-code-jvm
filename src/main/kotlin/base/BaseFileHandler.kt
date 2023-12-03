package base

import java.io.File

/**
 * Abstract class to facilitate retrieval of input files for the problem classes that extend this.
 * Recommended to be extended by the `Companion` of the problem class.
 */
abstract class BaseFileHandler {

    companion object {
        private const val RESOURCES_PATH = "src/main/resources"
        private const val INPUT_FILE_EXT = ".txt"
        private const val SAMPLE_FILE_NAME = "sample"
        private const val ACTUAL_TEST_FILE_NAME = "test"
        private const val DEFAULT_EMPTY_FILE_NAME_SUFFIX = ""
    }

    /**
     * To be implemented by the problem class to retrieve its Package name
     */
    abstract fun getCurrentPackageName(): String

    /**
     * To be implemented by the problem class to retrieve its Class name
     */
    abstract fun getClassName(): String

    /**
     * Returns the [File] directory to the input file resources [RESOURCES_PATH]
     */
    private fun getResourcesDirectory(): File = File(RESOURCES_PATH)

    /**
     * Returns the Package specific [File] directory under the input file resources [getResourcesDirectory] directory
     * using the Package name retrieved from [getCurrentPackageName]
     */
    private fun getResourceDirectoryForPackage(): File =
        getResourcesDirectory()
            .resolve(getCurrentPackageName())

    /**
     * Returns the Class specific [File] directory under the Package specific [getResourceDirectoryForPackage] resources
     * directory using the Class name retrieved from [getClassName]
     */
    private fun getResourceDirectoryForClass(): File =
        getResourceDirectoryForPackage()
            .resolve(getClassName().lowercase())

    /**
     * Returns the input sample [File] for the problem class. File should be named as `sample.txt`. If you have
     * several input sample files, then that can be managed by specifying the [suffix] like `_part2` for a file
     * with name as `sample_part2.txt`.
     */
    fun getSampleFile(suffix: String = DEFAULT_EMPTY_FILE_NAME_SUFFIX): File =
        getResourceDirectoryForClass()
            .resolve("$SAMPLE_FILE_NAME$suffix$INPUT_FILE_EXT")

    /**
     * Returns the input test [File] for the problem class. File should be named as `test.txt`. If you have
     * several input test files, then that can be managed by specifying the [suffix] like `_part2` for a file
     * with name as `test_part2.txt`.
     */
    fun getActualTestFile(suffix: String = DEFAULT_EMPTY_FILE_NAME_SUFFIX): File =
        getResourceDirectoryForClass()
            .resolve("$ACTUAL_TEST_FILE_NAME$suffix$INPUT_FILE_EXT")
}