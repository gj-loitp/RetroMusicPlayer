package code.roy.retromusic.activities.base

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import code.roy.appthemehelper.util.VersionUtils
import code.roy.retromusic.R
import code.roy.retromusic.extensions.accentColor
import code.roy.retromusic.extensions.rootView
import code.roy.retromusic.util.logD
import com.google.android.material.snackbar.Snackbar

abstract class AbsBaseActivity : AbsThemeActivity() {
    private var hadPermissions: Boolean = false
    private lateinit var permissions: Array<String>
    private var permissionDeniedMessage: String? = null

    open fun getPermissionsToRequest(): Array<String> {
        return arrayOf()
    }

    protected fun setPermissionDeniedMessage(message: String) {
        permissionDeniedMessage = message
    }

    fun getPermissionDeniedMessage(): String {
        return if (permissionDeniedMessage == null) getString(R.string.permissions_denied) else permissionDeniedMessage!!
    }

    private val snackBarContainer: View
        get() = rootView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        volumeControlStream = AudioManager.STREAM_MUSIC
        permissions = getPermissionsToRequest()
        hadPermissions = hasPermissions()
        permissionDeniedMessage = null
    }

    override fun onResume() {
        super.onResume()
        val hasPermissions = hasPermissions()
        if (hasPermissions != hadPermissions) {
            hadPermissions = hasPermissions
            if (VersionUtils.hasMarshmallow()) {
                onHasPermissionsChanged(hasPermissions)
            }
        }
    }

    protected open fun onHasPermissionsChanged(hasPermissions: Boolean) {
        // implemented by sub classes
        logD(hasPermissions)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_MENU && event.action == KeyEvent.ACTION_UP) {
            showOverflowMenu()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun showOverflowMenu() {
    }

    protected open fun requestPermissions() {
        ActivityCompat.requestPermissions(
            /* activity = */ this,
            /* permissions = */ permissions,
            /* requestCode = */ PERMISSION_REQUEST
        )
    }

    protected fun hasPermissions(): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    /* context = */ this,
                    /* permission = */ permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this@AbsBaseActivity, Manifest.permission.READ_EXTERNAL_STORAGE,
                        ) || ActivityCompat.shouldShowRequestPermissionRationale(
                            this@AbsBaseActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        )
                    ) {
                        // User has deny from permission dialog
                        Snackbar.make(
                            /* view = */ snackBarContainer,
                            /* text = */ permissionDeniedMessage!!,
                            /* duration = */ Snackbar.LENGTH_SHORT
                        )
                            .setAction(R.string.action_grant) { requestPermissions() }
                            .setActionTextColor(accentColor()).show()
                    } else {
                        // User has deny permission and checked never show permission dialog so you can redirect to Application settings page
                        Snackbar.make(
                            /* view = */ snackBarContainer,
                            /* text = */ permissionDeniedMessage!!,
                            /* duration = */ Snackbar.LENGTH_INDEFINITE
                        )
                            .setAction(R.string.action_settings) {
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri = Uri.fromParts(
                                    /* scheme = */ "package",
                                    /* ssp = */ this@AbsBaseActivity.packageName,
                                    /* fragment = */ null
                                )
                                intent.data = uri
                                startActivity(intent)
                            }.setActionTextColor(accentColor()).show()
                    }
                    return
                }
            }
            hadPermissions = true
            onHasPermissionsChanged(true)
        } else if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            /* activity = */ this@AbsBaseActivity,
                            /* permission = */ Manifest.permission.BLUETOOTH_CONNECT
                        )
                    ) {
                        // User has deny from permission dialog
                        Snackbar.make(
                            /* view = */ snackBarContainer,
                            /* resId = */ R.string.permission_bluetooth_denied,
                            /* duration = */ Snackbar.LENGTH_SHORT
                        )
                            .setAction(R.string.action_grant) {
                                ActivityCompat.requestPermissions(
                                    /* activity = */ this,
                                    /* permissions = */ arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                                    /* requestCode = */ BLUETOOTH_PERMISSION_REQUEST
                                )
                            }
                            .setActionTextColor(accentColor()).show()
                    }
                }
            }
        }
    }

    companion object {
        const val PERMISSION_REQUEST = 100
        const val BLUETOOTH_PERMISSION_REQUEST = 101
    }

    // this lets keyboard close when clicked in background
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(
                        /* windowToken = */ v.windowToken,
                        /* flags = */ 0
                    )
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}
