package code.roy.retromusic.fragments.player.full

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import code.roy.retromusic.util.color.MediaNotificationProcessor
import code.roy.appthemehelper.util.ColorUtil
import code.roy.appthemehelper.util.VersionUtils
import code.roy.retromusic.R
import code.roy.retromusic.databinding.FFullPlayerControlsBinding
import code.roy.retromusic.db.PlaylistEntity
import code.roy.retromusic.db.toSongEntity
import code.roy.retromusic.extensions.applyColor
import code.roy.retromusic.extensions.getSongInfo
import code.roy.retromusic.extensions.getTintedDrawable
import code.roy.retromusic.extensions.hide
import code.roy.retromusic.extensions.show
import code.roy.retromusic.fragments.LibraryViewModel
import code.roy.retromusic.fragments.ReloadType
import code.roy.retromusic.fragments.base.AbsPlayerControlsFragment
import code.roy.retromusic.fragments.base.goToAlbum
import code.roy.retromusic.fragments.base.goToArtist
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.helper.PlayPauseButtonOnClickHandler
import code.roy.retromusic.model.Song
import code.roy.retromusic.service.MusicService
import code.roy.retromusic.util.PreferenceUtil
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FullPlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.f_full_player_controls),
    PopupMenu.OnMenuItemClickListener {

    private val libraryViewModel: LibraryViewModel by activityViewModel()
    private var _binding: FFullPlayerControlsBinding? = null
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
        _binding = FFullPlayerControlsBinding.bind(view)

        setUpMusicControllers()
        binding.songTotalTime.setTextColor(Color.WHITE)
        binding.songCurrentProgress.setTextColor(Color.WHITE)
        binding.title.isSelected = true
        binding.title.setOnClickListener {
            goToAlbum(requireActivity())
        }
        binding.text.setOnClickListener {
            goToArtist(requireActivity())
        }
    }

    public override fun show() {
        binding.playPauseButton.animate()
            .scaleX(1f)
            .scaleY(1f)
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

    override fun setColor(color: MediaNotificationProcessor) {
        lastPlaybackControlsColor = color.primaryTextColor
        lastDisabledPlaybackControlsColor = ColorUtil.withAlpha(color.primaryTextColor, 0.3f)

        val tintList = ColorStateList.valueOf(color.primaryTextColor)
        binding.playerMenu.imageTintList = tintList
        binding.songFavourite.imageTintList = tintList
        volumeFragment?.setTintableColor(color.primaryTextColor)
        binding.progressSlider.applyColor(color.primaryTextColor)
        binding.title.setTextColor(color.primaryTextColor)
        binding.text.setTextColor(color.secondaryTextColor)
        binding.songInfo.setTextColor(color.secondaryTextColor)
        binding.songCurrentProgress.setTextColor(color.secondaryTextColor)
        binding.songTotalTime.setTextColor(color.secondaryTextColor)

        binding.playPauseButton.backgroundTintList = tintList
        binding.playPauseButton.imageTintList = ColorStateList.valueOf(color.backgroundColor)

        updateRepeatState()
        updateShuffleState()
        updatePrevNextColor()
    }

    override fun onServiceConnected() {
        updatePlayPauseDrawableState()
        updateRepeatState()
        updateShuffleState()
        updateSong()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title
        binding.text.text = song.artistName
        updateIsFavorite()
        if (PreferenceUtil.isSongInfo) {
            binding.songInfo.text = getSongInfo(song)
            binding.songInfo.show()
        } else {
            binding.songInfo.hide()
        }
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onPlayStateChanged() {
        updatePlayPauseDrawableState()
    }

    private fun updatePlayPauseDrawableState() {
        if (MusicPlayerRemote.isPlaying) {
            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
        } else {
            binding.playPauseButton.setImageResource(R.drawable.ic_play_arrow_white_32dp)
        }
    }

    private fun setUpPlayPauseFab() {
        binding.playPauseButton.setOnClickListener(PlayPauseButtonOnClickHandler())

        binding.playPauseButton.pivotX = (binding.playPauseButton.width / 2).toFloat()
        binding.playPauseButton.pivotY = (binding.playPauseButton.height / 2).toFloat()
    }

    private fun setUpMusicControllers() {
        setUpPlayPauseFab()
        setupFavourite()
        setupMenu()
    }

    private fun setupMenu() {
        binding.playerMenu.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.setOnMenuItemClickListener(this)
            popupMenu.inflate(R.menu.menu_player)
            popupMenu.menu.findItem(R.id.action_toggle_favorite).isVisible = false
            popupMenu.menu.findItem(R.id.action_toggle_lyrics).isChecked = PreferenceUtil.showLyrics
            popupMenu.show()
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return (parentFragment as FullPlayerFragment).onMenuItemClick(item!!)
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    private fun setupFavourite() {
        binding.songFavourite.setOnClickListener {
            toggleFavorite(MusicPlayerRemote.currentSong)
        }
    }

    override fun onFavoriteStateChanged() {
        updateIsFavorite(animate = true)
    }

    fun updateIsFavorite(animate: Boolean = false) {
        lifecycleScope.launch(Dispatchers.IO) {
            val isFavorite: Boolean =
                libraryViewModel.isSongFavorite(MusicPlayerRemote.currentSong.id)
            withContext(Dispatchers.Main) {
                val icon = if (animate && VersionUtils.hasMarshmallow()) {
                    if (isFavorite) R.drawable.avd_favorite else R.drawable.avd_unfavorite
                } else {
                    if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                }
                val drawable = requireContext().getTintedDrawable(
                    icon,
                    Color.WHITE
                )
                binding.songFavourite.apply {
                    setImageDrawable(drawable)
                    if (drawable is AnimatedVectorDrawable) {
                        drawable.start()
                    }
                }
            }
        }
    }

    private fun toggleFavorite(song: Song) {
        lifecycleScope.launch(Dispatchers.IO) {
            val playlist: PlaylistEntity = libraryViewModel.favoritePlaylist()
            val songEntity = song.toSongEntity(playlist.playListId)
            val isFavorite = libraryViewModel.isFavoriteSong(songEntity).isNotEmpty()
            if (isFavorite) {
                libraryViewModel.removeSongFromPlaylist(songEntity)
            } else {
                libraryViewModel.insertSongs(listOf(song.toSongEntity(playlist.playListId)))
            }
            libraryViewModel.forceReload(ReloadType.Playlists)
            requireContext().sendBroadcast(Intent(MusicService.FAVORITE_STATE_CHANGED))
        }
    }

    fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
