fun getGitCommitCount(): Int {
    val process = Runtime.getRuntime().exec("git rev-list --count HEAD")
    return process.inputStream.bufferedReader().use { it.readText().trim().toInt() }
}

allprojects {
    val oagVersionName = project.properties["OAG_VERSION_NAME"]?.toString()
    val oagVersionCode = project.properties["OAG_VERSION_CODE"]?.toString()?.ifBlank { null }

    val gitVersionName = if (oagVersionName?.toString()?.isBlank() == true) {
        describe().toString()
    } else {
        project.properties["OAG_VERSION_NAME"]
    }
    val gitVersionCode = Integer.valueOf(oagVersionCode ?: getGitCommitCount().toString())

    extra.set("gitVersionName", gitVersionName)
    extra.set("gitVersionCode", gitVersionCode)
}

fun describe(): String {
    val nearestTag = providers.exec {
        commandLine(
            "git",
            "describe",
            "--tags",
            "--abbrev=0",
        )
    }.standardOutput.asText.get().trim()

    val isCiBuild = project.properties["CI_BUILD"] == true
    if (isCiBuild) {
        // we're building in our CI
        var branch = project.properties["CI_SOURCEBRANCHNAME"] as String
        val hash = (project.properties["CI_SOURCEVERSION"] as String).take(6)
        val buildId = project.properties["CI_BUILDID"]

        // Semver meta only allows 0-9 a-z and -
        branch = branch.replace("[^0-9a-zA-Z-]".toRegex(), "-")

        val tag = providers.exec {
            commandLine(
                "git",
                "tag",
                "-l",
                "--points-at",
                "HEAD",
                "--sort",
                "creatordate",
            )
        }.standardOutput.asText.get()
            .trim()
            .replace("_", " ")
            .ifBlank { null }

        return tag ?: "$nearestTag+$branch.g$hash.b$buildId"
    } else {
        return "$nearestTag.localbuild"
    }
}
