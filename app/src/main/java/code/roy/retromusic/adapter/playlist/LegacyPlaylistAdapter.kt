package code.roy.retromusic.adapter.playlist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import code.roy.retromusic.adapter.base.MediaEntryViewHolder
import code.roy.retromusic.model.Playlist
import code.roy.retromusic.util.MusicUtil

class LegacyPlaylistAdapter(
    private val activity: FragmentActivity,
    private var list: List<Playlist>,
    private val layoutRes: Int,
    private val playlistClickListener: PlaylistClickListener,
) :
    RecyclerView.Adapter<LegacyPlaylistAdapter.ViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun swapData(list: List<Playlist>) {
        this.list = list
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist: Playlist = list[position]
        holder.title?.text = playlist.name
        holder.text?.text = MusicUtil.getPlaylistInfoString(activity, playlist.getSongs())
        holder.itemView.setOnClickListener {
            playlistClickListener.onPlaylistClick(playlist)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface PlaylistClickListener {
        fun onPlaylistClick(playlist: Playlist)
    }
}
