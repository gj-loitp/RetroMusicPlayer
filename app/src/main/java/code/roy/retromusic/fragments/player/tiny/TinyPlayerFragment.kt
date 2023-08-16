package code.roy.retromusic.fragments.player.tiny

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.*
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.Toolbar
import androidx.core.content.getSystemService
import code.roy.appthemehelper.util.ToolbarContentTintHelper
import code.roy.retromusic.util.color.MediaNotificationProcessor
import code.roy.appthemehelper.util.VersionUtils
import code.roy.retromusic.R
import code.roy.retromusic.databinding.FTinyPlayerBinding
import code.roy.retromusic.extensions.drawAboveSystemBars
import code.roy.retromusic.extensions.getSongInfo
import code.roy.retromusic.extensions.hide
import code.roy.retromusic.extensions.show
import code.roy.retromusic.extensions.whichFragment
import code.roy.retromusic.fragments.base.AbsPlayerFragment
import code.roy.retromusic.fragments.base.goToAlbum
import code.roy.retromusic.fragments.base.goToArtist
import code.roy.retromusic.fragments.player.PlayerAlbumCoverFragment
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.helper.MusicProgressViewUpdateHelper
import code.roy.retromusic.helper.PlayPauseButtonOnClickHandler
import code.roy.retromusic.model.Song
import code.roy.retromusic.util.MusicUtil
import code.roy.retromusic.util.PreferenceUtil
import code.roy.retromusic.util.ViewUtil
import kotlin.math.abs

class TinyPlayerFragment : AbsPlayerFragment(R.layout.f_tiny_player),
    MusicProgressViewUpdateHelper.Callback {
    private var _binding: FTinyPlayerBinding? = null
    private val binding get() = _binding!!

    private var lastColor: Int = 0
    private var toolbarColor: Int = 0
    private var isDragEnabled = false
    lateinit var animator: ObjectAnimator

    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    override fun onShow() {}

    override fun onHide() {}

    override fun toolbarIconColor(): Int {
        return toolbarColor
    }

    override val paletteColor: Int
        get() = lastColor

    override fun onColorChanged(color: MediaNotificationProcessor) {
        lastColor = color.backgroundColor
        libraryViewModel.updateColor(color.backgroundColor)
        toolbarColor = color.secondaryTextColor
        controlsFragment.setColor(color)

        binding.title.setTextColor(color.primaryTextColor)
        binding.playerSongTotalTime.setTextColor(color.primaryTextColor)
        binding.text.setTextColor(color.secondaryTextColor)
        binding.songInfo.setTextColor(color.secondaryTextColor)
        ViewUtil.setProgressDrawable(binding.progressBar, color.backgroundColor)

        Handler(Looper.myLooper()!!).post {
            ToolbarContentTintHelper.colorizeToolbar(
                /* toolbarView = */ binding.playerToolbar,
                /* toolbarIconsColor = */ color.secondaryTextColor,
                /* activity = */ requireActivity()
            )
        }
    }


    override fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    private lateinit var controlsFragment: TinyPlaybackControlsFragment
    private lateinit var progressViewUpdateHelper: MusicProgressViewUpdateHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(this)
    }

    override fun onResume() {
        super.onResume()
        progressViewUpdateHelper.start()
    }

    override fun onPause() {
        super.onPause()
        progressViewUpdateHelper.stop()
    }

    private fun updateSong() {
        val song = MusicPlayerRemote.currentSong
        binding.title.text = song.title
        binding.text.text = String.format("%s \nby - %s", song.albumName, song.artistName)

        if (PreferenceUtil.isSongInfo) {
            binding.songInfo.text = getSongInfo(song)
            binding.songInfo.show()
        } else {
            binding.songInfo.hide()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FTinyPlayerBinding.bind(view)
        binding.title.isSelected = true
        binding.progressBar.setOnClickListener(PlayPauseButtonOnClickHandler())
        binding.progressBar.setOnTouchListener(ProgressHelper(requireContext()))

        setUpPlayerToolbar()
        setUpSubFragments()
        binding.title.setOnClickListener {
            goToAlbum(requireActivity())
        }
        binding.text.setOnClickListener {
            goToArtist(requireActivity())
        }
        playerToolbar().drawAboveSystemBars()
    }

    private fun setUpSubFragments() {
        controlsFragment = whichFragment(R.id.playbackControlsFragment)
        val playerAlbumCoverFragment: PlayerAlbumCoverFragment =
            whichFragment(R.id.playerAlbumCoverFragment)
        playerAlbumCoverFragment.setCallbacks(this)
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.apply {
            inflateMenu(R.menu.menu_player)
            setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
            setOnMenuItemClickListener(this@TinyPlayerFragment)
        }
    }

    override fun toggleFavorite(song: Song) {
        super.toggleFavorite(song)
        if (song.id == MusicPlayerRemote.currentSong.id) {
            updateIsFavorite()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        binding.progressBar.max = total

        if (isDragEnabled) {
            binding.progressBar.progress = progress
        } else {
            animator = ObjectAnimator.ofInt(binding.progressBar, "progress", progress)

            val animatorSet = AnimatorSet()
            animatorSet.playSequentially(animator)

            animatorSet.duration = 1500
            animatorSet.interpolator = LinearInterpolator()
            animatorSet.start()
        }
        binding.playerSongTotalTime.text = String.format(
            format = "%s/%s", MusicUtil.getReadableDurationString(total.toLong()),
            MusicUtil.getReadableDurationString(progress.toLong())
        )
    }

    inner class ProgressHelper(context: Context) : View.OnTouchListener {
        private var initialY: Int = 0
        private var initialProgress = 0
        private var progress: Int = 0
        private val displayHeight = resources.displayMetrics.heightPixels
        private var gestureDetector: GestureDetector

        init {
            gestureDetector = GestureDetector(context, object :
                GestureDetector.SimpleOnGestureListener() {

                override fun onLongPress(e: MotionEvent) {
                    if (abs(e.y - initialY) <= 2) {
                        vibrate()
                        isDragEnabled = true
                        binding.progressBar.parent.requestDisallowInterceptTouchEvent(true)
                        animator.pause()
                    }
                    super.onLongPress(e)
                }

                override fun onFling(
                    e1: MotionEvent,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float,
                ): Boolean {
                    if (abs(velocityX) > abs(velocityY)) {
                        if (velocityX < 0) {
                            MusicPlayerRemote.playNextSong()
                            return true
                        } else if (velocityX > 0) {
                            MusicPlayerRemote.playPreviousSong()
                            return true
                        }
                    }
                    return false
                }
            })
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    initialProgress = MusicPlayerRemote.songProgressMillis
                    initialY = event.y.toInt()
                    progressViewUpdateHelper.stop()
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL,
                -> {
                    progressViewUpdateHelper.start()
                    if (isDragEnabled) {
                        MusicPlayerRemote.seekTo(progress)
                        isDragEnabled = false
                        return true
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isDragEnabled) {
                        val diffY = (initialY - event.y).toInt()
                        progress =
                            initialProgress + diffY * (binding.progressBar.max / displayHeight) // Multiplier
                        if (progress > 0 && progress < binding.progressBar.max) {
                            onUpdateProgressViews(
                                progress,
                                MusicPlayerRemote.songDurationMillis
                            )
                        }
                    }
                }
            }
            return gestureDetector.onTouchEvent(event)
        }

        @Suppress("Deprecation")
        private fun vibrate() {
            val v = requireContext().getSystemService<Vibrator>()
            if (VersionUtils.hasOreo()) {
                v?.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                v?.vibrate(10)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
