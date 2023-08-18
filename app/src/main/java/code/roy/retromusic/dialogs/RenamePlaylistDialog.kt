package code.roy.retromusic.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import code.roy.retromusic.EXTRA_PLAYLIST_ID
import code.roy.retromusic.R
import code.roy.retromusic.db.PlaylistEntity
import code.roy.retromusic.extensions.accentColor
import code.roy.retromusic.extensions.colorButtons
import code.roy.retromusic.extensions.extraNotNull
import code.roy.retromusic.extensions.materialDialog
import code.roy.retromusic.fragments.LibraryViewModel
import code.roy.retromusic.fragments.ReloadType
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class RenamePlaylistDialog : DialogFragment() {

    private val libraryViewModel by activityViewModel<LibraryViewModel>()

    companion object {
        fun create(playlistEntity: PlaylistEntity): RenamePlaylistDialog {
            return RenamePlaylistDialog().apply {
                arguments = bundleOf(
                    EXTRA_PLAYLIST_ID to playlistEntity
                )
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val playlistEntity = extraNotNull<PlaylistEntity>(EXTRA_PLAYLIST_ID).value
        val layout = layoutInflater.inflate(R.layout.dlg_playlist, null)
        val inputEditText: TextInputEditText = layout.findViewById(R.id.actionNewPlaylist)
        val nameContainer: TextInputLayout = layout.findViewById(R.id.actionNewPlaylistContainer)
        nameContainer.accentColor()
        inputEditText.setText(playlistEntity.playlistName)
        return materialDialog(R.string.rename_playlist_title)
            .setView(layout)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.action_rename) { _, _ ->
                val name = inputEditText.text.toString()
                if (name.isNotEmpty()) {
                    libraryViewModel.renameRoomPlaylist(playlistEntity.playListId, name)
                    libraryViewModel.forceReload(ReloadType.Playlists)
                } else {
                    nameContainer.error = "Playlist name should'nt be empty"
                }
            }
            .create()
            .colorButtons()
    }
}
