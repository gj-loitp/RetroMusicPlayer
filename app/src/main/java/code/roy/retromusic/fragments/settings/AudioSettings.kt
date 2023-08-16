package code.roy.retromusic.fragments.settings

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.audiofx.AudioEffect
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.preference.Preference
import code.roy.appthemehelper.util.VersionUtils
import code.roy.retromusic.BLUETOOTH_PLAYBACK
import code.roy.retromusic.EQUALIZER
import code.roy.retromusic.R
import code.roy.retromusic.activities.base.AbsBaseActivity.Companion.BLUETOOTH_PERMISSION_REQUEST
import code.roy.retromusic.util.NavigationUtil

class AudioSettings : AbsSettingsFragment() {
    override fun invalidateSettings() {
        val eqPreference: Preference? = findPreference(EQUALIZER)
        if (!hasEqualizer()) {
            eqPreference?.isEnabled = false
            eqPreference?.summary = resources.getString(R.string.no_equalizer)
        } else {
            eqPreference?.isEnabled = true
        }
        eqPreference?.setOnPreferenceClickListener {
            NavigationUtil.openEqualizer(requireActivity())
            true
        }
        val bluetoothPreference: Preference? = findPreference(BLUETOOTH_PLAYBACK)
        if (VersionUtils.hasS()) {
            bluetoothPreference?.setOnPreferenceChangeListener { _, newValue ->
                if (newValue as Boolean) {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            BLUETOOTH_CONNECT
                        ) != PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            requireActivity(), arrayOf(
                                BLUETOOTH_CONNECT
                            ), BLUETOOTH_PERMISSION_REQUEST
                        )
                    }
                }
                return@setOnPreferenceChangeListener true
            }
        }
    }

    private fun hasEqualizer(): Boolean {
        val effects = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)

        val pm = requireActivity().packageManager
        val ri = pm.resolveActivity(effects, 0)
        return ri != null
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        addPreferencesFromResource(R.xml.pref_audio)
    }
}
