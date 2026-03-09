package dk.clausr

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.ProductFlavor

enum class FlavorDimension { ContentType }

enum class Flavor(val dimension: FlavorDimension, val applicationIdSuffix: String? = null) {
    Demo(FlavorDimension.ContentType, applicationIdSuffix = ".demo"),
    Prod(FlavorDimension.ContentType),
}

fun configureFlavors(
    extension: ApplicationExtension,
    flavorConfigurationBlock: ProductFlavor.(flavor: Flavor) -> Unit = {},
) {
    extension.apply {
        flavorDimensions += FlavorDimension.ContentType.name
        productFlavors {
            Flavor.entries.forEach {
                create(it.name.replaceFirstChar(Char::lowercase)) {
                    dimension = it.dimension.name
                    flavorConfigurationBlock(this, it)
                    if (this is ApplicationProductFlavor) {
                        applicationIdSuffix = it.applicationIdSuffix
                    }
                }
            }
        }
    }
}

fun configureFlavors(
    extension: LibraryExtension,
    flavorConfigurationBlock: ProductFlavor.(flavor: Flavor) -> Unit = {},
) {
    extension.apply {
        flavorDimensions += FlavorDimension.ContentType.name
        productFlavors {
            Flavor.entries.forEach {
                create(it.name.replaceFirstChar(Char::lowercase)) {
                    dimension = it.dimension.name
                    flavorConfigurationBlock(this, it)
                }
            }
        }
    }
}
