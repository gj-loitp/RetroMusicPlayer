package code.roy.retromusic.fragments.player.material

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import code.roy.retromusic.util.color.MediaNotificationProcessor
import code.roy.appthemehelper.util.ATHUtil
import code.roy.appthemehelper.util.MaterialValueHelper
import code.roy.retromusic.R
import code.roy.retromusic.databinding.FMaterialPlaybackControlsBinding
import code.roy.retromusic.extensions.applyColor
import code.roy.retromusic.extensions.getSongInfo
import code.roy.retromusic.extensions.hide
import code.roy.retromusic.extensions.ripAlpha
import code.roy.retromusic.extensions.show
import code.roy.retromusic.extensions.textColorSecondary
import code.roy.retromusic.fragments.base.AbsPlayerControlsFragment
import code.roy.retromusic.fragments.base.goToAlbum
import code.roy.retromusic.fragments.base.goToArtist
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.helper.PlayPauseButtonOnClickHandler
import code.roy.retromusic.util.PreferenceUtil
import com.google.android.material.slider.Slider

class MaterialControlsFragment :
    AbsPlayerControlsFragment(R.layout.f_material_playback_controls) {


    private var _binding: FMaterialPlaybackControlsBinding? = null
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

        _binding = FMaterialPlaybackControlsBinding.bind(view)
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
        if (ATHUtil.isWindowBackgroundDark(context = requireContext())) {
            lastPlaybackControlsColor =
                MaterialValueHelper.getPrimaryTextColor(context = requireContext(), dark = false)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getPrimaryDisabledTextColor(
                    context = requireContext(),
                    dark = false
                )
        } else {
            lastPlaybackControlsColor =
                MaterialValueHelper.getSecondaryTextColor(context = requireContext(), dark = true)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getSecondaryDisabledTextColor(
                    context = requireContext(),
                    dark = true
                )
        }
        updateRepeatState()
        updateShuffleState()

        val colorFinal = if (PreferenceUtil.isAdaptiveColor) {
            lastPlaybackControlsColor
        } else {
            textColorSecondary()
        }.ripAlpha()

        binding.text.setTextColor(colorFinal)
        binding.progressSlider.applyColor(colorFinal)

        volumeFragment?.setTintable(colorFinal)

        updateRepeatState()
        updateShuffleState()
        updatePlayPauseColor()
        updatePrevNextColor()
    }

    private fun updatePlayPauseColor() {
        binding.playPauseButton.setColorFilter(
            /* color = */ lastPlaybackControlsColor,
            /* mode = */ PorterDuff.Mode.SRC_IN
        )
    }

    private fun setUpPlayPauseFab() {
        binding.playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageDrawable(
                ContextCompat.getDrawable(
                    /* context = */ requireContext(),
                    /* id = */ R.drawable.ic_pause_outline
                )
            )
        } else if (!MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageDrawable(
                ContextCompat.getDrawable(
                    /* context = */ requireContext(),
                    /* id = */ R.drawable.ic_play_arrow_outline
                )
            )
        }
    }

    public override fun show() {}

    public override fun hide() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
