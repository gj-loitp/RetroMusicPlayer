package code.roy.retromusic.fragments.other

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import code.roy.retromusic.EXTRA_ALBUM_ID
import code.roy.retromusic.EXTRA_ARTIST_ID
import code.roy.retromusic.FAVOURITES
import code.roy.retromusic.HISTORY_PLAYLIST
import code.roy.retromusic.LAST_ADDED_PLAYLIST
import code.roy.retromusic.R
import code.roy.retromusic.RECENT_ALBUMS
import code.roy.retromusic.RECENT_ARTISTS
import code.roy.retromusic.TOP_ALBUMS
import code.roy.retromusic.TOP_ARTISTS
import code.roy.retromusic.TOP_PLAYED_PLAYLIST
import code.roy.retromusic.adapter.album.AlbumAdapter
import code.roy.retromusic.adapter.artist.ArtistAdapter
import code.roy.retromusic.adapter.song.ShuffleButtonSongAdapter
import code.roy.retromusic.adapter.song.SongAdapter
import code.roy.retromusic.databinding.FPlaylistDetailBinding
import code.roy.retromusic.db.toSong
import code.roy.retromusic.fragments.base.AbsMainActivityFragment
import code.roy.retromusic.itf.IAlbumClickListener
import code.roy.retromusic.itf.IArtistClickListener
import code.roy.retromusic.model.Album
import code.roy.retromusic.model.Artist
import code.roy.retromusic.util.RetroUtil
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis

class DetailListFragment : AbsMainActivityFragment(R.layout.f_playlist_detail),
    IArtistClickListener, IAlbumClickListener {
    private val args by navArgs<DetailListFragmentArgs>()
    private var _binding: FPlaylistDetailBinding? = null
    private val binding get() = _binding!!
    private var showClearHistoryOption = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (args.type) {
            TOP_ARTISTS,
            RECENT_ARTISTS,
            TOP_ALBUMS,
            RECENT_ALBUMS,
            FAVOURITES,
            -> {
                enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
            }

            else -> {
                enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
                returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FPlaylistDetailBinding.bind(view)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        mainActivity.setSupportActionBar(binding.toolbar)
        binding.progressIndicator.hide()
        when (args.type) {
            TOP_ARTISTS -> loadArtists(title = R.string.top_artists, type = TOP_ARTISTS)
            RECENT_ARTISTS -> loadArtists(title = R.string.recent_artists, type = RECENT_ARTISTS)
            TOP_ALBUMS -> loadAlbums(title = R.string.top_albums, type = TOP_ALBUMS)
            RECENT_ALBUMS -> loadAlbums(title = R.string.recent_albums, type = RECENT_ALBUMS)
            FAVOURITES -> loadFavorite()
            HISTORY_PLAYLIST -> {
                loadHistory()
                showClearHistoryOption = true // Reference to onCreateOptionsMenu
            }

            LAST_ADDED_PLAYLIST -> lastAddedSongs()
            TOP_PLAYED_PLAYLIST -> topPlayed()
        }

        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
    }

    private fun lastAddedSongs() {
        binding.toolbar.setTitle(R.string.last_added)
        val songAdapter = ShuffleButtonSongAdapter(
            activity = requireActivity(),
            dataSet = mutableListOf(),
            itemLayoutRes = R.layout.item_list
        )
        binding.recyclerView.apply {
            adapter = songAdapter
            layoutManager = linearLayoutManager()
            scheduleLayoutAnimation()
        }
        libraryViewModel.recentSongs().observe(viewLifecycleOwner) { songs ->
            songAdapter.swapDataSet(songs)
        }
    }

    private fun topPlayed() {
        binding.toolbar.setTitle(R.string.my_top_tracks)
        val songAdapter = ShuffleButtonSongAdapter(
            activity = requireActivity(),
            dataSet = mutableListOf(),
            itemLayoutRes = R.layout.item_list
        )
        binding.recyclerView.apply {
            adapter = songAdapter
            layoutManager = linearLayoutManager()
        }
        libraryViewModel.playCountSongs().observe(viewLifecycleOwner) { songs ->
            songAdapter.swapDataSet(songs)
        }
    }

    private fun loadHistory() {
        binding.toolbar.setTitle(R.string.history)

        val songAdapter = ShuffleButtonSongAdapter(
            activity = requireActivity(),
            dataSet = mutableListOf(),
            itemLayoutRes = R.layout.item_list
        )
        binding.recyclerView.apply {
            adapter = songAdapter
            layoutManager = linearLayoutManager()
        }

        libraryViewModel.observableHistorySongs().observe(viewLifecycleOwner) {
            songAdapter.swapDataSet(it)
            binding.empty.isVisible = it.isEmpty()
        }

    }

    private fun loadFavorite() {
        binding.toolbar.setTitle(R.string.favorites)
        val songAdapter = SongAdapter(
            activity = requireActivity(),
            dataSet = mutableListOf(),
            itemLayoutRes = R.layout.item_list
        )
        binding.recyclerView.apply {
            adapter = songAdapter
            layoutManager = linearLayoutManager()
        }
        libraryViewModel.favorites().observe(viewLifecycleOwner) { songEntities ->
            val songs = songEntities.map { songEntity -> songEntity.toSong() }
            songAdapter.swapDataSet(songs)
        }
    }


    private fun loadArtists(title: Int, type: Int) {
        binding.toolbar.setTitle(title)
        val artistAdapter = artistAdapter(listOf())
        binding.recyclerView.apply {
            adapter = artistAdapter
            layoutManager = gridLayoutManager()
        }
        libraryViewModel.artists(type).observe(viewLifecycleOwner) { artists ->
            artistAdapter.swapDataSet(artists)
        }
    }

    private fun loadAlbums(title: Int, type: Int) {
        binding.toolbar.setTitle(title)
        val albumAdapter = albumAdapter(listOf())
        binding.recyclerView.apply {
            adapter = albumAdapter
            layoutManager = gridLayoutManager()
        }
        libraryViewModel.albums(type).observe(viewLifecycleOwner) { albums ->
            albumAdapter.swapDataSet(albums)
        }
    }

    private fun artistAdapter(artists: List<Artist>): ArtistAdapter = ArtistAdapter(
        activity = requireActivity(),
        dataSet = artists,
        itemLayoutRes = R.layout.v_item_grid_circle,
        IArtistClickListener = this
    )

    private fun albumAdapter(albums: List<Album>): AlbumAdapter = AlbumAdapter(
        activity = requireActivity(),
        dataSet = albums,
        itemLayoutRes = R.layout.v_item_grid,
        listener = this
    )

    private fun linearLayoutManager(): LinearLayoutManager = LinearLayoutManager(
        /* context = */ requireContext(),
        /* orientation = */ LinearLayoutManager.VERTICAL,
        /* reverseLayout = */ false
    )

    private fun gridLayoutManager(): GridLayoutManager = GridLayoutManager(
        /* context = */ requireContext(),
        /* spanCount = */ gridCount(),
        /* orientation = */ GridLayoutManager.VERTICAL,
        /* reverseLayout = */ false
    )

    private fun gridCount(): Int {
        if (RetroUtil.isTablet) {
            return if (RetroUtil.isLandscape) 6 else 4
        }
        return if (RetroUtil.isLandscape) 4 else 2
    }


    override fun onArtist(artistId: Long, view: View) {
        findNavController().navigate(
            resId = R.id.artistDetailsFragment,
            args = bundleOf(EXTRA_ARTIST_ID to artistId),
            navOptions = null,
            navigatorExtras = FragmentNavigatorExtras(view to artistId.toString())
        )
    }

    override fun onAlbumClick(albumId: Long, view: View) {
        findNavController().navigate(
            resId = R.id.albumDetailsFragment,
            args = bundleOf(EXTRA_ALBUM_ID to albumId),
            navOptions = null,
            navigatorExtras = FragmentNavigatorExtras(
                view to albumId.toString()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_clear_history, menu)
        if (showClearHistoryOption) {
            menu.findItem(R.id.action_clear_history).isVisible = true // Show Clear History option
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_clear_history -> {
                if (binding.recyclerView.adapter?.itemCount!! > 0) {
                    libraryViewModel.clearHistory()

                    val snackBar =
                        Snackbar.make(
                            /* view = */ binding.container,
                            /* text = */ getString(R.string.history_cleared),
                            /* duration = */ Snackbar.LENGTH_LONG
                        )
                            .setAction(getString(R.string.history_undo_button)) {
                                libraryViewModel.restoreHistory()
                            }
                            .setActionTextColor(Color.YELLOW)
                    val snackBarView = snackBar.view
                    snackBarView.translationY =
                        -(resources.getDimension(R.dimen.mini_player_height))
                    snackBar.show()
                }
            }
        }
        return false
    }
}
