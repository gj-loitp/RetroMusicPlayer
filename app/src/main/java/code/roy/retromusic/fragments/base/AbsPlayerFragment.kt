package code.roy.retromusic.fragments.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.viewpager.widget.ViewPager
import code.roy.appthemehelper.util.VersionUtils
import code.roy.retromusic.EXTRA_ALBUM_ID
import code.roy.retromusic.EXTRA_ARTIST_ID
import code.roy.retromusic.R
import code.roy.retromusic.activities.MainActivity
import code.roy.retromusic.activities.tageditor.AbsTagEditorActivity
import code.roy.retromusic.activities.tageditor.SongTagEditorActivity
import code.roy.retromusic.db.PlaylistEntity
import code.roy.retromusic.db.toSongEntity
import code.roy.retromusic.dialogs.AddToPlaylistDialog
import code.roy.retromusic.dialogs.CreatePlaylistDialog
import code.roy.retromusic.dialogs.DeleteSongsDialog
import code.roy.retromusic.dialogs.PlaybackSpeedDialog
import code.roy.retromusic.dialogs.SleepTimerDialog
import code.roy.retromusic.dialogs.SongDetailDialog
import code.roy.retromusic.dialogs.SongShareDialog
import code.roy.retromusic.extensions.currentFragment
import code.roy.retromusic.extensions.getTintedDrawable
import code.roy.retromusic.extensions.hide
import code.roy.retromusic.extensions.keepScreenOn
import code.roy.retromusic.extensions.showToast
import code.roy.retromusic.extensions.whichFragment
import code.roy.retromusic.fragments.LibraryViewModel
import code.roy.retromusic.fragments.NowPlayingScreen
import code.roy.retromusic.fragments.ReloadType
import code.roy.retromusic.fragments.player.PlayerAlbumCoverFragment
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.itf.IPaletteColorHolder
import code.roy.retromusic.model.Song
import code.roy.retromusic.repository.RealRepository
import code.roy.retromusic.service.MusicService
import code.roy.retromusic.util.NavigationUtil
import code.roy.retromusic.util.PreferenceUtil
import code.roy.retromusic.util.RingtoneManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.math.abs

abstract class AbsPlayerFragment(@LayoutRes layout: Int) : AbsMusicServiceFragment(layout),
    Toolbar.OnMenuItemClickListener, IPaletteColorHolder, PlayerAlbumCoverFragment.Callbacks {

    val libraryViewModel: LibraryViewModel by activityViewModel()

    val mainActivity: MainActivity
        get() = activity as MainActivity

    private var playerAlbumCoverFragment: PlayerAlbumCoverFragment? = null

    override fun onMenuItemClick(
        item: MenuItem,
    ): Boolean {
        val song = MusicPlayerRemote.currentSong
        when (item.itemId) {
            R.id.action_playback_speed -> {
                PlaybackSpeedDialog.newInstance().show(
                    /* manager = */ childFragmentManager,
                    /* tag = */ "PLAYBACK_SETTINGS"
                )
                return true
            }

            R.id.action_toggle_lyrics -> {
                PreferenceUtil.showLyrics = !PreferenceUtil.showLyrics
                showLyricsIcon(item)
                if (PreferenceUtil.lyricsScreenOn && PreferenceUtil.showLyrics) {
                    mainActivity.keepScreenOn(true)
                } else if (!PreferenceUtil.isScreenOnEnabled && !PreferenceUtil.showLyrics) {
                    mainActivity.keepScreenOn(false)
                }
                return true
            }

            R.id.action_go_to_lyrics -> {
                goToLyrics(activity = requireActivity())
                return true
            }

            R.id.action_toggle_favorite -> {
                toggleFavorite(song)
                return true
            }

            R.id.action_share -> {
                SongShareDialog.create(song).show(
                    /* manager = */ childFragmentManager,
                    /* tag = */ "SHARE_SONG"
                )
                return true
            }

            R.id.action_go_to_drive_mode -> {
                NavigationUtil.gotoDriveMode(requireActivity())
                return true
            }

            R.id.action_delete_from_device -> {
                DeleteSongsDialog.create(song).show(
                    /* manager = */ childFragmentManager,
                    /* tag = */ "DELETE_SONGS"
                )
                return true
            }

            R.id.action_add_to_playlist -> {
                lifecycleScope.launch(IO) {
                    val playlists = get<RealRepository>().fetchPlaylists()
                    withContext(Main) {
                        AddToPlaylistDialog.create(playlists, song)
                            .show(
                                /* manager = */ childFragmentManager,
                                /* tag = */ "ADD_PLAYLIST"
                            )
                    }
                }
                return true
            }

            R.id.action_clear_playing_queue -> {
                MusicPlayerRemote.clearQueue()
                return true
            }

            R.id.action_save_playing_queue -> {
                CreatePlaylistDialog.create(ArrayList(MusicPlayerRemote.playingQueue))
                    .show(
                        /* manager = */ childFragmentManager,
                        /* tag = */ "ADD_TO_PLAYLIST"
                    )
                return true
            }

            R.id.action_tag_editor -> {
                val intent = Intent(activity, SongTagEditorActivity::class.java)
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, song.id)
                startActivity(intent)
                return true
            }

            R.id.action_details -> {
                SongDetailDialog.create(song).show(
                    /* manager = */ childFragmentManager,
                    /* tag = */ "SONG_DETAIL"
                )
                return true
            }

            R.id.action_go_to_album -> {
                //Hide Bottom Bar First, else Bottom Sheet doesn't collapse fully
                mainActivity.setBottomNavVisibility(false)
                mainActivity.collapsePanel()
                requireActivity().findNavController(R.id.fragment_container).navigate(
                    resId = R.id.albumDetailsFragment,
                    args = bundleOf(EXTRA_ALBUM_ID to song.albumId)
                )
                return true
            }

            R.id.action_go_to_artist -> {
                goToArtist(requireActivity())
                return true
            }

            R.id.now_playing -> {
                requireActivity().findNavController(R.id.fragment_container).navigate(
                    resId = R.id.playing_queue_fragment,
                    args = null,
                    navOptions = navOptions { launchSingleTop = true }
                )
                mainActivity.collapsePanel()
                return true
            }

            R.id.action_show_lyrics -> {
                goToLyrics(requireActivity())
                return true
            }

            R.id.action_equalizer -> {
                NavigationUtil.openEqualizer(requireActivity())
                return true
            }

            R.id.action_sleep_timer -> {
                SleepTimerDialog().show(parentFragmentManager, "SLEEP_TIMER")
                return true
            }

            R.id.action_set_as_ringtone -> {
                requireContext().run {
                    if (RingtoneManager.requiresDialog(this)) {
                        RingtoneManager.showDialog(this)
                    } else {
                        RingtoneManager.setRingtone(context = this, song = song)
                    }
                }

                return true
            }

            R.id.action_go_to_genre -> {
                val retriever = MediaMetadataRetriever()
                val trackUri =
                    ContentUris.withAppendedId(
                        /* contentUri = */ MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        /* id = */ song.id
                    )
                retriever.setDataSource(activity, trackUri)
                var genre: String? =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                if (genre == null) {
                    genre = "Not Specified"
                }
                showToast(genre)
                return true
            }
        }
        return false
    }

    private fun showLyricsIcon(item: MenuItem) {
        val icon =
            if (PreferenceUtil.showLyrics) R.drawable.ic_lyrics else R.drawable.ic_lyrics_outline
        val drawable = requireContext().getTintedDrawable(
            icon,
            toolbarIconColor()
        )
        item.isChecked = PreferenceUtil.showLyrics
        item.icon = drawable
    }

    abstract fun playerToolbar(): Toolbar?

    abstract fun onShow()

    abstract fun onHide()

    abstract fun toolbarIconColor(): Int

    override fun onServiceConnected() {
        updateIsFavorite()
    }

    override fun onPlayingMetaChanged() {
        updateIsFavorite()
    }

    override fun onFavoriteStateChanged() {
        updateIsFavorite(animate = true)
    }

    protected open fun toggleFavorite(song: Song) {
        lifecycleScope.launch(IO) {
            val playlist: PlaylistEntity = libraryViewModel.favoritePlaylist()
            val songEntity = song.toSongEntity(playlist.playListId)
            val isFavorite = libraryViewModel.isSongFavorite(song.id)
            if (isFavorite) {
                libraryViewModel.removeSongFromPlaylist(songEntity)
            } else {
                libraryViewModel.insertSongs(listOf(song.toSongEntity(playlist.playListId)))
            }
            libraryViewModel.forceReload(ReloadType.Playlists)
            requireContext().sendBroadcast(Intent(MusicService.FAVORITE_STATE_CHANGED))
        }
    }

    fun updateIsFavorite(animate: Boolean = false) {
        lifecycleScope.launch(IO) {
            val isFavorite: Boolean =
                libraryViewModel.isSongFavorite(MusicPlayerRemote.currentSong.id)
            withContext(Main) {
                val icon = if (animate && VersionUtils.hasMarshmallow()) {
                    if (isFavorite) R.drawable.avd_favorite else R.drawable.avd_unfavorite
                } else {
                    if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
                }
                val drawable = requireContext().getTintedDrawable(
                    id = icon,
                    color = toolbarIconColor()
                )
                if (playerToolbar() != null) {
                    playerToolbar()?.menu?.findItem(R.id.action_toggle_favorite)?.apply {
                        setIcon(drawable)
                        title =
                            if (isFavorite) getString(R.string.action_remove_from_favorites)
                            else getString(R.string.action_add_to_favorites)
                        getIcon().also {
                            if (it is AnimatedVectorDrawable) {
                                it.start()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PreferenceUtil.circlePlayButton) {
            requireContext().theme.applyStyle(R.style.CircleFABOverlay, true)
        } else {
            requireContext().theme.applyStyle(R.style.RoundedFABOverlay, true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (PreferenceUtil.isFullScreenMode &&
            view.findViewById<View>(R.id.status_bar) != null
        ) {
            view.findViewById<View>(R.id.status_bar).isVisible = false
        }
        playerAlbumCoverFragment = whichFragment(R.id.playerAlbumCoverFragment)
        playerAlbumCoverFragment?.setCallbacks(this)

        if (VersionUtils.hasMarshmallow())
            view.findViewById<RelativeLayout>(R.id.statusBarShadow)?.hide()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onResume() {
        super.onResume()
        val nps = PreferenceUtil.nowPlayingScreen

        if (nps == NowPlayingScreen.Circle || nps == NowPlayingScreen.Peek || nps == NowPlayingScreen.Tiny) {
            playerToolbar()?.menu?.removeItem(R.id.action_toggle_lyrics)
        } else {
            playerToolbar()?.menu?.findItem(R.id.action_toggle_lyrics)?.apply {
                isChecked = PreferenceUtil.showLyrics
                showLyricsIcon(this)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        addSwipeDetector()
    }

    fun addSwipeDetector() {
        view?.setOnTouchListener(
            if (PreferenceUtil.swipeAnywhereToChangeSong) {
                SwipeDetector(
                    context = requireContext(),
                    viewPager = playerAlbumCoverFragment?.viewPager,
                    view = requireView()
                )
            } else null
        )
    }

    class SwipeDetector(val context: Context, val viewPager: ViewPager?, val view: View) :
        View.OnTouchListener {
        private var flingPlayBackController: GestureDetector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onScroll(
                    e1: MotionEvent,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float,
                ): Boolean {
                    return when {
                        abs(distanceX) > abs(distanceY) -> {
                            // Disallow Intercept Touch Event so that parent(BottomSheet) doesn't consume the events
                            view.parent.requestDisallowInterceptTouchEvent(true)
                            true
                        }

                        else -> {
                            false
                        }
                    }
                }
            })

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            viewPager?.dispatchTouchEvent(event)
            return flingPlayBackController.onTouchEvent(event)
        }
    }

    companion object {
        val TAG: String = AbsPlayerFragment::class.java.simpleName
        const val VISIBILITY_ANIM_DURATION: Long = 300
    }
}

fun goToArtist(activity: Activity) {
    if (activity !is MainActivity) return
    val song = MusicPlayerRemote.currentSong
    activity.apply {

        // Remove exit transition of current fragment so
        // it doesn't exit with a weird transition
        currentFragment(R.id.fragment_container)?.exitTransition = null

        //Hide Bottom Bar First, else Bottom Sheet doesn't collapse fully
        setBottomNavVisibility(false)
        if (getBottomSheetBehavior().state == BottomSheetBehavior.STATE_EXPANDED) {
            collapsePanel()
        }

        findNavController(R.id.fragment_container).navigate(
            resId = R.id.artistDetailsFragment,
            args = bundleOf(EXTRA_ARTIST_ID to song.artistId)
        )
    }
}

fun goToAlbum(activity: Activity) {
    if (activity !is MainActivity) return
    val song = MusicPlayerRemote.currentSong
    activity.apply {
        currentFragment(R.id.fragment_container)?.exitTransition = null

        //Hide Bottom Bar First, else Bottom Sheet doesn't collapse fully
        setBottomNavVisibility(false)
        if (getBottomSheetBehavior().state == BottomSheetBehavior.STATE_EXPANDED) {
            collapsePanel()
        }

        findNavController(R.id.fragment_container).navigate(
            R.id.albumDetailsFragment,
            bundleOf(EXTRA_ALBUM_ID to song.albumId)
        )
    }
}

fun goToLyrics(activity: Activity) {
    if (activity !is MainActivity) return
    activity.apply {
        //Hide Bottom Bar First, else Bottom Sheet doesn't collapse fully
        setBottomNavVisibility(false)
        if (getBottomSheetBehavior().state == BottomSheetBehavior.STATE_EXPANDED) {
            collapsePanel()
        }

        findNavController(R.id.fragment_container).navigate(
            resId = R.id.lyrics_fragment,
            args = null,
            navOptions = navOptions { launchSingleTop = true }
        )
    }
}
