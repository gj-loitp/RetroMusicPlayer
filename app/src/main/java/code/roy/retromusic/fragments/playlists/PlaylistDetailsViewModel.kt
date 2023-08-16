package code.roy.retromusic.fragments.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import code.roy.retromusic.db.PlaylistWithSongs
import code.roy.retromusic.db.SongEntity
import code.roy.retromusic.repository.RealRepository

class PlaylistDetailsViewModel(
    private val realRepository: RealRepository,
    private var playlistId: Long,
) : ViewModel() {
    fun getSongs(): LiveData<List<SongEntity>> =
        realRepository.playlistSongs(playlistId)

    fun playlistExists(): LiveData<Boolean> =
        realRepository.checkPlaylistExists(playlistId)

    fun getPlaylist(): LiveData<PlaylistWithSongs> = realRepository.getPlaylist(playlistId)
}
