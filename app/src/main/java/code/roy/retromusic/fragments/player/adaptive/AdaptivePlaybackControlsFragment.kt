package code.roy.retromusic.fragments.player.adaptive

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import code.roy.appthemehelper.util.ATHUtil
import code.roy.appthemehelper.util.ColorUtil
import code.roy.appthemehelper.util.MaterialValueHelper
import code.roy.retromusic.R
import code.roy.retromusic.databinding.FAdaptivePlayerPlaybackControlsBinding
import code.roy.retromusic.extensions.accentColor
import code.roy.retromusic.extensions.applyColor
import code.roy.retromusic.extensions.getSongInfo
import code.roy.retromusic.extensions.hide
import code.roy.retromusic.extensions.ripAlpha
import code.roy.retromusic.extensions.show
import code.roy.retromusic.fragments.base.AbsPlayerControlsFragment
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.util.PreferenceUtil
import code.roy.retromusic.util.color.MediaNotificationProcessor
import com.google.android.material.slider.Slider

class AdaptivePlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.f_adaptive_player_playback_controls) {

    private var _binding: FAdaptivePlayerPlaybackControlsBinding? = null
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FAdaptivePlayerPlaybackControlsBinding.bind(view)

        setUpPlayPauseFab()
    }

    private fun updateSong() {
        if (PreferenceUtil.isSongInfo) {
            binding.songInfo.text = getSongInfo(MusicPlayerRemote.currentSong)
            binding.songInfo.show()
        } else {
            binding.songInfo.hide()
        }
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    override fun setColor(color: MediaNotificationProcessor) {
        if (ColorUtil.isColorLight(
                ATHUtil.resolveColor(
                    context = requireContext(),
                    attr = android.R.attr.windowBackground
                )
            )
        ) {
            lastPlaybackControlsColor = MaterialValueHelper.getSecondaryTextColor(
                context = activity,
                dark = true
            )
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getSecondaryDisabledTextColor(context = activity, dark = true)
        } else {
            lastPlaybackControlsColor = MaterialValueHelper.getPrimaryTextColor(
                context = activity,
                dark = false
            )
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getPrimaryDisabledTextColor(context = activity, dark = false)
        }

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
        updatePlayPauseColor()

        val colorFinal = if (PreferenceUtil.isAdaptiveColor) {
            color.primaryTextColor
        } else {
            accentColor()
        }.ripAlpha()

        code.roy.appthemehelper.util.TintHelper.setTintAuto(
            /* view = */ binding.playPauseButton,
            /* color = */ MaterialValueHelper.getPrimaryTextColor(
                context = context,
                dark = ColorUtil.isColorLight(colorFinal)
            ),
            /* background = */ false
        )
        code.roy.appthemehelper.util.TintHelper.setTintAuto(
            /* view = */ binding.playPauseButton,
            /* color = */ colorFinal,
            /* background = */ true
        )
        binding.progressSlider.applyColor(colorFinal)
        volumeFragment?.setTintable(colorFinal)
    }

    private fun updatePlayPauseColor() {
        // playPauseButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
    }

    private fun setUpPlayPauseFab() {
        binding.playPauseButton.setOnClickListener {
            if (MusicPlayerRemote.isPlaying) {
                MusicPlayerRemote.pauseSong()
            } else {
                MusicPlayerRemote.resumePlaying()
            }
            it.showBounceAnimation()
        }
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    override fun show() {}

    override fun hide() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
