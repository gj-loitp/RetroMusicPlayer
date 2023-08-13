package code.roy.retromusic.util

import android.content.Context
import android.content.Intent
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.net.toUri
import code.roy.appthemehelper.util.VersionUtils
import code.roy.retromusic.R
import code.roy.retromusic.extensions.showToast
import code.roy.retromusic.model.Song
import code.roy.retromusic.util.MusicUtil.getSongFileUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object RingtoneManager {
    fun setRingtone(context: Context, song: Song) {
        val uri = getSongFileUri(song.id)
        val resolver = context.contentResolver

        try {
            val cursor = resolver.query(
                /* uri = */ MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                /* projection = */ arrayOf(MediaStore.MediaColumns.TITLE),
                /* selection = */ BaseColumns._ID + "=?",
                /* selectionArgs = */ arrayOf(song.id.toString()), /* sortOrder = */ null
            )
            cursor.use { cursorSong ->
                if (cursorSong != null && cursorSong.count == 1) {
                    cursorSong.moveToFirst()
                    Settings.System.putString(resolver, Settings.System.RINGTONE, uri.toString())
                    val message = context
                        .getString(R.string.x_has_been_set_as_ringtone, cursorSong.getString(0))
                    context.showToast(message)
                }
            }
        } catch (ignored: SecurityException) {
        }
    }

    fun requiresDialog(context: Context): Boolean {
        if (VersionUtils.hasMarshmallow()) {
            if (!Settings.System.canWrite(context)) {
                return true
            }
        }
        return false
    }

    fun showDialog(context: Context) {
        return MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialogTheme)
            .setTitle(R.string.dialog_title_set_ringtone)
            .setMessage(R.string.dialog_message_set_ringtone)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = ("package:" + context.applicationContext.packageName).toUri()
                context.startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create().show()
    }
}
