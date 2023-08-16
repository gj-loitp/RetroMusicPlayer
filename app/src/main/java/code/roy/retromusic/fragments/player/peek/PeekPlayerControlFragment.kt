package code.roy.retromusic.fragments.player.peek

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import code.roy.appthemehelper.util.ATHUtil
import code.roy.appthemehelper.util.MaterialValueHelper
import code.roy.retromusic.R
import code.roy.retromusic.databinding.FPeekControlPlayerBinding
import code.roy.retromusic.extensions.accentColor
import code.roy.retromusic.extensions.applyColor
import code.roy.retromusic.fragments.base.AbsPlayerControlsFragment
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.helper.PlayPauseButtonOnClickHandler
import code.roy.retromusic.util.PreferenceUtil
import code.roy.retromusic.util.color.MediaNotificationProcessor
import com.google.android.material.slider.Slider

class PeekPlayerControlFragment : AbsPlayerControlsFragment(R.layout.f_peek_control_player) {

    private var _binding: FPeekControlPlayerBinding? = null
    private val binding get() = _binding!!

    override val progressSlider: Slider
        get() = binding.progressSlider

    override val shuffleButton: ImageButton
        get() = binding.shuffleButton

    override val repeatButton: ImageButton
        get() = binding.repeatButton

    override val nextButton: ImageButton
        get() = binding.nextButton

    override val previousButton: ImageButton
        get() = binding.previousButton

    override val songTotalTime: TextView
        get() = binding.songTotalTime

    override val songCurrentProgress: TextView
        get() = binding.songCurrentProgress

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FPeekControlPlayerBinding.bind(view)
        setUpPlayPauseFab()
    }

    override fun show() {}

    override fun hide() {}

    override fun setColor(color: MediaNotificationProcessor) {
        val controlsColor = if (PreferenceUtil.isAdaptiveColor) {
            color.primaryTextColor
        } else {
            accentColor()
        }
        binding.progressSlider.applyColor(controlsColor)
        volumeFragment?.setTintableColor(controlsColor)
        binding.playPauseButton.setColorFilter(controlsColor, PorterDuff.Mode.SRC_IN)
        binding.nextButton.setColorFilter(controlsColor, PorterDuff.Mode.SRC_IN)
        binding.previousButton.setColorFilter(controlsColor, PorterDuff.Mode.SRC_IN)

        if (!ATHUtil.isWindowBackgroundDark(context = requireContext())) {
            lastPlaybackControlsColor =
                MaterialValueHelper.getSecondaryTextColor(context = requireContext(), dark = true)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getSecondaryDisabledTextColor(
                    context = requireContext(),
                    dark = true
                )
        } else {
            lastPlaybackControlsColor =
                MaterialValueHelper.getPrimaryTextColor(context = requireContext(), dark = false)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getPrimaryDisabledTextColor(
                    context = requireContext(),
                    dark = false
                )
        }
        updateRepeatState()
        updateShuffleState()
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    private fun setUpPlayPauseFab() {
        code.roy.appthemehelper.util.TintHelper.setTintAuto(
            /* view = */ binding.playPauseButton,
            /* color = */ Color.WHITE,
            /* background = */ true
        )
        code.roy.appthemehelper.util.TintHelper.setTintAuto(
            /* view = */ binding.playPauseButton,
            /* color = */ Color.BLACK,
            /* background = */ false
        )
        binding.playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    override fun onPlayStateChanged() {
        super.onPlayStateChanged()
        updatePlayPauseDrawableState()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updatePlayPauseDrawableState()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
