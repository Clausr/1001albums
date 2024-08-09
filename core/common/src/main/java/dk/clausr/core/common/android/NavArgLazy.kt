package dk.clausr.core.common.android

fun String.navArg(
    name: String,
    value: String?,
): String {
    return if (value == null) {
        if (!this.contains("{$name}")) error("no such argument $name found!")
        if (!this.contains("$name={$name}")) error("null specified for non-optional argument $name!")
        this.replaceFirst("$name={$name}", "")
    } else {
        this.replaceFirst("{$name}", value)
    }
        .replaceFirst("?&", "?")
        .removeSuffix("?")
        .removeSuffix("&")
}
