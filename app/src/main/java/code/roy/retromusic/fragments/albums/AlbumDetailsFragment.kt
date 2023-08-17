package code.roy.retromusic.fragments.albums

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.text.parseAsHtml
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import code.roy.appthemehelper.common.ATHToolbarActivity.getToolbarBackgroundColor
import code.roy.retromusic.EXTRA_ALBUM_ID
import code.roy.retromusic.EXTRA_ARTIST_ID
import code.roy.retromusic.EXTRA_ARTIST_NAME
import code.roy.retromusic.R
import code.roy.retromusic.activities.tageditor.AbsTagEditorActivity
import code.roy.retromusic.activities.tageditor.AlbumTagEditorActivity
import code.roy.retromusic.adapter.album.HorizontalAlbumAdapter
import code.roy.retromusic.adapter.song.SimpleSongAdapter
import code.roy.retromusic.databinding.FragmentAlbumDetailsBinding
import code.roy.retromusic.dialogs.AddToPlaylistDialog
import code.roy.retromusic.dialogs.DeleteSongsDialog
import code.roy.retromusic.extensions.applyColor
import code.roy.retromusic.extensions.applyOutlineColor
import code.roy.retromusic.extensions.findActivityNavController
import code.roy.retromusic.extensions.show
import code.roy.retromusic.extensions.surfaceColor
import code.roy.retromusic.fragments.base.AbsMainActivityFragment
import code.roy.retromusic.glide.RetroGlideExtension
import code.roy.retromusic.glide.RetroGlideExtension.albumCoverOptions
import code.roy.retromusic.glide.RetroGlideExtension.artistImageOptions
import code.roy.retromusic.glide.RetroGlideExtension.asBitmapPalette
import code.roy.retromusic.glide.SingleColorTarget
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.helper.SortOrder.AlbumSongSortOrder.Companion.SONG_A_Z
import code.roy.retromusic.helper.SortOrder.AlbumSongSortOrder.Companion.SONG_DURATION
import code.roy.retromusic.helper.SortOrder.AlbumSongSortOrder.Companion.SONG_TRACK_LIST
import code.roy.retromusic.helper.SortOrder.AlbumSongSortOrder.Companion.SONG_Z_A
import code.roy.retromusic.itf.IAlbumClickListener
import code.roy.retromusic.model.Album
import code.roy.retromusic.model.Artist
import code.roy.retromusic.network.Result
import code.roy.retromusic.network.model.LastFmAlbum
import code.roy.retromusic.repository.RealRepository
import code.roy.retromusic.util.MusicUtil
import code.roy.retromusic.util.PreferenceUtil
import code.roy.retromusic.util.RetroUtil
import code.roy.retromusic.util.logD
import code.roy.retromusic.util.logE
import com.bumptech.glide.Glide
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.Collator

class AlbumDetailsFragment : AbsMainActivityFragment(R.layout.fragment_album_details),
    IAlbumClickListener {

    private var _binding: FragmentAlbumDetailsBinding? = null
    private val binding get() = _binding!!

    private val arguments by navArgs<AlbumDetailsFragmentArgs>()
    private val detailsViewModel by viewModel<AlbumDetailsViewModel> {
        parametersOf(arguments.extraAlbumId)
    }

    private lateinit var simpleSongAdapter: SimpleSongAdapter
    private lateinit var album: Album
    private var albumArtistExists = false

    private val savedSortOrder: String
        get() = PreferenceUtil.albumDetailSongSortOrder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment_container
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(surfaceColor())
            setPathMotion(MaterialArcMotion())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlbumDetailsBinding.bind(view)
        mainActivity.addMusicServiceEventListener(detailsViewModel)
        mainActivity.setSupportActionBar(binding.toolbar)

        binding.toolbar.title = " "
        binding.albumCoverContainer.transitionName = arguments.extraAlbumId.toString()
        postponeEnterTransition()
        detailsViewModel.getAlbum().observe(viewLifecycleOwner) { album ->
            view.doOnPreDraw {
                startPostponedEnterTransition()
            }
            albumArtistExists = !album.albumArtist.isNullOrEmpty()
            showAlbum(album)
            binding.artistImage.transitionName = if (albumArtistExists) {
                album.albumArtist
            } else {
                album.artistId.toString()
            }
        }

        setupRecyclerView()
        binding.artistImage.setOnClickListener { artistView ->
            if (albumArtistExists) {
                findActivityNavController(R.id.fragment_container)
                    .navigate(
                        resId = R.id.albumArtistDetailsFragment,
                        args = bundleOf(EXTRA_ARTIST_NAME to album.albumArtist),
                        navOptions = null,
                        navigatorExtras = FragmentNavigatorExtras(artistView to album.albumArtist.toString())
                    )
            } else {
                findActivityNavController(R.id.fragment_container)
                    .navigate(
                        resId = R.id.artistDetailsFragment,
                        args = bundleOf(EXTRA_ARTIST_ID to album.artistId),
                        navOptions = null,
                        navigatorExtras = FragmentNavigatorExtras(artistView to album.artistId.toString())
                    )
            }

        }
        binding.fragmentAlbumContent.playAction.setOnClickListener {
            MusicPlayerRemote.openQueue(queue = album.songs, startPosition = 0, startPlaying = true)
        }
        binding.fragmentAlbumContent.shuffleAction.setOnClickListener {
            MusicPlayerRemote.openAndShuffleQueue(
                queue = album.songs,
                startPlaying = true
            )
        }

        binding.fragmentAlbumContent.aboutAlbumText.setOnClickListener {
            if (binding.fragmentAlbumContent.aboutAlbumText.maxLines == 4) {
                binding.fragmentAlbumContent.aboutAlbumText.maxLines = Integer.MAX_VALUE
            } else {
                binding.fragmentAlbumContent.aboutAlbumText.maxLines = 4
            }
        }

        binding.appBarLayout?.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceActivity?.removeMusicServiceEventListener(detailsViewModel)
    }

    private fun setupRecyclerView() {
        simpleSongAdapter = SimpleSongAdapter(
            context = requireActivity() as AppCompatActivity,
            songs = ArrayList(),
            layoutRes = R.layout.v_item_song
        )
        binding.fragmentAlbumContent.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            isNestedScrollingEnabled = false
            adapter = simpleSongAdapter
        }
    }

    private fun showAlbum(album: Album) {
        if (album.songs.isEmpty()) {
            findNavController().navigateUp()
            return
        }
        this.album = album

        binding.albumTitle.text = album.title
        val songText = resources.getQuantityString(
            R.plurals.albumSongs,
            album.songCount,
            album.songCount
        )
        binding.fragmentAlbumContent.songTitle.text = songText
        if (MusicUtil.getYearString(album.year) == "-") {
            binding.albumText.text = String.format(
                "%s • %s",
                if (albumArtistExists) album.albumArtist else album.artistName,
                MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(album.songs))
            )
        } else {
            binding.albumText.text = String.format(
                "%s • %s • %s",
                album.artistName,
                MusicUtil.getYearString(album.year),
                MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(album.songs))
            )
        }
        loadAlbumCover(album)
        simpleSongAdapter.swapDataSet(album.songs)
        if (albumArtistExists) {
            detailsViewModel.getAlbumArtist(album.albumArtist.toString())
                .observe(viewLifecycleOwner) {
                    loadArtistImage(it)
                }
        } else {
            detailsViewModel.getArtist(album.artistId).observe(viewLifecycleOwner) {
                loadArtistImage(it)
            }
        }

        detailsViewModel.getAlbumInfo(album).observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    logD("Loading")
                }

                is Result.Error -> {
                    logE("Error")
                }

                is Result.Success -> {
                    aboutAlbum(result.data)
                }
            }
        }
    }

    private fun moreAlbums(albums: List<Album>) {
        binding.fragmentAlbumContent.moreTitle.show()
        binding.fragmentAlbumContent.moreRecyclerView.show()
        binding.fragmentAlbumContent.moreTitle.text =
            String.format(getString(R.string.label_more_from), album.artistName)

        val albumAdapter =
            HorizontalAlbumAdapter(requireActivity() as AppCompatActivity, albums, this)
        binding.fragmentAlbumContent.moreRecyclerView.layoutManager = GridLayoutManager(
            /* context = */ requireContext(),
            /* spanCount = */ 1,
            /* orientation = */ GridLayoutManager.HORIZONTAL,
            /* reverseLayout = */ false
        )
        binding.fragmentAlbumContent.moreRecyclerView.adapter = albumAdapter
    }

    private fun aboutAlbum(lastFmAlbum: LastFmAlbum) {
        if (lastFmAlbum.album != null) {
            if (lastFmAlbum.album.wiki != null) {
                binding.fragmentAlbumContent.aboutAlbumText.show()
                binding.fragmentAlbumContent.aboutAlbumTitle.show()
                binding.fragmentAlbumContent.aboutAlbumTitle.text =
                    String.format(getString(R.string.about_album_label), lastFmAlbum.album.name)
                binding.fragmentAlbumContent.aboutAlbumText.text =
                    lastFmAlbum.album.wiki.content.parseAsHtml()
            }
            if (lastFmAlbum.album.listeners.isNotEmpty()) {
                binding.fragmentAlbumContent.listeners.show()
                binding.fragmentAlbumContent.listenersLabel.show()
                binding.fragmentAlbumContent.scrobbles.show()
                binding.fragmentAlbumContent.scrobblesLabel.show()

                binding.fragmentAlbumContent.listeners.text =
                    RetroUtil.formatValue(lastFmAlbum.album.listeners.toFloat())
                binding.fragmentAlbumContent.scrobbles.text =
                    RetroUtil.formatValue(lastFmAlbum.album.playcount.toFloat())
            }
        }
    }

    private fun loadArtistImage(artist: Artist) {
        detailsViewModel.getMoreAlbums(artist).observe(viewLifecycleOwner) {
            moreAlbums(it)
        }
        Glide.with(requireContext())
            //.forceDownload(PreferenceUtil.isAllowedToDownloadMetadata())
            .load(
                RetroGlideExtension.getArtistModel(
                    artist,
                    PreferenceUtil.isAllowedToDownloadMetadata(requireContext())
                )
            )
            .artistImageOptions(artist)
            .dontAnimate()
            .dontTransform()
            .into(binding.artistImage)
    }

    private fun loadAlbumCover(album: Album) {
        Glide.with(requireContext())
            .asBitmapPalette()
            .albumCoverOptions(album.safeGetFirstSong())
            //.checkIgnoreMediaStore()
            .load(RetroGlideExtension.getSongModel(album.safeGetFirstSong()))
            .into(object : SingleColorTarget(binding.image) {
                override fun onColorReady(color: Int) {
                    setColors(color)
                }
            })
    }

    private fun setColors(color: Int) {
        _binding?.fragmentAlbumContent?.apply {
            shuffleAction.applyColor(color)
            playAction.applyOutlineColor(color)
        }
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

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_album_detail, menu)
        val sortOrder = menu.findItem(R.id.action_sort_order)
        setUpSortOrderMenu(sortOrder.subMenu!!)
        code.roy.appthemehelper.util.ToolbarContentTintHelper.handleOnCreateOptionsMenu(
            /* context = */ requireContext(),
            /* toolbar = */ binding.toolbar,
            /* menu = */ menu,
            /* toolbarColor = */ getToolbarBackgroundColor(binding.toolbar)
        )
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return handleSortOrderMenuItem(item)
    }

    private fun handleSortOrderMenuItem(item: MenuItem): Boolean {
        var sortOrder: String? = null
        val songs = simpleSongAdapter.dataSet
        when (item.itemId) {
            android.R.id.home -> findNavController().navigateUp()
            R.id.action_play_next -> {
                MusicPlayerRemote.playNext(songs)
                return true
            }

            R.id.action_add_to_current_playing -> {
                MusicPlayerRemote.enqueue(songs)
                return true
            }

            R.id.action_add_to_playlist -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    val playlists = get<RealRepository>().fetchPlaylists()
                    withContext(Dispatchers.Main) {
                        AddToPlaylistDialog.create(playlists, songs)
                            .show(childFragmentManager, "ADD_PLAYLIST")
                    }
                }
                return true
            }

            R.id.action_delete_from_device -> {
                DeleteSongsDialog.create(songs).show(childFragmentManager, "DELETE_SONGS")
                return true
            }

            R.id.action_tag_editor -> {
                val intent = Intent(requireContext(), AlbumTagEditorActivity::class.java)
                intent.putExtra(AbsTagEditorActivity.EXTRA_ID, album.id)
                val options = ActivityOptions.makeSceneTransitionAnimation(
                    /* activity = */ requireActivity(),
                    /* sharedElement = */
                    binding.albumCoverContainer,
                    /* sharedElementName = */
                    "${getString(R.string.transition_album_art)}_${album.id}"
                )
                startActivity(
                    intent, options.toBundle()
                )
                return true
            }

            R.id.action_sort_order_title -> sortOrder = SONG_A_Z
            R.id.action_sort_order_title_desc -> sortOrder = SONG_Z_A
            R.id.action_sort_order_track_list -> sortOrder = SONG_TRACK_LIST
            R.id.action_sort_order_artist_song_duration -> sortOrder = SONG_DURATION
        }
        if (sortOrder != null) {
            item.isChecked = true
            setSaveSortOrder(sortOrder)
        }
        return true
    }

    private fun setUpSortOrderMenu(sortOrder: SubMenu) {
        when (savedSortOrder) {
            SONG_A_Z -> sortOrder.findItem(R.id.action_sort_order_title).isChecked = true
            SONG_Z_A -> sortOrder.findItem(R.id.action_sort_order_title_desc).isChecked = true
            SONG_TRACK_LIST ->
                sortOrder.findItem(R.id.action_sort_order_track_list).isChecked = true

            SONG_DURATION ->
                sortOrder.findItem(R.id.action_sort_order_artist_song_duration).isChecked = true
        }
    }

    private fun setSaveSortOrder(sortOrder: String) {
        PreferenceUtil.albumDetailSongSortOrder = sortOrder
        val songs = when (sortOrder) {
            SONG_TRACK_LIST -> album.songs.sortedWith { o1, o2 ->
                o1.trackNumber.compareTo(
                    o2.trackNumber
                )
            }

            SONG_A_Z -> {
                val collator = Collator.getInstance()
                album.songs.sortedWith { o1, o2 -> collator.compare(o1.title, o2.title) }
            }

            SONG_Z_A -> {
                val collator = Collator.getInstance()
                album.songs.sortedWith { o1, o2 -> collator.compare(o2.title, o1.title) }
            }

            SONG_DURATION -> album.songs.sortedWith { o1, o2 ->
                o1.duration.compareTo(
                    o2.duration
                )
            }

            else -> throw IllegalArgumentException("invalid $sortOrder")
        }
        album = album.copy(songs = songs)
        simpleSongAdapter.swapDataSet(album.songs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
