package code.roy.retromusic.appshortcuts.shortcuttype

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.os.Build
import androidx.core.os.bundleOf
import code.roy.retromusic.appshortcuts.AppShortcutLauncherActivity

@TargetApi(Build.VERSION_CODES.N_MR1)
abstract class BaseShortcutType(internal var context: Context) {

    internal abstract val shortcutInfo: ShortcutInfo

    /**
     * Creates an Intent that will launch MainActivtiy and immediately play {@param songs} in either shuffle or normal mode
     *
     * @param shortcutType Describes the type of shortcut to create (ShuffleAll, TopTracks, custom playlist, etc.)
     * @return
     */
    internal fun getPlaySongsIntent(shortcutType: Long): Intent {
        val intent = Intent(context, AppShortcutLauncherActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        val b = bundleOf(AppShortcutLauncherActivity.KEY_SHORTCUT_TYPE to shortcutType)
        intent.putExtras(b)
        return intent
    }

    companion object {
        internal const val ID_PREFIX = "code.roy.retromusic.appshortcuts.id."
        val id: String
            get() = ID_PREFIX + "invalid"
    }
}
