package dk.clausr

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ProductFlavor

enum class FlavorDimension { ContentType }

enum class Flavor(val dimension: FlavorDimension, val applicationIdSuffix: String? = null) {
    Demo(FlavorDimension.ContentType, applicationIdSuffix = ".demo"),
    Prod(FlavorDimension.ContentType),
}

fun configureFlavors(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    flavorConfigurationBlock: ProductFlavor.(flavor: Flavor) -> Unit = {},
) {
    commonExtension.apply {
        flavorDimensions += FlavorDimension.ContentType.name
        productFlavors {
            Flavor.values().forEach {
                create(it.name.replaceFirstChar(Char::lowercase)) {
                    dimension = it.dimension.name
                    flavorConfigurationBlock(this, it)
                    if (this@apply is ApplicationExtension && this is ApplicationProductFlavor) {
                        applicationIdSuffix = it.applicationIdSuffix
                    }
                }
            }
        }
    }
}
