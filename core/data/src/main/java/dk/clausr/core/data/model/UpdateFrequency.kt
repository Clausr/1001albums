package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkUpdateFrequency
import dk.clausr.core.model.UpdateFrequency

fun NetworkUpdateFrequency.asExternalModel(): UpdateFrequency = UpdateFrequency.entries.first { it.name.equals(name, ignoreCase = true) }
