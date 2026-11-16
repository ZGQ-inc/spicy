package com.tx24.spicyplayer.ui.settings

import com.tx24.spicyplayer.i18n.t

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Sensors
import kotlinx.coroutines.flow.MutableStateFlow
import com.tx24.spicyplayer.LyricsActivity
import com.tx24.spicyplayer.playback.SpotifyCloudStatus
import com.tx24.spicyplayer.playback.remote.RemoteAction
import com.tx24.spicyplayer.playback.remote.RemoteCommandPayload
import com.tx24.spicyplayer.playback.remote.RemoteConfigPayload
import com.tx24.spicyplayer.playback.remote.RemoteMessage
import com.tx24.spicyplayer.playback.remote.RemoteMessageType
import com.tx24.spicyplayer.network.data.spotify.SpotifyAuthUtils
import com.tx24.spicyplayer.network.data.spotify.SpotifyWebApiClient
import com.tx24.spicyplayer.playback.remote.NetworkUtils
import com.tx24.spicyplayer.ui.components.QrCodeView
import android.os.Build
import com.tx24.spicyplayer.ui.components.SpicyGlyphs
import com.tx24.spicyplayer.ui.components.drawBrandRamp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.hazeEffect
import com.tx24.spicyplayer.ui.components.focusRing
import com.tx24.spicyplayer.ui.theme.SpicyRadii
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.icons.rounded.VerticalAlignTop
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.composed
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.zIndex
import com.tx24.spicyplayer.ui.components.LocalUiAnimations
import com.tx24.spicyplayer.ui.components.LocalSettingsQuery
import androidx.compose.runtime.saveable.rememberSaveable
import com.tx24.spicyplayer.ui.theme.SpicyMotion
import java.util.Collections
import kotlin.math.abs
import com.tx24.spicyplayer.ui.background.LEGACY_BLUR_DP
import com.tx24.spicyplayer.ui.components.ChromeTheme
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material.icons.automirrored.rounded.CallMerge

import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.LinearScale
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material.icons.rounded.Sync
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import com.tx24.spicyplayer.backup.SettingsBackup
import com.tx24.spicyplayer.backup.SettingsBackupStore
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Tune
import com.tx24.spicyplayer.ui.nowplaying.HeaderSize
import com.tx24.spicyplayer.ui.nowplaying.StageArt
import com.tx24.spicyplayer.ui.nowplaying.LoopKind
import android.content.ClipboardManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.HideImage
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.FormatAlignRight
import androidx.compose.material.icons.rounded.FormatIndentIncrease
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.VerticalAlignCenter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.tx24.spicyplayer.lyrics.spicy.canvas.PinnedFooterMode
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Panorama
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material.icons.rounded.BlurLinear
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.MotionPhotosPaused
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.Lyrics
import java.util.Locale
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.ScreenLockPortrait
import androidx.compose.material.icons.rounded.StayCurrentPortrait
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.CallMerge
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.KeyOff
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowUp
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import com.tx24.spicyplayer.PictureInPicture
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tx24.spicyplayer.lyrics.LyricsState
import com.tx24.spicyplayer.lyrics.spicy.SyllableMerge
import com.tx24.spicyplayer.lyrics.spicy.models.LyricsType
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import com.tx24.spicyplayer.network.data.LyricsBlends
import com.tx24.spicyplayer.network.data.LyricsOrigin
import com.tx24.spicyplayer.network.data.LyricsOrigins
import com.tx24.spicyplayer.network.data.LyricsSourceSettings
import com.tx24.spicyplayer.network.data.SourceRanking
import com.tx24.spicyplayer.ui.components.SpicyMultiSelect
import com.tx24.spicyplayer.network.data.ProviderAttempt
import com.tx24.spicyplayer.network.data.ProviderAttemptOutcome
import com.tx24.spicyplayer.network.data.RemoteLyricsQuality
import com.tx24.spicyplayer.playback.ExternalPlaybackViewModel
import com.tx24.spicyplayer.playback.LOCAL_SOURCE
import com.tx24.spicyplayer.playback.UPLOADED_SOURCE
import com.tx24.spicyplayer.playback.PlayerUiState
import com.tx24.spicyplayer.ui.components.DISABLED_ALPHA
import com.tx24.spicyplayer.ui.components.DescriptionStyle
import com.tx24.spicyplayer.ui.components.RowLabel
import com.tx24.spicyplayer.ui.components.SpicySelect
import com.tx24.spicyplayer.ui.components.Searchable
import com.tx24.spicyplayer.ui.components.SettingRow
import com.tx24.spicyplayer.ui.components.SettingsSection
import com.tx24.spicyplayer.ui.components.SpicyBipolarSlider
import com.tx24.spicyplayer.ui.components.SpicyButton
import com.tx24.spicyplayer.ui.components.SpicyResettableValue
import com.tx24.spicyplayer.ui.components.SpicyIconButton
import com.tx24.spicyplayer.ui.components.SpicyTextField
import com.tx24.spicyplayer.ui.components.SpicyToggle
import com.tx24.spicyplayer.ui.components.ToggleRow
import com.tx24.spicyplayer.ui.components.outlinedCard
import com.tx24.spicyplayer.ui.theme.SpicyColors
import com.tx24.spicyplayer.ui.theme.SpicySpacing
import com.tx24.spicyplayer.ui.theme.SpicyType
import com.tx24.spicyplayer.update.UpdateViewModel
import com.tx24.spicyplayer.analytics.UsageCounter
import com.tx24.spicyplayer.network.data.SpicyLyricsKey
import com.tx24.spicyplayer.analytics.UsageStats

/*
 * What each group's page holds. Every page also shows up, row by row, in the search results, so a
 * page's first rows go without a section title (the page header says it) and later groups get one.
 */

@Composable
internal fun ThisSongContent(
    state: PlayerUiState,
    viewModel: ExternalPlaybackViewModel,
    onOpenLyricsManager: () -> Unit,
    onOpenSpotifySearch: () -> Unit,
) {
    val context = LocalContext.current
    var spotifyInput by remember { mutableStateOf("") }
    LaunchedEffect(state.title, state.artist) { spotifyInput = "" }

    Searchable("This song", "Now playing", state.title, state.artist) {
        Column(
            Modifier.fillMaxWidth().outlinedCard(tinted = true).padding(SpicySpacing.S4),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(state.title, style = SpicyType.Headline)
            Text(state.artist, style = SpicyType.Caption.copy(color = SpicyColors.TextSecondary))
            Spacer(Modifier.height(SpicySpacing.S2))
            Text(lyricsSummary(state.lyrics), style = SpicyType.Caption)
            state.lookupStatus?.let { Text(it, style = DescriptionStyle) }
        }
        Spacer(Modifier.height(SpicySpacing.S2))
    }
    SettingRow(
        label = "Wrong lyrics?",
        description = "Paste the song's Spotify link and the lyrics will come from that recording.",
        icon = Icons.Rounded.Link,
        stacked = true,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SpicySpacing.S2)) {
            SpicyTextField(spotifyInput, { spotifyInput = it }, placeholder = "Spotify link or track ID", modifier = Modifier.weight(1f))
            if (spotifyInput.isBlank()) {
                SpicyButton("Paste", onClick = {
                    val pasted = context.getSystemService(ClipboardManager::class.java)?.primaryClip
                        ?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
                    spotifyInput = pasted
                    viewModel.overrideSpotifyId(pasted)
                })
            } else {
                SpicyButton("Use", onClick = { viewModel.overrideSpotifyId(spotifyInput) })
            }
        }
    }
    SettingRow(label = "Find on Spotify", description = "Pick the right recording from Spotify's results for this song.", icon = Icons.Rounded.Search) {
        SpicyButton("Search", onClick = onOpenSpotifySearch)
    }
    state.manualSpotifyId?.let { id ->
        SettingRow(label = "Using your Spotify link", description = id, icon = Icons.Rounded.Link) {
            SpicyButton("Auto-match", onClick = viewModel::clearSpotifyIdOverride)
        }
    }
    SettingRow(label = "Look again", description = "Ask the sources again instead of using the saved lyrics.", icon = Icons.Rounded.Refresh) {
        SpicyButton("Retry", onClick = { viewModel.loadLyrics(force = true) })
    }
    SongDelayRow(state, viewModel)
    SettingRow(
        label = "Lyrics Manager",
        description = "Use your own TTML for this song, once or saved, and manage the songs you've saved.",
        icon = Icons.Rounded.LibraryMusic,
    ) {
        SpicyButton("Open", onClick = onOpenLyricsManager)
    }
    state.status?.let { status ->
        Searchable("Status", status) {
            Text(status, style = DescriptionStyle, modifier = Modifier.padding(horizontal = SpicySpacing.S3, vertical = SpicySpacing.S2))
        }
    }
}

/**
 * A page's less-used rows, folded behind one row that names them until it's opened, so a page
 * shows first what people actually change. Search shows them unfolded (each row still matches
 * on its own), so a folded row is never harder to find.
 */
@Composable
internal fun MoreOptions(summary: String, content: @Composable () -> Unit) {
    if (LocalSettingsQuery.current.isNotBlank()) return content()
    var open by rememberSaveable { mutableStateOf(false) }
    val animate = LocalUiAnimations.current
    Column(Modifier.fillMaxWidth().padding(vertical = SpicySpacing.S2).outlinedCard()) {
        GroupRow(
            Icons.Rounded.Tune,
            if (open) "Fewer options" else "More options",
            summary,
            trailing = if (open) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
        ) { open = !open }
    }
    AnimatedVisibility(
        visible = open,
        enter = if (animate) expandVertically(tween(SpicyMotion.MODAL_MS, easing = SpicyMotion.Modal)) + fadeIn(tween(SpicyMotion.FAST_MS)) else EnterTransition.None,
        exit = if (animate) shrinkVertically(tween(SpicyMotion.FAST_MS, easing = SpicyMotion.Standard)) + fadeOut(tween(SpicyMotion.FAST_MS)) else ExitTransition.None,
    ) {
        Column(Modifier.fillMaxWidth()) { content() }
    }
}

@Composable
internal fun LyricsContent(state: PlayerUiState, viewModel: ExternalPlaybackViewModel, settings: AppSettings) {
    SettingRow(
        label = "Renderer Visual Style",
        description = "How the lyrics look and move. Spring is the default; Bloom fills each line behind one soft edge.",
        icon = Icons.Rounded.Style,
        searchDescription = "Lyrics Style. Simple Lyrics Mode, Refreshed Simple Lyrics, Apple Music style. Spring, Bloom.",
    ) {
        SpicySelect(
            value = settings.rendererStyle.name,
            options = RendererStyle.entries.map { it.name },
            labels = RendererStyle.entries.map { it.label },
            onChange = {
                val style = RendererStyle.valueOf(it)
                settings.rendererStyle = style
                LyricsActivity.dispatchConfigChange(RemoteConfigPayload(rendererStyle = style.name))
            },
        )
    }
    ToggleRow(
        label = "Fade Away Previous Lines",
        checked = settings.minimalLyricsMode,
        onCheckedChange = {
            settings.minimalLyricsMode = it
            LyricsActivity.dispatchConfigChange(RemoteConfigPayload(minimalLyricsMode = it))
        },
        description = "Hides sung lyrics lines.",
        icon = Icons.Rounded.VisibilityOff,
        searchDescription = "Minimal Lyrics Mode. Hides sung lyrics lines.",
    )
    ToggleRow(
        label = "Original word motion",
        checked = settings.originalWordMotion,
        onCheckedChange = { settings.originalWordMotion = it },
        description = t("The desktop amount of grow and lift on sung words. Off: %s×, which reads better on a phone.", AppSettings.WORD_MOTION_BOOST),
        icon = Icons.Rounded.Height,
    )
    SettingsSection("Text") {
        SettingRow(
            label = "Lyrics size",
            description = "With the phone upright, against the screen's default.",
            icon = Icons.Rounded.FormatSize,
            stacked = true,
            searchDescription = "Make the lyrics smaller or bigger than the screen's default. Portrait.",
        ) {
            SpicyBipolarSlider(
                value = settings.lyricsSizePortrait,
                range = LYRICS_SIZE_RANGE,
                step = 5,
                onValueChange = {
                    settings.lyricsSizePortrait = it
                    LyricsActivity.dispatchConfigChange(RemoteConfigPayload(lyricsSizePortrait = it))
                },
                default = 100,
                unit = "%",
            )
        }
        SettingRow(
            label = "Lyrics size in landscape",
            description = "With the phone on its side, against the screen's default.",
            icon = Icons.Rounded.ScreenRotation,
            stacked = true,
            searchDescription = "Make the lyrics smaller or bigger in landscape.",
        ) {
            SpicyBipolarSlider(
                value = settings.lyricsSizeLandscape,
                range = LYRICS_SIZE_RANGE,
                step = 5,
                onValueChange = {
                    settings.lyricsSizeLandscape = it
                    LyricsActivity.dispatchConfigChange(RemoteConfigPayload(lyricsSizeLandscape = it))
                },
                default = 100,
                unit = "%",
            )
        }
        LyricsFontRows(settings)
    }
    MoreOptions("Merge syllables, duet padding, credits, blur and glow") {
        SettingRow(
            label = "Merge syllables",
            description = "Draw a word split into syllables as one word. Steadily sung words only merges the words whose parts have no gap and keep an even pace. Held words only merges the words that are then long enough for the held-word letter effect.",
            icon = Icons.Rounded.CallMerge,
        ) {
            SpicySelect(
                value = settings.syllableMerge.name,
                options = SyllableMerge.entries.map { it.name },
                labels = SyllableMerge.entries.map { it.label },
                onChange = { settings.syllableMerge = SyllableMerge.valueOf(it) },
            )
        }
        ToggleRow(
            label = "Keep Syllable Pace",
            checked = settings.syllablePace,
            onCheckedChange = { settings.syllablePace = it },
            description = "A merged held word's letters follow each syllable's timing, slow on a long syllable and quick on a short one.",
            icon = Icons.Rounded.Speed,
            enabled = settings.syllableMerge != SyllableMerge.Off,
        )
        ToggleRow(
            label = "Duet Line Padding",
            checked = settings.duetLinePadding,
            onCheckedChange = { settings.duetLinePadding = it },
            description = "Indents lyrics lines on the side they lean away from when a song has duet lines, so the two voices read as separate columns. Disable to give every line the same slight padding.",
            icon = Icons.Rounded.FormatIndentIncrease,
        )
        ToggleRow(
            label = "Credits Follow the Last Singer",
            checked = settings.creditsFollowLastLine,
            onCheckedChange = { settings.creditsFollowLastLine = it },
            description = "When a song ends on a duet line sung from the right, move the credits under it to the right too.",
            icon = Icons.Rounded.FormatAlignRight,
        )
        ToggleRow(
            label = "Keep Overlapping Lines",
            checked = settings.keepOverlappingLines,
            onCheckedChange = { settings.keepOverlappingLines = it },
            description = "In Minimal Lyrics Mode, keep a line that ends while one above it is still sung on screen, dimmed, instead of fading it out early.",
            icon = Icons.Rounded.Layers,
        )
        ToggleRow(
            label = "Background Vocal Pop-Out",
            checked = settings.backgroundPop,
            onCheckedChange = { settings.backgroundPop = it },
            description = "Keep background vocal lines tucked under their line until it is sung, then slide them out.",
            icon = Icons.Rounded.UnfoldMore,
        )
        ToggleRow(
            label = "Refined Credits",
            checked = settings.refinedCredits,
            onCheckedChange = { settings.refinedCredits = it },
            description = "Show the credits as quiet type close under the last line, dim until the song has been sung.",
            icon = Icons.Rounded.Notes,
        )
        SettingsSection("Effects") {
            ToggleRow(
                label = "Blur distant lines",
                checked = settings.distanceBlur && !settings.lowPerformance,
                onCheckedChange = { settings.distanceBlur = it },
                description = "Soften the lines further from the one being sung.",
                icon = Icons.Rounded.BlurOn,
                enabled = !settings.lowPerformance,
            )
            ToggleRow(
                label = "Glow",
                checked = settings.glow && !settings.lowPerformance,
                onCheckedChange = { settings.glow = it },
                description = "Let sung words glow.",
                icon = Icons.Rounded.Flare,
                enabled = !settings.lowPerformance,
            )
        }
    }
}

/**
 * The lyrics font: ours, the phone's, or a font file picked from the phone. Choosing Custom with
 * nothing picked yet opens the picker.
 */
@Composable
private fun LyricsFontRows(settings: AppSettings) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var failed by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val imported = LyricsFontFile.import(context, uri)
            failed = imported == null
            if (imported != null) {
                settings.customFontFile = imported.fileName
                settings.customFontName = imported.displayName
                settings.lyricsFont = LyricsFont.Custom
            }
        }
    }
    val pick = { picker.launch(FONT_MIME_TYPES) }
    val hasCustom = settings.customFontFile.isNotBlank()
    SettingRow(label = "Lyrics Font", description = "Draw the lyrics in their own font, your phone's, or any font file on your phone.", icon = Icons.Rounded.FontDownload) {
        SpicySelect(
            value = settings.lyricsFont.name,
            options = LyricsFont.entries.map { it.name },
            labels = LyricsFont.entries.map { if (it == LyricsFont.Custom && hasCustom) settings.customFontName else it.label },
            onChange = { choice ->
                val font = LyricsFont.valueOf(choice)
                if (font == LyricsFont.Custom && !hasCustom) pick() else settings.lyricsFont = font
            },
        )
    }
    SettingRow(
        label = t("Font File"),
        description = when {
            failed -> t("That file isn't a font Android can read. Pick a .ttf or .otf file.")
            hasCustom -> t("Using %s.").format(settings.customFontName ?: "")
            else -> t("Pick a .ttf or .otf font file from your phone.")
        },
        icon = Icons.Rounded.FolderOpen,
    ) {
        SpicyButton(if (hasCustom) t("Change") else t("Choose"), onClick = pick)
    }
}

/** Font files come labelled all sorts of ways, so the picker offers these (and checks the file). */
private val FONT_MIME_TYPES = arrayOf(
    "font/*", "application/font-sfnt", "application/x-font-ttf", "application/x-font-otf",
    "application/vnd.ms-opentype", "application/octet-stream",
)

@Composable
internal fun ScrollSyncContent(state: PlayerUiState, viewModel: ExternalPlaybackViewModel, settings: AppSettings) {
    SettingRow(
        label = "Lyric delay",
        description = t("Saved for %s. Move it right if the lyrics run ahead of the song.", state.outputLabel),
        icon = Icons.Rounded.Timer,
        stacked = true,
    ) {
        DelayControl(state.lyricDelayMs, viewModel::setLyricDelay)
    }
    PlayerDelayRow(state, viewModel)
    SongDelayRow(state, viewModel)
    DelayCalibrationRow(state, viewModel)
    SettingRow(
        label = "Delays in Quick Settings",
        description = "Which delays the quick settings pop-up shows.",
        icon = Icons.Rounded.Tune,
    ) {
        SpicyMultiSelect(
            selected = settings.quickDelays.mapTo(mutableSetOf()) { it.name },
            options = DelayKind.entries.map { it.name },
            labels = DelayKind.entries.map { it.label },
            none = "None",
            onChange = { picked -> settings.quickDelays = DelayKind.entries.filter { it.name in picked }.toSet() },
        )
    }
    SettingsSection("Scrolling") {
        ToggleRow(
            label = "Early Scroll",
            checked = settings.scrollLeadEnabled,
            onCheckedChange = { settings.scrollLeadEnabled = it },
            description = "Start scrolling to the next line slightly before it becomes active, so the move feels less abrupt.",
            icon = Icons.Rounded.KeyboardDoubleArrowUp,
        )
        SettingRow(
            label = "Early Scroll Time",
            description = "How early the next line is scrolled to, before it becomes active.",
            icon = Icons.Rounded.Timer,
            enabled = settings.scrollLeadEnabled,
            stacked = true,
        ) {
            SpicyBipolarSlider(
                value = settings.scrollLeadMs,
                range = 0..800,
                step = 10,
                onValueChange = { settings.scrollLeadMs = it },
                default = 250,
                unit = "ms",
                enabled = settings.scrollLeadEnabled,
            )
        }
        ToggleRow(
            label = "Smooth Scrolling",
            checked = settings.smoothScrolling,
            onCheckedChange = { settings.smoothScrolling = it },
            description = "Makes the lyrics scroll smoothly.",
            icon = Icons.Rounded.Waves,
        )
    }
    MoreOptions("Seek fade-in compensation, scroll-to-active button, auto-scroll") {
        ToggleRow(
            label = "Seek Fade-in Compensation",
            checked = settings.seekFadeCompensation,
            onCheckedChange = { settings.seekFadeCompensation = it },
            description = "Tapping a line jumps 300ms before it, so the player's fade-in doesn't cut off the start. Best for rap or fast-paced songs.",
            icon = Icons.Rounded.FastRewind,
        )
        ToggleRow(
            label = "Show Scroll to Active Button",
            checked = settings.showScrollToActive,
            onCheckedChange = { settings.showScrollToActive = it },
            description = "Show an arrow when the active lyric is outside the viewport.",
            icon = Icons.Rounded.VerticalAlignCenter,
        )
        SettingRow(
            label = "Auto-scroll Unsynced Lyrics",
            description = "Lyrics without timing scroll through with the song. Basic holds through quiet intros, outros and breaks, using Spotify's analysis of the song.",
            searchDescription = "Lyrics without timing scroll through at the song's pace, from top to bottom.",
            icon = Icons.Rounded.SwapVert,
        ) {
            SpicySelect(
                value = settings.staticAutoScroll.name,
                options = StaticAutoScroll.entries.map { it.name },
                labels = StaticAutoScroll.entries.map { it.label },
                onChange = { settings.staticAutoScroll = StaticAutoScroll.valueOf(it) },
            )
        }
    }
}

/** Dark presets that keep the white lyrics readable, black first. */
private val PresetBackgroundColors = listOf("#000000", "#121212", "#0B1426", "#1E0E24", "#0D1F17", "#2A0B12", "#2B2118")

/** Swatches for the presets, and a hex field for any other colour; [onPick] gets `#RRGGBB`. */
@Composable
private fun CustomColorPicker(value: String, onPick: (String) -> Unit) {
    val haptics = LocalHapticFeedback.current
    val current = parseHexColor(value)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for (preset in PresetBackgroundColors) {
                val argb = parseHexColor(preset)!!
                val selected = argb == current
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(argb))
                        .border(if (selected) 2.dp else 1.dp, if (selected) Color.White else SpicyColors.Hairline, CircleShape)
                        .clickable(role = Role.RadioButton) {
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onPick(preset)
                        },
                )
            }
        }
        // Only a whole colour is kept, so a half-typed one doesn't flash the background.
        SpicyTextField(
            value = value,
            onValueChange = { text -> parseHexColor(text)?.let { onPick("#" + text.trim().removePrefix("#").uppercase()) } },
            placeholder = "#000000",
            leading = { Box(Modifier.size(18.dp).clip(CircleShape).background(Color(current ?: 0xFF000000.toInt())).border(1.dp, SpicyColors.Hairline, CircleShape)) },
        )
    }
}

@Composable
internal fun BackgroundContent(settings: AppSettings, viewModel: ExternalPlaybackViewModel) {
    val type = settings.backgroundType
    SettingRow(
        label = "Background Type",
        description = "Choose the dynamic, legacy, static image, cover color, or a color of your own.",
        searchDescription = "Choose the dynamic, legacy, static image, or color background.",
        icon = Icons.Rounded.Wallpaper,
    ) {
        SpicySelect(
            value = type.name,
            options = BackgroundType.entries.map { it.name },
            labels = BackgroundType.entries.map { it.label },
            onChange = { settings.backgroundType = BackgroundType.valueOf(it) },
        )
    }
    if (type == BackgroundType.Custom) {
        SettingRow(
            label = "Background Color",
            description = "Pick one, or type a hex color like #1A1A2E. Black suits AMOLED screens.",
            icon = Icons.Rounded.Palette,
            stacked = true,
        ) {
            CustomColorPicker(settings.backgroundColor) { settings.backgroundColor = it }
        }
    }
    // Each background keeps its own blur: they're measured differently and start from different looks.
    if (type != BackgroundType.Color && type != BackgroundType.Custom) {
        SettingRow(
            label = "Background Blur",
            description = if (type.image) "Soften the static background image." else "How soft the moving cover is.",
            icon = Icons.Rounded.BlurLinear,
            stacked = true,
        ) {
            when (type) {
                BackgroundType.Default -> SpicyBipolarSlider(
                    value = settings.dynamicBackgroundBlur,
                    range = 0..100,
                    step = 1,
                    onValueChange = { settings.dynamicBackgroundBlur = it },
                    default = DEFAULT_DYNAMIC_BLUR,
                    unit = "%",
                )
                BackgroundType.Legacy -> SpicyBipolarSlider(
                    value = settings.legacyBackgroundBlur,
                    range = 0..MAX_BACKGROUND_BLUR,
                    step = 1,
                    onValueChange = { settings.legacyBackgroundBlur = it },
                    default = LEGACY_BLUR_DP,
                    unit = "dp",
                )
                else -> SpicyBipolarSlider(
                    value = settings.backgroundBlur,
                    range = 0..MAX_BACKGROUND_BLUR,
                    step = 1,
                    onValueChange = { settings.backgroundBlur = it },
                    unit = "dp",
                )
            }
        }
    }
    if (type == BackgroundType.CoverArt) {
        ToggleRow(
            label = "Animated Background",
            checked = settings.animatedBackground && !settings.lowPerformance,
            onCheckedChange = { settings.animatedBackground = it },
            description = "Play the album's animated cover from Apple Music behind the lyrics, where it has one.",
            icon = Icons.Rounded.Animation,
            enabled = !settings.lowPerformance,
        )
    }
    // Next to the animated cover's rows: the other video a background can show.
    ToggleRow(
        label = "Show Canvas Where Available",
        checked = settings.canvasBackground && !settings.lowPerformance,
        onCheckedChange = { on ->
            // Canvas asks a third-party service, so it says what it sends first.
            if (on) viewModel.chooseCanvas { settings.canvasBackground = true } else settings.canvasBackground = false
        },
        description = "Where the song has a Spotify Canvas, the background is that Canvas, blurred. Other songs keep your background.",
        icon = Icons.Rounded.VideoLibrary,
        enabled = !settings.lowPerformance,
    )
    if (settings.canvasBackground && !settings.lowPerformance) {
        SettingRow(
            label = "Canvas Blur",
            description = "How soft the Canvas is. Below 100% the video itself plays behind the lyrics.",
            icon = Icons.Rounded.BlurOn,
            stacked = true,
        ) {
            SpicyBipolarSlider(
                value = settings.canvasBlur,
                range = 0..100,
                step = 1,
                onValueChange = { settings.canvasBlur = it },
                default = 100,
                unit = "%",
            )
        }
    }
    MoreOptions("Refined background, still background, moving with the music") {
        ToggleRow(
            label = "Refined Background",
            checked = settings.refinedBackground,
            onCheckedChange = { settings.refinedBackground = it },
            description = "Crossfade background changes, keep the image's edges, and deepen Cover Colors.",
            icon = Icons.Rounded.AutoAwesome,
        )
        if (type.moving) {
            ToggleRow(
                label = "Still Background",
                checked = settings.staticBackground || settings.lowPerformance,
                onCheckedChange = { settings.staticBackground = it },
                description = "Hold the background still instead of animating it.",
                icon = Icons.Rounded.MotionPhotosPaused,
                enabled = !settings.lowPerformance,
            )
        }
        if (type == BackgroundType.Default) {
            ToggleRow(
                label = "Move with the Music",
                checked = settings.beatReactiveBackground && !settings.staticBackground && !settings.lowPerformance,
                onCheckedChange = { settings.beatReactiveBackground = it },
                description = "Let the background flow faster and swell in the loud parts of the song, where Spotify has them.",
                icon = Icons.Rounded.GraphicEq,
                enabled = !settings.staticBackground && !settings.lowPerformance,
            )
            val held = settings.staticBackground || settings.lowPerformance
            ToggleRow(
                label = "Follow the Animated Cover",
                checked = settings.backgroundFollowsMotion && settings.animatedCover && !held,
                onCheckedChange = { settings.backgroundFollowsMotion = it },
                description = when {
                    held -> "Off while the background is held still."
                    !settings.animatedCover -> "Needs Animated Cover (Now Playing) on, whose video it follows."
                    else -> "When the song has an animated cover, the background moves with it."
                },
                icon = Icons.Rounded.SlowMotionVideo,
                enabled = settings.animatedCover && !held,
                searchDescription = "When the song has an animated cover, the background moves with it.",
            )
        }
    }
}

@Composable
internal fun NowPlayingContent(settings: AppSettings, viewModel: ExternalPlaybackViewModel) {
    SettingRow(
        label = "Song Header Size",
        description = "How tall the song header is. Small is a slim row, like Apple Music's.",
        icon = Icons.Rounded.Height,
        enabled = !settings.hideHeader,
    ) {
        SpicySelect(
            value = settings.headerSize.name,
            options = HeaderSize.entries.map { it.name },
            labels = HeaderSize.entries.map { it.label },
            onChange = { settings.headerSize = HeaderSize.valueOf(it) },
        )
    }
    SettingRow(
        label = "Release Year Position",
        description = "Show the release year beside the artists.",
        icon = Icons.Rounded.CalendarMonth,
    ) {
        SpicySelect(
            value = settings.releaseYearPosition.name,
            options = ReleaseYearPosition.entries.map { it.name },
            labels = ReleaseYearPosition.entries.map { it.label },
            onChange = { settings.releaseYearPosition = ReleaseYearPosition.valueOf(it) },
        )
    }
    ToggleRow(
        label = "Up Next",
        checked = settings.nextUpCard,
        onCheckedChange = {
            settings.nextUpCard = it
            LyricsActivity.dispatchConfigChange(RemoteConfigPayload(nextUpCard = it))
        },
        description = "In the last 30 seconds of a song, show the one queued next at the bottom of the lyrics.",
        icon = Icons.Rounded.SkipNext,
    )
    if (settings.nextUpCard) {
        SettingRow(
            label = "Up Next size",
            stacked = true,
        ) {
            SpicyBipolarSlider(
                value = settings.nextUpScalePercent,
                range = 50..300,
                step = 5,
                onValueChange = {
                    settings.nextUpScalePercent = it
                    LyricsActivity.dispatchConfigChange(RemoteConfigPayload(nextUpScalePercent = it))
                },
                default = 100,
                unit = "%",
            )
        }
    }
    ToggleRow(
        label = "Animated Cover",
        checked = settings.animatedCover && !settings.lowPerformance,
        onCheckedChange = { settings.animatedCover = it },
        description = "Play the album's animated cover from Apple Music, where it has one.",
        icon = Icons.Rounded.Animation,
        enabled = !settings.lowPerformance,
    )
    SettingRow(
        label = "Cover stage (landscape)",
        description = if (!settings.animatedCover && StageArt.AnimatedCover in settings.stageArt) {
            "In landscape, the first of these the song has fills its side of the screen. The animated cover needs Animated Cover on."
        } else {
            "In landscape, the first of these the song has fills its side of the screen, with the song docked along its bottom. None picked is off."
        },
        icon = Icons.Rounded.Panorama,
        enabled = !settings.lowPerformance,
        searchDescription = "In landscape, the song's Spotify Canvas, animated cover or cover art fills its side of the screen, with the song docked along its bottom.",
    ) {
        SpicyMultiSelect(
            selected = settings.stageArt.mapTo(mutableSetOf()) { it.name },
            options = StageArt.entries.map { it.name },
            labels = StageArt.entries.map { it.label },
            none = "Off",
            enabled = !settings.lowPerformance,
            onChange = { picked ->
                val arts = StageArt.entries.filter { it.name in picked }.toSet()
                // Canvas asks a third-party service, so it says what it sends first.
                if (StageArt.Canvas in arts && StageArt.Canvas !in settings.stageArt) {
                    viewModel.chooseCanvas { settings.stageArt = arts }
                } else {
                    settings.stageArt = arts
                }
            },
        )
    }
    SettingRow(
        label = "Big cover (portrait)",
        description = if (!settings.animatedCover && StageArt.AnimatedCover in settings.bigCoverArt) {
            "In portrait, the first of these the song has takes the big cover's place. The animated cover needs Animated Cover on."
        } else {
            "In portrait, the first of these the song has takes the big cover's place: a Canvas fills the screen, a cover spreads edge to edge. None picked is the big cover as it is."
        },
        icon = Icons.Rounded.Fullscreen,
        enabled = !settings.lowPerformance,
        searchDescription = "In portrait, the song's Spotify Canvas, animated cover or cover art takes the big cover's place, with the song docked under it.",
    ) {
        SpicyMultiSelect(
            selected = settings.bigCoverArt.mapTo(mutableSetOf()) { it.name },
            options = StageArt.entries.map { it.name },
            labels = StageArt.entries.map { it.label },
            none = "Off",
            enabled = !settings.lowPerformance,
            onChange = { picked ->
                val arts = StageArt.entries.filter { it.name in picked }.toSet()
                // Canvas asks a third-party service, so it says what it sends first.
                if (StageArt.Canvas in arts && StageArt.Canvas !in settings.bigCoverArt) {
                    viewModel.chooseCanvas { settings.bigCoverArt = arts }
                } else {
                    settings.bigCoverArt = arts
                }
            },
        )
    }
    if (StageArt.Canvas in settings.bigCoverArt && StageArt.AnimatedCover in settings.bigCoverArt) {
        ToggleRow(
            label = "Animated cover before Canvas",
            description = "The big cover shows a song's animated cover, and its Canvas only when it has none.",
            icon = Icons.Rounded.Fullscreen,
            checked = settings.bigCoverAnimatedFirst,
            enabled = !settings.lowPerformance,
            onCheckedChange = { settings.bigCoverAnimatedFirst = it },
        )
    }
    MoreOptions("Hiding the header, cover without lyrics, blurred Canvas, pausing, credits") {
        ToggleRow(
            label = "Hide the song header",
            checked = settings.hideHeader,
            onCheckedChange = { settings.hideHeader = it },
            description = "Show only the lyrics, centred on the page (in landscape, without the cover panel). The expand button still opens the big cover.",
            icon = Icons.Rounded.HideImage,
        )
        ToggleRow(
            label = "Show the cover without lyrics",
            checked = settings.expandWithoutLyrics,
            onCheckedChange = { settings.expandWithoutLyrics = it },
            description = "Grow the song header into the big cover when a song has no lyrics.",
            icon = Icons.Rounded.Album,
        )
        val showsCanvas = StageArt.Canvas in settings.stageArt || StageArt.Canvas in settings.bigCoverArt
        ToggleRow(
            label = t("Blurred Canvas Behind"),
            checked = settings.canvasBackdrop && showsCanvas && !settings.lowPerformance,
            onCheckedChange = { settings.canvasBackdrop = it },
            description = if (showsCanvas) {
                t("While the stage or the big cover shows a Canvas, the background becomes it, blurred.")
            } else {
                t("Needs Spotify Canvas on the cover stage or the big cover.")
            },
            icon = Icons.Rounded.BlurOn,
            enabled = showsCanvas && !settings.lowPerformance,
            searchDescription = t("While the stage or the big cover shows a Canvas, the background becomes it, blurred."),
        )
        SettingRow(
            label = "Pause with the Music",
            description = "These hold their frame while the music is paused.",
            icon = Icons.Rounded.PauseCircle,
            searchDescription = "Animated covers and Canvas videos hold their frame while the music is paused.",
        ) {
            SpicyMultiSelect(
                selected = settings.pauseWithMusic.mapTo(mutableSetOf()) { it.name },
                options = LoopKind.entries.map { it.name },
                labels = LoopKind.entries.map { it.label },
                none = "Neither",
                onChange = { picked -> settings.pauseWithMusic = LoopKind.entries.filter { it.name in picked }.toSet() },
            )
        }
        SettingsSection("Credits") {
            SettingRow(
                label = "Pinned Lyrics Footer",
                description = "Keep source and community credits visible. Full also pins writers.",
                icon = Icons.Rounded.PushPin,
            ) {
                SpicySelect(
                    value = settings.pinnedFooter.name,
                    options = PinnedFooterMode.entries.map { it.name },
                    labels = PinnedFooterMode.entries.map { it.label },
                    onChange = { settings.pinnedFooter = PinnedFooterMode.valueOf(it) },
                )
            }
            ToggleRow(
                label = "Translation & Romanization Credits",
                checked = settings.textCredits,
                onCheckedChange = { settings.textCredits = it },
                description = "Say in the credits who translated and romanized the lyrics on screen.",
                icon = Icons.Rounded.Translate,
            )
            ToggleRow(
                label = "Open Profiles in Browser",
                checked = settings.profilesInBrowser,
                onCheckedChange = { settings.profilesInBrowser = it },
                description = "Open contributor profiles in your browser instead of inside the app.",
                icon = Icons.Rounded.OpenInBrowser,
            )
        }
    }
}

@Composable
internal fun ControlsContent(settings: AppSettings) {
    ToggleRow(
        label = "Timeline Outside Cover",
        checked = settings.timelineOutsideCover,
        onCheckedChange = { settings.timelineOutsideCover = it },
        description = "In landscape, show the timeline under the cover, above the song, where it always stays.",
        icon = Icons.Rounded.LinearScale,
    )
    SettingsSection("Floating Buttons") {
        ToggleRow(
            label = "Grouped view controls",
            checked = settings.groupedControls,
            onCheckedChange = { settings.groupedControls = it },
            description = "The buttons share one frosted capsule. Toggles fill in white while on, and the arrow back to the current line matches. Off gives each its own circle.",
            icon = Icons.Rounded.Widgets,
        )
        ToggleRow(
            label = "Player buttons",
            checked = settings.playerButtons,
            onCheckedChange = { settings.playerButtons = it },
            description = "Show the extra buttons the player offers, like Like or Save. Shuffle and repeat always stay.",
            icon = Icons.Rounded.Widgets,
        )
        ToggleRow(
            label = "Floating lyrics button",
            checked = settings.pipButton,
            onCheckedChange = { settings.pipButton = it },
            description = "Show the button that shrinks the lyrics into a small floating window, over other apps.",
            icon = SpicyGlyphs.PictureInPicture,
            enabled = PictureInPicture.supported(LocalContext.current),
        )
        ToggleRow(
            label = "Lyrics Manager button",
            checked = settings.lyricsManagerButton,
            onCheckedChange = { settings.lyricsManagerButton = it },
            description = "Show its own button for the Lyrics Manager. It's always in quick settings and This song.",
            icon = SpicyGlyphs.LyricsManager,
        )
        ToggleRow(
            label = "Queue button",
            checked = settings.queueButton,
            onCheckedChange = { settings.queueButton = it },
            description = "Show its own button for the player's queue, while it shares one. It's always in quick settings.",
            icon = SpicyGlyphs.Queue,
        )
    }
    MoreOptions("Hiding the controls, and the resync, expand, quick settings, rotate and source buttons") {
        ToggleRow(
            label = "Hide controls",
            checked = settings.autoHideControls,
            onCheckedChange = { settings.autoHideControls = it },
            description = "Fade the controls out a while after the last touch. A touch brings them back.",
            icon = Icons.Rounded.TouchApp,
        )
        SettingRow(
            label = "Hide After",
            description = "How long the controls stay after the last touch.",
            icon = Icons.Rounded.Timer,
            enabled = settings.autoHideControls,
        ) {
            SpicySelect(
                value = settings.controlsHideDelay.name,
                options = ControlsHideDelay.entries.map { it.name },
                labels = ControlsHideDelay.entries.map { it.label },
                onChange = { settings.controlsHideDelay = ControlsHideDelay.valueOf(it) },
            )
        }
        ToggleRow(
            label = "Hide while paused",
            checked = settings.hideControlsWhilePaused,
            onCheckedChange = { settings.hideControlsWhilePaused = it },
            description = "Fade the controls while the song is paused too, not only while it plays.",
            icon = Icons.Rounded.PauseCircle,
            enabled = settings.autoHideControls,
        )
        SettingsSection("More Buttons") {
            ToggleRow(
                label = "Resync button",
                checked = settings.resyncButton,
                onCheckedChange = { settings.resyncButton = it },
                description = "Show the button that snaps the lyrics back in time with the player.",
                icon = SpicyGlyphs.Resync,
            )
            ToggleRow(
                label = "Expand button",
                checked = settings.expandButton,
                onCheckedChange = { settings.expandButton = it },
                description = "Show the button that opens the big cover. It stays while the cover is open, to get back.",
                icon = SpicyGlyphs.Expand,
            )
            ToggleRow(
                label = "Quick settings button",
                checked = settings.quickSettingsButton,
                onCheckedChange = { settings.quickSettingsButton = it },
                description = "Show the button that opens the delays, the Lyrics Manager and the queue without leaving the lyrics.",
                icon = SpicyGlyphs.QuickSettings,
            )
            ToggleRow(
                label = "Rotate button",
                checked = settings.rotateButton,
                onCheckedChange = { settings.rotateButton = it },
                description = "Show the button that turns the lyrics to landscape and back, even with rotation locked.",
                icon = SpicyGlyphs.Rotate,
            )
            ToggleRow(
                label = "Lyrics source button",
                checked = settings.sourceButton,
                onCheckedChange = { settings.sourceButton = it },
                description = "Show its own button for choosing this song's lyrics source. It's always in quick settings.",
                icon = SpicyGlyphs.Source,
            )
            ToggleRow(
                label = t("Standby Clock Button"),
                checked = settings.smartDisplayButton,
                onCheckedChange = { settings.smartDisplayButton = it },
                description = "Show the button that toggles the 24/7 smart standby clock and weather display.",
                icon = Icons.Rounded.Schedule,
            )
        }
    }
}

@Composable
internal fun ThemeContent(settings: AppSettings) {
    SettingRow(
        label = "Settings Theme",
        description = "Dress this screen in Spicy Lyrics' purple, or keep the plain glass.",
        icon = Icons.Rounded.Tune,
    ) {
        SpicySelect(
            value = settings.settingsTheme.name,
            options = ChromeTheme.entries.map { it.name },
            labels = ChromeTheme.entries.map { it.label },
            onChange = { settings.settingsTheme = ChromeTheme.valueOf(it) },
        )
    }
    SettingRow(
        label = "Pop-up Theme",
        description = "Dress the pop-ups, like quick settings, the queue and update notices, the same way.",
        icon = Icons.Rounded.Widgets,
    ) {
        SpicySelect(
            value = settings.popupTheme.name,
            options = ChromeTheme.entries.map { it.name },
            labels = ChromeTheme.entries.map { it.label },
            onChange = { settings.popupTheme = ChromeTheme.valueOf(it) },
        )
    }
    SettingRow(
        label = "App Icon",
        description = "Choose the icon on your home screen. It changes once you leave the app.",
        icon = Icons.Rounded.Apps,
    ) {
        SpicySelect(
            value = settings.appIcon.name,
            options = AppIcon.entries.map { it.name },
            labels = AppIcon.entries.map { it.label },
            onChange = { name ->
                settings.appIcon = AppIcon.valueOf(name)
            },
        )
    }
    ToggleRow(
        label = "Peek While Adjusting",
        checked = settings.peekWhileAdjusting,
        onCheckedChange = { settings.peekWhileAdjusting = it },
        description = "Clear this screen away while you drag a slider, so you see the change on the lyrics.",
        icon = Icons.Rounded.Visibility,
    )
}

@Composable
internal fun SmartDisplayContent(
    state: PlayerUiState,
    viewModel: ExternalPlaybackViewModel,
    settings: AppSettings,
    onToggleAmbient: () -> Unit = {},
    onOpenConnectModal: () -> Unit = {},
) {
    SettingsSection(t("Lyrics Screen Clock")) {
        ToggleRow(
            label = t("Show Clock on Lyrics Screen"),
            checked = settings.showClockOnLyricsScreen,
            onCheckedChange = {
                settings.showClockOnLyricsScreen = it
                LyricsActivity.dispatchConfigChange(RemoteConfigPayload(showClockOnLyricsScreen = it))
            },
            description = t("Display a permanent clock and weather header on the now playing and lyrics screen."),
            icon = Icons.Rounded.Schedule,
        )
        ToggleRow(
            label = t("Show Battery Indicator"),
            checked = settings.showBatteryIndicator,
            onCheckedChange = {
                settings.showBatteryIndicator = it
                LyricsActivity.dispatchConfigChange(RemoteConfigPayload(showBatteryIndicator = it))
            },
            description = t("Show battery percentage and charging status on lyrics screen and screensaver."),
            icon = Icons.Rounded.BatteryChargingFull,
        )
        if (settings.showBatteryIndicator) {
            SettingRow(
                label = t("Battery Indicator Size"),
                description = t("Adjust the size of the battery indicator on lyrics screen and screensaver."),
                icon = Icons.Rounded.FormatSize,
                stacked = true,
            ) {
                SpicyBipolarSlider(
                    value = settings.batteryIndicatorScale,
                    range = 50..200,
                    step = 5,
                    onValueChange = {
                        settings.batteryIndicatorScale = it
                        LyricsActivity.dispatchConfigChange(RemoteConfigPayload(batteryIndicatorScale = it))
                    },
                    default = 100,
                    unit = "%",
                )
            }
        }
        ToggleRow(
            label = t("Show Weather on Clock"),
            checked = settings.showWeatherOnLyricsScreen,
            enabled = settings.showClockOnLyricsScreen,
            onCheckedChange = {
                settings.showWeatherOnLyricsScreen = it
                LyricsActivity.dispatchConfigChange(RemoteConfigPayload(showWeatherOnLyricsScreen = it))
            },
            description = t("Display local temperature and weather status next to the time."),
            icon = Icons.Rounded.WbSunny,
        )
        ToggleRow(
            label = t("12-Hour Clock"),
            checked = settings.clock12h,
            onCheckedChange = {
                settings.clock12h = it
                LyricsActivity.dispatchConfigChange(RemoteConfigPayload(clock12h = it))
            },
            description = t("Turn off to use 24-hour time format."),
            icon = Icons.Rounded.Schedule,
        )
        ToggleRow(
            label = t("Show Seconds (:SS)"),
            checked = settings.clockShowSeconds,
            onCheckedChange = {
                settings.clockShowSeconds = it
                LyricsActivity.dispatchConfigChange(RemoteConfigPayload(showSeconds = it))
            },
            description = t("Show ticking seconds after the time."),
            icon = Icons.Rounded.Timer,
        )
        ToggleRow(
            label = t("Show Date and Day"),
            checked = settings.smartClockShowDate,
            onCheckedChange = {
                settings.smartClockShowDate = it
                LyricsActivity.dispatchConfigChange(RemoteConfigPayload(showDate = it))
            },
            description = t("Display full date and weekday under the time."),
            icon = Icons.Rounded.Schedule,
        )
        ToggleRow(
            label = t("Show Detailed Weather"),
            checked = settings.clockShowWeatherDetails,
            onCheckedChange = {
                settings.clockShowWeatherDetails = it
                LyricsActivity.dispatchConfigChange(RemoteConfigPayload(showWeatherDetails = it))
            },
            description = t("Display feels-like temperature, humidity and city name."),
            icon = Icons.Rounded.WaterDrop,
        )
        SettingRow(
            label = t("Lyrics Clock Scale"),
            description = t("Adjust the font size scale of the clock on the lyrics screen."),
            icon = Icons.Rounded.FormatSize,
        ) {
            SpicySelect(
                value = settings.clockScalePercent.toString(),
                options = listOf("80", "90", "100", "110", "120", "130", "150", "180", "200", "250", "300", "350", "400"),
                labels = listOf("80%", "90%", "100%", "110%", "120%", "130%", "150%", "180%", "200%", "250%", "300%", "350%", "400%"),
                onChange = {
                    val scale = it.toIntOrNull() ?: 100
                    settings.clockScalePercent = scale
                    LyricsActivity.dispatchConfigChange(RemoteConfigPayload(clockScalePercent = scale))
                },
            )
        }
    }

    SettingsSection(t("Smart Standby Clock")) {
        ToggleRow(
            label = t("Standby Clock"),
            checked = settings.smartDisplayEnabled,
            onCheckedChange = {
                settings.smartDisplayEnabled = it
                LyricsActivity.dispatchConfigChange(RemoteConfigPayload(smartDisplayEnabled = it))
            },
            description = t("Automatically transition to standby clock and weather after playback stops."),
            icon = Icons.Rounded.Schedule,
        )
        SettingRow(
            label = t("Standby Timeout (Seconds)"),
            description = t("How long after music stops before entering standby clock."),
            icon = Icons.Rounded.Timer,
            enabled = settings.smartDisplayEnabled,
        ) {
            SpicySelect(
                value = settings.smartDisplayIdleTimeoutSeconds.toString(),
                options = listOf("5", "10", "15", "30", "60", "120", "300"),
                labels = listOf(t("5 seconds"), t("10 seconds"), t("15 seconds"), t("30 seconds"), t("1 minute"), t("2 minutes"), t("5 minutes")),
                onChange = {
                    val timeout = it.toIntOrNull() ?: 15
                    settings.smartDisplayIdleTimeoutSeconds = timeout
                    LyricsActivity.dispatchConfigChange(RemoteConfigPayload(ambientIdleTimeoutSeconds = timeout))
                },
            )
        }
        SettingRow(
            label = t("Standby Clock Scale"),
            description = t("Adjust the size scale of the full-screen standby clock and weather."),
            icon = Icons.Rounded.FormatSize,
            enabled = settings.smartDisplayEnabled,
        ) {
            SpicySelect(
                value = settings.ambientClockScalePercent.toString(),
                options = listOf("80", "90", "100", "110", "120", "130", "150", "180", "200", "250", "300", "350", "400"),
                labels = listOf("80%", "90%", "100%", "110%", "120%", "130%", "150%", "180%", "200%", "250%", "300%", "350%", "400%"),
                onChange = {
                    val scale = it.toIntOrNull() ?: 100
                    settings.ambientClockScalePercent = scale
                    LyricsActivity.dispatchConfigChange(RemoteConfigPayload(ambientClockScalePercent = scale))
                },
            )
        }
        ToggleRow(
            label = t("Pixel Shift (Burn-in Protection)"),
            checked = settings.pixelShiftEnabled,
            onCheckedChange = {
                settings.pixelShiftEnabled = it
                LyricsActivity.dispatchConfigChange(RemoteConfigPayload(pixelShiftEnabled = it))
            },
            description = t("Periodically shifts display elements slightly to prevent AMOLED/OLED burn-in."),
            icon = Icons.Rounded.Visibility,
        )
        ToggleRow(
            label = t("Night Mode"),
            checked = settings.nightModeEnabled,
            enabled = settings.smartDisplayEnabled,
            onCheckedChange = {
                settings.nightModeEnabled = it
                LyricsActivity.dispatchConfigChange(RemoteConfigPayload(nightMode = it))
            },
            description = t("Reduce brightness and switch to amber glow for dark environments."),
            icon = Icons.Rounded.Bedtime,
        )
        SettingRow(
            label = t("Preview Standby Clock"),
            description = t("Immediately switch to standby clock to preview (tap anywhere to return)."),
            icon = Icons.Rounded.Visibility,
        ) {
            SpicyButton(
                text = t("Preview Now"),
                onClick = onToggleAmbient,
            )
        }
    }

    SettingsSection(t("OpenWeatherMap Weather")) {
        ToggleRow(
            label = t("Show Real-time Weather"),
            checked = settings.smartClockShowWeather,
            onCheckedChange = {
                settings.smartClockShowWeather = it
                LyricsActivity.dispatchConfigChange(RemoteConfigPayload(showWeather = it))
            },
            description = t("Display local weather, real-time temperature and conditions on standby clock and lyrics screen."),
            icon = Icons.Rounded.WbSunny,
        )
        if (settings.smartClockShowWeather) {
            var inputKey by remember(settings.owmApiKey) { mutableStateOf(settings.owmApiKey) }
            var inputCity by remember(settings.owmCity) { mutableStateOf(settings.owmCity) }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SpicyTextField(
                    inputKey,
                    {
                        inputKey = it
                        settings.owmApiKey = it
                    },
                    onFocusLost = {
                        inputKey = it
                        settings.owmApiKey = it
                        LyricsActivity.dispatchConfigChange(RemoteConfigPayload(owmApiKey = it))
                    },
                    placeholder = t("OWM API Key (Supports remote push from phone)"),
                    password = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SpicyTextField(
                    inputCity,
                    {
                        inputCity = it
                        settings.owmCity = it
                    },
                    onFocusLost = {
                        inputCity = it
                        settings.owmCity = it
                        LyricsActivity.dispatchConfigChange(RemoteConfigPayload(owmCity = it))
                    },
                    placeholder = t("City Name (e.g. Beijing, leave blank for IP location)"),
                    modifier = Modifier.weight(1f),
                )
            }
            ToggleRow(
                label = t("Celsius (°C)"),
                checked = settings.owmUseCelsius,
                onCheckedChange = {
                    settings.owmUseCelsius = it
                    LyricsActivity.dispatchConfigChange(RemoteConfigPayload(owmUseCelsius = it))
                },
                description = t("Turn off to display Fahrenheit (°F)."),
                icon = Icons.Rounded.WbSunny,
            )
            SettingRow(
                label = t("Weather Refresh Interval"),
                description = t("Frequency of automatically updating weather from OpenWeatherMap (default 60 minutes)."),
                icon = Icons.Rounded.Schedule,
            ) {
                SpicySelect(
                    value = settings.weatherRefreshIntervalMinutes.toString(),
                    options = listOf("15", "30", "60", "120", "180", "360"),
                    labels = listOf(t("15 minutes"), t("30 minutes"), t("60 minutes (Default)"), t("2 hours"), t("3 hours"), t("6 hours")),
                    onChange = {
                        val minutes = it.toIntOrNull() ?: 60
                        settings.weatherRefreshIntervalMinutes = minutes
                        LyricsActivity.dispatchConfigChange(RemoteConfigPayload(weatherRefreshIntervalMinutes = minutes))
                    },
                )
            }
            val weather by viewModel.weather.collectAsState()
            val weatherState by viewModel.weatherClient.weatherStateFlow.collectAsState()
            val (weatherDotColor, weatherDotDesc) = when {
                !settings.smartClockShowWeather -> Pair(SpicyColors.TextSecondary.copy(alpha = 0.5f), t("Disabled"))
                weather != null -> {
                    val w = weather!!
                    val tempStr = if (settings.owmUseCelsius) "${w.tempC.toInt()}°C" else "${w.tempF.toInt()}°F"
                    val sourceLabel = if (weatherState.source.contains("Open-Meteo", ignoreCase = true)) {
                        t("Open-Meteo (Fallback)")
                    } else if (weatherState.source.contains("OpenWeatherMap", ignoreCase = true)) {
                        t("OpenWeatherMap")
                    } else weatherState.source
                    val city = com.tx24.spicyplayer.i18n.localizedCity(w.cityName)
                    val cond = com.tx24.spicyplayer.i18n.localizedWeather(w.description, w.condition)
                    Pair(SpicyColors.StatusSuccess, "$city $tempStr · $cond (${t("Data Source: %s", sourceLabel)})")
                }
                weatherState.status == "Fetching…" -> Pair(Color(0xFFFFB300), t("Fetching weather…"))
                else -> Pair(SpicyColors.StatusDanger, t(weatherState.error ?: t("Not connected / No data")))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .outlinedCard(tinted = true)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(weatherDotColor),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = t("Weather API Status"),
                            style = SpicyType.Body.copy(fontWeight = FontWeight.SemiBold),
                            color = SpicyColors.TextPrimary,
                        )
                        if (weatherState.lastFetchTimeMs > 0L) {
                            val timeFormatter = remember { java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()) }
                            Text(
                                text = "· " + t("Last updated: %s", timeFormatter.format(java.util.Date(weatherState.lastFetchTimeMs))),
                                style = SpicyType.Caption,
                                color = SpicyColors.TextSecondary,
                            )
                        }
                    }
                    Text(
                        text = weatherDotDesc,
                        style = SpicyType.Caption,
                        color = SpicyColors.TextSecondary,
                    )
                }
                SpicyButton(
                    text = t("Fetch Weather Now"),
                    onClick = { viewModel.fetchWeather(settings.owmApiKey, settings.owmCity, force = true) },
                )
            }
        }
    }

    SettingsSection(t("Remote Collaboration & Multi-Device")) {
        SettingRow(
            label = t("Remote Device Role"),
            description = t("Configure this device as a standalone player, a smart display receiver, or a remote controller."),
            icon = Icons.Rounded.Devices,
        ) {
            SpicySelect(
                value = settings.remoteRole.name,
                options = RemoteRole.entries.map { it.name },
                labels = RemoteRole.entries.map { it.label },
                onChange = { settings.remoteRole = RemoteRole.valueOf(it) },
            )
        }

        if (settings.remoteRole == RemoteRole.Sender) {
            val remoteClient = LyricsActivity.remoteClientHolder
            val isConnected by (remoteClient?.connected ?: MutableStateFlow(false)).collectAsState()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpicySpacing.S4, vertical = SpicySpacing.S2)
                    .outlinedCard(tinted = true)
                    .padding(SpicySpacing.S3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(SpicyRadii.Sm))
                        .background(if (isConnected) SpicyColors.StatusSuccess.copy(alpha = 0.15f) else SpicyColors.StatusDanger.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Rounded.CheckCircle else Icons.Rounded.Sensors,
                        contentDescription = null,
                        tint = if (isConnected) SpicyColors.StatusSuccess else SpicyColors.StatusDanger,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = if (isConnected) t("Connected to Target Display") else t("Not Connected to Display"),
                        style = SpicyType.Body.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        text = if (isConnected)
                            "${t("Target Host:")} ${settings.remoteTargetHost}:${settings.remoteTargetPort}\n${t("Display state and settings are fully synced in real-time.")}"
                        else if (settings.remoteTargetHost.isNotBlank())
                            t("Connecting to %s… Make sure display receiver is active on the same Wi-Fi.", settings.remoteTargetHost)
                        else
                            t("Tap 'Scan or Switch Display' below to scan display devices or connect via IP."),
                        style = SpicyType.Caption.copy(color = SpicyColors.TextSecondary),
                    )
                }
            }

            SettingRow(
                label = t("Select or Scan Display"),
                description = if (settings.remoteTargetHost.isNotBlank()) "${t("Target Display:")} ${settings.remoteTargetHost}:${settings.remoteTargetPort}" else t("No target display connected, tap to scan"),
                icon = Icons.Rounded.Devices,
            ) {
                SpicyButton(
                    text = t("Scan or Switch Display"),
                    onClick = onOpenConnectModal,
                )
            }
            ToggleRow(
                label = t("Remote Playback Redirection"),
                checked = settings.remotePlaybackRedirectEnabled,
                onCheckedChange = { enabled ->
                    settings.remotePlaybackRedirectEnabled = enabled
                    if (enabled) {
                        settings.spotifyCloudSyncEnabled = false
                    }
                },
                description = t("Stream local playback and lyrics to the connected display receiver.") + " " +
                    t("Note: Spotify Web API Cloud Sync and Remote Playback Redirection are mutually exclusive."),
                icon = Icons.Rounded.Sensors,
            )
        } else if (settings.remoteRole == RemoteRole.Receiver) {
            val remoteServer = LyricsActivity.remoteServerHolder
            val clientsCount by (remoteServer?.clientsCount ?: MutableStateFlow(0)).collectAsState()
            val connectedClients by (remoteServer?.connectedClients ?: MutableStateFlow(emptyList())).collectAsState()
            val localIp = remember { NetworkUtils.getLocalIpAddress() ?: "127.0.0.1" }

                if (clientsCount > 0) {
                    androidx.compose.material3.Card(
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0x334CAF50)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(12.dp)),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(28.dp),
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "● " + t("Connected to %d remote controller(s)", clientsCount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                )
                                val ipList = connectedClients.distinct().joinToString(", ").ifBlank { "127.0.0.1" }
                                Text(
                                    text = "${t("Client IPs:")} $ipList\n${t("Supports multi-client real-time remote control, playback and config sync.")}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE0E0E0),
                                )
                            }
                        }
                    }
                } else {
                    androidx.compose.material3.Card(
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0x221E93E5)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .border(1.dp, Color(30, 147, 229, 120), RoundedCornerShape(12.dp)),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                Icons.Rounded.Sensors,
                                contentDescription = null,
                                tint = Color(30, 147, 229),
                                modifier = Modifier.size(28.dp),
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "○ " + t("Waiting for remote controllers to connect…"),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                )
                                Text(
                                    text = t("Open Spicy Controller on your phone to discover this device, or enter IP (%s) to connect directly.", localIp),
                                    fontSize = 11.sp,
                                    color = Color(0xFFCCCCCC),
                                )
                            }
                        }
                    }
                }

                var nameInput by remember(settings.remoteDeviceName) { mutableStateOf(settings.remoteDeviceName) }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SpicyTextField(
                        nameInput,
                        {
                            nameInput = it
                            settings.remoteDeviceName = it
                        },
                        onFocusLost = {
                            nameInput = it
                            settings.remoteDeviceName = it
                            LyricsActivity.dispatchConfigChange(RemoteConfigPayload(remoteDeviceName = it))
                        },
                        placeholder = t("Display Broadcast Name (e.g. Living Room Display)"),
                        modifier = Modifier.weight(1f),
                    )
                }
                SettingRow(
                    label = t("Display Local Address"),
                    description = "${t("IP Address:")} $localIp  ${t("Port:")} ${settings.remoteServerPort}\n${t("Phones running Spicy Controller can auto-discover this device or connect via IP directly.")}",
                    icon = Icons.Rounded.Link,
                )
                ToggleRow(
                    label = t("Remote Playback Redirection"),
                    checked = settings.remotePlaybackRedirectEnabled,
                    onCheckedChange = { enabled ->
                        settings.remotePlaybackRedirectEnabled = enabled
                        if (enabled) {
                            settings.spotifyCloudSyncEnabled = false
                            LyricsActivity.dispatchConfigChange(RemoteConfigPayload(spotifyCloudSyncEnabled = false))
                        }
                    },
                    description = t("Stream playback and lyrics from connected phone controller to this display.") + " " +
                        t("Note: Spotify Web API Cloud Sync and Remote Playback Redirection are mutually exclusive."),
                    icon = Icons.Rounded.Sensors,
                )
            }
        }

    SettingsSection(t("Spotify Web API Cloud Sync")) {
        ToggleRow(
            label = t("Enable Spotify Cloud Sync"),
            checked = settings.spotifyCloudSyncEnabled,
            onCheckedChange = { enabled ->
                settings.spotifyCloudSyncEnabled = enabled
                if (enabled) {
                    settings.remotePlaybackRedirectEnabled = false
                }
                LyricsActivity.dispatchConfigChange(RemoteConfigPayload(spotifyCloudSyncEnabled = enabled))
            },
            description = t("Fetch playback state and lyrics directly via Spotify Web API without local media player.") + " " +
                t("Note: Spotify Web API Cloud Sync and Remote Playback Redirection are mutually exclusive."),
            icon = Icons.Rounded.CloudSync,
        )
        if (settings.spotifyCloudSyncEnabled) {
            var tokenInput by remember(settings.spotifyRefreshToken) { mutableStateOf(settings.spotifyRefreshToken) }
            var clientIdInput by remember(settings.spotifyClientId) { mutableStateOf(settings.spotifyClientId) }
            var clientSecretInput by remember(settings.spotifyClientSecret) { mutableStateOf(settings.spotifyClientSecret) }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SpicyTextField(
                    clientIdInput,
                    {
                        clientIdInput = it
                        settings.spotifyClientId = it
                    },
                    onFocusLost = {
                        clientIdInput = it
                        settings.spotifyClientId = it
                        LyricsActivity.dispatchConfigChange(RemoteConfigPayload(spotifyClientId = it))
                    },
                    placeholder = t("Spotify Client ID (Optional, used with Secret)"),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SpicyTextField(
                    clientSecretInput,
                    {
                        clientSecretInput = it
                        settings.spotifyClientSecret = it
                    },
                    onFocusLost = {
                        clientSecretInput = it
                        settings.spotifyClientSecret = it
                        LyricsActivity.dispatchConfigChange(RemoteConfigPayload(spotifyClientSecret = it))
                    },
                    placeholder = t("Spotify Client Secret (Optional)"),
                    password = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SpicyTextField(
                    tokenInput,
                    {
                        tokenInput = it
                        settings.spotifyRefreshToken = it
                    },
                    onFocusLost = {
                        tokenInput = it
                        settings.spotifyRefreshToken = it
                        LyricsActivity.dispatchConfigChange(RemoteConfigPayload(spotifyRefreshToken = it))
                    },
                    placeholder = t("Spotify Refresh Token (Supports remote push from controller)"),
                    password = true,
                    modifier = Modifier.weight(1f),
                )
            }
            val cloudManager by viewModel.cloudSpotifyManagerFlow.collectAsState()
            val spotifyStatus by (cloudManager?.status ?: MutableStateFlow(SpotifyCloudStatus.DISABLED)).collectAsState()
            val spotifyMsg by (cloudManager?.statusMessage ?: MutableStateFlow(if (settings.spotifyCloudSyncEnabled) t("Connecting / Refreshing token…") else t("Disabled"))).collectAsState()
            val spotifyLastSync by (cloudManager?.lastSyncTimeMs ?: MutableStateFlow(0L)).collectAsState()
            val spotifyPlayback by (cloudManager?.playback ?: MutableStateFlow(null)).collectAsState()

            val (spotifyDotColor, spotifyDotDesc) = when {
                !settings.spotifyCloudSyncEnabled -> Pair(SpicyColors.TextSecondary.copy(alpha = 0.5f), t("Disabled"))
                spotifyStatus == SpotifyCloudStatus.ACTIVE_PLAYBACK -> {
                    val title = spotifyPlayback?.track?.title ?: ""
                    val artist = spotifyPlayback?.track?.artist ?: ""
                    Pair(SpicyColors.StatusSuccess, t("Active Playback: %s", if (title.isNotBlank()) "$title · $artist" else "Spotify"))
                }
                spotifyStatus == SpotifyCloudStatus.NO_PLAYBACK -> Pair(SpicyColors.StatusSuccess, t("Connected (Idle, no active device)"))
                spotifyStatus == SpotifyCloudStatus.CONNECTING -> Pair(Color(0xFFFFB300), t("Connecting / Refreshing token…"))
                else -> Pair(SpicyColors.StatusDanger, t(spotifyMsg))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .outlinedCard(tinted = true)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(spotifyDotColor),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = t("Spotify Web API Status"),
                            style = SpicyType.Body.copy(fontWeight = FontWeight.SemiBold),
                            color = SpicyColors.TextPrimary,
                        )
                        if (spotifyLastSync > 0L) {
                            val timeFormatter = remember { java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()) }
                            Text(
                                text = "· " + t("Last sync: %s", timeFormatter.format(java.util.Date(spotifyLastSync))),
                                style = SpicyType.Caption,
                                color = SpicyColors.TextSecondary,
                            )
                        }
                    }
                    Text(
                        text = spotifyDotDesc,
                        style = SpicyType.Caption,
                        color = SpicyColors.TextSecondary,
                    )
                }
                SpicyButton(
                    text = t("Diagnose / Reconnect"),
                    onClick = {
                        settings.spotifyAccessToken = ""
                        cloudManager?.start()
                        cloudManager?.triggerImmediatePoll()
                    },
                )
            }

            SettingRow(
                label = t("Spotify Sync Interval"),
                description = t("Frequency of polling playback state from Spotify Web API (default 5 seconds)."),
                icon = Icons.Rounded.Schedule,
            ) {
                SpicySelect(
                    value = settings.spotifySyncIntervalSeconds.toString(),
                    options = listOf("1", "2", "3", "5", "8", "10", "15"),
                    labels = listOf(
                        t("1 second (High API Usage)"),
                        t("2 seconds (Fast)"),
                        t("3 seconds"),
                        t("5 seconds (Default / Recommended)"),
                        t("8 seconds (Balanced / Safe)"),
                        t("10 seconds (Power & Quota Save)"),
                        t("15 seconds"),
                    ),
                    onChange = {
                        val seconds = it.toIntOrNull() ?: 5
                        settings.spotifySyncIntervalSeconds = seconds
                        LyricsActivity.dispatchConfigChange(RemoteConfigPayload(spotifySyncIntervalSeconds = seconds))
                    },
                )
            }

            var showQrSection by remember { mutableStateOf(false) }
            val context = androidx.compose.ui.platform.LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            var useDeepLinkScheme by remember { mutableStateOf(true) }
            val redirectUri = if (useDeepLinkScheme) {
                SpotifyAuthUtils.DEEP_LINK_REDIRECT
            } else {
                "http://127.0.0.1:${settings.remoteServerPort}/callback"
            }
            val authUrl = remember(settings.spotifyClientId, redirectUri) {
                if (settings.spotifyClientId.isNotBlank()) {
                    SpotifyAuthUtils.buildAuthorizeUrl(settings.spotifyClientId, redirectUri)
                } else ""
            }

            SettingRow(
                label = t("QR Code and Auth Link Login"),
                description = t("Display mobile QR code, direct auth URL, or paste auth Code manually."),
                icon = Icons.Rounded.QrCode2,
            ) {
                SpicyButton(
                    text = if (showQrSection) t("Collapse Login Guide") else t("QR Code / Link Auth"),
                    onClick = {
                        if (settings.spotifyClientId.isBlank()) {
                            Toast.makeText(context, t("Please enter Spotify Client ID above first"), Toast.LENGTH_SHORT).show()
                        } else {
                            showQrSection = !showQrSection
                        }
                    },
                )
            }

            if (showQrSection && authUrl.isNotBlank()) {
                var manualInput by remember { mutableStateOf("") }
                var isExchanging by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x33000000))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    androidx.compose.material3.Card(
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(30, 147, 229, 30)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(30, 147, 229, 100), RoundedCornerShape(12.dp)),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "💡 " + t("Spotify Developer Dashboard Setup:"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(30, 147, 229),
                            )
                            Text(
                                text = t("Spotify security policy restricts local HTTP URIs. Please add both URIs to Redirect URIs in Developer Dashboard:"),
                                fontSize = 11.sp,
                                color = Color(210, 210, 210),
                            )

                            // URI 1: spicyremote://callback
                            val deepLinkUri = SpotifyAuthUtils.DEEP_LINK_REDIRECT
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (useDeepLinkScheme) Color(30, 147, 229, 60) else Color.Black.copy(alpha = 0.4f))
                                    .border(1.dp, if (useDeepLinkScheme) Color(30, 147, 229) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable {
                                        useDeepLinkScheme = true
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("Redirect URI", deepLinkUri))
                                        Toast.makeText(context, t("Copied %s to clipboard", "spicyremote://callback"), Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text = "$deepLinkUri (" + t("Recommended: Phone & Controller wake-up") + ")",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = if (useDeepLinkScheme) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    )
                                }
                                androidx.compose.material3.Icon(
                                    Icons.Rounded.ContentCopy,
                                    contentDescription = t("Copy"),
                                    tint = Color(30, 147, 229),
                                    modifier = Modifier.size(16.dp),
                                )
                            }

                            // URI 2: 127.0.0.1
                            val localUri = "http://127.0.0.1:${settings.remoteServerPort}/callback"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!useDeepLinkScheme) Color(30, 147, 229, 60) else Color.Black.copy(alpha = 0.4f))
                                    .border(1.dp, if (!useDeepLinkScheme) Color(30, 147, 229) else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable {
                                        useDeepLinkScheme = false
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                        clipboard?.setPrimaryClip(ClipData.newPlainText("Redirect URI", localUri))
                                        Toast.makeText(context, t("Copied %s to clipboard", "127.0.0.1"), Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text = "$localUri (" + t("Display Local Browser") + ")",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = if (!useDeepLinkScheme) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    )
                                }
                                androidx.compose.material3.Icon(
                                    Icons.Rounded.ContentCopy,
                                    contentDescription = t("Copy"),
                                    tint = Color(30, 147, 229),
                                    modifier = Modifier.size(16.dp),
                                )
                            }

                            if (!useDeepLinkScheme) {
                                var portInput by remember(settings.remoteServerPort) { mutableStateOf(settings.remoteServerPort.toString()) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = t("Custom Port:"),
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    SpicyTextField(
                                        portInput,
                                        {
                                            portInput = it
                                            val p = it.toIntOrNull()
                                            if (p != null && p in 1024..65535) {
                                                settings.remoteServerPort = p
                                            }
                                        },
                                        onFocusLost = {
                                            val p = portInput.toIntOrNull()
                                            if (p != null && p in 1024..65535) {
                                                settings.remoteServerPort = p
                                            } else {
                                                portInput = settings.remoteServerPort.toString()
                                            }
                                        },
                                        placeholder = "8887",
                                        modifier = Modifier.width(90.dp),
                                    )
                                    Text(
                                        text = t("(1024–65535, e.g. 8888)"),
                                        fontSize = 10.sp,
                                        color = Color(180, 180, 180),
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = t("Method 1: Scan QR Code with Phone (Recommended)"),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = t("Scan the QR code with phone camera or browser to authorize, automatically syncing to display:"),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )

                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        QrCodeView(
                            content = authUrl,
                            modifier = Modifier.fillMaxSize(),
                            sizePx = 400,
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = t("Method 2: Open Auth Link in Browser"),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SpicyButton(
                            text = t("Open in Browser"),
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, t("Cannot open browser: %s", e.message ?: ""), Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )

                        SpicyButton(
                            text = t("Copy Auth Link"),
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("Spotify URL", authUrl))
                                Toast.makeText(context, t("Auth link copied to clipboard"), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = t("Method 3: Paste Callback URL or Code"),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = t("If browser shows page cannot be opened after login, copy the full address bar URL and paste below:"),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SpicyTextField(
                            manualInput,
                            { manualInput = it },
                            placeholder = t("Paste callback URL or Code"),
                            modifier = Modifier.weight(1f),
                        )

                        SpicyButton(
                            text = if (isExchanging) t("Verifying…") else t("Verify & Authorize"),
                            onClick = {
                                val code = SpotifyAuthUtils.extractCode(manualInput)
                                if (code.isBlank()) {
                                    Toast.makeText(context, t("Please enter a valid callback URL or Code"), Toast.LENGTH_SHORT).show()
                                    return@SpicyButton
                                }

                                if (settings.remoteRole == RemoteRole.Sender) {
                                    LyricsActivity.remoteClientHolder?.sendMessage(
                                        RemoteMessage(
                                            type = RemoteMessageType.COMMAND,
                                            command = RemoteCommandPayload(
                                                action = RemoteAction.SPOTIFY_AUTH_CODE,
                                                valueString = code,
                                            ),
                                        )
                                    )
                                }

                                isExchanging = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    val client = SpotifyWebApiClient()
                                    val secret = settings.spotifyClientSecret.ifBlank { null }
                                    val res = client.exchangeAuthCode(settings.spotifyClientId, secret ?: "", code, redirectUri)
                                        ?: client.exchangeAuthCode(settings.spotifyClientId, secret ?: "", code, SpotifyAuthUtils.DEEP_LINK_REDIRECT)
                                        ?: client.exchangeAuthCode(settings.spotifyClientId, secret ?: "", code, SpotifyAuthUtils.getLocalhostRedirect(settings.remoteServerPort))
                                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                                        isExchanging = false
                                        if (res != null && !res.refreshToken.isNullOrBlank()) {
                                            settings.spotifyAccessToken = ""
                                            settings.spotifyRefreshToken = res.refreshToken ?: ""
                                            settings.spotifyAccessToken = res.accessToken
                                            tokenInput = res.refreshToken ?: ""
                                            settings.spotifyCloudSyncEnabled = true
                                            showQrSection = false
                                            if (settings.remoteRole == RemoteRole.Sender) {
                                                LyricsActivity.remoteClientHolder?.sendMessage(
                                                    RemoteMessage(
                                                        type = RemoteMessageType.CONFIG_PUSH,
                                                        config = com.tx24.spicyplayer.playback.remote.RemoteConfigPayload(
                                                            spotifyRefreshToken = res.refreshToken,
                                                            spotifyCloudSyncEnabled = true,
                                                        ),
                                                    )
                                                )
                                            }
                                            Toast.makeText(context, t("Spotify Authorization Successful!"), Toast.LENGTH_LONG).show()
                                        } else {
                                            if (settings.remoteRole == RemoteRole.Sender) {
                                                Toast.makeText(context, t("Sent auth code to display for token exchange!"), Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, t("Token exchange failed, please check Client Secret or Code"), Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DeviceContent(settings: AppSettings) {
    ToggleRow(
        label = "Smoother motion",
        checked = settings.highRefreshRate,
        onCheckedChange = { settings.highRefreshRate = it },
        description = "Draw at your screen's full refresh rate instead of 60 Hz. Uses slightly more battery.",
        icon = Icons.Rounded.Animation,
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) ToggleRow(
        label = "Show over lock screen",
        checked = settings.showOverLockScreen,
        onCheckedChange = { settings.showOverLockScreen = it },
        description = "The power button brings the lyrics back without unlocking. Settings still ask to unlock.",
        icon = Icons.Rounded.ScreenLockPortrait,
    )
    ToggleRow(
        label = "Ambient mode",
        checked = settings.ambientMode,
        onCheckedChange = { settings.ambientMode = it },
        description = "Left untouched while music plays, dim to the lyrics and the song on black.",
        searchDescription = "Untouched for 30 seconds while music plays, dim to the lyrics alone on black.",
        icon = Icons.Rounded.DarkMode,
    )
    SettingRow(
        label = "Dim After",
        description = "How long ambient mode waits after the last touch.",
        icon = Icons.Rounded.Timer,
        enabled = settings.ambientMode,
    ) {
        SpicySelect(
            value = settings.ambientDelay.name,
            options = AmbientDelay.entries.map { it.name },
            labels = AmbientDelay.entries.map { it.label },
            onChange = { settings.ambientDelay = AmbientDelay.valueOf(it) },
        )
    }
    SubPageCard {
        val supported = PictureInPicture.supported(LocalContext.current)
        SubPageRow(
            SettingsPage.FloatingLyrics,
            SpicyGlyphs.PictureInPicture,
            when {
                !supported -> t("Not available on this phone")
                settings.autoPip -> t("Opens when you leave the app · %s", t(settings.pipShape.label))
                else -> t("Off")
            },
        )
    }
    HapticsSection(settings)
    MoreOptions("Keeping the screen on, system bars, low performance mode, interface animations") {
        ToggleRow(
            label = "Keep the screen on",
            checked = settings.keepScreenOn,
            onCheckedChange = { settings.keepScreenOn = it },
            description = "Stop the screen from turning off while music plays.",
            icon = Icons.Rounded.StayCurrentPortrait,
        )
        SettingRow(
            label = "System bars",
            description = "Whether the status and navigation bars stay on screen over the lyrics.",
            icon = Icons.Rounded.Fullscreen,
        ) {
            SpicySelect(
                value = settings.systemBars.name,
                options = SystemBarsMode.entries.map { it.name },
                labels = SystemBarsMode.entries.map { it.label },
                onChange = { settings.systemBars = SystemBarsMode.valueOf(it) },
            )
        }
        SettingsSection("Performance") {
            ToggleRow(
                label = "Low performance mode",
                checked = settings.lowPerformance,
                onCheckedChange = { settings.lowPerformance = it },
                description = "Stills the background and cover and turns off blur and glow, for smoother lyrics on slower phones.",
                icon = Icons.Rounded.Speed,
            )
            ToggleRow(
                label = "Interface animations",
                checked = settings.uiAnimations && !settings.lowPerformance,
                onCheckedChange = { settings.uiAnimations = it },
                description = "Animate the playback buttons, the queue and pop-up screens. Off in low performance mode.",
                icon = Icons.Rounded.Animation,
                enabled = !settings.lowPerformance,
            )
        }
    }
}

@Composable
internal fun FloatingLyricsContent(settings: AppSettings) {
    val supported = PictureInPicture.supported(LocalContext.current)
    ToggleRow(
        label = "Floating lyrics",
        checked = settings.autoPip,
        onCheckedChange = { settings.autoPip = it },
        description = "Leave the app while a song plays and the lyrics shrink into a small floating window.",
        icon = SpicyGlyphs.PictureInPicture,
        enabled = supported,
    )
    SettingRow(
        label = "Shape",
        description = "The floating window's width to height.",
        icon = Icons.Rounded.AspectRatio,
        enabled = supported,
    ) {
        SpicySelect(
            value = settings.pipShape.name,
            options = PipShape.entries.map { it.name },
            labels = PipShape.entries.map { it.label },
            onChange = { settings.pipShape = PipShape.valueOf(it) },
            enabled = supported,
        )
    }
    if (settings.pipShape == PipShape.Custom) {
        SettingRow(
            label = "Custom shape",
            description = "Width to height, from tall (0.42 : 1) to wide (2.39 : 1).",
            icon = Icons.Rounded.Crop,
            enabled = supported,
            stacked = true,
        ) {
            SpicyBipolarSlider(
                value = settings.pipCustomRatio,
                range = PIP_RATIO_RANGE,
                step = 1,
                onValueChange = { settings.pipCustomRatio = it },
                default = 100,
                enabled = supported,
                valueText = { String.format(Locale.ROOT, "%.2f : 1", it / 100f) },
            )
        }
    }
    ToggleRow(
        label = "Show lyrics",
        checked = settings.pipShowLyrics,
        onCheckedChange = { settings.pipShowLyrics = it },
        description = "Off shows the song instead: cover, title and artist.",
        icon = Icons.Rounded.Lyrics,
        enabled = supported,
    )
    SettingRow(
        label = "Lyrics size",
        description = "The floating window's own text size, apart from the full screen's.",
        icon = Icons.Rounded.FormatSize,
        enabled = supported && settings.pipShowLyrics,
        stacked = true,
        searchDescription = "Make the floating lyrics smaller or bigger. Picture-in-picture text size.",
    ) {
        SpicyBipolarSlider(
            value = settings.pipLyricsSize,
            range = LYRICS_SIZE_RANGE,
            step = 5,
            onValueChange = { settings.pipLyricsSize = it },
            default = 100,
            unit = "%",
            enabled = supported && settings.pipShowLyrics,
        )
    }
    ToggleRow(
        label = "Current line at the top",
        checked = settings.pipLineAtTop,
        onCheckedChange = { settings.pipLineAtTop = it },
        description = "Keep the line being sung at the top of the window, so more of what's next fits.",
        icon = Icons.Rounded.VerticalAlignTop,
        enabled = supported && settings.pipShowLyrics,
    )
    ToggleRow(
        label = "Song bar",
        checked = settings.pipDock,
        onCheckedChange = { settings.pipDock = it },
        description = "Square and tall windows show the title, artist and timeline under the lyrics.",
        icon = Icons.Rounded.MusicNote,
        enabled = supported && settings.pipShowLyrics,
    )
    ToggleRow(
        label = "Cover in the song bar",
        checked = settings.pipDockCover,
        onCheckedChange = { settings.pipDockCover = it },
        description = "A small cover beside the title and artist.",
        icon = Icons.Rounded.Album,
        enabled = supported && settings.pipShowLyrics && settings.pipDock,
    )
    SettingRow(
        label = "Without lyrics, show",
        description = "What the window shows for a song that has no lyrics.",
        icon = Icons.Rounded.Album,
        enabled = supported && settings.pipShowLyrics,
    ) {
        SpicySelect(
            value = settings.pipWithoutLyrics.name,
            options = PipWithoutLyrics.entries.map { it.name },
            labels = PipWithoutLyrics.entries.map { it.label },
            onChange = { settings.pipWithoutLyrics = PipWithoutLyrics.valueOf(it) },
            enabled = supported && settings.pipShowLyrics,
        )
    }
}

@Composable
internal fun SourcesContent(state: PlayerUiState, viewModel: ExternalPlaybackViewModel) {
    var clientKey by remember { mutableStateOf("") }
    val ranking = state.sourceRanking
    val names = state.sourceDescriptors.associate { it.id to it.displayName }
    Searchable("Sources", "Priority", "Order", "Providers", *ranking.origins.map { it.displayName }.toTypedArray()) {
        Text(
            t(
                "Each source is where lyrics come from; higher sources win when the timing is as good. Tap one to pick the services " +
                    "that fetch it and the sync types it may give. Services that haven't said any app may use them say what they send " +
                    "before they're switched on."
            ),
            style = DescriptionStyle,
            modifier = Modifier.padding(start = 2.dp, end = 2.dp, bottom = SpicySpacing.S3),
        )
    }
    SourceList(ranking, names, viewModel)
    MoreOptions("Custom sources, background vocals in brackets, keeping lyrics longer, your own Spicy Lyrics key") {
        CustomSourcesSection(state, viewModel)
        SettingsSection("Reading lyrics") {
            ToggleRow(
                label = "Background vocals in brackets",
                description = "A \"(…)\" at the start or end of a synced line is shown as its background vocals.",
                checked = state.splitBackgroundVocals,
                onCheckedChange = viewModel::setSplitBackgroundVocals,
                icon = Icons.Rounded.RecordVoiceOver,
            )
            ToggleRow(
                label = "Keep lyrics longer",
                description = "Found lyrics are kept for 30 days instead of 3, so they still show offline. Off, a newly uploaded TTML arrives sooner.",
                checked = state.keepLyricsLonger,
                onCheckedChange = viewModel::setKeepLyricsLonger,
                icon = Icons.Rounded.Storage,
            )
        }
        SettingsSection("Spicy Lyrics key") {
            val context = LocalContext.current
            val ownKey = state.ownKeyHint
            SettingRow(
                label = "Get your own key",
                description = if (ownKey == null) {
                    "The built-in key works for everyone. If you want your own, sign in, tap Add, and paste the client key below."
                } else {
                    "You're using your own key, $ownKey. Its page on the developer site can pause or replace it."
                },
                icon = Icons.Rounded.Key,
            ) {
                SpicyButton(if (ownKey == null) "Get key" else "Open", onClick = { openUrl(context, SpicyLyricsKey.CATALOG_URL) })
            }
            SettingRow(
                label = "Your Spicy Lyrics key",
                description = if (ownKey == null) "Paste the client key (sl_pk_…). Empty uses the built-in key." else "Paste a new key to replace yours.",
                icon = Icons.Rounded.Key,
                stacked = true,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SpicySpacing.S2)) {
                    SpicyTextField(clientKey, { clientKey = it }, placeholder = "sl_pk_…", password = true, modifier = Modifier.weight(1f))
                    SpicyButton("Use", onClick = {
                        viewModel.useApiKey(clientKey)
                        if (SpicyLyricsKey.check(clientKey) is SpicyLyricsKey.Check.Ok) clientKey = ""
                    })
                }
            }
            if (ownKey != null) {
                SettingRow(
                    label = "Use the built-in key",
                    description = "Forget your key on this phone. It keeps working on the developer site until you delete it there.",
                    icon = Icons.Rounded.KeyOff,
                ) {
                    SpicyButton("Remove", onClick = { viewModel.useApiKey("") })
                }
            }
        }
    }
}

/**
 * The sources as cards, in the user's order, reordered by dragging a card's handle: the card
 * follows the finger, the others make way, the page scrolls at its edges, and the order is saved
 * when it's dropped. Tapping a card opens it.
 */
@Composable
private fun SourceList(ranking: SourceRanking, names: Map<String, String>, viewModel: ExternalPlaybackViewModel) {
    var expanded by remember { mutableStateOf(emptySet<String>()) }
    val byId = ranking.origins.associateBy { it.id }
    // While a card is dragged: the order shown, which card, and how far it is from its slot.
    var dragOrder by remember { mutableStateOf<List<String>?>(null) }
    var dragged by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val heights = remember { mutableMapOf<String, Float>() }
    val tops = remember { mutableMapOf<String, Float>() }
    val density = LocalDensity.current
    val gap = with(density) { SpicySpacing.S2.toPx() }
    val edge = with(density) { DRAG_EDGE.toPx() }
    val windowHeight = LocalWindowInfo.current.containerSize.height.toFloat()
    val scroll = LocalPageScroll.current
    val haptics = LocalHapticFeedback.current
    val animate = LocalUiAnimations.current
    val saved = ranking.origins.map { it.id }
    val shown = dragOrder ?: saved
    // A card's drag handler outlives the composition it started in: it reads the order through
    // these, or a second drag starts from the order before the first one.
    val currentShown by rememberUpdatedState(shown)
    val currentSaved by rememberUpdatedState(saved)

    // Past half of a neighbour, the dragged card takes its place; its offset keeps it under the finger.
    fun settle() {
        val id = dragged ?: return
        val order = (dragOrder ?: return).toMutableList()
        var i = order.indexOf(id)
        var moved = false
        while (true) {
            val toward = if (dragOffset > 0f) i + 1 else i - 1
            if (toward !in order.indices) break
            val room = (heights[order[toward]] ?: break) + gap
            if (abs(dragOffset) <= room / 2f) break
            Collections.swap(order, i, toward)
            dragOffset += if (toward > i) -room else room
            i = toward
            moved = true
        }
        if (moved) {
            dragOrder = order
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    // Near the top or bottom of the screen, the page scrolls under the dragged card.
    LaunchedEffect(dragged) {
        val id = dragged ?: return@LaunchedEffect
        if (scroll == null || windowHeight <= 0f) return@LaunchedEffect
        while (dragged == id) {
            val top = (tops[id] ?: 0f) + dragOffset
            val bottom = top + (heights[id] ?: 0f)
            val speed = when {
                top < edge -> -(edge - top) / edge
                bottom > windowHeight - edge -> (bottom - windowHeight + edge) / edge
                else -> 0f
            }.coerceIn(-1f, 1f) * DRAG_SCROLL_PX
            if (speed != 0f) {
                dragOffset += scroll.scrollBy(speed)
                settle()
            }
            withFrameNanos { }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(SpicySpacing.S2)) {
        shown.forEachIndexed { index, id ->
            val origin = byId[id] ?: return@forEachIndexed
            val summary = origin.summary(ranking)
            val isDragged = dragged == id
            key(id) {
                Searchable("Sources", origin.displayName, summary, *origin.providerIds.map { names[it] ?: it }.toTypedArray()) {
                    SourceCard(
                        rank = index + 1,
                        origin = origin,
                        summary = summary,
                        ranking = ranking,
                        providerName = { names[it] ?: it },
                        expanded = id in expanded,
                        dragged = isDragged,
                        onExpand = { expanded = if (id in expanded) expanded - id else expanded + id },
                        onEnabledChange = { viewModel.setSourceEnabled(id, it) },
                        onProviderChange = { provider, on -> viewModel.setProviderEnabled(id, provider, on) },
                        onSyncTypesChange = { viewModel.setSourceSyncTypes(id, it) },
                        handle = Modifier.pointerInput(id) {
                            detectDragGestures(
                                onDragStart = {
                                    dragOrder = currentShown
                                    dragged = id
                                    dragOffset = 0f
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDragEnd = {
                                    val order = dragOrder
                                    dragged = null
                                    dragOffset = 0f
                                    dragOrder = null
                                    if (order != null && order != currentSaved) viewModel.setSourceOrder(order)
                                },
                                onDragCancel = {
                                    dragged = null
                                    dragOffset = 0f
                                    dragOrder = null
                                },
                            ) { change, amount ->
                                change.consume()
                                dragOffset += amount.y
                                settle()
                            }
                        },
                        modifier = Modifier
                            // Where the card's slot is, read before the drag moves it.
                            .onGloballyPositioned {
                                heights[id] = it.size.height.toFloat()
                                tops[id] = it.positionInWindow().y
                            }
                            .glidesToPlace(enabled = animate && dragged != null && !isDragged)
                            .zIndex(if (isDragged) 1f else 0f)
                            .graphicsLayer {
                                if (isDragged) {
                                    translationY = dragOffset
                                    scaleX = DRAGGED_SCALE
                                    scaleY = DRAGGED_SCALE
                                    // Lifted off the list: a shadow under it.
                                    shadowElevation = DRAGGED_ELEVATION.toPx()
                                    shape = RoundedCornerShape(SpicyRadii.Lg)
                                }
                            },
                    )
                }
            }
        }
    }
}

/** A card the reorder moved glides from where it was instead of jumping. */
private fun Modifier.glidesToPlace(enabled: Boolean): Modifier = composed {
    val shift = remember { Animatable(0f) }
    var lastY by remember { mutableStateOf<Float?>(null) }
    val scope = rememberCoroutineScope()
    val enabledNow by rememberUpdatedState(enabled)
    onPlaced { coordinates ->
        val y = coordinates.positionInParent().y
        val previous = lastY
        lastY = y
        if (enabledNow && previous != null && previous != y) {
            scope.launch {
                shift.snapTo(shift.value + previous - y)
                shift.animateTo(0f, tween(SpicyMotion.FAST_MS, easing = SpicyMotion.Standard))
            }
        }
    }.graphicsLayer { translationY = shift.value }
}

/**
 * A source's card: the rank in a ring, the name and what it gives, a chevron, the switch and the
 * drag handle. Tapping the card opens it to the providers that fetch the source and the sync
 * types it may give.
 */
@Composable
private fun SourceCard(
    rank: Int,
    origin: LyricsOrigin,
    summary: String,
    ranking: SourceRanking,
    providerName: (String) -> String,
    expanded: Boolean,
    dragged: Boolean,
    onExpand: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onProviderChange: (String, Boolean) -> Unit,
    onSyncTypesChange: (Set<RemoteLyricsQuality>) -> Unit,
    handle: Modifier,
    modifier: Modifier = Modifier,
) {
    val enabled = LyricsSourceSettings.isOn(ranking, origin.id)
    val animate = LocalUiAnimations.current
    val chevron by animateFloatAsState(
        if (expanded) 180f else 0f,
        if (animate) tween(SpicyMotion.FAST_MS, easing = SpicyMotion.Standard) else snap(),
        label = "sourceChevron",
    )
    // Dragged, the card is solid, or the cards it passes would show through its tint: it carries
    // the page's own backdrop (the blurred lyrics and the theme's wash), so it looks the same.
    val backdrop = LocalSettingsBackdrop.current
    val brand = SpicyColors.brand
    val lifted = if (!dragged) Modifier else Modifier
        .clip(RoundedCornerShape(SpicyRadii.Lg))
        .then(
            if (backdrop != null) {
                Modifier.hazeEffect(backdrop, SettingsBackdrop) { inputScale = HazeInputScale.Fixed(BACKDROP_INPUT_SCALE) }
            } else {
                Modifier.background(Color.Black.copy(alpha = 0.97f))
            },
        )
        .drawBehind {
            if (brand != null) {
                drawRect(brand.fieldDeep.copy(alpha = 0.14f))
                drawBrandRamp(brand, fromAlpha = 0.26f, toAlpha = 0.4f)
            }
        }
    Column(modifier.fillMaxWidth().then(lifted).outlinedCard(tinted = true)) {
        Row(
            Modifier.fillMaxWidth().padding(end = SpicySpacing.S1),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpicySpacing.S2),
        ) {
          // What opens the source, beside its switch rather than around it: a remote's arrows only
          // move to what lies outside the focused control.
          Row(
            Modifier
                .weight(1f)
                .focusRing(RoundedCornerShape(SpicyRadii.Md))
                .clickable(remember { MutableInteractionSource() }, indication = null, role = Role.Button, onClick = onExpand)
                .padding(start = SpicySpacing.S3, top = SpicySpacing.S3, bottom = SpicySpacing.S3),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpicySpacing.S2),
          ) {
            Box(
                Modifier
                    .size(28.dp)
                    .background(RankFill, CircleShape)
                    .border(1.dp, SpicyColors.Hairline, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("$rank", style = SpicyType.Caption.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"))
            }
            RowLabel(origin.displayName, summary, Modifier.weight(1f).alpha(if (enabled) 1f else DISABLED_ALPHA))
            Icon(
                Icons.Rounded.ExpandMore,
                if (expanded) "Close ${origin.displayName}" else "Open ${origin.displayName}",
                tint = SpicyColors.TextSecondary,
                modifier = Modifier.size(20.dp).rotate(chevron),
            )
          }
            SpicyToggle(enabled, onCheckedChange = onEnabledChange)
            Box(handle.size(32.dp, 40.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.DragIndicator, "Drag ${origin.displayName} to reorder", tint = SpicyColors.TextTertiary, modifier = Modifier.size(20.dp))
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = if (animate) expandVertically(tween(SpicyMotion.MODAL_MS, easing = SpicyMotion.Modal)) + fadeIn(tween(SpicyMotion.FAST_MS)) else EnterTransition.None,
            exit = if (animate) shrinkVertically(tween(SpicyMotion.FAST_MS, easing = SpicyMotion.Standard)) + fadeOut(tween(SpicyMotion.FAST_MS)) else ExitTransition.None,
        ) {
            Column(
                Modifier.fillMaxWidth().padding(start = SpicySpacing.S3, end = SpicySpacing.S3, bottom = SpicySpacing.S3),
                verticalArrangement = Arrangement.spacedBy(SpicySpacing.S3),
            ) {
                origin.providerIds.forEach { provider ->
                    val on = ranking.pairOn(origin.id, provider)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SpicySpacing.S2)) {
                        RowLabel(
                            providerName(provider),
                            providerNote(origin, provider),
                            Modifier.weight(1f).alpha(if (on && ranking.sourceOn(origin.id)) 1f else DISABLED_ALPHA),
                            icon = if (LyricsBlends.byId(provider) != null) Icons.AutoMirrored.Rounded.CallMerge else Icons.Rounded.Cloud,
                        )
                        SpicyToggle(on, onCheckedChange = { onProviderChange(provider, it) })
                    }
                }
                if (origin.qualities.size > 1) {
                    val off = ranking.policy.syncTypesOff[origin.id].orEmpty()
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SpicySpacing.S2)) {
                        RowLabel(t("Sync types"), t("Answers in a type left out don't count."), Modifier.weight(1f), icon = Icons.Rounded.Sync)
                        SpicyMultiSelect(
                            selected = origin.qualities.filter { it !in off }.map { it.name }.toSet(),
                            options = origin.qualities.map { it.name },
                            labels = origin.qualities.map { it.typeLabel() },
                            onChange = { picked -> onSyncTypesChange(picked.map(RemoteLyricsQuality::valueOf).toSet()) },
                        )
                    }
                }
            }
        }
    }
}

/** How close to the screen's top or bottom a dragged card scrolls the page. */
private val DRAG_EDGE = 72.dp
/** The page's fastest scroll under a dragged card, per frame. */
private const val DRAG_SCROLL_PX = 18f
private const val DRAGGED_SCALE = 1.02f
private val DRAGGED_ELEVATION = 12.dp

/** The rank ring's fill. */
private val RankFill = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.09f)

/** What a source gives (the sync types it has on) and, when several can fetch it, how many do. */
private fun LyricsOrigin.summary(ranking: SourceRanking): String {
    val off = ranking.policy.syncTypesOff[id].orEmpty()
    val types = qualities.filter { it !in off }
    val hasWord = RemoteLyricsQuality.WORD_SYNCED in types
    val hasLine = RemoteLyricsQuality.LINE_SYNCED in types
    val hasPlain = RemoteLyricsQuality.PLAIN in types
    val syncPart = when {
        hasWord && hasLine -> t("Word and line synced")
        hasWord -> t("Word synced")
        hasLine -> t("Line synced")
        hasPlain -> t("Unsynced")
        else -> t("No sync type on")
    }
    val gives = if (hasPlain && (hasWord || hasLine)) "$syncPart, ${t("unsynced")}" else syncPart
    if (providerIds.size <= 1) return gives
    val activeCount = providerIds.count { ranking.pairOn(id, it) }
    return t("%s · %d of %d on", gives, activeCount, providerIds.size)
}

private fun RemoteLyricsQuality.typeLabel() = when (this) {
    RemoteLyricsQuality.WORD_SYNCED -> t("Word")
    RemoteLyricsQuality.LINE_SYNCED -> t("Line")
    else -> t("Unsynced")
}

/** A short line on how [provider] gets [origin]'s lyrics, where that isn't plain from its name. */
private fun providerNote(origin: LyricsOrigin, provider: String): String? = when {
    LyricsBlends.byId(provider) != null -> t("Lines from the sources above, word timing from its donors.")
    provider == "rmm_revival" -> t("Relays Spicy Lyrics' API by Apple Music ID.")
    provider == "spicy_unison" -> t("Spicy Lyrics' copy of Unison's lyrics, by the song's ISRC or YouTube video.")
    provider == "spicy_lyrics" && origin.id != LyricsOrigins.SPICY_COMMUNITY -> t("Passed on by Spicy Lyrics' API.")
    provider == "lrc_red" && origin.id == LyricsOrigins.APPLE_MUSIC -> t("Serves Apple Music's lyrics by ISRC.")
    provider == "lrcmux" -> t("Passes on what it finds at %s.", origin.displayName)
    else -> null
}

@Composable
internal fun AdvancedContent(state: PlayerUiState, viewModel: ExternalPlaybackViewModel, updater: UpdateViewModel, settings: AppSettings) {
    SettingsSection(t("Language")) {
        val languageCodes = listOf("system", "en", "zh")
        val languageLabels = listOf(
            t("Follow System (%s)", java.util.Locale.getDefault().displayName),
            "English",
            t("Simplified Chinese"),
        )
        SettingRow(
            label = t("Language"),
            description = t("Select application display language. Changes take effect immediately."),
            icon = Icons.Rounded.Translate,
        ) {
            SpicySelect(
                value = settings.appLanguage,
                options = languageCodes,
                labels = languageLabels,
                onChange = {
                    settings.appLanguage = it
                    LyricsActivity.dispatchConfigChange(RemoteConfigPayload(appLanguage = it))
                    viewModel.fetchWeather(settings.owmApiKey, settings.owmCity, force = true)
                },
            )
        }
    }

    SettingsSection(t("Cache & Data")) {
        SettingRow(
            label = t("Clear All Caches for Current Song"),
            description = t("Remove all cached lyrics data for the currently playing track."),
            icon = Icons.Rounded.DeleteSweep,
        ) {
            SpicyButton(t("Clear"), onClick = viewModel::clearCurrentSongCaches)
        }
        SettingRow(
            label = t("Clear Stored Lyrics Cache"),
            description = t("Delete every cached lyric and translation, then reload the current song."),
            icon = Icons.Rounded.DeleteForever,
        ) {
            SpicyButton(t("Clear Cache"), onClick = viewModel::clearStoredLyricsCache)
        }
        SettingRow(
            label = t("Clear Current Song from Internal State"),
            description = t("Remove the current song's lyrics from the in-memory state only."),
            icon = Icons.Rounded.Memory,
        ) {
            SpicyButton(t("Clear State"), onClick = viewModel::clearCurrentSongFromMemory)
        }
    }
    SettingsSection("Backup") {
        SettingsBackupRows(viewModel, settings)
    }
    SettingsSection("Help") {
        SettingRow(
            label = "Show the Introduction",
            description = "Walk through the first-run screens again, gestures included.",
            icon = Icons.Rounded.School,
        ) {
            SpicyButton("Show", onClick = {
                settings.introDone = false
                settings.introReplay = true
            })
        }
    }
    if (updater.enabled) {
        SettingsSection("Updates") {
            ToggleRow(
                label = "Include pre-releases",
                checked = settings.includePrereleases,
                onCheckedChange = { settings.includePrereleases = it },
                description = "Also offer test builds, which get new things first and may be rough.",
                icon = Icons.Rounded.Science,
            )
        }
    }
    SettingsSection("Privacy") {
        val context = LocalContext.current
        ToggleRow(
            label = "Share anonymous usage stats",
            checked = settings.usageStats,
            onCheckedChange = {
                settings.usageStats = it
                UsageStats.onSettingChanged(context, it)
            },
            description = "How many people use the app, on which versions and devices, and which features they use. Never what you listen to.",
            icon = Icons.Rounded.Insights,
        )
    }
    SettingsSection("Debug info") {
        CopyDebugInfoRow(state)
        CopyExtraLogsRow()
    }
    MoreOptions("Session, these lyrics, translation and last lookup details") {
        SettingsSection("Session") { InfoLines(sessionLines(state)) }
        lyricsLines(state)?.let { details -> SettingsSection("These lyrics") { InfoLines(details) } }
        SettingsSection("Romanization and translation") {
            InfoLines(textLines(LocalContext.current, state))
            // What translation did last, newest last. The whole story goes in the extra logs.
            val log = com.tx24.spicyplayer.playback.TranslationTrace.lines(android.os.SystemClock.elapsedRealtime())
            if (log.isEmpty()) {
                Text("Nothing translated yet", style = DescriptionStyle, modifier = Modifier.padding(horizontal = SpicySpacing.S3, vertical = SpicySpacing.S1))
            }
            log.takeLast(SHOWN_TRANSLATION_LOG).forEach { line ->
                Searchable("Translation log", line, "Diagnostics") {
                    Text(line, style = SpicyType.Caption.copy(color = SpicyColors.TextSecondary),
                        modifier = Modifier.padding(horizontal = SpicySpacing.S3, vertical = SpicySpacing.S1))
                }
            }
        }
        SettingsSection("Last lookup") {
            Searchable("Last lookup", "Attempts", "Diagnostics") {
                if (state.providerAttempts.isEmpty()) {
                    Text("No lookup yet", style = DescriptionStyle, modifier = Modifier.padding(horizontal = SpicySpacing.S3, vertical = SpicySpacing.S1))
                }
                state.providerAttempts.forEach { attempt -> AttemptLine(attempt, state) }
            }
        }
    }
}

/** How many of the translation log's lines Advanced shows; Copy extra logs has them all. */
private const val SHOWN_TRANSLATION_LOG = 5

internal fun sessionLines(state: PlayerUiState): List<Pair<String, String>> = listOfNotNull(
    "Player" to (state.sourcePackage ?: "None"),
    "Output" to state.outputLabel,
    state.matchInfo?.let { "Match" to it },
    state.detectedSpotifyId?.let { "Spotify ID" to it },
    if (state.canSeek) null else "Seeking" to "This player doesn't allow it",
    state.lastCommandLatencyMs?.let { "Command answered" to "in $it ms" },
    state.clockDriftMs?.let { "Clock drift" to "${it.signed()} ms" },
)

/** Where the shown lyrics came from, or null while none are shown. */
internal fun lyricsLines(state: PlayerUiState): List<Pair<String, String>>? {
    val lyrics = state.lyrics as? LyricsState.Ready ?: return null
    return listOfNotNull(
        "Source" to lyrics.provider,
        lyrics.source?.let { "Origin" to it },
        lyrics.maker?.let { "Made by" to it.username },
        lyrics.uploader?.let { "Uploaded by" to it.username },
        lyrics.songwriters.takeIf { it.isNotEmpty() }?.let { "Writers" to it.joinToString() },
    )
}

internal fun attemptSourceName(attempt: ProviderAttempt, state: PlayerUiState): String =
    (state.sourceDescriptors + LOCAL_SOURCE + UPLOADED_SOURCE)
        .firstOrNull { it.id == attempt.sourceId }?.displayName ?: attempt.sourceId

internal fun ProviderAttemptOutcome.label(): String = when (this) {
    ProviderAttemptOutcome.HIT -> "Found"
    ProviderAttemptOutcome.MISS -> "Not found"
    ProviderAttemptOutcome.NEEDS_MATCH -> "Needs a Spotify link"
    ProviderAttemptOutcome.DISABLED -> "Switched off"
    ProviderAttemptOutcome.COOLING_DOWN -> "Resting after errors"
    ProviderAttemptOutcome.QUEUED, ProviderAttemptOutcome.PENDING -> "Asking…"
    ProviderAttemptOutcome.SKIPPED -> "Skipped"
    ProviderAttemptOutcome.FILTERED -> "Not counted"
    ProviderAttemptOutcome.UNAVAILABLE, ProviderAttemptOutcome.MALFORMED_HIT -> "Failed"
}

/** The attempt's quality, failure and retry time, joined; [time] formats the retry time. */
internal fun attemptDetail(attempt: ProviderAttempt, time: (java.util.Date) -> String): String = listOfNotNull(
    attempt.quality.takeIf { it != RemoteLyricsQuality.NONE }?.label(),
    attempt.failureCategory?.name?.lowercase()?.replace('_', ' '),
    attempt.message,
    attempt.retryAt?.let { "tries again at " + time(java.util.Date(it.toEpochMilli())) },
).joinToString(" · ")

/** Label and value pairs, searchable by their labels. */
@Composable
private fun InfoLines(lines: List<Pair<String, String>>) {
    lines.forEach { (label, value) ->
        Searchable(label, value, "Diagnostics") {
            Row(Modifier.fillMaxWidth().padding(horizontal = SpicySpacing.S3, vertical = SpicySpacing.S1)) {
                Text(label, style = SpicyType.Caption.copy(color = SpicyColors.TextSecondary), modifier = Modifier.width(INFO_LABEL_WIDTH))
                Text(value, style = SpicyType.Caption, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AttemptLine(attempt: ProviderAttempt, state: PlayerUiState) {
    val name = attemptSourceName(attempt, state)
    val outcome = attempt.outcome.label()
    val color = when (attempt.outcome) {
        ProviderAttemptOutcome.HIT -> SpicyColors.StatusSuccess
        ProviderAttemptOutcome.NEEDS_MATCH, ProviderAttemptOutcome.COOLING_DOWN -> SpicyColors.StatusWarning
        ProviderAttemptOutcome.QUEUED, ProviderAttemptOutcome.PENDING -> SpicyColors.StatusInfo
        ProviderAttemptOutcome.UNAVAILABLE, ProviderAttemptOutcome.MALFORMED_HIT -> SpicyColors.StatusDanger
        ProviderAttemptOutcome.MISS, ProviderAttemptOutcome.DISABLED, ProviderAttemptOutcome.SKIPPED,
        ProviderAttemptOutcome.FILTERED -> SpicyColors.TextTertiary
    }
    val context = LocalContext.current
    val locale = androidx.compose.ui.platform.LocalLocale.current.platformLocale
    val detail = attemptDetail(attempt) { date ->
        val pattern = if (android.text.format.DateFormat.is24HourFormat(context)) "HH:mm:ss" else "h:mm:ss a"
        java.text.SimpleDateFormat(pattern, locale).format(date)
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = SpicySpacing.S3, vertical = SpicySpacing.S1)) {
        Text(name, style = SpicyType.Caption.copy(color = SpicyColors.TextSecondary), modifier = Modifier.width(INFO_LABEL_WIDTH))
        Column(Modifier.weight(1f)) {
            Text(outcome, style = SpicyType.Caption.copy(color = color, fontWeight = FontWeight.Medium))
            if (detail.isNotEmpty()) Text(detail, style = DescriptionStyle)
        }
    }
}

private fun RemoteLyricsQuality.label() = when (this) {
    RemoteLyricsQuality.WORD_SYNCED -> t("word synced")
    RemoteLyricsQuality.LINE_SYNCED -> t("line synced")
    RemoteLyricsQuality.PLAIN -> t("unsynced")
    RemoteLyricsQuality.NONE -> t("nothing")
}

/** The source, and for a source passing on another's lyrics, where they came from: "Spicy Lyrics (Apple Music)". */
internal fun lyricsFrom(lyrics: LyricsState.Ready): String {
    val origin = lyrics.source?.takeIf { it.isNotBlank() && it != lyrics.provider } ?: return lyrics.provider
    return "${lyrics.provider} (${origin})"
}

/** One line on where the current lyrics stand. */
internal fun lyricsSummary(lyrics: LyricsState): String = when (lyrics) {
    is LyricsState.Ready -> lyricsFrom(lyrics) + ", " + when (lyrics.lyricsType) {
        LyricsType.Syllable -> t("word synced")
        LyricsType.Line -> t("line synced")
        LyricsType.Static -> t("unsynced")
    }
    LyricsState.Loading -> t("Looking for lyrics…")
    is LyricsState.Error -> t(lyrics.message)
    LyricsState.Idle -> t("No lyrics yet")
}

/**
 * [lyricsSummary], naming the source the lookup is still waiting on while the lyrics shown are a
 * stand-in that may yet be replaced.
 */
internal fun songSummary(state: PlayerUiState): String {
    val summary = lyricsSummary(state.lyrics) +
        if (state.songDelayMs != 0) " · ${t("%s ms delay", state.songDelayMs.signed())}" else ""
    if (state.lyrics !is LyricsState.Ready) return summary
    val waiting = state.providerAttempts.firstOrNull { it.outcome == ProviderAttemptOutcome.PENDING } ?: return summary
    val name = state.sourceDescriptors.firstOrNull { it.id == waiting.sourceId }?.displayName ?: waiting.sourceId
    return "$summary · ${t("checking %s…", name)}"
}

/**
 * A delay's slider, with a stepper under it: −100, −10, the delay (tap to reset), +10, +100.
 * Used for both the output's delay and the song's.
 */
@Composable
internal fun DelayControl(valueMs: Int, onValueChange: (Int) -> Unit, enabled: Boolean = true) {
    fun nudge(deltaMs: Int) {
        if (enabled) onValueChange((valueMs + deltaMs).coerceIn(-DELAY_RANGE_MS, DELAY_RANGE_MS))
    }
    Column(verticalArrangement = Arrangement.spacedBy(SpicySpacing.S2)) {
        SpicyBipolarSlider(
            value = valueMs,
            range = -DELAY_RANGE_MS..DELAY_RANGE_MS,
            step = DELAY_STEP_MS,
            onValueChange = onValueChange,
            enabled = enabled,
            showValue = false,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(SpicySpacing.S2), verticalAlignment = Alignment.CenterVertically) {
            val step = Modifier.weight(1f)
            SpicyButton("−$DELAY_BIG_STEP_MS", { nudge(-DELAY_BIG_STEP_MS) }, step, enabled, horizontalPadding = 0.dp)
            SpicyButton("−$DELAY_STEP_MS", { nudge(-DELAY_STEP_MS) }, step, enabled, horizontalPadding = 0.dp)
            SpicyResettableValue(
                "${valueMs.signed()} ms",
                atDefault = valueMs == 0,
                onReset = { if (enabled) onValueChange(0) },
                modifier = Modifier.weight(1.6f),
                enabled = enabled,
            )
            SpicyButton("+$DELAY_STEP_MS", { nudge(DELAY_STEP_MS) }, step, enabled, horizontalPadding = 0.dp)
            SpicyButton("+$DELAY_BIG_STEP_MS", { nudge(DELAY_BIG_STEP_MS) }, step, enabled, horizontalPadding = 0.dp)
        }
    }
}

/** This song's own delay, added to the output's. */
@Composable
internal fun SongDelayRow(state: PlayerUiState, viewModel: ExternalPlaybackViewModel) {
    SettingRow(
        label = "Song delay",
        description = if (state.localLyricsKey == null) t("Play a song to give it its own delay.")
            else t("Only for this song, added to the delay for %s. For lyrics timed a little off.", state.outputLabel),
        icon = Icons.Rounded.MusicNote,
        stacked = true,
    ) {
        DelayControl(state.songDelayMs, viewModel::setSongDelay, enabled = state.localLyricsKey != null)
    }
}

/** The player app's own delay, added to the output's. */
@Composable
internal fun PlayerDelayRow(state: PlayerUiState, viewModel: ExternalPlaybackViewModel) {
    val player = state.playerLabel
    SettingRow(
        label = "Player delay",
        description = if (player == null) t("Play something to give its app its own delay.")
            else t("For every song from %s, added to the delay for %s.", player, state.outputLabel),
        icon = Icons.Rounded.Apps,
        stacked = true,
    ) {
        DelayControl(state.playerDelayMs, viewModel::setPlayerDelay, enabled = state.sourcePackage != null)
    }
}

/** The delay slider's reach and step; the buttons nudge by one step. */
private const val DELAY_RANGE_MS = com.tx24.spicyplayer.playback.MAX_DELAY_MS
private const val DELAY_STEP_MS = 10
private const val DELAY_BIG_STEP_MS = 100

private val INFO_LABEL_WIDTH = 116.dp

/** Saving the settings to a file and bringing them back from one, through the system's file picker. */
@Composable
private fun SettingsBackupRows(viewModel: ExternalPlaybackViewModel, settings: AppSettings) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember { SettingsBackupStore(context.applicationContext) }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val written = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(store.export().toByteArray()) } != null
                }.getOrDefault(false)
            }
            if (written) UsageStats.count(UsageCounter.BACKUP_EXPORT)
            viewModel.showMessage(if (written) "Settings saved." else "Couldn't save the settings there.")
        }
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val error = withContext(Dispatchers.IO) {
                runCatching {
                    val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                        ?: throw SettingsBackup.InvalidBackup("Couldn't open that file.")
                    store.restore(text)
                }.exceptionOrNull()
            }
            if (error == null) {
                UsageStats.count(UsageCounter.BACKUP_RESTORE)
                settings.reload()
                viewModel.reloadSavedSettings()
                viewModel.showMessage("Settings restored.")
            } else {
                viewModel.showMessage((error as? SettingsBackup.InvalidBackup)?.message ?: "Couldn't read that backup.")
            }
        }
    }
    SettingRow(
        label = "Back up settings",
        description = "Save your settings, source order, delays and Spotify links to a file. Your custom font and Spicy Lyrics key stay out.",
        icon = Icons.Rounded.Save,
    ) {
        SpicyButton("Save", onClick = { export.launch("spicy-lyrics-mobile-settings-${java.time.LocalDate.now()}.json") })
    }
    SettingRow(
        label = "Restore settings",
        description = "Replace your settings with the ones in a backup file.",
        icon = Icons.Rounded.SettingsBackupRestore,
    ) {
        // Some file managers label .json as plain text or as nothing in particular.
        SpicyButton("Restore", onClick = { import.launch(arrayOf("application/json", "text/*", "application/octet-stream")) })
    }
}
