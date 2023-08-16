package code.roy.retromusic.fragments.settings

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import code.roy.monkey.retromusic.extensions.goToProVersion
import code.roy.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat
import code.roy.retromusic.R
import code.roy.retromusic.extensions.dip
import code.roy.retromusic.extensions.showToast
import code.roy.retromusic.preferences.AlbumCoverStylePreference
import code.roy.retromusic.preferences.AlbumCoverStylePreferenceDialog
import code.roy.retromusic.preferences.BlacklistPreference
import code.roy.retromusic.preferences.BlacklistPreferenceDialog
import code.roy.retromusic.preferences.DurationPreference
import code.roy.retromusic.preferences.DurationPreferenceDialog
import code.roy.retromusic.preferences.LibraryPreference
import code.roy.retromusic.preferences.LibraryPreferenceDialog
import code.roy.retromusic.preferences.NowPlayingScreenPreference
import code.roy.retromusic.preferences.NowPlayingScreenPreferenceDialog
import dev.chrisbanes.insetter.applyInsetter

abstract class AbsSettingsFragment : ATEPreferenceFragmentCompat() {

    internal fun showProToastAndNavigate(message: String) {
        showToast(getString(R.string.message_pro_feature, message))
        requireContext().goToProVersion()
    }

    internal fun setSummary(
        preference: Preference,
        value: Any?,
    ) {
        val stringValue = value.toString()
        if (preference is ListPreference) {
            val index = preference.findIndexOfValue(stringValue)
            preference.setSummary(if (index >= 0) preference.entries[index] else null)
        } else {
            preference.summary = stringValue
        }
    }

    abstract fun invalidateSettings()

    protected fun setSummary(preference: Preference?) {
        preference?.let {
            setSummary(
                it, PreferenceManager
                    .getDefaultSharedPreferences(it.context)
                    .getString(it.key, "")
            )
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setDivider(ColorDrawable(Color.TRANSPARENT))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            listView.overScrollMode = View.OVER_SCROLL_NEVER
        }

        listView.updatePadding(bottom = dip(R.dimen.mini_player_height))
        listView.applyInsetter {
            type(navigationBars = true) {
                padding(vertical = true)
            }
        }
        invalidateSettings()
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        when (preference) {
            is LibraryPreference -> {
                val fragment = LibraryPreferenceDialog.newInstance()
                fragment.show(/* manager = */ childFragmentManager, /* tag = */ preference.key)
            }

            is NowPlayingScreenPreference -> {
                val fragment = NowPlayingScreenPreferenceDialog.newInstance()
                fragment.show(/* manager = */ childFragmentManager, /* tag = */ preference.key)
            }

            is AlbumCoverStylePreference -> {
                val fragment = AlbumCoverStylePreferenceDialog.newInstance()
                fragment.show(/* manager = */ childFragmentManager, /* tag = */ preference.key)
            }

            is BlacklistPreference -> {
                val fragment = BlacklistPreferenceDialog.newInstance()
                fragment.show(/* manager = */ childFragmentManager, /* tag = */ preference.key)
            }

            is DurationPreference -> {
                val fragment = DurationPreferenceDialog.newInstance()
                fragment.show(/* manager = */ childFragmentManager, /* tag = */ preference.key)
            }

            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

    fun restartActivity() {
        activity?.recreate()
    }
}
