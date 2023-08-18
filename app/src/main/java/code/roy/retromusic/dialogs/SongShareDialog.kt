package code.roy.retromusic.dialogs

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import code.roy.retromusic.EXTRA_SONG
import code.roy.retromusic.R
import code.roy.retromusic.activities.ShareInstagramStory
import code.roy.retromusic.extensions.colorButtons
import code.roy.retromusic.extensions.materialDialog
import code.roy.retromusic.model.Song
import code.roy.retromusic.util.MusicUtil

class SongShareDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val song: Song? =
            BundleCompat.getParcelable(requireArguments(), EXTRA_SONG, Song::class.java)
        val listening: String =
            String.format(
                getString(R.string.currently_listening_to_x_by_x),
                song?.title,
                song?.artistName
            )
        return materialDialog(R.string.what_do_you_want_to_share)
            .setItems(
                /* items = */ arrayOf(
                    getString(/* resId = */ R.string.the_audio_file),
                    "\u201C" + listening + "\u201D",
                    getString(R.string.social_stories)
                )
            ) { _, which ->
                withAction(which = which, song = song, currentlyListening = listening)
            }
            .setNegativeButton(
                /* textId = */ R.string.action_cancel,
                /* listener = */ null
            )
            .create()
            .colorButtons()
    }

    private fun withAction(
        which: Int,
        song: Song?,
        currentlyListening: String,
    ) {
        when (which) {
            0 -> {
                startActivity(Intent.createChooser(
                    /* target = */ song?.let {
                        MusicUtil.createShareSongFileIntent(
                            requireContext(), it
                        )
                    }, /* title = */ null
                )
                )
            }

            1 -> {
                startActivity(
                    Intent.createChooser(
                        Intent()
                            .setAction(Intent.ACTION_SEND)
                            .putExtra(Intent.EXTRA_TEXT, currentlyListening)
                            .setType("text/plain"),
                        null
                    )
                )
            }

            2 -> {
                if (song != null) {
                    startActivity(
                        Intent(
                            requireContext(),
                            ShareInstagramStory::class.java
                        ).putExtra(
                            ShareInstagramStory.EXTRA_SONG,
                            song
                        )
                    )
                }
            }
        }
    }

    companion object {

        fun create(song: Song): SongShareDialog {
            return SongShareDialog().apply {
                arguments = bundleOf(
                    EXTRA_SONG to song
                )
            }
        }
    }
}
