/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
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

/**
 * @author Hemanth S (h4h13).
 */
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
        if (ATHUtil.isWindowBackgroundDark(requireContext())) {
            lastPlaybackControlsColor =
                MaterialValueHelper.getPrimaryTextColor(requireContext(), false)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getPrimaryDisabledTextColor(requireContext(), false)
        } else {
            lastPlaybackControlsColor =
                MaterialValueHelper.getSecondaryTextColor(requireContext(), true)
            lastDisabledPlaybackControlsColor =
                MaterialValueHelper.getSecondaryDisabledTextColor(requireContext(), true)
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
        binding.playPauseButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN)
    }

    private fun setUpPlayPauseFab() {
        binding.playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_pause_outline
                )
            )
        } else if (!MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_play_arrow_outline
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
