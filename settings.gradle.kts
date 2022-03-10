pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Android Stuff"
Settings().includeDirectory(parentDirectories = emptyList(), targetDirectory = rootDir)

private class Settings {

    fun includeDirectory(parentDirectories: List<File>, targetDirectory: File) {
        val entries = targetDirectory.listFiles().orEmpty()
        val files = entries.filter(File::isFile)
        val subDirectories = entries.filter(File::isDirectory)

        val buildFile = files.getBuildFileOrNull()
        val isModule = buildFile != null
        val isRootDirectory = targetDirectory == rootDir
        if (isModule && !isRootDirectory) {
            val moduleName = getModuleName(parentDirectories, targetDirectory)
            include(moduleName)
            project(moduleName).projectDir = targetDirectory
        }

        for (subDirectory in subDirectories) {
            val isExcluded = excludedDirectoryNames.any { excludedDirectoryName ->
                subDirectory.name.equals(excludedDirectoryName)
            }
            if (!isExcluded) {
                includeDirectory(
                    parentDirectories = when {
                        isRootDirectory -> parentDirectories
                        else -> parentDirectories + targetDirectory
                    },
                    targetDirectory = subDirectory
                )
            }
        }
    }

    private val buildFileExtensions = listOf(
        "gradle",
        "gradle.kts",
    )
    private val excludedDirectoryNames = listOf(
        ".git",
        ".idea",
        ".gradle",
        "gradle",
        "build",
    )

    private fun List<File>.getBuildFileOrNull(): File? {
        val files = this
        for (file in files) {
            for (buildFileExtension in buildFileExtensions) {
                val isBuildFile = file.name.endsWith(".$buildFileExtension")
                if (isBuildFile) {
                    return file
                }
            }
        }
        return null
    }

    private fun getModuleName(parentDirectories: List<File>, currentDirectory: File): String {
        val moduleNameBuilder = kotlin.text.StringBuilder()
        for (parentDirectory in parentDirectories) {
            moduleNameBuilder.append(":${parentDirectory.name}")
        }
        moduleNameBuilder.append(":${currentDirectory.name}")
        return moduleNameBuilder.toString()
    }
}