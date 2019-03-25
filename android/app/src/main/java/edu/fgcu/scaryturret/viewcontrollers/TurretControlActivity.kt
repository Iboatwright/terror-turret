package edu.fgcu.scaryturret.viewcontrollers

import android.content.Context
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import edu.fgcu.scaryturret.LoggerTags.LOG_WEBRTC
import edu.fgcu.scaryturret.R
import edu.fgcu.scaryturret.applogic.TurretController
import edu.fgcu.scaryturret.network.TurretConnection
import edu.fgcu.scaryturret.network.webrtc.WebRtcConnectionManager
import edu.fgcu.scaryturret.utils.toast
import kotlinx.android.synthetic.main.activity_turret_control.*
import org.webrtc.*

class TurretControlActivity : AppCompatActivity(),
        WebRtcConnectionManager.WebRtcStreamReceiver {

    private lateinit var webRtcConnectionManager: WebRtcConnectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_turret_control)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        registerClickListeners()
        registerJoystickMovementListener()
        onClickArmSwitch(false)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        webRtcConnectionManager = WebRtcConnectionManager(this, this)
        beginStreamingVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        webRtcConnectionManager.cleanup()
    }

    private fun beginStreamingVideo() {
        video_view.init(webRtcConnectionManager.rootEglBase.eglBaseContext, null)

        val webRtcProtocol = TurretConnection.protocol
        val webRtcIp = TurretConnection.turretIp
        val webRtcPort = TurretConnection.turretPort

        try {
            webRtcConnectionManager.connect(webRtcProtocol, webRtcIp, webRtcPort)
        } catch (ex: Exception) {
            toast(R.string.toast_error_video_stream_failed)
            Log.e(LOG_WEBRTC, ex.toString())
            Handler().postDelayed({ finish() }, 400)
        }

        enableSpeakerphone()
    }

    // TODO ability to mute remote audio stream
    override fun onStreamReady(mediaStream: MediaStream) {
        val videoTrack = mediaStream.videoTracks[0]
        val audioTrack = mediaStream.audioTracks[0]
        audioTrack.setEnabled(true)
        runOnUiThread {
            try {
                videoTrack.addSink(video_view)
            } catch (ex: Exception) {
                toast(R.string.toast_error_video_stream_failed)
                Log.e(LOG_WEBRTC, ex.toString())
            }
        }
    }

    /**
     * Called when connecting to the signalling server fails.
     *
     * Displays a toast with an error message, and returns to the previous screen.
     */
    override fun onSignallingConnectionFailed(msg: String) {
        runOnUiThread {
            toast("Signalling connection failed")
            finish()
        }
    }

    /**
     * This function is needed because WebRTC streams are treated as a call by Android,
     * so we have to enable speakerphone if we want the audio to play through the main device
     * speaker and not just the tiny speaker used for phone calls.
     */
    private fun enableSpeakerphone() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_CALL
        audioManager.isSpeakerphoneOn = true
    }

    // TODO fun to disable speakerphone

    private fun registerJoystickMovementListener() {
        analog_stick.setOnMoveListener({ angle, strength ->
            // Strength is a percentage value [0, 100]
            // Angle is degrees measured from the right, counterclockwise.
            // We need to convert this to a normalized value [0, 1] in (x,y) coordinates

            val angleInRadians = Math.toRadians(angle.toDouble())
            val normalizedX = (strength / 100.0) * Math.cos(angleInRadians)
            val normalizedY = (strength / 100.0) * Math.sin(angleInRadians)

            TurretController.updateAnalogPosition(normalizedX, normalizedY)
        },
        JOYSTICK_UPDATE_FREQ_HZ
        )
    }

    private fun registerClickListeners() {
        fire_button.setOnTouchListener { _, motionEvent -> onClickFire(motionEvent) }
        arm_switch.setOnCheckedChangeListener { _, checked -> onClickArmSwitch(checked) }
    }

    private fun onClickFire(motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                // Button was pressed
                if (arm_switch.isChecked) {
                    TurretController.fireZeMissiles()
                }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                // Button was released
                TurretController.ceaseFire()
            }
        }
        return true // We handled the event
    }

    private fun onClickArmSwitch(checked: Boolean) {
        if (checked) {
            fire_button.alpha = 1.0f
            crosshair.alpha = 1.0f
            TurretController.engageSafety(false)
        } else {
            fire_button.alpha = 0.2f
            crosshair.alpha = 0.1f
            TurretController.engageSafety(true)
        }
    }

    companion object {
        // TODO update
        const val JOYSTICK_UPDATE_FREQ_HZ = 10
    }

}
