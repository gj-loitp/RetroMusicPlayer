package code.roy.retromusic.fragments.player.tiny

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import code.roy.appthemehelper.util.ColorUtil
import code.roy.retromusic.R
import code.roy.retromusic.databinding.FTinyControlsFragmentBinding
import code.roy.retromusic.fragments.base.AbsPlayerControlsFragment
import code.roy.retromusic.util.color.MediaNotificationProcessor

class TinyPlaybackControlsFragment :
    AbsPlayerControlsFragment(R.layout.f_tiny_controls_fragment) {
    private var _binding: FTinyControlsFragmentBinding? = null
    private val binding get() = _binding!!

    override val shuffleButton: ImageButton
        get() = binding.shuffleButton

    override val repeatButton: ImageButton
        get() = binding.repeatButton

    override fun show() {}

    override fun hide() {}

    override fun setColor(color: MediaNotificationProcessor) {
        lastPlaybackControlsColor = color.secondaryTextColor
        lastDisabledPlaybackControlsColor = ColorUtil.withAlpha(
            baseColor = color.secondaryTextColor,
            alpha = 0.25f
        )

        updateRepeatState()
        updateShuffleState()
    }

    override fun onUpdateProgressViews(
        progress: Int,
        total: Int,
    ) {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FTinyControlsFragmentBinding.bind(view)
    }

    override fun onServiceConnected() {
        updateRepeatState()
        updateShuffleState()
    }

    override fun onRepeatModeChanged() {
        updateRepeatState()
    }

    override fun onShuffleModeChanged() {
        updateShuffleState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
