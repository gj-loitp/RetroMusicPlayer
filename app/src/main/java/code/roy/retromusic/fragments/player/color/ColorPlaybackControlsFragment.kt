package code.roy.retromusic.fragments.player.color

import android.animation.Animator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.TextView
import code.roy.retromusic.util.color.MediaNotificationProcessor
import code.roy.appthemehelper.util.ColorUtil
import code.roy.retromusic.R
import code.roy.retromusic.databinding.FColorPlayerPlaybackControlsBinding
import code.roy.retromusic.extensions.applyColor
import code.roy.retromusic.extensions.getSongInfo
import code.roy.retromusic.extensions.hide
import code.roy.retromusic.extensions.show
import code.roy.retromusic.fragments.base.AbsPlayerControlsFragment
import code.roy.retromusic.fragments.base.goToAlbum
import code.roy.retromusic.fragments.base.goToArtist
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.util.PreferenceUtil
import com.google.android.material.slider.Slider
import kotlin.math.sqrt

class ColorPlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.f_color_player_playback_controls) {

    private var _binding: FColorPlayerPlaybackControlsBinding? = null
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
        _binding = FColorPlayerPlaybackControlsBinding.bind(view)

        setUpPlayPauseFab()
        binding.title.isSelected = true
        binding.text.isSelected = true
        binding.title.setOnClickListener {
            goToAlbum(requireActivity())
        }
        binding.text.setOnClickListener {
            goToArtist(requireActivity())
        }
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title
        binding.text.text = song.artistName

        if (PreferenceUtil.isSongInfo) {
            binding.songInfo.text = getSongInfo(song)
            binding.songInfo.show()
        } else {
            binding.songInfo.hide()
        }
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
        code.roy.appthemehelper.util.TintHelper.setTintAuto(
            /* view = */ binding.playPauseButton,
            /* color = */ color.primaryTextColor,
            /* background = */ true
        )
        code.roy.appthemehelper.util.TintHelper.setTintAuto(
            /* view = */ binding.playPauseButton,
            /* color = */ color.backgroundColor,
            /* background = */ false
        )
        binding.progressSlider.applyColor(color.primaryTextColor)

        binding.title.setTextColor(color.primaryTextColor)
        binding.text.setTextColor(color.secondaryTextColor)
        binding.songInfo.setTextColor(color.secondaryTextColor)
        binding.songCurrentProgress.setTextColor(color.secondaryTextColor)
        binding.songTotalTime.setTextColor(color.secondaryTextColor)
        volumeFragment?.setTintableColor(color.primaryTextColor)

        lastPlaybackControlsColor = color.secondaryTextColor
        lastDisabledPlaybackControlsColor = ColorUtil.withAlpha(
            baseColor = color.secondaryTextColor,
            alpha = 0.25f
        )

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
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
        binding.playPauseButton.setImageResource(
            when {
                MusicPlayerRemote.isPlaying -> R.drawable.ic_pause
                else -> R.drawable.ic_play_arrow
            }
        )
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

    fun createRevealAnimator(view: View): Animator {
        val location = IntArray(2)
        binding.playPauseButton.getLocationOnScreen(location)
        val x = (location[0] + binding.playPauseButton.measuredWidth / 2)
        val y = (location[1] + binding.playPauseButton.measuredHeight / 2)
        val endRadius = sqrt((x * x + y * y).toFloat())
        val startRadius =
            binding.playPauseButton.measuredWidth.coerceAtMost(binding.playPauseButton.measuredHeight)
        return ViewAnimationUtils.createCircularReveal(
            /* view = */ view,
            /* centerX = */ x,
            /* centerY = */ y,
            /* startRadius = */ startRadius.toFloat(),
            /* endRadius = */ endRadius
        ).apply {
            duration = 300
            interpolator = AccelerateInterpolator()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
