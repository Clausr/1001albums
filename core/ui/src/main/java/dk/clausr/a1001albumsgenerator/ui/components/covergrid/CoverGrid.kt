package dk.clausr.a1001albumsgenerator.ui.components.covergrid

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme

@Composable
fun CoverGrid(
    modifier: Modifier = Modifier,
    covers: CoverData = CoverData(),
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(initialFirstVisibleItemIndex = Int.MAX_VALUE / 2),
    rowCount: Int = 8,
) {
    LaunchedEffect(Unit) {
        while (true) {
            state.animateScrollBy(
                value = 10f,
                animationSpec = tween(easing = LinearEasing),
            )
        }
    }

    LazyHorizontalStaggeredGrid(
        userScrollEnabled = false,
        modifier = modifier,
        state = state,
        rows = StaggeredGridCells.Fixed(count = rowCount),
    ) {
        items(count = Int.MAX_VALUE - 1) {
            val index = it % covers.covers.size

            AsyncImage(
                model = covers.covers[index],
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
private fun CoverGridPreview() {
    OagTheme {
        Column(Modifier.fillMaxSize()) {
            CoverGrid(modifier = Modifier.fillMaxHeight(0.5f))
        }
    }
}
