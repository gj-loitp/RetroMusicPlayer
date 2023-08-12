package code.roy.retromusic.model.smartplaylist

import code.roy.retromusic.App
import code.roy.retromusic.R
import code.roy.retromusic.model.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class LastAddedPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.last_added),
    iconRes = R.drawable.ic_library_add
) {
    override fun songs(): List<Song> {
        return lastAddedRepository.recentSongs()
    }
}