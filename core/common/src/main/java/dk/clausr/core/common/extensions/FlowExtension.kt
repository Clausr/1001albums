package dk.clausr.core.common.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Composable
inline fun <reified T> Flow<T>.collectWithLifecycle(
    vararg keys: Any?,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    context: CoroutineContext = EmptyCoroutineContext,
    noinline block: suspend (T) -> Unit,
) {
    LaunchedEffect(this, lifecycleOwner.lifecycle, minActiveState, context, *keys) {
        val callback: suspend (T) -> Unit = {
            if (!isActive) Timber.wtf("collect called while inactive")
            block(it)
        }

        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            if (context == EmptyCoroutineContext) {
                this@collectWithLifecycle.collect(callback)
            } else {
                withContext(context) { this@collectWithLifecycle.collect(callback) }
            }
        }
    }
}
