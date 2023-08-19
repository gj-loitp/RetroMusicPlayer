package code.roy.retromusic.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import code.roy.retromusic.R
import code.roy.retromusic.activities.base.AbsMusicServiceActivity
import code.roy.retromusic.databinding.ActivityDriveModeBinding
import code.roy.retromusic.db.toSongEntity
import code.roy.retromusic.extensions.accentColor
import code.roy.retromusic.extensions.drawAboveSystemBars
import code.roy.retromusic.glide.BlurTransformation
import code.roy.retromusic.glide.RetroGlideExtension
import code.roy.retromusic.glide.RetroGlideExtension.songCoverOptions
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.helper.MusicProgressViewUpdateHelper
import code.roy.retromusic.helper.PlayPauseButtonOnClickHandler
import code.roy.retromusic.model.Song
import code.roy.retromusic.repository.RealRepository
import code.roy.retromusic.service.MusicService
import code.roy.retromusic.util.MusicUtil
import com.bumptech.glide.Glide
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class DriveModeActivity : AbsMusicServiceActivity(), MusicProgressViewUpdateHelper.Callback {

    private lateinit var binding: ActivityDriveModeBinding
    private var lastPlaybackControlsColor: Int = Color.GRAY
    private var lastDisabledPlaybackControlsColor: Int = Color.GRAY
    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper
    private val repository: RealRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDriveModeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpMusicControllers()

        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
        lastPlaybackControlsColor = accentColor()
        binding.close.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.repeatButton.drawAboveSystemBars()
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseFab()
        setUpPrevNext()
        setUpRepeatButton()
        setUpShuffleButton()
        setUpProgressSlider()
        setupFavouriteToggle()
    }

    private fun setupFavouriteToggle() {
        binding.songFavourite.setOnClickListener {
            toggleFavorite(MusicPlayerRemote.currentSong)
        }
    }

    private fun toggleFavorite(song: Song) {
        lifecycleScope.launch(Dispatchers.IO) {
            val playlist = repository.favoritePlaylist()
            val songEntity = song.toSongEntity(playlist.playListId)
            val isFavorite = repository.isSongFavorite(song.id)
            if (isFavorite) {
                repository.removeSongFromPlaylist(songEntity)
            } else {
                repository.insertSongs(listOf(song.toSongEntity(playlist.playListId)))
            }
            sendBroadcast(Intent(MusicService.FAVORITE_STATE_CHANGED))
        }
    }

    private fun updateFavorite() {
        lifecycleScope.launch(Dispatchers.IO) {
            val isFavorite: Boolean = repository.isSongFavorite(MusicPlayerRemote.currentSong.id)
            withContext(Dispatchers.Main) {
                binding.songFavourite.setImageResource(if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
            }
        }
    }

    private fun setUpProgressSlider() {
        binding.progressSlider.addOnChangeListener { _: Slider, progress: Float, fromUser: Boolean ->
            if (fromUser) {
                MusicPlayerRemote.seekTo(progress.toInt())
                onUpdateProgressViews(
                    progress = MusicPlayerRemote.songProgressMillis,
                    total = MusicPlayerRemote.songDurationMillis
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper.stop()
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    private fun setUpPrevNext() {
        binding.nextButton.setOnClickListener {
            MusicPlayerRemote.playNextSong()
        }
        binding.previousButton.setOnClickListener {
            MusicPlayerRemote.back()
        }
    }

    private fun setUpShuffleButton() {
        binding.shuffleButton.setOnClickListener {
            MusicPlayerRemote.toggleShuffleMode()
        }
    }

    private fun setUpRepeatButton() {
        binding.repeatButton.setOnClickListener {
            MusicPlayerRemote.cycleRepeatMode()
        }
    }

    private fun setUpPlayPauseFab() {
        binding.playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())
    }

    override fun onRepeatModeChanged() {
        super.onRepeatModeChanged()
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        super.onShuffleModeChanged()
        updateShuffleState()
    }

    override fun onPlayStateChanged() {
        super.onPlayStateChanged()
        updatePlayPauseDrawableState()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updatePlayPauseDrawableState()
        updateSong()
        updateRepeatState()
        updateShuffleState()
        updateFavorite()
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    fun updateShuffleState() {
        when (MusicPlayerRemote.shuffleMode) {
            MusicService.SHUFFLE_MODE_SHUFFLE -> binding.shuffleButton.setColorFilter(
                lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN
            )

            else -> binding.shuffleButton.setColorFilter(
                lastDisabledPlaybackControlsColor, PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun updateRepeatState() {
        when (MusicPlayerRemote.repeatMode) {
            MusicService.REPEAT_MODE_NONE -> {
                binding.repeatButton.setImageResource(R.drawable.ic_repeat)
                binding.repeatButton.setColorFilter(
                    /* color = */ lastDisabledPlaybackControlsColor,
                    /* mode = */ PorterDuff.Mode.SRC_IN
                )
            }

            MusicService.REPEAT_MODE_ALL -> {
                binding.repeatButton.setImageResource(R.drawable.ic_repeat)
                binding.repeatButton.setColorFilter(
                    /* color = */ lastPlaybackControlsColor,
                    /* mode = */ PorterDuff.Mode.SRC_IN
                )
            }

            MusicService.REPEAT_MODE_THIS -> {
                binding.repeatButton.setImageResource(R.drawable.ic_repeat_one)
                binding.repeatButton.setColorFilter(
                    /* color = */ lastPlaybackControlsColor,
                    /* mode = */ PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
        updateFavorite()
    }

    override fun onFavoriteStateChanged() {
        super.onFavoriteStateChanged()
        updateFavorite()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong

        binding.songTitle.text = song.title
        binding.songText.text = song.artistName

        Glide.with(this).load(RetroGlideExtension.getSongModel(song)).songCoverOptions(song)
            .transform(BlurTransformation.Builder(this).build()).into(binding.image)
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        binding.progressSlider.run {
            valueTo = total.toFloat()
            value = progress.toFloat().coerceIn(valueFrom, valueTo)
        }

        binding.songTotalTime.text = MusicUtil.getReadableDurationString(total.toLong())
        binding.songCurrentProgress.text = MusicUtil.getReadableDurationString(progress.toLong())
    }
}
