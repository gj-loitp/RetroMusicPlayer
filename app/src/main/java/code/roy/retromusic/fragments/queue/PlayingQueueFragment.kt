package code.roy.retromusic.fragments.queue

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import code.roy.appthemehelper.util.ColorUtil
import code.roy.appthemehelper.util.MaterialValueHelper
import code.roy.retromusic.R
import code.roy.retromusic.activities.MainActivity
import code.roy.retromusic.adapter.song.PlayingQueueAdapter
import code.roy.retromusic.databinding.FragmentPlayingQueueBinding
import code.roy.retromusic.extensions.accentColor
import code.roy.retromusic.fragments.base.AbsMusicServiceFragment
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.util.MusicUtil
import code.roy.retromusic.util.ThemedFastScroller
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils

class PlayingQueueFragment : AbsMusicServiceFragment(R.layout.fragment_playing_queue) {

    private var _binding: FragmentPlayingQueueBinding? = null
    private val binding get() = _binding!!
    private var wrappedAdapter: RecyclerView.Adapter<*>? = null
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null
    private var recyclerViewSwipeManager: RecyclerViewSwipeManager? = null
    private var recyclerViewTouchActionGuardManager: RecyclerViewTouchActionGuardManager? = null
    private var playingQueueAdapter: PlayingQueueAdapter? = null
    private lateinit var linearLayoutManager: LinearLayoutManager

    val mainActivity: MainActivity
        get() = activity as MainActivity

    private fun getUpNextAndQueueTime(): String {
        val duration = MusicPlayerRemote.getQueueDurationMillis(MusicPlayerRemote.position)
        return MusicUtil.buildInfoString(
            string1 = resources.getString(R.string.up_next),
            string2 = MusicUtil.getReadableDurationString(duration)
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlayingQueueBinding.bind(view)

        setupToolbar()
        setUpRecyclerView()

        binding.clearQueue.setOnClickListener {
            MusicPlayerRemote.clearQueue()
        }
        checkForPadding()
        mainActivity.collapsePanel()
    }

    private fun setUpRecyclerView() {
        recyclerViewTouchActionGuardManager = RecyclerViewTouchActionGuardManager()
        recyclerViewDragDropManager = RecyclerViewDragDropManager()
        recyclerViewSwipeManager = RecyclerViewSwipeManager()

        playingQueueAdapter = PlayingQueueAdapter(
            activity = requireActivity(),
            dataSet = MusicPlayerRemote.playingQueue.toMutableList(),
            current = MusicPlayerRemote.position,
            itemLayoutRes = R.layout.item_queue
        )
        wrappedAdapter = recyclerViewDragDropManager?.createWrappedAdapter(playingQueueAdapter!!)
        wrappedAdapter = wrappedAdapter?.let {
            recyclerViewSwipeManager?.createWrappedAdapter(it)
        }

        linearLayoutManager = LinearLayoutManager(requireContext())

        binding.recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = wrappedAdapter
            itemAnimator = DraggableItemAnimator()
            recyclerViewTouchActionGuardManager?.attachRecyclerView(this)
            recyclerViewDragDropManager?.attachRecyclerView(this)
            recyclerViewSwipeManager?.attachRecyclerView(this)
        }
        linearLayoutManager.scrollToPositionWithOffset(
            /* position = */ MusicPlayerRemote.position + 1,
            /* offset = */ 0
        )

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    binding.clearQueue.shrink()
                } else if (dy < 0) {
                    binding.clearQueue.extend()
                }
            }
        })
        ThemedFastScroller.create(binding.recyclerView)
    }

    private fun checkForPadding() {
    }

    override fun onQueueChanged() {
        if (MusicPlayerRemote.playingQueue.isEmpty()) {
            findNavController().navigateUp()
            return
        }
        checkForPadding()
        updateQueue()
        updateCurrentSong()
    }

    override fun onMediaStoreChanged() {
        updateQueue()
        updateCurrentSong()
    }

    private fun updateCurrentSong() {
        binding.appBarLayout.toolbar.subtitle = getUpNextAndQueueTime()
    }

    override fun onPlayingMetaChanged() {
        updateQueuePosition()
    }

    private fun updateQueuePosition() {
        playingQueueAdapter?.setCurrent(MusicPlayerRemote.position)
        resetToCurrentPosition()
        binding.appBarLayout.toolbar.subtitle = getUpNextAndQueueTime()
    }

    private fun updateQueue() {
        playingQueueAdapter?.swapDataSet(
            dataSet = MusicPlayerRemote.playingQueue,
            position = MusicPlayerRemote.position
        )
    }

    private fun resetToCurrentPosition() {
        binding.recyclerView.stopScroll()
        linearLayoutManager.scrollToPositionWithOffset(
            /* position = */ MusicPlayerRemote.position + 1,
            /* offset = */ 0
        )
    }

    override fun onPause() {
        recyclerViewDragDropManager?.cancelDrag()
        super.onPause()
    }

    override fun onDestroy() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager?.release()
            recyclerViewDragDropManager = null
        }
        if (recyclerViewSwipeManager != null) {
            recyclerViewSwipeManager?.release()
            recyclerViewSwipeManager = null
        }
        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter)
            wrappedAdapter = null
        }
        playingQueueAdapter = null
        super.onDestroy()
        if (MusicPlayerRemote.playingQueue.isNotEmpty())
            mainActivity.expandPanel()
    }

    private fun setupToolbar() {
        binding.appBarLayout.toolbar.subtitle = getUpNextAndQueueTime()
        binding.appBarLayout.toolbar.isTitleCentered = false
        binding.clearQueue.backgroundTintList = ColorStateList.valueOf(accentColor())
        ColorStateList.valueOf(
            MaterialValueHelper.getPrimaryTextColor(
                context = requireContext(),
                dark = ColorUtil.isColorLight(accentColor())
            )
        ).apply {
            binding.clearQueue.setTextColor(this)
            binding.clearQueue.iconTint = this
        }
        binding.appBarLayout.pinWhenScrolled()
        binding.appBarLayout.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            setTitle(R.string.now_playing_queue)
            setTitleTextAppearance(context, R.style.ToolbarTextAppearanceNormal)
            setNavigationIcon(R.drawable.ic_arrow_back)
            code.roy.appthemehelper.util.ToolbarContentTintHelper.colorBackButton(this)
        }
    }
}
