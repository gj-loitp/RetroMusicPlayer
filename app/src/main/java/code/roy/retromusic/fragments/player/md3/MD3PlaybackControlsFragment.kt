package code.roy.retromusic.fragments.player.md3

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import code.roy.retromusic.util.color.MediaNotificationProcessor
import code.roy.appthemehelper.util.ATHUtil
import code.roy.appthemehelper.util.ColorUtil
import code.roy.appthemehelper.util.MaterialValueHelper
import code.roy.retromusic.R
import code.roy.retromusic.databinding.FMd3PlayerPlaybackControlsBinding
import code.roy.retromusic.extensions.accentColor
import code.roy.retromusic.extensions.animateRadius
import code.roy.retromusic.extensions.animateToCircle
import code.roy.retromusic.extensions.applyColor
import code.roy.retromusic.extensions.getSongInfo
import code.roy.retromusic.extensions.hide
import code.roy.retromusic.extensions.ripAlpha
import code.roy.retromusic.extensions.show
import code.roy.retromusic.fragments.base.AbsPlayerControlsFragment
import code.roy.retromusic.fragments.base.goToAlbum
import code.roy.retromusic.fragments.base.goToArtist
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.helper.PlayPauseButtonOnClickHandler
import code.roy.retromusic.util.PreferenceUtil
import com.google.android.material.slider.Slider

class MD3PlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.f_md3_player_playback_controls) {

    private var _binding: FMd3PlayerPlaybackControlsBinding? = null
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

        _binding = FMd3PlayerPlaybackControlsBinding.bind(view)
        setUpMusicControllers()
        binding.playPauseButton.setOnClickListener {
            if (MusicPlayerRemote.isPlaying) {
                MusicPlayerRemote.pauseSong()
            } else {
                MusicPlayerRemote.resumePlaying()
            }
        }
        binding.title.isSelected = true
        binding.text.isSelected = true
        binding.title.setOnClickListener {
            goToAlbum(requireActivity())
        }
        binding.text.setOnClickListener {
            goToArtist(requireActivity())
        }
    }

    override fun setColor(color: MediaNotificationProcessor) {
        val colorBg = ATHUtil.resolveColor(
            context = requireContext(),
            attr = android.R.attr.colorBackground
        )
        if (ColorUtil.isColorLight(colorBg)) {
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

        val colorFinal = accentColor().ripAlpha()

        code.roy.appthemehelper.util.TintHelper.setTintAuto(
            binding.playPauseButton,
            MaterialValueHelper.getPrimaryTextColor(
                context = requireContext(),
                dark = ColorUtil.isColorLight(colorFinal)
            ),
            false
        )
        binding.playPauseCard.setCardBackgroundColor(colorFinal)

        binding.progressSlider.applyColor(colorFinal)
        volumeFragment?.setTintable(colorFinal)
        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
        updatePlayPauseDrawableState()
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

    private fun setUpPlayPauseFab() {
        binding.playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
        updatePlayPauseDrawableState()
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause_outline_small)
            binding.playPauseCard.animateRadius(40F)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow_outline_small)
            binding.playPauseCard.animateToCircle()
        }
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseFab()
    }

    public override fun show() {}

    public override fun hide() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
