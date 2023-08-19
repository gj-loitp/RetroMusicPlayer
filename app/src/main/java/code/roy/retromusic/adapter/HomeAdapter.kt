package code.roy.retromusic.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.fragment.app.findFragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import code.roy.retromusic.EXTRA_ALBUM_ID
import code.roy.retromusic.EXTRA_ARTIST_ID
import code.roy.retromusic.FAVOURITES
import code.roy.retromusic.R
import code.roy.retromusic.RECENT_ALBUMS
import code.roy.retromusic.RECENT_ARTISTS
import code.roy.retromusic.TOP_ALBUMS
import code.roy.retromusic.TOP_ARTISTS
import code.roy.retromusic.adapter.album.AlbumAdapter
import code.roy.retromusic.adapter.artist.ArtistAdapter
import code.roy.retromusic.adapter.song.SongAdapter
import code.roy.retromusic.fragments.home.HomeFragment
import code.roy.retromusic.itf.IAlbumClickListener
import code.roy.retromusic.itf.IArtistClickListener
import code.roy.retromusic.model.Album
import code.roy.retromusic.model.Artist
import code.roy.retromusic.model.Home
import code.roy.retromusic.model.Song
import code.roy.retromusic.util.PreferenceUtil

class HomeAdapter(private val activity: AppCompatActivity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), IArtistClickListener, IAlbumClickListener {

    private var list = listOf<Home>()

    override fun getItemViewType(position: Int): Int {
        return list[position].homeSection
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout =
            LayoutInflater.from(activity).inflate(R.layout.v_section_recycler_view, parent, false)
        return when (viewType) {
            RECENT_ARTISTS, TOP_ARTISTS -> ArtistViewHolder(layout)
            FAVOURITES -> PlaylistViewHolder(layout)
            TOP_ALBUMS, RECENT_ALBUMS -> AlbumViewHolder(layout)
            else -> {
                ArtistViewHolder(layout)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val home = list[position]
        when (getItemViewType(position)) {
            RECENT_ALBUMS -> {
                val viewHolder = holder as AlbumViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        resId = R.id.detailListFragment,
                        args = bundleOf("type" to RECENT_ALBUMS)
                    )
                }
            }

            TOP_ALBUMS -> {
                val viewHolder = holder as AlbumViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        resId = R.id.detailListFragment,
                        args = bundleOf("type" to TOP_ALBUMS)
                    )
                }
            }

            RECENT_ARTISTS -> {
                val viewHolder = holder as ArtistViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        resId = R.id.detailListFragment,
                        args = bundleOf("type" to RECENT_ARTISTS)
                    )
                }
            }

            TOP_ARTISTS -> {
                val viewHolder = holder as ArtistViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        resId = R.id.detailListFragment,
                        args = bundleOf("type" to TOP_ARTISTS)
                    )
                }
            }

            FAVOURITES -> {
                val viewHolder = holder as PlaylistViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        resId = R.id.detailListFragment,
                        args = bundleOf("type" to FAVOURITES)
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun swapData(sections: List<Home>) {
        list = sections
        notifyDataSetChanged()
    }

    @Suppress("UNCHECKED_CAST")
    private inner class AlbumViewHolder(view: View) : AbsHomeViewItem(view) {
        fun bindView(home: Home) {
            title.setText(home.titleRes)
            recyclerView.apply {
                adapter = albumAdapter(home.arrayList as List<Album>)
                layoutManager = gridLayoutManager()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inner class ArtistViewHolder(view: View) : AbsHomeViewItem(view) {
        fun bindView(home: Home) {
            title.setText(home.titleRes)
            recyclerView.apply {
                layoutManager = linearLayoutManager()
                adapter = artistsAdapter(home.arrayList as List<Artist>)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inner class PlaylistViewHolder(view: View) : AbsHomeViewItem(view) {
        fun bindView(home: Home) {
            title.setText(home.titleRes)
            recyclerView.apply {
                val songAdapter = SongAdapter(
                    activity = activity,
                    dataSet = home.arrayList as MutableList<Song>,
                    itemLayoutRes = R.layout.v_item_favourite_card
                )
                layoutManager = linearLayoutManager()
                adapter = songAdapter
            }
        }
    }

    open class AbsHomeViewItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
        val title: AppCompatTextView = itemView.findViewById(R.id.title)
        val clickableArea: ViewGroup = itemView.findViewById(R.id.clickable_area)
    }

    private fun artistsAdapter(artists: List<Artist>) =
        ArtistAdapter(
            activity = activity,
            dataSet = artists,
            itemLayoutRes = PreferenceUtil.homeArtistGridStyle,
            IArtistClickListener = this
        )

    private fun albumAdapter(albums: List<Album>) =
        AlbumAdapter(
            activity = activity,
            dataSet = albums,
            itemLayoutRes = PreferenceUtil.homeAlbumGridStyle,
            listener = this
        )

    private fun gridLayoutManager() = GridLayoutManager(
        /* context = */ activity,
        /* spanCount = */ 1,
        /* orientation = */ GridLayoutManager.HORIZONTAL,
        /* reverseLayout = */ false
    )

    private fun linearLayoutManager() = LinearLayoutManager(
        /* context = */ activity,
        /* orientation = */ LinearLayoutManager.HORIZONTAL,
        /* reverseLayout = */ false
    )

    override fun onArtist(artistId: Long, view: View) {
        activity.findNavController(R.id.fragment_container).navigate(
            resId = R.id.artistDetailsFragment,
            args = bundleOf(EXTRA_ARTIST_ID to artistId),
            navOptions = null,
            navigatorExtras = FragmentNavigatorExtras(
                view to artistId.toString()
            )
        )
    }

    override fun onAlbumClick(albumId: Long, view: View) {
        activity.findNavController(R.id.fragment_container).navigate(
            resId = R.id.albumDetailsFragment,
            args = bundleOf(EXTRA_ALBUM_ID to albumId),
            navOptions = null,
            navigatorExtras = FragmentNavigatorExtras(
                view to albumId.toString()
            )
        )
    }
}
