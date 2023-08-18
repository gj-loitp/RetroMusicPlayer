package code.roy.retromusic.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Spanned
import android.util.Log
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.text.parseAsHtml
import androidx.fragment.app.DialogFragment
import code.roy.retromusic.EXTRA_SONG
import code.roy.retromusic.R
import code.roy.retromusic.databinding.DlgFileDetailsBinding
import code.roy.retromusic.extensions.colorButtons
import code.roy.retromusic.extensions.materialDialog
import code.roy.retromusic.model.Song
import code.roy.retromusic.util.MusicUtil
import org.jaudiotagger.audio.AudioFileIO
import java.io.File

class SongDetailDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context: Context = requireContext()
        val binding = DlgFileDetailsBinding.inflate(layoutInflater)

        val song = BundleCompat.getParcelable(
            /* in = */ requireArguments(),
            /* key = */ EXTRA_SONG,
            /* clazz = */ Song::class.java
        )
        with(binding) {
            fileName.text = makeTextWithTitle(
                context = context,
                titleResId = R.string.label_file_name,
                text = "-"
            )
            filePath.text = makeTextWithTitle(
                context = context,
                titleResId = R.string.label_file_path,
                text = "-"
            )
            fileSize.text = makeTextWithTitle(
                context = context,
                titleResId = R.string.label_file_size,
                text = "-"
            )
            fileFormat.text = makeTextWithTitle(
                context = context,
                titleResId = R.string.label_file_format,
                text = "-"
            )
            trackLength.text = makeTextWithTitle(
                context = context,
                titleResId = R.string.label_track_length,
                text = "-"
            )
            bitrate.text = makeTextWithTitle(
                context = context,
                titleResId = R.string.label_bit_rate,
                text = "-"
            )
            samplingRate.text = makeTextWithTitle(
                context = context,
                titleResId = R.string.label_sampling_rate,
                text = "-"
            )
        }

        if (song != null) {
            val songFile = File(song.data)
            if (songFile.exists()) {
                binding.fileName.text =
                    makeTextWithTitle(
                        context = context,
                        titleResId = R.string.label_file_name,
                        text = songFile.name
                    )
                binding.filePath.text =
                    makeTextWithTitle(
                        context = context,
                        titleResId = R.string.label_file_path,
                        text = songFile.absolutePath
                    )

                binding.dateModified.text = makeTextWithTitle(
                    context = context,
                    titleResId = R.string.label_last_modified,
                    text = MusicUtil.getDateModifiedString(songFile.lastModified())
                )

                binding.fileSize.text = makeTextWithTitle(
                    context = context,
                    titleResId = R.string.label_file_size,
                    text = getFileSizeString(songFile.length())
                )
                try {
                    val audioFile = AudioFileIO.read(songFile)
                    val audioHeader = audioFile.audioHeader

                    binding.fileFormat.text =
                        makeTextWithTitle(
                            context = context,
                            titleResId = R.string.label_file_format,
                            text = audioHeader.format
                        )
                    binding.trackLength.text = makeTextWithTitle(
                        context = context,
                        titleResId = R.string.label_track_length,
                        text = MusicUtil.getReadableDurationString((audioHeader.trackLength * 1000).toLong())
                    )
                    binding.bitrate.text = makeTextWithTitle(
                        context = context,
                        titleResId = R.string.label_bit_rate,
                        text = audioHeader.bitRate + " kb/s"
                    )
                    binding.samplingRate.text = makeTextWithTitle(
                        context = context,
                        titleResId = R.string.label_sampling_rate,
                        text = audioHeader.sampleRate + " Hz"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "error while reading the song file", e)
                    // fallback
                    binding.trackLength.text = makeTextWithTitle(
                        context = context,
                        titleResId = R.string.label_track_length,
                        text = MusicUtil.getReadableDurationString(song.duration)
                    )
                }
            } else {
                // fallback
                binding.fileName.text =
                    makeTextWithTitle(
                        context = context,
                        titleResId = R.string.label_file_name,
                        text = song.title
                    )
                binding.trackLength.text = makeTextWithTitle(
                    context = context,
                    titleResId = R.string.label_track_length,
                    text = MusicUtil.getReadableDurationString(song.duration)
                )
            }
        }
        return materialDialog(R.string.action_details).setPositiveButton(android.R.string.ok, null)
            .setView(binding.root).create().colorButtons()
    }

    companion object {

        val TAG: String = SongDetailDialog::class.java.simpleName

        fun create(song: Song): SongDetailDialog {
            return SongDetailDialog().apply {
                arguments = bundleOf(
                    EXTRA_SONG to song
                )
            }
        }

        private fun makeTextWithTitle(
            context: Context,
            titleResId: Int,
            text: String?,
        ): Spanned {
            return ("<b>" + context.resources.getString(titleResId) + ": " + "</b>" + text).parseAsHtml()
        }

        private fun getFileSizeString(sizeInBytes: Long): String {
            val fileSizeInKB = sizeInBytes / 1024
            val fileSizeInMB = fileSizeInKB / 1024
            return "$fileSizeInMB MB"
        }
    }
}
