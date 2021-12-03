package base

import java.io.File

abstract class BaseFileHandler {

    companion object {
        const val resourcesPathStr = "src/main/resources"
        const val inputFileExtension = ".txt"
        const val sampleFileName = "sample"
        const val actualTestFileName = "test"
        private const val DEFAULT_EMPTY_FILE_NAME_SUFFIX = ""
    }

    abstract fun getCurrentPackageName(): String

    abstract fun getClassName(): String

    private fun getResourcesDirectory(): File = File(resourcesPathStr)

    private fun getResourceDirectoryForPackage(): File =
        getResourcesDirectory()
            .resolve(getCurrentPackageName())

    private fun getResourceDirectoryForClass(): File =
        getResourceDirectoryForPackage()
            .resolve(getClassName().lowercase())

    fun getSampleFile(suffix: String = DEFAULT_EMPTY_FILE_NAME_SUFFIX): File =
        getResourceDirectoryForClass()
            .resolve("$sampleFileName$suffix$inputFileExtension")

    fun getActualTestFile(suffix: String = DEFAULT_EMPTY_FILE_NAME_SUFFIX): File =
        getResourceDirectoryForClass()
            .resolve("$actualTestFileName$suffix$inputFileExtension")
}