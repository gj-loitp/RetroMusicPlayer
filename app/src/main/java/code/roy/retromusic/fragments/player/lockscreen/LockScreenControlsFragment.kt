package code.roy.retromusic.fragments.player.lockscreen

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.TextView
import code.roy.retromusic.util.color.MediaNotificationProcessor
import code.roy.appthemehelper.util.ATHUtil
import code.roy.appthemehelper.util.ColorUtil
import code.roy.appthemehelper.util.MaterialValueHelper
import code.roy.retromusic.R
import code.roy.retromusic.databinding.FLockScreenPlaybackControlsBinding
import code.roy.retromusic.extensions.applyColor
import code.roy.retromusic.extensions.ripAlpha
import code.roy.retromusic.extensions.textColorSecondary
import code.roy.retromusic.fragments.base.AbsPlayerControlsFragment
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.helper.PlayPauseButtonOnClickHandler
import code.roy.retromusic.util.PreferenceUtil
import com.google.android.material.slider.Slider

class LockScreenControlsFragment :
    AbsPlayerControlsFragment(R.layout.f_lock_screen_playback_controls) {

    private var _binding: FLockScreenPlaybackControlsBinding? = null
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

        _binding = FLockScreenPlaybackControlsBinding.bind(view)
        setUpPlayPauseFab()
        binding.title.isSelected = true
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title
        binding.text.text = String.format(format = "%s - %s", song.artistName, song.albumName)
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
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
        val context = context ?: return

        val colorBg = ATHUtil.resolveColor(context = context, attr = android.R.attr.colorBackground)
        if (ColorUtil.isColorLight(colorBg)) {
            lastPlaybackControlsColor =
                MaterialValueHelper.getSecondaryTextColor(context = context, dark = true)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getSecondaryDisabledTextColor(context = context, dark = true)
        } else {
            lastPlaybackControlsColor =
                MaterialValueHelper.getPrimaryTextColor(context = context, dark = false)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getPrimaryDisabledTextColor(context = context, dark = false)
        }

        val colorFinal = if (PreferenceUtil.isAdaptiveColor) {
            color.primaryTextColor
        } else {
            textColorSecondary()
        }.ripAlpha()

        volumeFragment?.setTintable(colorFinal)
        binding.progressSlider.applyColor(colorFinal)

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()

        val isDark = ColorUtil.isColorLight(colorFinal)
        binding.text.setTextColor(colorFinal)

        code.roy.appthemehelper.util.TintHelper.setTintAuto(
            /* view = */ binding.playPauseButton,
            /* color = */ MaterialValueHelper.getPrimaryTextColor(context, isDark),
            /* background = */ false
        )
        code.roy.appthemehelper.util.TintHelper.setTintAuto(
            /* view = */ binding.playPauseButton,
            /* color = */ colorFinal,
            /* background = */ true
        )
    }

    private fun setUpPlayPauseFab() {
        binding.playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    public override fun show() {
        binding.playPauseButton.animate()
            .scaleX(1f)
            .scaleY(1f)
            .rotation(360f)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    public override fun hide() {
        binding.playPauseButton.apply {
            scaleX = 0f
            scaleY = 0f
            rotation = 0f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
