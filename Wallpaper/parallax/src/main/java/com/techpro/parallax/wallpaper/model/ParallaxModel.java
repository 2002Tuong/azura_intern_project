package com.techpro.parallax.wallpaper.model;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;

public class ParallaxModel {
    private String url;
    private Float powerX = 1f;
    private Float powerY = 1f;
    private Bitmap bitmap;

    /**
     *
     * @param url Image from uri or path file or assets project
     * @param powerX Translate x from -1 to 1
     * @param powerY Translate y from -1 to 1
     */
    public ParallaxModel(@NonNull String url, Float powerX, Float powerY) {
        this.url = url;
        this.powerX = powerX;
        this.powerY = powerY;
    }

    public ParallaxModel(@NonNull String url) {
        this.url = url;
    }

    public ParallaxModel(@NonNull Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setValue(Float value) {
        this.powerX = value;
        this.powerY = value;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Float getPowerX() {
        return powerX;
    }

    public void setPowerX(Float powerX) {
        this.powerX = powerX;
    }

    public Float getPowerY() {
        return powerY;
    }

    public void setPowerY(Float powerY) {
        this.powerY = powerY;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
