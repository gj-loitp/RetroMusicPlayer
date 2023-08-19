package code.roy.retromusic.activities.base

import android.Manifest
import android.content.*
import android.os.Bundle
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import code.roy.appthemehelper.util.VersionUtils
import code.roy.retromusic.R
import code.roy.retromusic.db.toPlayCount
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.itf.IMusicServiceEventListener
import code.roy.retromusic.repository.RealRepository
import code.roy.retromusic.service.MusicService.Companion.FAVORITE_STATE_CHANGED
import code.roy.retromusic.service.MusicService.Companion.MEDIA_STORE_CHANGED
import code.roy.retromusic.service.MusicService.Companion.META_CHANGED
import code.roy.retromusic.service.MusicService.Companion.PLAY_STATE_CHANGED
import code.roy.retromusic.service.MusicService.Companion.QUEUE_CHANGED
import code.roy.retromusic.service.MusicService.Companion.REPEAT_MODE_CHANGED
import code.roy.retromusic.service.MusicService.Companion.SHUFFLE_MODE_CHANGED
import code.roy.retromusic.util.PreferenceUtil
import code.roy.retromusic.util.logD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.lang.ref.WeakReference

abstract class AbsMusicServiceActivity : AbsBaseActivity(), IMusicServiceEventListener {
    private val mMusicServiceEventListeners = ArrayList<IMusicServiceEventListener>()
    private val repository: RealRepository by inject()
    private var serviceToken: MusicPlayerRemote.ServiceToken? = null
    private var musicStateReceiver: MusicStateReceiver? = null
    private var receiverRegistered: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceToken = MusicPlayerRemote.bindToService(this, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                this@AbsMusicServiceActivity.onServiceConnected()
            }

            override fun onServiceDisconnected(name: ComponentName) {
                this@AbsMusicServiceActivity.onServiceDisconnected()
            }
        })

        setPermissionDeniedMessage(getString(R.string.permission_external_storage_denied))
    }

    override fun onDestroy() {
        super.onDestroy()
        MusicPlayerRemote.unbindFromService(serviceToken)
        if (receiverRegistered) {
            unregisterReceiver(musicStateReceiver)
            receiverRegistered = false
        }
    }

    fun addMusicServiceEventListener(listenerI: IMusicServiceEventListener?) {
        if (listenerI != null) {
            mMusicServiceEventListeners.add(listenerI)
        }
    }

    fun removeMusicServiceEventListener(listenerI: IMusicServiceEventListener?) {
        if (listenerI != null) {
            mMusicServiceEventListeners.remove(listenerI)
        }
    }

    override fun onServiceConnected() {
        if (!receiverRegistered) {
            musicStateReceiver = MusicStateReceiver(this)

            val filter = IntentFilter()
            filter.addAction(PLAY_STATE_CHANGED)
            filter.addAction(SHUFFLE_MODE_CHANGED)
            filter.addAction(REPEAT_MODE_CHANGED)
            filter.addAction(META_CHANGED)
            filter.addAction(QUEUE_CHANGED)
            filter.addAction(MEDIA_STORE_CHANGED)
            filter.addAction(FAVORITE_STATE_CHANGED)

            ContextCompat.registerReceiver(
                /* context = */ this,
                /* receiver = */ musicStateReceiver,
                /* filter = */ filter,
                /* flags = */ ContextCompat.RECEIVER_NOT_EXPORTED
            )
            receiverRegistered = true
        }

        for (listener in mMusicServiceEventListeners) {
            listener.onServiceConnected()
        }
    }

    override fun onServiceDisconnected() {
        if (receiverRegistered) {
            unregisterReceiver(musicStateReceiver)
            receiverRegistered = false
        }

        for (listener in mMusicServiceEventListeners) {
            listener.onServiceDisconnected()
        }
    }

    override fun onPlayingMetaChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayingMetaChanged()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            if (!PreferenceUtil.pauseHistory) {
                repository.upsertSongInHistory(MusicPlayerRemote.currentSong)
            }
            val song = repository.findSongExistInPlayCount(MusicPlayerRemote.currentSong.id)
                ?.apply { playCount += 1 }
                ?: MusicPlayerRemote.currentSong.toPlayCount()

            repository.upsertSongInPlayCount(song)
        }
    }

    override fun onQueueChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onQueueChanged()
        }
    }

    override fun onPlayStateChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onPlayStateChanged()
        }
    }

    override fun onMediaStoreChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onMediaStoreChanged()
        }
    }

    override fun onRepeatModeChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onRepeatModeChanged()
        }
    }

    override fun onShuffleModeChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onShuffleModeChanged()
        }
    }

    override fun onFavoriteStateChanged() {
        for (listener in mMusicServiceEventListeners) {
            listener.onFavoriteStateChanged()
        }
    }

    override fun onHasPermissionsChanged(hasPermissions: Boolean) {
        super.onHasPermissionsChanged(hasPermissions)
        val intent = Intent(MEDIA_STORE_CHANGED)
        intent.putExtra(
            "from_permissions_changed",
            true
        ) // just in case we need to know this at some point
        sendBroadcast(intent)
        logD("sendBroadcast $hasPermissions")
    }

    override fun getPermissionsToRequest(): Array<String> {
        return mutableListOf<String>().apply {
            if (VersionUtils.hasT()) {
                add(Manifest.permission.READ_MEDIA_AUDIO)
                add(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (!VersionUtils.hasR()) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    private class MusicStateReceiver(activity: AbsMusicServiceActivity) : BroadcastReceiver() {

        private val reference: WeakReference<AbsMusicServiceActivity> = WeakReference(activity)

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val activity = reference.get()
            if (activity != null && action != null) {
                when (action) {
                    FAVORITE_STATE_CHANGED -> activity.onFavoriteStateChanged()
                    META_CHANGED -> activity.onPlayingMetaChanged()
                    QUEUE_CHANGED -> activity.onQueueChanged()
                    PLAY_STATE_CHANGED -> activity.onPlayStateChanged()
                    REPEAT_MODE_CHANGED -> activity.onRepeatModeChanged()
                    SHUFFLE_MODE_CHANGED -> activity.onShuffleModeChanged()
                    MEDIA_STORE_CHANGED -> activity.onMediaStoreChanged()
                }
            }
        }
    }

    companion object {
        val TAG: String = AbsMusicServiceActivity::class.java.simpleName
    }
}
