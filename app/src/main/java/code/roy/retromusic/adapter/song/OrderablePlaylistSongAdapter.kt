package code.roy.retromusic.adapter.song

import android.annotation.SuppressLint
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import code.roy.retromusic.R
import code.roy.retromusic.db.PlaylistEntity
import code.roy.retromusic.db.toSongEntity
import code.roy.retromusic.db.toSongsEntity
import code.roy.retromusic.dialogs.RemoveSongFromPlaylistDialog
import code.roy.retromusic.fragments.LibraryViewModel
import code.roy.retromusic.model.Song
import code.roy.retromusic.util.ViewUtil
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class OrderablePlaylistSongAdapter(
    private val playlistId: Long,
    activity: FragmentActivity,
    dataSet: MutableList<Song>,
    itemLayoutRes: Int,
) : SongAdapter(activity, dataSet, itemLayoutRes),
    DraggableItemAdapter<OrderablePlaylistSongAdapter.ViewHolder> {

    val libraryViewModel: LibraryViewModel by activity.viewModel()

    init {
        this.setHasStableIds(true)
        this.setMultiSelectMenuRes(R.menu.menu_playlists_songs_selection)
    }

    override fun getItemId(position: Int): Long {
        // requires static value, it means need to keep the same value
        // even if the item position has been changed.
        return dataSet[position].id
    }

    override fun createViewHolder(view: View): SongAdapter.ViewHolder {
        return ViewHolder(view)
    }

    override fun onMultipleItemAction(menuItem: MenuItem, selection: List<Song>) {
        when (menuItem.itemId) {
            R.id.action_remove_from_playlist -> RemoveSongFromPlaylistDialog.create(
                selection.toSongsEntity(
                    playlistId
                )
            )
                .show(activity.supportFragmentManager, "REMOVE_FROM_PLAYLIST")

            else -> super.onMultipleItemAction(menuItem, selection)
        }
    }

    inner class ViewHolder(itemView: View) : SongAdapter.ViewHolder(itemView) {

        override var songMenuRes: Int
            get() = R.menu.menu_item_playlist_song
            set(value) {
                super.songMenuRes = value
            }

        override fun onSongMenuItemClick(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_remove_from_playlist -> {
                    RemoveSongFromPlaylistDialog.create(song.toSongEntity(playlistId))
                        .show(activity.supportFragmentManager, "REMOVE_FROM_PLAYLIST")
                    return true
                }
            }
            return super.onSongMenuItemClick(item)
        }

        init {
            dragView?.isVisible = true
        }
    }

    override fun onCheckCanStartDrag(holder: ViewHolder, position: Int, x: Int, y: Int): Boolean {
        if (isInQuickSelectMode) {
            return false
        }
        return ViewUtil.hitTest(holder.imageText!!, x, y) || ViewUtil.hitTest(
            v = holder.dragView!!,
            x = x,
            y = y
        )
    }

    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
        dataSet.add(toPosition, dataSet.removeAt(fromPosition))
    }

    override fun onGetItemDraggableRange(holder: ViewHolder, position: Int): ItemDraggableRange? {
        return null
    }

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean {
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemDragStarted(position: Int) {
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
        notifyDataSetChanged()
    }

    fun saveSongs(playlistEntity: PlaylistEntity) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            libraryViewModel.insertSongs(dataSet.toSongsEntity(playlistEntity))
        }
    }
}
