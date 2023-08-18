package code.roy.retromusic.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.text.parseAsHtml
import androidx.fragment.app.DialogFragment
import code.roy.retromusic.EXTRA_SONG
import code.roy.retromusic.R
import code.roy.retromusic.db.SongEntity
import code.roy.retromusic.extensions.colorButtons
import code.roy.retromusic.extensions.extraNotNull
import code.roy.retromusic.extensions.materialDialog
import code.roy.retromusic.fragments.LibraryViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class RemoveSongFromPlaylistDialog : DialogFragment() {
    private val libraryViewModel by activityViewModel<LibraryViewModel>()

    companion object {
        fun create(song: SongEntity): RemoveSongFromPlaylistDialog {
            val list = mutableListOf<SongEntity>()
            list.add(song)
            return create(list)
        }

        fun create(songs: List<SongEntity>): RemoveSongFromPlaylistDialog {
            return RemoveSongFromPlaylistDialog().apply {
                arguments = bundleOf(
                    EXTRA_SONG to songs
                )
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val songs = extraNotNull<List<SongEntity>>(EXTRA_SONG).value
        val pair = if (songs.size > 1) {
            Pair(
                first = R.string.remove_songs_from_playlist_title,
                second = String.format(getString(R.string.remove_x_songs_from_playlist), songs.size)
                    .parseAsHtml()
            )
        } else {
            Pair(
                first = R.string.remove_song_from_playlist_title,
                second = String.format(
                    getString(R.string.remove_song_x_from_playlist),
                    songs[0].title
                ).parseAsHtml()
            )
        }
        return materialDialog(pair.first)
            .setMessage(pair.second)
            .setPositiveButton(R.string.remove_action) { _, _ ->
                libraryViewModel.deleteSongsInPlaylist(songs)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .colorButtons()
    }
}
