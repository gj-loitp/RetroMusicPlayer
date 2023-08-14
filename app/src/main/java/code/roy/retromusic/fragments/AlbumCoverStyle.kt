package code.roy.retromusic.fragments

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import code.roy.retromusic.R

enum class AlbumCoverStyle(
    @StringRes val titleRes: Int,
    @DrawableRes val drawableResId: Int,
    val id: Int,
) {
    Card(titleRes = R.string.card, drawableResId = R.drawable.np_blur_card, id = 3),
    Circle(titleRes = R.string.circular, drawableResId = R.drawable.np_circle, id = 2),
    Flat(titleRes = R.string.flat, drawableResId = R.drawable.np_flat, id = 1),
    FullCard(titleRes = R.string.full_card, drawableResId = R.drawable.np_adaptive, id = 5),
    Full(titleRes = R.string.full, drawableResId = R.drawable.np_full, id = 4),
    Normal(titleRes = R.string.normal, drawableResId = R.drawable.np_normal, id = 0),
}
