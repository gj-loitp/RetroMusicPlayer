package code.roy.retromusic.fragments.player.color

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.doOnEnd
import code.roy.retromusic.R
import code.roy.retromusic.databinding.FragmentColorPlayerBinding
import code.roy.retromusic.extensions.colorControlNormal
import code.roy.retromusic.extensions.drawAboveSystemBars
import code.roy.retromusic.extensions.whichFragment
import code.roy.retromusic.fragments.base.AbsPlayerFragment
import code.roy.retromusic.fragments.player.PlayerAlbumCoverFragment
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.model.Song
import code.roy.retromusic.util.color.MediaNotificationProcessor

class ColorFragment : AbsPlayerFragment(R.layout.fragment_color_player) {

    private var lastColor: Int = 0
    private var navigationColor: Int = 0
    private lateinit var playbackControlsFragment: ColorPlaybackControlsFragment
    private var valueAnimator: ValueAnimator? = null
    private var _binding: FragmentColorPlayerBinding? = null
    private val binding get() = _binding!!

    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    override val paletteColor: Int
        get() = navigationColor

    override fun onColorChanged(color: MediaNotificationProcessor) {
        libraryViewModel.updateColor(color.backgroundColor)
        lastColor = color.secondaryTextColor
        playbackControlsFragment.setColor(color)
        navigationColor = color.backgroundColor

        binding.colorGradientBackground.setBackgroundColor(color.backgroundColor)
        val animator =
            playbackControlsFragment.createRevealAnimator(binding.colorGradientBackground)
        animator.doOnEnd {
            _binding?.root?.setBackgroundColor(color.backgroundColor)
        }
        animator.start()
        binding.playerToolbar.post {
            code.roy.appthemehelper.util.ToolbarContentTintHelper.colorizeToolbar(
                /* toolbarView = */ binding.playerToolbar,
                /* toolbarIconsColor = */ color.secondaryTextColor,
                /* activity = */ requireActivity()
            )
        }
    }

    override fun onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.currentSong)
    }

    override fun onShow() {
        playbackControlsFragment.show()
    }

    override fun onHide() {
        playbackControlsFragment.hide()
    }

    override fun toolbarIconColor(): Int {
        return lastColor
    }

    override fun toggleFavorite(song: Song) {
        super.toggleFavorite(song)
        if (song.id == MusicPlayerRemote.currentSong.id) {
            updateIsFavorite()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (valueAnimator != null) {
            valueAnimator!!.cancel()
            valueAnimator = null
        }
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentColorPlayerBinding.bind(view)
        setUpSubFragments()
        setUpPlayerToolbar()
        val playerAlbumCoverFragment: PlayerAlbumCoverFragment =
            whichFragment(R.id.playerAlbumCoverFragment)
        playerAlbumCoverFragment.setCallbacks(this)
        playerToolbar().drawAboveSystemBars()
    }

    private fun setUpSubFragments() {
        playbackControlsFragment = whichFragment(R.id.playbackControlsFragment)
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.apply {
            inflateMenu(R.menu.menu_player)
            setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
            setOnMenuItemClickListener(this@ColorFragment)
            code.roy.appthemehelper.util.ToolbarContentTintHelper.colorizeToolbar(
                /* toolbarView = */ this,
                /* toolbarIconsColor = */ colorControlNormal(),
                /* activity = */ requireActivity()
            )
        }
    }

    companion object {
        fun newInstance(): ColorFragment {
            return ColorFragment()
        }
    }
}
