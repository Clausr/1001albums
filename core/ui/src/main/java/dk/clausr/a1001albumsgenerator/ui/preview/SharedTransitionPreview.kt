package dk.clausr.a1001albumsgenerator.ui.preview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dk.clausr.a1001albumsgenerator.ui.components.LocalNavAnimatedVisibilityScope
import dk.clausr.a1001albumsgenerator.ui.components.LocalSharedTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PreviewSharedTransitionLayout(content: @Composable () -> Unit) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            AnimatedContent(targetState = true, label = "PreviewSharedTransitionLayout") {
                CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                    if (it) {
                        content()
                    }
                }
            }
        }
    }
}
