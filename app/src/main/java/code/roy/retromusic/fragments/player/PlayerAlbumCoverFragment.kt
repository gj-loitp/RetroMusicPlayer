package code.roy.retromusic.fragments.player

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import code.roy.appthemehelper.util.ColorUtil
import code.roy.appthemehelper.util.MaterialValueHelper
import code.roy.retromusic.LYRICS_TYPE
import code.roy.retromusic.R
import code.roy.retromusic.SHOW_LYRICS
import code.roy.retromusic.adapter.album.AlbumCoverPagerAdapter
import code.roy.retromusic.databinding.FPlayerAlbumCoverBinding
import code.roy.retromusic.extensions.isColorLight
import code.roy.retromusic.extensions.surfaceColor
import code.roy.retromusic.fragments.NowPlayingScreen
import code.roy.retromusic.fragments.base.AbsMusicServiceFragment
import code.roy.retromusic.fragments.base.goToLyrics
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.helper.MusicProgressViewUpdateHelper
import code.roy.retromusic.lyrics.CoverLrcView
import code.roy.retromusic.model.lyrics.Lyrics
import code.roy.retromusic.transform.CarousalPagerTransformer
import code.roy.retromusic.transform.ParallaxPagerTransformer
import code.roy.retromusic.util.CoverLyricsType
import code.roy.retromusic.util.LyricUtil
import code.roy.retromusic.util.PreferenceUtil
import code.roy.retromusic.util.color.MediaNotificationProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerAlbumCoverFragment : AbsMusicServiceFragment(R.layout.f_player_album_cover),
    ViewPager.OnPageChangeListener, MusicProgressViewUpdateHelper.Callback,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private var _binding: FPlayerAlbumCoverBinding? = null
    private val binding get() = _binding!!
    private var callbacks: Callbacks? = null
    private var currentPosition: Int = 0
    val viewPager get() = binding.viewPager

    private val colorReceiver = object : AlbumCoverPagerAdapter.AlbumCoverFragment.ColorReceiver {
        override fun onColorReady(color: MediaNotificationProcessor, request: Int) {
            if (currentPosition == request) {
                notifyColorChange(color)
            }
        }
    }
    private var progressViewUpdateHelper: MusicProgressViewUpdateHelper? = null

    private val lrcView: CoverLrcView get() = binding.lyricsView

    var lyrics: Lyrics? = null

    fun removeSlideEffect() {
        val transformer = ParallaxPagerTransformer(R.id.player_image)
        transformer.setSpeed(0.3f)
        lifecycleScope.launchWhenStarted {
            viewPager.setPageTransformer(
                /* reverseDrawingOrder = */ false,
                /* transformer = */ transformer
            )
        }
    }

    private fun updateLyrics() {
        val song = MusicPlayerRemote.currentSong
        lifecycleScope.launch(Dispatchers.IO) {
            val lrcFile = LyricUtil.getSyncedLyricsFile(song)
            if (lrcFile != null) {
                binding.lyricsView.loadLrc(lrcFile)
            } else {
                val embeddedLyrics = LyricUtil.getEmbeddedSyncedLyrics(song.data)
                if (embeddedLyrics != null) {
                    binding.lyricsView.loadLrc(embeddedLyrics)
                } else {
                    withContext(Dispatchers.Main) {
                        binding.lyricsView.reset()
                        binding.lyricsView.setLabel(context?.getString(R.string.no_lyrics_found))
                    }
                }
            }
        }

    }

    override fun onUpdateProgressViews(progress: Int, total: Int) {
        binding.lyricsView.updateTime(progress.toLong())
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FPlayerAlbumCoverBinding.bind(view)
        setupViewPager()
        progressViewUpdateHelper = MusicProgressViewUpdateHelper(
            callback = this,
            intervalPlaying = 500,
            intervalPaused = 1000
        )
        maybeInitLyrics()
        lrcView.apply {
            setDraggable(true) { time ->
                MusicPlayerRemote.seekTo(time.toInt())
                MusicPlayerRemote.resumePlaying()
                true
            }
            setOnClickListener {
                goToLyrics(requireActivity())
            }
        }
    }

    private fun setupViewPager() {
        binding.viewPager.addOnPageChangeListener(this)
        val nps = PreferenceUtil.nowPlayingScreen

        if (nps == NowPlayingScreen.Full || nps == NowPlayingScreen.Classic || nps == NowPlayingScreen.Fit || nps == NowPlayingScreen.Gradient) {
            binding.viewPager.offscreenPageLimit = 2
        } else if (PreferenceUtil.isCarouselEffect) {
            val metrics = resources.displayMetrics
            val ratio = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
            binding.viewPager.clipToPadding = false
            val padding =
                if (ratio >= 1.777f) {
                    40
                } else {
                    100
                }
            binding.viewPager.setPadding(padding, 0, padding, 0)
            binding.viewPager.pageMargin = 0
            binding.viewPager.setPageTransformer(false, CarousalPagerTransformer(requireContext()))
        } else {
            binding.viewPager.offscreenPageLimit = 2
            binding.viewPager.setPageTransformer(
                /* reverseDrawingOrder = */ true,
                /* transformer = */ PreferenceUtil.albumCoverTransform
            )
        }
    }

    override fun onResume() {
        super.onResume()
        maybeInitLyrics()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
        binding.viewPager.removeOnPageChangeListener(this)
        progressViewUpdateHelper?.stop()
        _binding = null
    }

    override fun onServiceConnected() {
        updatePlayingQueue()
        updateLyrics()
    }

    override fun onPlayingMetaChanged() {
        if (viewPager.currentItem != MusicPlayerRemote.position) {
            viewPager.setCurrentItem(MusicPlayerRemote.position, true)
        }
        updateLyrics()
    }

    override fun onQueueChanged() {
        updatePlayingQueue()
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences,
        key: String?,
    ) {
        when (key) {
            SHOW_LYRICS -> {
                if (PreferenceUtil.showLyrics) {
                    maybeInitLyrics()
                } else {
                    showLyrics(false)
                    progressViewUpdateHelper?.stop()
                }
            }

            LYRICS_TYPE -> {
                maybeInitLyrics()
            }
        }
    }

    private fun setLRCViewColors(
        @ColorInt primaryColor: Int,
        @ColorInt secondaryColor: Int,
    ) {
        lrcView.apply {
            setCurrentColor(primaryColor)
            setTimeTextColor(primaryColor)
            setTimelineColor(primaryColor)
            setNormalColor(secondaryColor)
            setTimelineTextColor(primaryColor)
        }
    }

    private fun showLyrics(visible: Boolean) {
        binding.coverLyrics.isVisible = false
        binding.lyricsView.isVisible = false
        binding.viewPager.isVisible = true
        val lyrics: View = if (PreferenceUtil.lyricsType == CoverLyricsType.REPLACE_COVER) {
            ObjectAnimator.ofFloat(viewPager, View.ALPHA, if (visible) 0F else 1F).start()
            lrcView
        } else {
            ObjectAnimator.ofFloat(viewPager, View.ALPHA, 1F).start()
            binding.coverLyrics
        }
        ObjectAnimator.ofFloat(lyrics, View.ALPHA, if (visible) 1F else 0F).apply {
            doOnEnd {
                lyrics.isVisible = visible
            }
            start()
        }
    }

    private fun maybeInitLyrics() {
        val nps = PreferenceUtil.nowPlayingScreen
        // Don't show lyrics container for below conditions
        if (lyricViewNpsList.contains(nps) && PreferenceUtil.showLyrics) {
            showLyrics(true)
            if (PreferenceUtil.lyricsType == CoverLyricsType.REPLACE_COVER) {
                progressViewUpdateHelper?.start()
            }
        } else {
            showLyrics(false)
            progressViewUpdateHelper?.stop()
        }
    }

    private fun updatePlayingQueue() {
        binding.viewPager.apply {
            adapter = AlbumCoverPagerAdapter(parentFragmentManager, MusicPlayerRemote.playingQueue)
            setCurrentItem(/* item = */ MusicPlayerRemote.position, /* smoothScroll = */ true)
            onPageSelected(position = MusicPlayerRemote.position)
        }
    }

    override fun onPageScrolled(
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int,
    ) {
    }

    override fun onPageSelected(position: Int) {
        currentPosition = position
        if (binding.viewPager.adapter != null) {
            (binding.viewPager.adapter as AlbumCoverPagerAdapter).receiveColor(
                colorReceiver = colorReceiver,
                position = position
            )
        }
        if (position != MusicPlayerRemote.position) {
            MusicPlayerRemote.playSongAt(position)
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    private fun notifyColorChange(color: MediaNotificationProcessor) {
        callbacks?.onColorChanged(color)
        val primaryColor = MaterialValueHelper.getPrimaryTextColor(
            context = requireContext(),
            dark = surfaceColor().isColorLight
        )
        val secondaryColor = MaterialValueHelper.getSecondaryDisabledTextColor(
            context = requireContext(),
            dark = surfaceColor().isColorLight
        )

        when (PreferenceUtil.nowPlayingScreen) {
            NowPlayingScreen.Flat, NowPlayingScreen.Normal, NowPlayingScreen.Material -> if (PreferenceUtil.isAdaptiveColor) {
                setLRCViewColors(
                    primaryColor = color.primaryTextColor,
                    secondaryColor = color.secondaryTextColor
                )
            } else {
                setLRCViewColors(primaryColor = primaryColor, secondaryColor = secondaryColor)
            }

            NowPlayingScreen.Color, NowPlayingScreen.Classic -> setLRCViewColors(
                primaryColor = color.primaryTextColor,
                secondaryColor = color.secondaryTextColor
            )

            NowPlayingScreen.Blur -> setLRCViewColors(
                primaryColor = Color.WHITE,
                secondaryColor = ColorUtil.withAlpha(Color.WHITE, 0.5f)
            )

            else -> setLRCViewColors(primaryColor = primaryColor, secondaryColor = secondaryColor)
        }
    }

    fun setCallbacks(listener: Callbacks) {
        callbacks = listener
    }

    interface Callbacks {

        fun onColorChanged(color: MediaNotificationProcessor)

        fun onFavoriteToggled()
    }

    companion object {
        val TAG: String = PlayerAlbumCoverFragment::class.java.simpleName
    }

    private val lyricViewNpsList =
        listOf(
            NowPlayingScreen.Blur,
            NowPlayingScreen.Classic,
            NowPlayingScreen.Color,
            NowPlayingScreen.Flat,
            NowPlayingScreen.Material,
            NowPlayingScreen.MD3,
            NowPlayingScreen.Normal,
            NowPlayingScreen.Plain,
            NowPlayingScreen.Simple
        )
}
