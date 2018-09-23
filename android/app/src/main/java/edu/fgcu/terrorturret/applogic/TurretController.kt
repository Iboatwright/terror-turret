package edu.fgcu.terrorturret.applogic

import android.util.Log
import edu.fgcu.terrorturret.LoggerTags.LOG_TURRET_CONTROL
import edu.fgcu.terrorturret.network.TurretConnection
import edu.fgcu.terrorturret.utils.map
import kotlin.math.roundToInt

object TurretController {

    private const val NUM_SPEED_SETTINGS = 10

    var isSafetyOn = true
        private set

    fun fireZeMissiles() {
        if (!isSafetyOn) {
            Log.i(LOG_TURRET_CONTROL, "Firing ze missiles!")
            val command = TurretCommands.CMD_FIRE
            TurretConnection.sendTurretCommand(command)
        } else {
            Log.i(LOG_TURRET_CONTROL, "Cannot fire, safety is on!")
        }
    }

    fun ceaseFire() {
        Log.i(LOG_TURRET_CONTROL, "Ceasing fire!")
        TurretConnection.sendTurretCommand(TurretCommands.CMD_CEASE_FIRE)
    }

    fun engageSafety(safetyOn: Boolean) {
        val command = if (safetyOn) {
            TurretCommands.CMD_SAFETY_ON
        } else {
            TurretCommands.CMD_SAFETY_OFF
        }
        TurretConnection.sendTurretCommand(command)
        isSafetyOn = safetyOn
    }

    /**
     * Updates the position of the analog joystick which is used to aim the turret.
     *
     * This function expects an x and y coordinate of the stick position, normalized to the range
     * [0, 1]. It applies some transformations internally, and then sends the appropriate speed
     * settings off to the turret.
     */
    fun updateAnalogPosition(normalizedX: Double, normalizedY: Double) {
        val horizontalSpeed = normalizedX.map(
                fromMin = 0.0,
                fromMax = 1.0,
                toMin = -NUM_SPEED_SETTINGS.toDouble(),
                toMax = NUM_SPEED_SETTINGS.toDouble()
        ).roundToInt()
        val verticalSpeed = normalizedY.map(
                fromMin = 0.0,
                fromMax = 1.0,
                toMin = -NUM_SPEED_SETTINGS.toDouble(),
                toMax = NUM_SPEED_SETTINGS.toDouble()
        ).roundToInt()

        rotateTurretAtSpeed(horizontalSpeed)
        pitchTurretAtSpeed(verticalSpeed)
    }

    private fun rotateTurretAtSpeed(speedLevel: Int) {
        val gatedSpeed = gateSpeedSetting(speedLevel)
        val rotateCommand = TurretCommands.CMD_ROTATE_FORMAT.format(gatedSpeed)
        TurretConnection.sendTurretCommand(rotateCommand)
    }

    private fun pitchTurretAtSpeed(speedLevel: Int) {
        val gatedSpeed = gateSpeedSetting(speedLevel)
        val pitchCommand = TurretCommands.CMD_PITCH_FORMAT.format(gatedSpeed)
        TurretConnection.sendTurretCommand(pitchCommand)
    }

    /**
     * Ensures that the speed level passed to one of the servo-commanding functions to pitch or
     * rotate the turret falls within the valid range of speed settings (positive and negative),
     * else adjusts it so that it does.
     */
    private fun gateSpeedSetting(speedLevel: Int): Int {
        return when {
            speedLevel > NUM_SPEED_SETTINGS -> NUM_SPEED_SETTINGS
            speedLevel < NUM_SPEED_SETTINGS -> NUM_SPEED_SETTINGS
            else -> NUM_SPEED_SETTINGS
        }
    }

}