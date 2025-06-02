package dk.clausr.feature.overview.details

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.placeholder
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
import dk.clausr.core.common.extensions.openLink
import dk.clausr.core.model.GroupReview
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

    AlbumDetailsContent(
        modifier = modifier,
        state = state,
        navigateToDetails = navigateToDetails,
        listName = viewModel.listName,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun AlbumDetailsContent(
    state: AlbumDetailsViewModel.AlbumDetailsViewState,
    navigateToDetails: (slug: String, list: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    listName: String = "List",
) {
    val context = LocalContext.current
    val historicAlbum = state.album
    val animatedContentScope = LocalNavAnimatedVisibilityScope.current
    val hazeState = remember { HazeState() }

    var something by remember { mutableStateOf(false) }
//    var coverBackgroundAlpha by remember { mutableFloatStateOf(0f) }
    val fromAlpha = if (LocalInspectionMode.current) 0.75f else 0.1f
    val animatedThing by animateFloatAsState(
        targetValue = if (something) 0.5f else fromAlpha,
        animationSpec = tween(durationMillis = 5000)
    )
    val scale by animateFloatAsState(targetValue = if (something) 1f else 0.8f, animationSpec = tween(durationMillis = 5000))
    LaunchedEffect(Unit) {
//        coverBackgroundAlpha = 1f
        something = true
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
                            text = historicAlbum?.album?.artist.orEmpty(),
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
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        // Background
//                        AlbumCover(
//                            coverUrl = historicAlbum?.album?.imageUrl,
//                            albumSlug = historicAlbum?.album?.slug,
//                            shape = RectangleShape,
//                            contentScale = ContentScale.Crop,
//                            modifier = Modifier
//                                .widthIn(maxWidth)
//                                .graphicsLayer(
//                                    alpha = animatedThing,
////                                    clip = true,
////                                    translationY = -paddingValues.calculateTopPadding().value,
//                                )
////                                .heightIn(min = maxWidth + paddingValues.calculateTopPadding())
////                                .padding(bottom = 16.dp),
//                                .blur(radius = 20.dp),
//                        )

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

                item {
                    Column {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = historicAlbum?.album?.name.orEmpty(),
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                        )

                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            text = historicAlbum?.album?.releaseDate.orEmpty(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                if (historicAlbum?.album != null) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    context.openLink(historicAlbum.album.wikipediaUrl)
                                },
                            ) {
                                Icon(
                                    modifier = Modifier.padding(end = 8.dp),
                                    painter = painterResource(id = dk.clausr.a1001albumsgenerator.ui.R.drawable.ic_wiki),
                                    contentDescription = "Wikipedia",
                                )
                                Text(text = stringResource(R.string.wikipedia_button_title))
                            }

                            StreamingServices.from(historicAlbum.album)
                                .getStreamingLinkFor(state.streamingPlatform)
                                ?.let { streamingLink ->
                                    FilledTonalButton(
                                        modifier = Modifier.sharedBounds(
                                            sharedContentState = rememberSharedContentState(key = "$listName-play-${historicAlbum.album.slug}"),
                                            animatedVisibilityScope = animatedContentScope,
                                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
                                        ),
                                        onClick = {
                                            context.openLink(streamingLink)
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
                        }
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
                            title = stringResource(R.string.related_albums_title),
                            albums = state.relatedAlbums,
                            onClickAlbum = onNavigateToDetails,
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
                        onNavigateToDetails = { _, _ -> },
                        onNavigateBack = {},
                    )
                }
            }
        }
    }
}
