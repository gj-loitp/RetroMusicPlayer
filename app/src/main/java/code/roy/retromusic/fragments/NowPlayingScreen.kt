package code.roy.retromusic.fragments

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import code.roy.retromusic.R

enum class NowPlayingScreen constructor(
    @param:StringRes @field:StringRes
    val titleRes: Int,
    @param:DrawableRes @field:DrawableRes val drawableResId: Int,
    val id: Int,
    val defaultCoverTheme: AlbumCoverStyle?,
) {
    // Some Now playing themes look better with particular Album cover theme

    Adaptive(
        titleRes = R.string.adaptive,
        drawableResId = R.drawable.np_adaptive,
        id = 10,
        defaultCoverTheme = AlbumCoverStyle.FullCard
    ),
    Blur(
        titleRes = R.string.blur,
        drawableResId = R.drawable.np_blur,
        id = 4,
        defaultCoverTheme = AlbumCoverStyle.Normal
    ),
    BlurCard(
        titleRes = R.string.blur_card,
        drawableResId = R.drawable.np_blur_card,
        id = 9,
        defaultCoverTheme = AlbumCoverStyle.Card
    ),
    Card(
        titleRes = R.string.card,
        drawableResId = R.drawable.np_card,
        id = 6,
        defaultCoverTheme = AlbumCoverStyle.Full
    ),
    Circle(
        titleRes = R.string.circle,
        drawableResId = R.drawable.np_minimalistic_circle,
        id = 15,
        defaultCoverTheme = null
    ),
    Classic(
        titleRes = R.string.classic,
        drawableResId = R.drawable.np_classic,
        id = 16,
        defaultCoverTheme = AlbumCoverStyle.Full
    ),
    Color(
        titleRes = R.string.color,
        drawableResId = R.drawable.np_color,
        id = 5,
        defaultCoverTheme = AlbumCoverStyle.Normal
    ),
    Fit(
        titleRes = R.string.fit,
        drawableResId = R.drawable.np_fit,
        id = 12,
        defaultCoverTheme = AlbumCoverStyle.Full
    ),
    Flat(
        titleRes = R.string.flat,
        drawableResId = R.drawable.np_flat,
        id = 1,
        defaultCoverTheme = AlbumCoverStyle.Flat
    ),
    Full(
        titleRes = R.string.full,
        drawableResId = R.drawable.np_full,
        id = 2,
        defaultCoverTheme = AlbumCoverStyle.Full
    ),
    Gradient(
        titleRes = R.string.gradient,
        drawableResId = R.drawable.np_gradient,
        id = 17,
        defaultCoverTheme = AlbumCoverStyle.Full
    ),
    Material(
        titleRes = R.string.material,
        drawableResId = R.drawable.np_material,
        id = 11,
        defaultCoverTheme = AlbumCoverStyle.Normal
    ),
    MD3(
        titleRes = R.string.md3,
        drawableResId = R.drawable.np_normal,
        id = 18,
        defaultCoverTheme = AlbumCoverStyle.Normal
    ),
    Normal(
        titleRes = R.string.normal,
        drawableResId = R.drawable.np_normal,
        id = 0,
        defaultCoverTheme = AlbumCoverStyle.Normal
    ),
    Peek(
        titleRes = R.string.peek,
        drawableResId = R.drawable.np_peek,
        id = 14,
        defaultCoverTheme = AlbumCoverStyle.Normal
    ),
    Plain(
        titleRes = R.string.plain,
        drawableResId = R.drawable.np_plain,
        id = 3,
        defaultCoverTheme = AlbumCoverStyle.Normal
    ),
    Simple(
        titleRes = R.string.simple,
        drawableResId = R.drawable.np_simple,
        id = 8,
        defaultCoverTheme = AlbumCoverStyle.Normal
    ),
    Tiny(
        titleRes = R.string.tiny,
        drawableResId = R.drawable.np_tiny,
        id = 7,
        defaultCoverTheme = null
    ),
}
