package dk.clausr


import org.gradle.api.GradleException
import org.gradle.api.Project

object GitVersion {

    fun resolveVersionName(project: Project): String {
        return project.gradleProperty("OAG_VERSION_NAME")
            ?: describeGitVersion(project)
    }

    fun resolveVersionCode(project: Project): Int {
        return project.gradleProperty("OAG_VERSION_CODE")
            ?.toInt()
            ?: if (project.isCiBuild) error("OAG_VERSION_CODE must be set on CI builds") else 1
    }

    private val Project.isCiBuild get() = gradleProperty("CI_BUILD") == "true"

    private fun Project.gradleProperty(name: String): String? {
        return providers.gradleProperty(name).getOrNull()?.ifBlank { null }
    }

    private fun describeGitVersion(project: Project): String {
        val nearestTag = execGit(project, "describe", "--tags", "--abbrev=0")

        if (project.isCiBuild) {
            var branch = project.gradleProperty("CI_SOURCEBRANCHNAME").orEmpty()
            val hash = project.gradleProperty("CI_SOURCEVERSION").orEmpty().take(6)
            val buildId = project.gradleProperty("CI_BUILDID").orEmpty()

            if (branch.endsWith("merge")) {
                branch = "pullrequest-${project.gradleProperty("CI_PULLREQUESTNUMBER")}"
            }
            branch = branch.replace("[^0-9a-zA-Z-]".toRegex(), "-")

            val tag = execGit(project, "tag", "-l", "--points-at", "HEAD", "--sort", "creatordate")
                .replace("_", " ")
                .ifBlank { null }

            return tag ?: "$nearestTag+$branch.g$hash.b$buildId"
        } else {
            try {
                val username = execGit(project, "config", "user.email")
                    .replace("[^0-9a-zA-Z-]".toRegex(), "-")
                return "$nearestTag+experimental.$username.localbuild"
            } catch (e: Exception) {
                throw GradleException(
                    """
                    Could not get git user.email

                    ## NOTE! ##
                    You need to configure git user.email and git user.name:
                        git config user.email "your@email.com"
                        git config user.name "User Example"
                    """.trimIndent(),
                    e,
                )
            }
        }
    }

    private fun execGit(
        project: Project,
        vararg args: String,
    ): String {
        val result = project.providers.exec {
            isIgnoreExitValue = true
            commandLine("git", *args)
            workingDir(project.rootDir)
        }
        val output = result.standardOutput.asText.get().trim()
        val error = result.standardError.asText.get().trim()
        val exitCode = result.result.get().exitValue
        if (exitCode != 0) {
            throw GradleException("git failed with exit code $exitCode:\nstdout: $output\nstderr: $error")
        }
        return output
    }
}
