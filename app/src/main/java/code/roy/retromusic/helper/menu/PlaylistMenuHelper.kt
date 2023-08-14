package code.roy.retromusic.helper.menu

import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import code.roy.retromusic.R
import code.roy.retromusic.db.PlaylistWithSongs
import code.roy.retromusic.db.toSongs
import code.roy.retromusic.dialogs.AddToPlaylistDialog
import code.roy.retromusic.dialogs.DeletePlaylistDialog
import code.roy.retromusic.dialogs.RenamePlaylistDialog
import code.roy.retromusic.dialogs.SavePlaylistDialog
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.repository.RealRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object PlaylistMenuHelper : KoinComponent {

    fun handleMenuClick(
        activity: FragmentActivity,
        playlistWithSongs: PlaylistWithSongs,
        item: MenuItem,
    ): Boolean {
        when (item.itemId) {
            R.id.action_play -> {
                MusicPlayerRemote.openQueue(playlistWithSongs.songs.toSongs(), 0, true)
                return true
            }

            R.id.action_play_next -> {
                MusicPlayerRemote.playNext(playlistWithSongs.songs.toSongs())
                return true
            }

            R.id.action_add_to_playlist -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val playlists = get<RealRepository>().fetchPlaylists()
                    withContext(Dispatchers.Main) {
                        AddToPlaylistDialog.create(playlists, playlistWithSongs.songs.toSongs())
                            .show(activity.supportFragmentManager, "ADD_PLAYLIST")
                    }
                }
                return true
            }

            R.id.action_add_to_current_playing -> {
                MusicPlayerRemote.enqueue(playlistWithSongs.songs.toSongs())
                return true
            }

            R.id.action_rename_playlist -> {
                RenamePlaylistDialog.create(playlistWithSongs.playlistEntity)
                    .show(activity.supportFragmentManager, "RENAME_PLAYLIST")
                return true
            }

            R.id.action_delete_playlist -> {
                DeletePlaylistDialog.create(playlistWithSongs.playlistEntity)
                    .show(activity.supportFragmentManager, "DELETE_PLAYLIST")
                return true
            }

            R.id.action_save_playlist -> {
                SavePlaylistDialog.create(playlistWithSongs)
                    .show(activity.supportFragmentManager, "SavePlaylist")
                return true
            }
        }
        return false
    }
}
