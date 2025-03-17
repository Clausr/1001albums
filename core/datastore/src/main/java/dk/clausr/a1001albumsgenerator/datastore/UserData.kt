package dk.clausr.a1001albumsgenerator.datastore

data class UserData(
    val hasOnboarded: Boolean,
    val projectId: String?,
    val groupSlug: String?,
)
