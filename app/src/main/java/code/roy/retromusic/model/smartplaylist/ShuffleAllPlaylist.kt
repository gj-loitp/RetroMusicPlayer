package code.roy.retromusic.model.smartplaylist

import androidx.annotation.Keep
import code.roy.retromusic.App
import code.roy.retromusic.R
import code.roy.retromusic.model.Song
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
class ShuffleAllPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.action_shuffle_all),
    iconRes = R.drawable.ic_shuffle
) {
    override fun songs(): List<Song> {
        return songRepository.songs()
    }
}
