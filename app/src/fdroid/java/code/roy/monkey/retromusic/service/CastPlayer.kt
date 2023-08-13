package code.roy.monkey.retromusic.service

import code.roy.retromusic.model.Song
import code.roy.retromusic.service.playback.Playback

// Empty CastPlayer implementation
class CastPlayer : Playback {
    override val isInitialized: Boolean
        get() = true
    override val isPlaying: Boolean
        get() = true
    override val audioSessionId: Int
        get() = 0

    override fun setDataSource(
        song: Song,
        force: Boolean,
        completion: (success: Boolean) -> Unit,
    ) {
    }

    override fun setNextDataSource(path: String?) {}

    override var callbacks: Playback.PlaybackCallbacks? = null

    override fun start() = true

    override fun stop() {}

    override fun release() {}

    override fun pause(): Boolean = true

    override fun duration() = 0

    override fun position() = 0

    override fun seek(whereto: Int, force: Boolean) = whereto

    override fun setVolume(vol: Float) = true

    override fun setAudioSessionId(sessionId: Int) = true

    override fun setCrossFadeDuration(duration: Int) {}

    override fun setPlaybackSpeedPitch(speed: Float, pitch: Float) {}
}
