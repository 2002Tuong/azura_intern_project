/**
 *
 */
package com.techpro.parallax.wallpaper.orientationProvider;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;

import com.techpro.parallax.wallpaper.representation.MatrixF4x4;
import com.techpro.parallax.wallpaper.representation.Quaternion;

import java.util.ArrayList;
import java.util.List;

/**
 * Classes implementing this interface provide an orientation of the device
 * either by directly accessing hardware, using Android sensor fusion or fusing
 * sensors itself.
 *
 * The orientation can be provided as rotation matrix or quaternion.
 *
 * @author Alexander Pacha
 *
 */
public abstract class OrientationProvider implements SensorEventListener {
    /**
     * Sync-token for syncing read/write to sensor-data from sensor manager and
     * fusion algorithm
     */
    protected final Object synchronizationToken = new Object();

    /**
     * The list of sensors used by this provider
     */
    protected List<Sensor> sensorList = new ArrayList<Sensor>();

    /**
     * The matrix that holds the current rotation
     */
    protected final MatrixF4x4 currentOrientationRotationMatrix;

    /**
     * The quaternion that holds the current rotation
     */
    protected final Quaternion currentOrientationQuaternion;

    /**
     * The sensor manager for accessing android sensors
     */
    protected SensorManager sensorManager;

    /**
     * Initialises a new OrientationProvider
     *
     * @param sensorManager
     *            The android sensor manager
     */
    public OrientationProvider(SensorManager sensorManager) {
        this.sensorManager = sensorManager;

        // Initialise with identity
        currentOrientationRotationMatrix = new MatrixF4x4();

        // Initialise with identity
        currentOrientationQuaternion = new Quaternion();
    }

    public boolean isSupported() {
        return !sensorList.isEmpty();
    }

    public void addSensor(Sensor sensor) {
        if (sensor != null)
            sensorList.add(sensor);
    }

    public void addSensors(Sensor... sensors) {
        if (sensors != null)
            for (Sensor se : sensors) {
                addSensor(se);
            }
    }

    public void addSensors(int... types) {
        if (sensorManager != null) {
            for (int type : types) {
                Sensor sensor = sensorManager.getDefaultSensor(type);
                if (sensor == null) {
                    // list sensors doesn't support
                    return;
                } else {
                    addSensor(sensor);
                }
            }
        }
    }

    public int getSize() {
        return sensorList.size();
    }

    public void unregisterListener() {
        final int size = sensorList.size();
        for (int i = 0; i < size; i++) {
            sensorManager.unregisterListener(this, sensorList.get(i));
        }
    }

    /**
     * Starts the sensor fusion (e.g. when resuming the activity)
     */
    public void start() {
        // enable our sensor when the activity is resumed, ask for
        // 10 ms updates.
        for (Sensor sensor : sensorList) {
            // enable our sensors when the activity is resumed, ask for
            // 20 ms updates (Sensor_delay_game)
            sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    /**
     * Stops the sensor fusion (e.g. when pausing/suspending the activity)
     */
    public void stop() {
        // make sure to turn our sensors off when the activity is paused
        for (Sensor sensor : sensorList) {
            sensorManager.unregisterListener(this, sensor);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not doing anything
    }

    /**
     * Get the current rotation of the device in the rotation matrix format (4x4 matrix)
     */
    public void getRotationMatrix(MatrixF4x4 matrix) {
        synchronized (synchronizationToken) {
            matrix.set(currentOrientationRotationMatrix);
        }
    }

    /**
     * Get the current rotation of the device in the quaternion format (vector4f)
     */
    public void getQuaternion(Quaternion quaternion) {
        synchronized (synchronizationToken) {
            quaternion.set(currentOrientationQuaternion);
        }
    }

    /**
     * Get the current rotation of the device in the Euler angles
     */
    public void getEulerAngles(float[] angles) {
        synchronized (synchronizationToken) {
            SensorManager.getOrientation(currentOrientationRotationMatrix.matrix, angles);
        }
    }


    public static OrientationProvider bestProvider(@NonNull Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        OrientationProvider provider = new ImprovedOrientationSensor2Provider(sensorManager);
        if (provider.isSupported()) {
            return provider;
        }
        provider = new RotationVectorProvider(sensorManager);
        if (provider.isSupported()) {
            return provider;
        }
        provider = new CalibratedGyroscopeProvider(sensorManager);
        if (provider.isSupported()) {
            return provider;
        }
        provider = new GravityCompassProvider(sensorManager);
        if (provider.isSupported()) {
            return provider;
        }
        provider = new AccelerometerProvider(sensorManager);
        if (provider.isSupported()) {
            return provider;
        }
        return new AccelerometerCompassProvider(sensorManager);
    }
}
