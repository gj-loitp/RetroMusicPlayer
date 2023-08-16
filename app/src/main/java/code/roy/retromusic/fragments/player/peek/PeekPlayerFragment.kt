package code.roy.retromusic.fragments.player.peek

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import code.roy.appthemehelper.util.ToolbarContentTintHelper
import code.roy.retromusic.R
import code.roy.retromusic.databinding.FPeekPlayerBinding
import code.roy.retromusic.extensions.colorControlNormal
import code.roy.retromusic.extensions.drawAboveSystemBarsWithPadding
import code.roy.retromusic.extensions.getSongInfo
import code.roy.retromusic.extensions.hide
import code.roy.retromusic.extensions.show
import code.roy.retromusic.extensions.whichFragment
import code.roy.retromusic.fragments.base.AbsPlayerFragment
import code.roy.retromusic.fragments.base.goToAlbum
import code.roy.retromusic.fragments.base.goToArtist
import code.roy.retromusic.fragments.player.PlayerAlbumCoverFragment
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.util.PreferenceUtil
import code.roy.retromusic.util.color.MediaNotificationProcessor

class PeekPlayerFragment : AbsPlayerFragment(R.layout.f_peek_player) {

    private lateinit var controlsFragment: PeekPlayerControlFragment
    private var lastColor: Int = 0
    private var _binding: FPeekPlayerBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FPeekPlayerBinding.bind(view)
        setUpPlayerToolbar()
        setUpSubFragments()
        binding.title.isSelected = true
        binding.title.setOnClickListener {
            goToAlbum(requireActivity())
        }
        binding.text.setOnClickListener {
            goToArtist(requireActivity())
        }
        binding.root.drawAboveSystemBarsWithPadding()
    }

    private fun setUpSubFragments() {
        controlsFragment =
            whichFragment(R.id.playbackControlsFragment) as PeekPlayerControlFragment

        val coverFragment =
            whichFragment(R.id.playerAlbumCoverFragment) as PlayerAlbumCoverFragment
        coverFragment.setCallbacks(this)
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.apply {
            inflateMenu(R.menu.menu_player)
            setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
            setOnMenuItemClickListener(this@PeekPlayerFragment)
            ToolbarContentTintHelper.colorizeToolbar(
                /* toolbarView = */ this,
                /* toolbarIconsColor = */ colorControlNormal(),
                /* activity = */ requireActivity()
            )
        }
    }

    override fun playerToolbar(): Toolbar {
        return binding.playerToolbar
    }

    override fun onShow() {
    }

    override fun onHide() {
    }

    override fun toolbarIconColor() = colorControlNormal()

    override val paletteColor: Int
        get() = lastColor

    override fun onColorChanged(color: MediaNotificationProcessor) {
        lastColor = color.primaryTextColor
        libraryViewModel.updateColor(color.primaryTextColor)
        controlsFragment.setColor(color)
    }

    override fun onFavoriteToggled() {
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
        super.onServiceConnected()
        updateSong()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        updateSong()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
