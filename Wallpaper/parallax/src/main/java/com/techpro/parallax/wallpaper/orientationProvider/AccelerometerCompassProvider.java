package com.techpro.parallax.wallpaper.orientationProvider;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

/**
 * The orientation provider that delivers the current orientation from the {@link Sensor#TYPE_ACCELEROMETER
 * Accelerometer} and {@link Sensor#TYPE_MAGNETIC_FIELD Compass}.
 * 
 * @author Alexander Pacha
 * 
 */
public class AccelerometerCompassProvider extends OrientationProvider {

    /**
     * Compass values
     */
    final private float[] magnitudeValues = new float[3];

    /**
     * Accelerometer values
     */
    final private float[] accelerometerValues = new float[3];

    /**
     * Inclination values
     */
    final float[] inclinationValues = new float[16];

    private final LowPassFilter lowPassFilter;
    /**
     * Initialises a new AccelerometerCompassProvider
     * 
     * @param sensorManager The android sensor manager
     */
    public AccelerometerCompassProvider(SensorManager sensorManager) {
        super(sensorManager);
        this.lowPassFilter = new LowPassFilter(0.18f, 3);
        //Add the compass and the accelerometer
        addSensors(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // we received a sensor event. it is a good practice to check
        // that we received the proper event
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnitudeValues, 0, magnitudeValues.length);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] filter = lowPassFilter.filter(event.values);
            System.arraycopy(filter, 0, accelerometerValues, 0, accelerometerValues.length);
        }

        // Fuse accelerometer with compass
        SensorManager.getRotationMatrix(currentOrientationRotationMatrix.matrix, inclinationValues, accelerometerValues,
                magnitudeValues);
        // Transform rotation matrix to quaternion
        currentOrientationQuaternion.setRowMajor(currentOrientationRotationMatrix.matrix);
    }
}
