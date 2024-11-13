package com.emopass.antitheftalarm.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.emopass.antitheftalarm.utils.Config;

public class DeviceSensorManager implements SensorEventListener {

    private static final int SENSOR_SENSITIVITY = 4;
    private SensorManager sensorManager;
    private Sensor sensor;
    private float[] gravity;
    private float accel;
    private float accelCurrent;
    private float accelLast;
    private CallbackSensor callbackSensor;
    private boolean isInPocket = false;


    private Context context;

    public DeviceSensorManager(Context context, CallbackSensor callbackSensor) {
        this.context = context;
        this.callbackSensor = callbackSensor;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void init(int type) {
        if (type == Config.DETECTION_PROXIMITY) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else if (type == Config.DETECTION_MOTION) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            accel = 0.00f;
            accelCurrent = SensorManager.GRAVITY_EARTH;
            accelLast = SensorManager.GRAVITY_EARTH;
            sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }


    public void unRegisterSensor() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone();
            // Shake detection
            float x = gravity[0];
            float y = gravity[1];
            float z = gravity[2];
            accelLast = accelCurrent;
            accelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = accelCurrent - accelLast;
            accel = accel * 0.9f + delta;
            // Make this higher or lower according to how much
            // motion you want to detect
            if (accel > 0.5) {
                if (callbackSensor != null) {
                    callbackSensor.onDetect();
                }
            }
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
                isInPocket = true;
            } else {
                if (callbackSensor != null && isInPocket) {
                    isInPocket = false;
                    callbackSensor.onDetect();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface CallbackSensor {
        void onDetect();
    }
}
