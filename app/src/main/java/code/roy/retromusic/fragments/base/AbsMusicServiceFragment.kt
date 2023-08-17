package code.roy.retromusic.fragments.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.navigation.navOptions
import code.roy.retromusic.R
import code.roy.retromusic.activities.base.AbsMusicServiceActivity
import code.roy.retromusic.itf.IMusicServiceEventListener

open class AbsMusicServiceFragment(@LayoutRes layout: Int) : Fragment(layout),
    IMusicServiceEventListener {

    val navOptions by lazy {
        navOptions {
            launchSingleTop = false
            anim {
                enter = R.anim.anim_retro_fragment_open_enter
                exit = R.anim.anim_retro_fragment_open_exit
                popEnter = R.anim.anim_retro_fragment_close_enter
                popExit = R.anim.anim_retro_fragment_close_exit
            }
        }
    }

    var serviceActivity: AbsMusicServiceActivity? = null
        private set

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            serviceActivity = context as AbsMusicServiceActivity?
        } catch (e: ClassCastException) {
            throw RuntimeException(context.javaClass.simpleName + " must be an instance of " + AbsMusicServiceActivity::class.java.simpleName)
        }
    }

    override fun onDetach() {
        super.onDetach()
        serviceActivity = null
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        serviceActivity?.addMusicServiceEventListener(this)
    }

    @CallSuper
    override fun onDestroyView() {
        super.onDestroyView()
        serviceActivity?.removeMusicServiceEventListener(this)
    }

    override fun onFavoriteStateChanged() {
    }

    override fun onPlayingMetaChanged() {
    }

    override fun onServiceConnected() {
    }

    override fun onServiceDisconnected() {
    }

    override fun onQueueChanged() {
    }

    override fun onPlayStateChanged() {
    }

    override fun onRepeatModeChanged() {
    }

    override fun onShuffleModeChanged() {
    }

    override fun onMediaStoreChanged() {
    }
}
