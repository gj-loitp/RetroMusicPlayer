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
package code.roy.retromusic.activities

import android.app.KeyguardManager
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.core.view.ViewCompat.animate
import code.roy.appthemehelper.util.VersionUtils
import code.roy.retromusic.R
import code.roy.retromusic.activities.base.AbsMusicServiceActivity
import code.roy.retromusic.databinding.ActivityLockScreenBinding
import code.roy.retromusic.extensions.hideStatusBar
import code.roy.retromusic.extensions.setTaskDescriptionColorAuto
import code.roy.retromusic.extensions.whichFragment
import code.roy.retromusic.fragments.player.lockscreen.LockScreenControlsFragment
import code.roy.retromusic.glide.RetroGlideExtension
import code.roy.retromusic.glide.RetroGlideExtension.asBitmapPalette
import code.roy.retromusic.glide.RetroGlideExtension.songCoverOptions
import code.roy.retromusic.glide.RetroMusicColoredTarget
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.util.color.MediaNotificationProcessor
import com.bumptech.glide.Glide
import com.r0adkll.slidr.Slidr
import com.r0adkll.slidr.model.SlidrConfig
import com.r0adkll.slidr.model.SlidrListener
import com.r0adkll.slidr.model.SlidrPosition

class LockScreenActivity : AbsMusicServiceActivity() {
    private lateinit var binding: code.roy.retromusic.databinding.ActivityLockScreenBinding
    private var fragment: LockScreenControlsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lockScreenInit()
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideStatusBar()
        setTaskDescriptionColorAuto()

        val config = SlidrConfig.Builder().listener(object : SlidrListener {
            override fun onSlideStateChanged(state: Int) {
            }

            override fun onSlideChange(percent: Float) {
            }

            override fun onSlideOpened() {
            }

            override fun onSlideClosed(): Boolean {
                if (VersionUtils.hasOreo()) {
                    val keyguardManager =
                        getSystemService<KeyguardManager>()
                    keyguardManager?.requestDismissKeyguard(this@LockScreenActivity, null)
                }
                finish()
                return true
            }
        }).position(SlidrPosition.BOTTOM).build()

        Slidr.attach(this, config)

        fragment = whichFragment<LockScreenControlsFragment>(R.id.playback_controls_fragment)

        binding.slide.apply {
            translationY = 100f
            alpha = 0f
            animate().translationY(0f).alpha(1f).setDuration(1500).start()
        }
    }

    @Suppress("Deprecation")
    private fun lockScreenInit() {
        if (VersionUtils.hasOreoMR1()) {
            setShowWhenLocked(true)
            //setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                //          or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSongs()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updateSongs()
    }

    private fun updateSongs() {
        val song = MusicPlayerRemote.currentSong
        Glide.with(this)
            .asBitmapPalette()
            .songCoverOptions(song)
            .load(RetroGlideExtension.getSongModel(song))
            .dontAnimate()
            .into(object : RetroMusicColoredTarget(binding.image) {
                override fun onColorReady(colors: MediaNotificationProcessor) {
                    fragment?.setColor(colors)
                }
            })
    }
}
