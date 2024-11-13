package com.techpro.parallax.wallpaper.orientationProvider;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import androidx.annotation.NonNull;


public final class AccelerometerProvider extends OrientationProvider {

    public static final String name = "accelerometer";
    public final float[] g = new float[3];
    public final LowPassFilter h = new LowPassFilter(0.18f, 3);


    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public AccelerometerProvider(@NonNull SensorManager sensorManager) {
        super(sensorManager);
        addSensors(Sensor.TYPE_ACCELEROMETER);
    }

    @Override // android.hardware.SensorEventListener
    public void onSensorChanged(@NonNull SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == 1) {
            float[] fArr = event.values;
            float[] filter = h.filter(fArr);
            float[] fArr2 = this.g;
            System.arraycopy(filter, 0, fArr2, 0, fArr2.length);
            float[] fArr3 = this.g;
            fArr3[0] = fArr3[0] / 9.80665f;
            fArr3[1] = fArr3[1] / 9.80665f;
            fArr3[2] = fArr3[2] / 9.80665f;
            float[] rotationMatrix = currentOrientationRotationMatrix.matrix;
            float[] fArr4 = this.g;
            SensorManager.getRotationMatrixFromVector(rotationMatrix, new float[]{fArr4[0], fArr4[1], fArr4[2], 0.0f, 0.0f});
            currentOrientationQuaternion.setRowMajor(rotationMatrix);
        }
    }

    @Override
    public void getEulerAngles(float[] angles) {
        super.getEulerAngles(angles);
        float[] fArr = this.g;
        double atan2 = Math.atan2(fArr[2], fArr[2]);
        double d = 180;
        angles[0] = (float) Math.toRadians(-((atan2 * d) / 3.141592653589793d));
        float[] fArr2 = this.g;
        angles[1] = (float) Math.toRadians(-((Math.atan2(fArr2[1], fArr2[2]) * d) / 3.141592653589793d));
        float[] fArr3 = this.g;
        angles[2] = (float) Math.toRadians(-((Math.atan2(fArr3[0], fArr3[2]) * d) / 3.141592653589793d));
    }
}
