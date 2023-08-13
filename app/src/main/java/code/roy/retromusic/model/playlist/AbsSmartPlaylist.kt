package code.roy.retromusic.model.playlist

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import code.roy.retromusic.R
import code.roy.retromusic.model.AbsCustomPlaylist

@Keep
abstract class AbsSmartPlaylist(
    name: String,
    @DrawableRes val iconRes: Int = R.drawable.ic_queue_music,
) : AbsCustomPlaylist(
    id = PlaylistIdGenerator(name, iconRes),
    name = name
)
