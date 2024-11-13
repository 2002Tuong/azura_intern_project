package com.techpro.parallax.wallpaper.model;

public final class Resolution {
    private int height = 1920;
    private int width = 1080;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getMax() {
        return Math.max(width, height);
    }
}