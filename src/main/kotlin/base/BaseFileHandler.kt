package base

import java.io.File

abstract class BaseFileHandler {

    companion object {
        const val resourcesPathStr = "src/main/resources"
        const val inputFileExtension = ".txt"
        const val sampleFileName = "sample"
        const val actualTestFileName = "test"
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

    fun getSampleFile(): File =
        getResourceDirectoryForClass()
            .resolve("$sampleFileName$inputFileExtension")

    fun getSampleFile(suffix: String): File =
        getResourceDirectoryForClass()
            .resolve("$sampleFileName$suffix$inputFileExtension")

    fun getActualTestFile(): File =
        getResourceDirectoryForClass()
            .resolve("$actualTestFileName$inputFileExtension")

    fun getActualTestFile(suffix: String): File =
        getResourceDirectoryForClass()
            .resolve("$actualTestFileName$suffix$inputFileExtension")
}