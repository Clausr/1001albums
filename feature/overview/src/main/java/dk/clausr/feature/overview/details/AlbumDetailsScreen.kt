package dk.clausr.feature.overview.details

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dk.clausr.a1001albumsgenerator.analytics.AnalyticsEvent
import dk.clausr.a1001albumsgenerator.ui.components.AlbumCover
import dk.clausr.a1001albumsgenerator.ui.components.LocalNavAnimatedVisibilityScope
import dk.clausr.a1001albumsgenerator.ui.components.LocalSharedTransitionScope
import dk.clausr.a1001albumsgenerator.ui.extensions.TrackScreenViewEvent
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
import dk.clausr.feature.overview.preview.historicAlbumPreviewData
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Composable
fun AlbumDetailsScreen(
    navigateToDetails: (slug: String, list: String) -> Unit,
    onNavigateBack: () -> Unit,
    openLink: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

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

    AlbumDetailsContent(
        modifier = modifier,
        state = state,
        navigateToDetails = navigateToDetails,
        listName = viewModel.listName,
        onNavigateBack = onNavigateBack,
        openLink = openLink,
    )
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun AlbumDetailsContent(
    state: AlbumDetailsViewModel.AlbumDetailsViewState,
    navigateToDetails: (slug: String, list: String) -> Unit,
    openLink: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    listName: String = "List",
) {
    val context = LocalContext.current
    val historicAlbum = state.album
    val animatedContentScope = LocalNavAnimatedVisibilityScope.current
    val hazeState = remember { HazeState() }

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
                            modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                contentPadding = PaddingValues(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                ),
            ) {
                item {
                    DetailsCoverArt(
                        historicAlbum = historicAlbum,
                        animatedAlpha = animatedAlpha,
                        paddingValues = paddingValues,
                        listName = listName,
                        animatedContentScope = animatedContentScope
                    )
                }

                if (historicAlbum?.album != null) {
                    item {
                        AlbumDetails(historicAlbum)
                    }
                    item {
                        LinkButtons(
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
                        modifier = Modifier.padding(
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
                            title = stringResource(R.string.related_albums_title, state.relatedAlbums.first().album.artist),
                            albums = state.relatedAlbums,
                            onClickAlbum = navigateToDetails,
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
) {
    FlowRow(
        modifier = Modifier
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
                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
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
private fun AlbumDetails(historicAlbum: HistoricAlbum) {
    Column(modifier = Modifier.padding(16.dp)) {
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
    historicAlbum: HistoricAlbum?,
    animatedAlpha: Float,
    paddingValues: PaddingValues,
    listName: String,
    animatedContentScope: AnimatedVisibilityScope,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val width = maxWidth
        // Background
        AlbumCover(
            coverUrl = historicAlbum?.album?.imageUrl,
            albumSlug = historicAlbum?.album?.slug,
            shape = RectangleShape,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier
                .graphicsLayer(
                    alpha = animatedAlpha,
                )
                .height(width + paddingValues.calculateTopPadding())
                .blur(radius = 20.dp),
        )

        // Foreground
        AlbumCover(
            coverUrl = historicAlbum?.album?.imageUrl,
            albumSlug = historicAlbum?.album?.slug,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .padding(
                    bottom = 16.dp,
                    top = paddingValues.calculateTopPadding() + 16.dp,
                )
                .sharedElement(
                    sharedContentState = rememberSharedContentState(key = "$listName-cover-${historicAlbum?.album?.slug}"),
                    animatedVisibilityScope = animatedContentScope,
                )
                .shadow(elevation = 8.dp),
        )
    }
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
                        navigateToDetails = { _, _ -> },
                        onNavigateBack = { },
                        openLink = { },
                    )
                }
            }
        }
    }
}
