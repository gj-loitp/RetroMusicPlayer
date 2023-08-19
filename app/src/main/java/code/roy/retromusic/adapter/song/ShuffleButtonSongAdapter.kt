package code.roy.retromusic.adapter.song

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import code.roy.retromusic.R
import code.roy.retromusic.extensions.accentColor
import code.roy.retromusic.extensions.accentOutlineColor
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.model.Song
import code.roy.retromusic.util.PreferenceUtil
import code.roy.retromusic.util.RetroUtil
import com.google.android.material.button.MaterialButton

class ShuffleButtonSongAdapter(
    activity: FragmentActivity,
    dataSet: MutableList<Song>,
    itemLayoutRes: Int,
) : AbsOffsetSongAdapter(activity, dataSet, itemLayoutRes) {


    override fun createViewHolder(view: View): SongAdapter.ViewHolder {
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) OFFSET_ITEM else SONG
    }

    override fun onBindViewHolder(holder: SongAdapter.ViewHolder, position: Int) {
        if (holder.itemViewType == OFFSET_ITEM) {
            val viewHolder = holder as ViewHolder
            viewHolder.playAction?.let {
                it.setOnClickListener {
                    MusicPlayerRemote.openQueue(
                        queue = dataSet,
                        startPosition = 0,
                        startPlaying = true
                    )
                }
                it.accentOutlineColor()
            }
            viewHolder.shuffleAction?.let {
                it.setOnClickListener {
                    MusicPlayerRemote.openAndShuffleQueue(queue = dataSet, startPlaying = true)
                }
                it.accentColor()
            }
        } else {
            super.onBindViewHolder(holder, position - 1)
            val landscape = RetroUtil.isLandscape
            if ((PreferenceUtil.songGridSize > 2 && !landscape) || (PreferenceUtil.songGridSizeLand > 5 && landscape)) {
                holder.menu?.isVisible = false
            }
        }
    }

    inner class ViewHolder(itemView: View) : AbsOffsetSongAdapter.ViewHolder(itemView) {
        val playAction: MaterialButton? = itemView.findViewById(R.id.playAction)
        val shuffleAction: MaterialButton? = itemView.findViewById(R.id.shuffleAction)

        override fun onClick(v: View?) {
            if (itemViewType == OFFSET_ITEM) {
                MusicPlayerRemote.openAndShuffleQueue(queue = dataSet, startPlaying = true)
                return
            }
            super.onClick(v)
        }
    }

}
