package code.roy.retromusic.dialogs

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import code.roy.appthemehelper.util.VersionUtils
import code.roy.retromusic.R
import code.roy.retromusic.databinding.DlgSleepTimerBinding
import code.roy.retromusic.extensions.addAccentColor
import code.roy.retromusic.extensions.materialDialog
import code.roy.retromusic.helper.MusicPlayerRemote
import code.roy.retromusic.service.MusicService
import code.roy.retromusic.service.MusicService.Companion.ACTION_PENDING_QUIT
import code.roy.retromusic.service.MusicService.Companion.ACTION_QUIT
import code.roy.retromusic.util.MusicUtil
import code.roy.retromusic.util.PreferenceUtil

class SleepTimerDialog : DialogFragment() {

    private var seekArcProgress: Int = 0
    private lateinit var timerUpdater: TimerUpdater
    private lateinit var dialog: AlertDialog

    private var _binding: DlgSleepTimerBinding? = null
    private val binding get() = _binding!!

    private val shouldFinishLastSong: CheckBox get() = binding.shouldFinishLastSong
    private val seekBar: SeekBar get() = binding.seekBar
    private val timerDisplay: TextView get() = binding.timerDisplay

    @SuppressLint("ScheduleExactAlarm")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        timerUpdater = TimerUpdater()
        _binding = DlgSleepTimerBinding.inflate(layoutInflater)

        val finishMusic = PreferenceUtil.isSleepTimerFinishMusic
        shouldFinishLastSong.apply {
            addAccentColor()
            isChecked = finishMusic
        }
        seekBar.apply {
            addAccentColor()
            seekArcProgress = PreferenceUtil.lastSleepTimerValue
            updateTimeDisplayTime()
            progress = seekArcProgress
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (i < 1) {
                    seekBar.progress = 1
                    return
                }
                seekArcProgress = i
                updateTimeDisplayTime()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                PreferenceUtil.lastSleepTimerValue = seekArcProgress
            }
        })

        materialDialog(R.string.action_sleep_timer).apply {
            if (PreferenceUtil.nextSleepTimerElapsedRealTime > System.currentTimeMillis()) {
                seekBar.isVisible = false
                shouldFinishLastSong.isVisible = false
                timerUpdater.start()
                setPositiveButton(android.R.string.ok, null)
                setNegativeButton(R.string.action_cancel) { _, _ ->
                    timerUpdater.cancel()
                    val previous = makeTimerPendingIntent(PendingIntent.FLAG_NO_CREATE)
                    if (previous != null) {
                        val am = requireContext().getSystemService<AlarmManager>()
                        am?.cancel(previous)
                        previous.cancel()
                        Toast.makeText(
                            /* context = */ requireContext(),
                            /* text = */
                            requireContext().resources.getString(R.string.sleep_timer_canceled),
                            /* duration = */
                            Toast.LENGTH_SHORT
                        ).show()
                        val musicService = MusicPlayerRemote.musicService
                        if (musicService != null && musicService.pendingQuit) {
                            musicService.pendingQuit = false
                            Toast.makeText(
                                /* context = */ requireContext(),
                                /* text = */
                                requireContext().resources.getString(R.string.sleep_timer_canceled),
                                /* duration = */
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                seekBar.isVisible = true
                shouldFinishLastSong.isVisible = true
                setPositiveButton(R.string.action_set) { _, _ ->
                    PreferenceUtil.isSleepTimerFinishMusic = shouldFinishLastSong.isChecked
                    val minutes = seekArcProgress
                    val pi = makeTimerPendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)
                    val nextSleepTimerElapsedTime =
                        SystemClock.elapsedRealtime() + minutes * 60 * 1000
                    PreferenceUtil.nextSleepTimerElapsedRealTime = nextSleepTimerElapsedTime.toInt()
                    val am = requireContext().getSystemService<AlarmManager>()
                    am?.setExact(
                        /* type = */ AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        /* triggerAtMillis = */ nextSleepTimerElapsedTime,
                        /* operation = */ pi
                    )

                    Toast.makeText(
                        /* context = */ requireContext(),
                        /* text = */
                        requireContext().resources.getString(R.string.sleep_timer_set, minutes),
                        /* duration = */Toast.LENGTH_SHORT
                    ).show()
                }
            }
            setView(binding.root)
            dialog = create()

        }
        return dialog
    }

    @SuppressLint("SetTextI18n")
    private fun updateTimeDisplayTime() {
        timerDisplay.text = "$seekArcProgress min"
    }

    private fun makeTimerPendingIntent(flag: Int): PendingIntent? {
        return PendingIntent.getService(
            /* context = */ requireActivity(),
            /* requestCode = */ 0,
            /* intent = */ makeTimerIntent(),
            /* flags = */ flag or if (VersionUtils.hasMarshmallow())
                PendingIntent.FLAG_IMMUTABLE
            else 0
        )
    }

    private fun makeTimerIntent(): Intent {
        val intent = Intent(requireActivity(), MusicService::class.java)
        return if (shouldFinishLastSong.isChecked) {
            intent.setAction(ACTION_PENDING_QUIT)
        } else intent.setAction(ACTION_QUIT)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        timerUpdater.cancel()
        _binding = null
    }

    private inner class TimerUpdater :
        CountDownTimer(
            /* millisInFuture = */ PreferenceUtil.nextSleepTimerElapsedRealTime - SystemClock.elapsedRealtime(),
            /* countDownInterval = */ 1000
        ) {

        override fun onTick(millisUntilFinished: Long) {
            timerDisplay.text = MusicUtil.getReadableDurationString(millisUntilFinished)
        }

        override fun onFinish() {}
    }
}
