package code.roy.retromusic.model

import androidx.annotation.Keep
import code.roy.retromusic.repository.LastAddedRepository
import code.roy.retromusic.repository.SongRepository
import code.roy.retromusic.repository.TopPlayedRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Keep
abstract class AbsCustomPlaylist(
    id: Long,
    name: String,
) : Playlist(id, name), KoinComponent {

    abstract fun songs(): List<Song>

    protected val songRepository by inject<SongRepository>()

    protected val topPlayedRepository by inject<TopPlayedRepository>()

    protected val lastAddedRepository by inject<LastAddedRepository>()
}
