package com.techpro.parallax.wallpaper.orientationProvider;


import androidx.annotation.NonNull;

public final class LowPassFilter {

    public float f11626a;
    public float b;
    public float c;
    public float d;
    public int e;
    public float[] f;
    public float[] g;
    public float h;
    public int i;

    public LowPassFilter() {
        this(0.0f, 0, 3);
    }

    public LowPassFilter(float f, int i) {
        this.h = f;
        this.i = i;
        this.f11626a = 0.9f;
        this.c = (float) System.nanoTime();
        float[] fArr = new float[i];
        for (int i2 = 0; i2 < i; i2++) {
            fArr[i2] = 0.0f;
        }
        this.f = fArr;
        int i3 = this.i;
        float[] fArr2 = new float[i3];
        for (int i4 = 0; i4 < i3; i4++) {
            fArr2[i4] = 0.0f;
        }
        this.g = fArr2;
    }

    public final float[] filter(@NonNull float[] values) {
        if (this.d == 0.0f) {
            this.d = (float) System.nanoTime();
        }
        this.c = (float) System.nanoTime();
        System.arraycopy(values, 0, this.g, 0, values.length);
        float f = 1;
        int i = this.e;
        int i2 = i + 1;
        this.e = i2;
        float f2 = f / (i / ((this.c - this.d) / 1.0E9f));
        this.b = f2;
        float f3 = this.h;
        this.f11626a = f3 / (f2 + f3);
        if (i2 > 5) {
            int i3 = this.i;
            for (int i4 = 0; i4 < i3; i4++) {
                float[] fArr = this.f;
                float f4 = this.f11626a;
                fArr[i4] = (fArr[i4] * f4) + ((f - f4) * this.g[i4]);
            }
        }
        float[] fArr2 = new float[3];
        float[] fArr3 = this.f;
        System.arraycopy(fArr3, 0, fArr2, 0, fArr3.length);
        return fArr2;
    }

    public final int getSize() {
        return this.i;
    }

    public final void reset() {
        this.d = 0.0f;
        this.c = 0.0f;
        this.e = 0;
        this.b = 0.0f;
        this.f11626a = 0.0f;
    }

    public LowPassFilter(float f, int i, int i2) {
        this((i2 & 1) != 0 ? 0.18f : f, (i2 & 2) != 0 ? 3 : i);
    }
}
