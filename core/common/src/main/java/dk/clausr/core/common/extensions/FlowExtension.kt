@file:Suppress("LongParameterList")

package dk.clausr.core.common.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

@Suppress("UNCHECKED_CAST")
fun <T0, T1, T2, T3, T4, T5, R> combine(
    flow0: Flow<T0>,
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    transform: suspend (T0, T1, T2, T3, T4, T5) -> R,
): Flow<R> = combine(flow0, flow1, flow2, flow3, flow4, flow5) { args: Array<*> ->
    val t0 = args[0] as T0
    val t1 = args[1] as T1
    val t2 = args[2] as T2
    val t3 = args[3] as T3
    val t4 = args[4] as T4
    val t5 = args[5] as T5
    transform(t0, t1, t2, t3, t4, t5)
}

@Suppress("UNCHECKED_CAST")
fun <T0, T1, T2, T3, T4, T5, T6, R> combine(
    flow0: Flow<T0>,
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    transform: suspend (T0, T1, T2, T3, T4, T5, T6) -> R,
): Flow<R> = combine(flow0, flow1, flow2, flow3, flow4, flow5, flow6) { args: Array<*> ->
    val t0 = args[0] as T0
    val t1 = args[1] as T1
    val t2 = args[2] as T2
    val t3 = args[3] as T3
    val t4 = args[4] as T4
    val t5 = args[5] as T5
    val t6 = args[6] as T6
    transform(t0, t1, t2, t3, t4, t5, t6)
}

@Suppress("UNCHECKED_CAST")
fun <T0, T1, T2, T3, T4, T5, T6, T7, R> combine(
    flow0: Flow<T0>,
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    transform: suspend (T0, T1, T2, T3, T4, T5, T6, T7) -> R,
): Flow<R> = combine(flow0, flow1, flow2, flow3, flow4, flow5, flow6, flow7) { args: Array<*> ->
    val t0 = args[0] as T0
    val t1 = args[1] as T1
    val t2 = args[2] as T2
    val t3 = args[3] as T3
    val t4 = args[4] as T4
    val t5 = args[5] as T5
    val t6 = args[6] as T6
    val t7 = args[7] as T7
    transform(t0, t1, t2, t3, t4, t5, t6, t7)
}

@Suppress("UNCHECKED_CAST")
fun <T0, T1, T2, T3, T4, T5, T6, T7, T8, R> combine(
    flow0: Flow<T0>,
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    transform: suspend (T0, T1, T2, T3, T4, T5, T6, T7, T8) -> R,
): Flow<R> = combine(flow0, flow1, flow2, flow3, flow4, flow5, flow6, flow7, flow8) { args: Array<*> ->
    val t0 = args[0] as T0
    val t1 = args[1] as T1
    val t2 = args[2] as T2
    val t3 = args[3] as T3
    val t4 = args[4] as T4
    val t5 = args[5] as T5
    val t6 = args[6] as T6
    val t7 = args[7] as T7
    val t8 = args[8] as T8
    transform(t0, t1, t2, t3, t4, t5, t6, t7, t8)
}

@Suppress("UNCHECKED_CAST", "LongParameterList")
fun <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, R> combine(
    flow0: Flow<T0>,
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    transform: suspend (T0, T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R,
): Flow<R> = combine(flow0, flow1, flow2, flow3, flow4, flow5, flow6, flow7, flow8, flow9) { args: Array<*> ->
    val t0 = args[0] as T0
    val t1 = args[1] as T1
    val t2 = args[2] as T2
    val t3 = args[3] as T3
    val t4 = args[4] as T4
    val t5 = args[5] as T5
    val t6 = args[6] as T6
    val t7 = args[7] as T7
    val t8 = args[8] as T8
    val t9 = args[9] as T9
    transform(t0, t1, t2, t3, t4, t5, t6, t7, t8, t9)
}

@Suppress("UNCHECKED_CAST", "LongParameterList")
fun <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> combine(
    flow0: Flow<T0>,
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    flow10: Flow<T10>,
    transform: suspend (T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> R,
): Flow<R> = combine(flow0, flow1, flow2, flow3, flow4, flow5, flow6, flow7, flow8, flow9, flow10) { args: Array<*> ->
    val t0 = args[0] as T0
    val t1 = args[1] as T1
    val t2 = args[2] as T2
    val t3 = args[3] as T3
    val t4 = args[4] as T4
    val t5 = args[5] as T5
    val t6 = args[6] as T6
    val t7 = args[7] as T7
    val t8 = args[8] as T8
    val t9 = args[9] as T9
    val t10 = args[10] as T10
    transform(t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10)
}

@Suppress("UNCHECKED_CAST", "LongParameterList")
fun <T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> combine(
    flow0: Flow<T0>,
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    flow10: Flow<T10>,
    flow11: Flow<T11>,
    transform: suspend (T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) -> R,
): Flow<R> = combine(flow0, flow1, flow2, flow3, flow4, flow5, flow6, flow7, flow8, flow9, flow10, flow11) { args: Array<*> ->
    val t0 = args[0] as T0
    val t1 = args[1] as T1
    val t2 = args[2] as T2
    val t3 = args[3] as T3
    val t4 = args[4] as T4
    val t5 = args[5] as T5
    val t6 = args[6] as T6
    val t7 = args[7] as T7
    val t8 = args[8] as T8
    val t9 = args[9] as T9
    val t10 = args[10] as T10
    val t11 = args[11] as T11
    transform(t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11)
}
