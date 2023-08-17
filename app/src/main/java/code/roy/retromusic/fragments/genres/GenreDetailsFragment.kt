package code.roy.retromusic.fragments.genres

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import code.roy.retromusic.R
import code.roy.retromusic.adapter.song.SongAdapter
import code.roy.retromusic.databinding.FPlaylistDetailBinding
import code.roy.retromusic.extensions.dipToPix
import code.roy.retromusic.fragments.base.AbsMainActivityFragment
import code.roy.retromusic.helper.menu.GenreMenuHelper
import code.roy.retromusic.model.Genre
import code.roy.retromusic.model.Song
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.transition.MaterialSharedAxis
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class GenreDetailsFragment : AbsMainActivityFragment(R.layout.f_playlist_detail) {
    private val arguments by navArgs<GenreDetailsFragmentArgs>()
    private val detailsViewModel: GenreDetailsViewModel by viewModel {
        parametersOf(arguments.extraGenre)
    }
    private lateinit var genre: Genre
    private lateinit var songAdapter: SongAdapter
    private var _binding: FPlaylistDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enterTransition = MaterialSharedAxis(
            /* axis = */ MaterialSharedAxis.Z,
            /* forward = */ true
        ).addTarget(view)
        returnTransition = MaterialSharedAxis(
            /* axis = */ MaterialSharedAxis.Z,
            /* forward = */ false
        )
        _binding = FPlaylistDetailBinding.bind(view)
        mainActivity.addMusicServiceEventListener(detailsViewModel)
        mainActivity.setSupportActionBar(binding.toolbar)
        binding.container.transitionName = "genre"
        genre = arguments.extraGenre
        binding.toolbar.title = arguments.extraGenre.name
        setupRecyclerView()
        detailsViewModel.getSongs().observe(viewLifecycleOwner) {
            songs(it)
        }
        postponeEnterTransition()
        view.doOnPreDraw {
            startPostponedEnterTransition()
        }
        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            activity = requireActivity(),
            dataSet = ArrayList(),
            itemLayoutRes = R.layout.item_list
        )
        binding.recyclerView.apply {
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }
        songAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
    }

    fun songs(songs: List<Song>) {
        binding.progressIndicator.hide()
        if (songs.isNotEmpty()) songAdapter.swapDataSet(songs)
        else songAdapter.swapDataSet(emptyList())
    }

    private fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    private fun checkIsEmpty() {
        checkForPadding()
        binding.emptyEmoji.text = getEmojiByUnicode(0x1F631)
        binding.empty.isVisible = songAdapter.itemCount == 0
    }

    private fun checkForPadding() {
        val height = dipToPix(52f).toInt()
        binding.recyclerView.setPadding(
            /* left = */ 0,
            /* top = */ 0,
            /* right = */ 0,
            /* bottom = */ height
        )
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_genre_detail, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return GenreMenuHelper.handleMenuClick(
            activity = requireActivity(),
            genre = genre,
            item = item
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
