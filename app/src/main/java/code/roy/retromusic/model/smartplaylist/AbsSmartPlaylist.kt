package code.roy.retromusic.model.smartplaylist

import androidx.annotation.DrawableRes
import code.roy.retromusic.R
import code.roy.retromusic.model.AbsCustomPlaylist

abstract class AbsSmartPlaylist(
    name: String,
    @DrawableRes val iconRes: Int = R.drawable.ic_queue_music
) : AbsCustomPlaylist(
    id = PlaylistIdGenerator(name, iconRes),
    name = name
)