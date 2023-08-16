package code.roy.retromusic.fragments.other

import android.graphics.Color
import android.graphics.PorterDuff
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import code.roy.appthemehelper.ThemeStore
import code.roy.retromusic.R
import code.roy.retromusic.databinding.FVolumeBinding
import code.roy.retromusic.extensions.applyColor
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.util.PreferenceUtil
import code.roy.retromusic.volume.AudioVolumeObserver
import code.roy.retromusic.volume.OnAudioVolumeChangedListener
import com.google.android.material.slider.Slider

class VolumeFragment : Fragment(), Slider.OnChangeListener, OnAudioVolumeChangedListener,
    View.OnClickListener {

    private var _binding: FVolumeBinding? = null
    private val binding get() = _binding!!

    private var audioVolumeObserver: AudioVolumeObserver? = null

    private val audioManager: AudioManager
        get() = requireContext().getSystemService()!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FVolumeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTintable(ThemeStore.accentColor(requireContext()))
        binding.volumeDown.setOnClickListener(this)
        binding.volumeUp.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (audioVolumeObserver == null) {
            audioVolumeObserver = AudioVolumeObserver(requireActivity())
        }
        audioVolumeObserver?.register(audioStreamType = AudioManager.STREAM_MUSIC, listener = this)

        val audioManager = audioManager
        binding.volumeSeekBar.valueTo =
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        binding.volumeSeekBar.value =
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        binding.volumeSeekBar.addOnChangeListener(this)
    }

    override fun onAudioVolumeChanged(currentVolume: Int, maxVolume: Int) {
        if (_binding != null) {
            binding.volumeSeekBar.valueTo = maxVolume.toFloat()
            binding.volumeSeekBar.value = currentVolume.toFloat()
            binding.volumeDown.setImageResource(
                if (currentVolume == 0) R.drawable.ic_volume_off
                else R.drawable.ic_volume_down
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioVolumeObserver?.unregister()
        _binding = null
    }

    override fun onValueChange(
        slider: Slider,
        value: Float,
        fromUser: Boolean,
    ) {
        val audioManager = audioManager
        audioManager.setStreamVolume(
            /* streamType = */ AudioManager.STREAM_MUSIC,
            /* index = */ value.toInt(),
            /* flags = */ 0
        )
        setPauseWhenZeroVolume(value < 1f)
        binding.volumeDown.setImageResource(
            if (value == 0f) R.drawable.ic_volume_off
            else R.drawable.ic_volume_down
        )
    }

    override fun onClick(view: View) {
        val audioManager = audioManager
        when (view.id) {
            R.id.volumeDown -> audioManager.adjustStreamVolume(
                /* streamType = */ AudioManager.STREAM_MUSIC,
                /* direction = */ AudioManager.ADJUST_LOWER,
                /* flags = */ 0
            )

            R.id.volumeUp -> audioManager.adjustStreamVolume(
                /* streamType = */ AudioManager.STREAM_MUSIC,
                /* direction = */ AudioManager.ADJUST_RAISE,
                /* flags = */ 0
            )
        }
    }

    fun tintWhiteColor() {
        val color = Color.WHITE
        binding.volumeDown.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        binding.volumeUp.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        binding.volumeSeekBar.applyColor(color)
    }

    fun setTintable(color: Int) {
        binding.volumeSeekBar.applyColor(color)
    }

    private fun setPauseWhenZeroVolume(pauseWhenZeroVolume: Boolean) {
        if (PreferenceUtil.isPauseOnZeroVolume)
            if (MusicPlayerRemote.isPlaying && pauseWhenZeroVolume)
                MusicPlayerRemote.pauseSong()
    }

    fun setTintableColor(color: Int) {
        binding.volumeDown.setColorFilter(/* color = */ color, /* mode = */ PorterDuff.Mode.SRC_IN)
        binding.volumeUp.setColorFilter(/* color = */ color, /* mode = */ PorterDuff.Mode.SRC_IN)
        // TintHelper.setTint(volumeSeekBar, color, false)
        binding.volumeSeekBar.applyColor(color)
    }

    companion object {
        fun newInstance(): VolumeFragment {
            return VolumeFragment()
        }
    }
}
