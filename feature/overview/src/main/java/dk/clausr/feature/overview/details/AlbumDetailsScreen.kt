package dk.clausr.feature.overview.details

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dk.clausr.a1001albumsgenerator.analytics.AnalyticsEvent
import dk.clausr.a1001albumsgenerator.analytics.LocalAnalyticsHelper
import dk.clausr.a1001albumsgenerator.ui.components.AlbumCover
import dk.clausr.a1001albumsgenerator.ui.components.LocalNavAnimatedVisibilityScope
import dk.clausr.a1001albumsgenerator.ui.components.LocalSharedTransitionScope
import dk.clausr.a1001albumsgenerator.ui.extensions.TrackScreenViewEvent
import dk.clausr.a1001albumsgenerator.ui.extensions.logAlbumSwiped
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.common.ExternalLinks
import dk.clausr.core.common.extensions.openLink
import dk.clausr.core.model.GroupReview
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Rating
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingServices
import dk.clausr.feature.overview.AlbumRow
import dk.clausr.feature.overview.R
import dk.clausr.feature.overview.navigation.AlbumDetailsRoute
import dk.clausr.feature.overview.navigation.AlbumListSource
import dk.clausr.feature.overview.navigation.detailsRoute
import dk.clausr.feature.overview.preview.historicAlbumPreviewData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AlbumDetailsScreen(
    navigateToDetails: (AlbumDetailsRoute) -> Unit,
    onNavigateBack: () -> Unit,
    openLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailsViewModel = hiltViewModel(),
) {
    TrackScreenViewEvent(
        screenName = "Album details",
        extras = listOfNotNull(
            if (viewModel.listName.isNotBlank()) {
                AnalyticsEvent.Param(key = AnalyticsEvent.ParamKeys.ITEM_LIST_NAME, viewModel.listName)
            } else {
                null
            },
        ).toPersistentList(),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val analyticsHelper = LocalAnalyticsHelper.current

    AlbumDetailsContent(
        modifier = modifier,
        state = state,
        navigateToDetails = navigateToDetails,
        listName = viewModel.listName,
        onNavigateBack = onNavigateBack,
        openLink = openLink,
        onAlbumSelected = viewModel::onAlbumSelected,
        consumeSwipeHint = viewModel::consumeSwipeHint,
        onAlbumSwiped = { forward ->
            viewModel.onAlbumSwiped()
            analyticsHelper.logAlbumSwiped(
                listName = viewModel.listSource.name,
                direction = if (forward) "next" else "previous",
            )
        },
    )
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun AlbumDetailsContent(
    state: AlbumDetailsViewModel.AlbumDetailsViewState,
    navigateToDetails: (AlbumDetailsRoute) -> Unit,
    openLink: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    listName: String = "List",
    onAlbumSelected: (HistoricAlbum) -> Unit = {},
    onAlbumSwiped: (forward: Boolean) -> Unit = {},
    consumeSwipeHint: suspend () -> Boolean = { false },
) {
    val context = LocalContext.current
    val historicAlbum = state.album
    val animatedContentScope = LocalNavAnimatedVisibilityScope.current
    val hazeState = remember { HazeState() }
    val listState = rememberLazyListState()

    // Created once the pages have loaded, so it starts on the album that was tapped
    val pagerState = key(state.pages.isNotEmpty()) {
        rememberPagerState(initialPage = state.selectedIndex) { state.pages.size }
    }

    LaunchedEffect(pagerState, state.pages) {
        val pages = state.pages
        if (pages.isEmpty()) return@LaunchedEffect

        // Stay on the same album if the list changes, e.g. after a refresh
        if (pagerState.currentPage != state.selectedIndex) {
            pagerState.scrollToPage(state.selectedIndex)
        }

        // Selects the album as soon as the cover is past halfway, so its details are ready when it lands
        launch {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                pages.getOrNull(page)?.let(onAlbumSelected)
            }
        }

        var previousPage = pagerState.settledPage
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (page != previousPage) {
                onAlbumSwiped(page > previousPage)
                previousPage = page
            }
        }
    }

    // Nudge the cover once the screen has opened, to show that it can be swiped
    val canSwipe = state.pages.size > 1
    LaunchedEffect(pagerState, canSwipe) {
        if (!canSwipe) return@LaunchedEffect
        snapshotFlow { animatedContentScope.transition.run { isRunning || currentState != targetState } }.first { !it }
        delay(SwipeHintDelay)
        if (!pagerState.isScrollInProgress && consumeSwipeHint()) {
            pagerState.nudge()
        }
    }

    val currentAlbumKey = state.pages.getOrNull(pagerState.currentPage)?.key
    // The details trail the current page until they have loaded for it
    val contentReady by rememberUpdatedState(currentAlbumKey == null || currentAlbumKey == state.albumKey)
    val contentFadeIn = remember { Animatable(1f) }

    LaunchedEffect(currentAlbumKey) {
        if (!contentReady) {
            contentFadeIn.snapTo(0f)
            snapshotFlow { contentReady }.first { it }
            contentFadeIn.animateTo(1f, animationSpec = tween(durationMillis = 300))
        }
    }

    // Fades out while the cover is dragged away, and in with the next album's details as that cover comes in
    val contentModifier = Modifier.graphicsLayer {
        val dragDistance = abs(pagerState.currentPageOffsetFraction)
        val dragAlpha = 1f - (dragDistance * 3f).coerceAtMost(1f)
        alpha = if (contentReady) min(dragAlpha, contentFadeIn.value) else 0f
    }

    var enterAnimationRunning by remember { mutableStateOf(false) }
    val fromAlpha = if (LocalInspectionMode.current) 0.75f else 0.1f
    val animatedAlpha by animateFloatAsState(
        targetValue = if (enterAnimationRunning) 0.5f else fromAlpha,
        animationSpec = tween(durationMillis = 2500),
    )

    // Run album cover background animation
    LaunchedEffect(Unit) {
        enterAnimationRunning = true
    }

    with(LocalSharedTransitionScope.current) {
        Scaffold(
            modifier = modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "$listName-bounds-${historicAlbum?.album?.slug}"),
                    animatedVisibilityScope = animatedContentScope,
                ),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    modifier = Modifier.hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.regular(),
                    ) {
                        progressive = HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {
                        Text(
                            modifier = contentModifier.fillMaxWidth(),
                            text = historicAlbum?.album?.name.orEmpty(),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                        }
                    },
                )
            },
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                contentPadding = PaddingValues(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                ),
            ) {
                item {
                    DetailsCoverArt(
                        pages = state.pages,
                        pagerState = pagerState,
                        animatedAlpha = animatedAlpha,
                        paddingValues = paddingValues,
                        listName = listName,
                        animatedContentScope = animatedContentScope,
                        isScrollingVertically = listState.isScrollInProgress,
                    )
                }

                if (historicAlbum?.album != null) {
                    item {
                        AlbumDetails(
                            historicAlbum = historicAlbum,
                            modifier = contentModifier,
                        )
                    }
                    item {
                        LinkButtons(
                            modifier = contentModifier,
                            historicAlbum = historicAlbum,
                            state = state,
                            listName = listName,
                            animatedContentScope = animatedContentScope,
                            openLink = openLink,
                        )
                    }
                }

                item {
                    Column(
                        modifier = contentModifier.padding(
                            vertical = 8.dp,
                            horizontal = 16.dp,
                        ),
                    ) {
                        when (val reviewState = state.reviewViewState) {
                            is AlbumDetailsViewModel.AlbumReviewsViewState.Failed -> {
                                Text(
                                    "Woopsie doopsie daisy..\n${reviewState.error.cause}",
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }

                            AlbumDetailsViewModel.AlbumReviewsViewState.Loading -> {
                                LoadingRow()
                            }

                            AlbumDetailsViewModel.AlbumReviewsViewState.None -> Unit
                            is AlbumDetailsViewModel.AlbumReviewsViewState.Success -> {
                                reviewState.reviews.forEachIndexed { index, review ->
                                    Text(
                                        text = "${review.author} ${review.rating?.ratingText(context).orEmpty()}",
                                        style = MaterialTheme.typography.labelLarge,
                                    )
                                    if (review.review?.isNotBlank() == true) {
                                        Text(
                                            text = "${review.review}",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .skipToLookaheadSize(),
                                            style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                                        )
                                    }
                                    if (index < reviewState.reviews.size) {
                                        Spacer(Modifier.height(8.dp))
                                    }
                                }
                                AnimatedVisibility(reviewState.loading) {
                                    LoadingRow()
                                }
                            }
                        }
                    }
                }

                if (state.relatedAlbums.isNotEmpty()) {
                    item {
                        AlbumRow(
                            modifier = contentModifier,
                            title = stringResource(R.string.related_albums_title, state.relatedAlbums.first().album.artist),
                            albums = state.relatedAlbums,
                            onClickAlbum = { album, listName ->
                                navigateToDetails(
                                    album.detailsRoute(
                                        listName = listName,
                                        source = AlbumListSource.Related,
                                        relatedTo = state.albumKey,
                                    ),
                                )
                            },
                            streamingPlatform = state.streamingPlatform,
                            tertiaryTextTransform = { "${it.metadata?.rating?.ratingText(context)}\n${it.album.releaseDate}" },
                            onClickPlay = { context.openLink(it) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedTransitionScope.LinkButtons(
    historicAlbum: HistoricAlbum,
    state: AlbumDetailsViewModel.AlbumDetailsViewState,
    listName: String,
    animatedContentScope: AnimatedVisibilityScope,
    openLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
    ) {
        IconButton(onClick = { openLink(historicAlbum.album.wikipediaUrl) }) {
            Icon(
                painter = painterResource(id = dk.clausr.a1001albumsgenerator.ui.R.drawable.ic_wiki),
                contentDescription = "Wikipedia",
            )
        }

        StreamingServices.from(historicAlbum.album)
            .getStreamingLinkFor(state.streamingPlatform)
            ?.let { streamingLink ->
                FilledTonalButton(
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "$listName-play-${historicAlbum.album.slug}"),
                            animatedVisibilityScope = animatedContentScope,
                            resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
                        ),
                    onClick = {
                        openLink(streamingLink)
                    },
                ) {
                    Icon(
                        modifier = Modifier.padding(end = 8.dp),
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                    )
                    Text(text = stringResource(R.string.play_button_title))
                }
            }

        state.historyLink?.let { historyLink ->
            IconButton(onClick = { openLink(historyLink) }) {
                Icon(
                    painter = painterResource(id = dk.clausr.a1001albumsgenerator.ui.R.drawable.ic_open_external),
                    contentDescription = "Open in history",
                )
            }
        }
    }
}

@Composable
private fun AlbumDetails(
    historicAlbum: HistoricAlbum,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = historicAlbum.album.artist,
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = historicAlbum.album.releaseDate,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SharedTransitionScope.DetailsCoverArt(
    pages: ImmutableList<HistoricAlbum>,
    pagerState: PagerState,
    animatedAlpha: Float,
    paddingValues: PaddingValues,
    listName: String,
    animatedContentScope: AnimatedVisibilityScope,
    isScrollingVertically: Boolean,
    modifier: Modifier = Modifier,
) {
    val topBarHeight = remember { paddingValues.calculateTopPadding() }
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        // Background. Stays in place and blends into the next album's cover as the foreground is swiped
        val position = { pagerState.currentPage + pagerState.currentPageOffsetFraction }
        val fromPage by remember(pagerState) { derivedStateOf { floor(position()).toInt() } }
        Box(
            modifier = Modifier
                .graphicsLayer(alpha = animatedAlpha)
                .fillMaxWidth()
                .height(maxWidth + topBarHeight)
                .blur(radius = 20.dp),
        ) {
            pages.getOrNull(fromPage)?.let { album ->
                key(album.key) {
                    BackgroundCover(historicAlbum = album)
                }
            }
            pages.getOrNull(fromPage + 1)?.let { album ->
                key(album.key) {
                    BackgroundCover(
                        historicAlbum = album,
                        modifier = Modifier.graphicsLayer { alpha = position() - fromPage },
                    )
                }
            }
        }

        // Foreground
        val coverModifier = Modifier
            .padding(horizontal = 32.dp)
            .aspectRatio(1f)
        if (pages.isEmpty()) {
            // Keeps the space while loading, without flashing the placeholder cover
            Spacer(
                modifier = Modifier
                    .padding(top = topBarHeight + 32.dp)
                    .then(coverModifier),
            )
        } else {
            // The covers shrink a bit from the moment they're touched, and the selected one grows back once it settles.
            // Not when the touch turns into scrolling the page, though.
            var isCoverPressed by remember { mutableStateOf(false) }
            val isCoverHeld = isCoverPressed && !isScrollingVertically
            val coverScale by animateFloatAsState(
                targetValue = if ((isCoverHeld || pagerState.isScrollInProgress) && pages.size > 1) 0.9f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .padding(top = topBarHeight + 32.dp)
                    .pointerInput(Unit) {
                        // Only observes the touches, so the swiping and scrolling work as usual
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            isCoverPressed = true
                            try {
                                do {
                                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                } while (event.changes.any { it.pressed })
                            } finally {
                                isCoverPressed = false
                            }
                        }
                    },
                key = { page -> pages[page].key.toString() },
            ) { page ->
                val album = pages[page].album
                AlbumCover(
                    coverUrl = album.imageUrl,
                    albumSlug = album.slug,
                    modifier = coverModifier
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "$listName-cover-${album.slug}"),
                            animatedVisibilityScope = animatedContentScope,
                        )
                        .graphicsLayer {
                            scaleX = coverScale
                            scaleY = coverScale
                        }
                        .shadow(elevation = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun BackgroundCover(
    historicAlbum: HistoricAlbum,
    modifier: Modifier = Modifier,
) {
    AlbumCover(
        coverUrl = historicAlbum.album.imageUrl,
        albumSlug = historicAlbum.album.slug,
        shape = RectangleShape,
        contentScale = ContentScale.FillHeight,
        modifier = modifier.fillMaxHeight(),
    )
}

private val SwipeHintDelay = 600.milliseconds

/** A small fake drag towards the next page, or the previous one on the last page. */
private suspend fun PagerState.nudge() {
    val startPage = currentPage
    val direction = if (startPage < pageCount - 1) 1 else -1
    animateScrollBy(
        value = direction * layoutInfo.pageSize * 0.15f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
    )
    animateScrollToPage(
        page = startPage,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
    )
}

@Composable
private fun LoadingRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

private fun Rating?.ratingText(context: Context): String {
    return when (this) {
        Rating.DidNotListen -> context.getString(R.string.rating_did_not_listen)
        is Rating.Rated -> List(size = rating) { "⭐" }.joinToString("")
        Rating.Unrated -> context.getString(R.string.rating_text_unrated)
        else -> ""
    }
}

@Preview
@Composable
private fun DetailsPreview() {
    OagTheme {
        AnimatedVisibility(visible = true) {
            SharedTransitionLayout {
                CompositionLocalProvider(
                    LocalNavAnimatedVisibilityScope provides this@AnimatedVisibility,
                    LocalSharedTransitionScope provides this,
                ) {
                    AlbumDetailsContent(
                        state = AlbumDetailsViewModel.AlbumDetailsViewState(
                            pages = persistentListOf(historicAlbumPreviewData()),
                            albumKey = historicAlbumPreviewData().key,
                            album = historicAlbumPreviewData(),
                            streamingPlatform = StreamingPlatform.Tidal,
                            relatedAlbums = persistentListOf(
                                historicAlbumPreviewData(slug = "1"),
                                historicAlbumPreviewData(slug = "2"),
                            ),
                            historyLink = ExternalLinks.Generator.BASE_URL,
                            reviewViewState = AlbumDetailsViewModel.AlbumReviewsViewState.Success(
                                listOf(
                                    GroupReview(
                                        author = "oag_user",
                                        rating = Rating.Rated(5),
                                        review = "Some preview review",
                                    ),
                                    GroupReview(
                                        author = "oag_user1",
                                        rating = Rating.Rated(2),
                                        review = "Some bad preview review :(",
                                    ),
                                ),
                            ),
                        ),
                        navigateToDetails = { },
                        onNavigateBack = { },
                        openLink = { },
                    )
                }
            }
        }
    }
}
