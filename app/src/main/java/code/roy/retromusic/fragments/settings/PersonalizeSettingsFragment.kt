package code.roy.retromusic.fragments.settings

import android.os.Bundle
import android.view.View
import code.roy.appthemehelper.common.prefs.supportv7.ATEListPreference
import code.roy.appthemehelper.common.prefs.supportv7.ATESwitchPreference
import code.roy.appthemehelper.util.VersionUtils
import code.roy.retromusic.ALBUM_ART_ON_LOCK_SCREEN
import code.roy.retromusic.APPBAR_MODE
import code.roy.retromusic.BLURRED_ALBUM_ART
import code.roy.retromusic.HOME_ALBUM_GRID_STYLE
import code.roy.retromusic.HOME_ARTIST_GRID_STYLE
import code.roy.retromusic.R
import code.roy.retromusic.TAB_TEXT_MODE

class PersonalizeSettingsFragment : AbsSettingsFragment() {

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        addPreferencesFromResource(R.xml.pref_ui)
        // Hide Blur album art preference on Android 11+ devices as the lockscreen album art feature was removed by Google
        // And if the feature is present in some Custom ROM's there is also an option to set blur so this preference is unnecessary on Android 11 and above
        val blurredAlbumArt: ATESwitchPreference? = findPreference(BLURRED_ALBUM_ART)
        blurredAlbumArt?.isVisible = !VersionUtils.hasR()
    }

    override fun invalidateSettings() {}

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val albumArtOnLockscreen: ATESwitchPreference? = findPreference(ALBUM_ART_ON_LOCK_SCREEN)
        albumArtOnLockscreen?.isVisible = !VersionUtils.hasT()

        val homeArtistStyle: ATEListPreference? = findPreference(HOME_ARTIST_GRID_STYLE)
        homeArtistStyle?.setOnPreferenceChangeListener { preference, newValue ->
            setSummary(preference = preference, value = newValue)
            true
        }
        val homeAlbumStyle: ATEListPreference? = findPreference(HOME_ALBUM_GRID_STYLE)
        homeAlbumStyle?.setOnPreferenceChangeListener { preference, newValue ->
            setSummary(preference = preference, value = newValue)
            true
        }
        val tabTextMode: ATEListPreference? = findPreference(TAB_TEXT_MODE)
        tabTextMode?.setOnPreferenceChangeListener { prefs, newValue ->
            setSummary(preference = prefs, value = newValue)
            true
        }
        val appBarMode: ATEListPreference? = findPreference(APPBAR_MODE)
        appBarMode?.setOnPreferenceChangeListener { _, _ ->
            restartActivity()
            true
        }
    }
}
