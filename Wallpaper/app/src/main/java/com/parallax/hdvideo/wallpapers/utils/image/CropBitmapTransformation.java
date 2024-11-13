package com.parallax.hdvideo.wallpapers.utils.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

import java.security.MessageDigest;

public class CropBitmapTransformation extends BitmapTransformation {

  private static final int CUR_VERSION = 1;
  private static final String ID = "jp.wasabeef.glide.transformations.CropTransformation." + CUR_VERSION;

  public enum CropType {
    TOP,
    CENTER,
    BOTTOM
  }

  private int widthValue;
  private int heightValue;

  private CropType cropType = CropType.CENTER;

  public CropBitmapTransformation(int width, int height) {
    this(width, height, CropType.CENTER);
  }

  public CropBitmapTransformation(int width, int height, CropType cropType) {
    this.widthValue = width;
    this.heightValue = height;
    this.cropType = cropType;
  }

  @Override
  protected Bitmap transform(@NonNull Context context, @NonNull BitmapPool pool,
                             @NonNull Bitmap toTransform, int outWidth, int outHeight) {

    widthValue = widthValue == 0 ? toTransform.getWidth() : widthValue;
    heightValue = heightValue == 0 ? toTransform.getHeight() : heightValue;

    Bitmap.Config bitmapConfig =
      toTransform.getConfig() != null ? toTransform.getConfig() : Bitmap.Config.ARGB_8888;
    Bitmap bitmap = pool.get(widthValue, heightValue, bitmapConfig);

    bitmap.setHasAlpha(true);

    float scaleXValue = (float) widthValue / toTransform.getWidth();
    float scaleYValue = (float) heightValue / toTransform.getHeight();
    float scaleValue = Math.max(scaleXValue, scaleYValue);

    float scaledWidthValue = scaleValue * toTransform.getWidth();
    float scaledHeightValue = scaleValue * toTransform.getHeight();
    float left = (widthValue - scaledWidthValue) / 2;
    float top = getTop(scaledHeightValue);
    RectF targetRectF = new RectF(left, top, left + scaledWidthValue, top + scaledHeightValue);

    setCanvasBitmapDensity(toTransform, bitmap);

    Canvas canvas = new Canvas(bitmap);
    canvas.drawBitmap(toTransform, null, targetRectF, null);

    return bitmap;
  }

  private float getTop(float scaledHeight) {
    switch (cropType) {
      case TOP:
        return 0;
      case CENTER:
        return (heightValue - scaledHeight) / 2;
      case BOTTOM:
        return heightValue - scaledHeight;
      default:
        return 0;
    }
  }

  @Override
  public String toString() {
    return "CropTransformation(width=" + widthValue + ", height=" + heightValue + ", cropType=" + cropType + ")";
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof CropBitmapTransformation &&
      ((CropBitmapTransformation) o).widthValue == widthValue &&
      ((CropBitmapTransformation) o).heightValue == heightValue &&
      ((CropBitmapTransformation) o).cropType == cropType;
  }

  @Override
  public int hashCode() {
    return ID.hashCode() + widthValue * 100000 + heightValue * 1000 + cropType.ordinal() * 10;
  }

  @Override
  public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
    messageDigest.update((ID + widthValue + heightValue + cropType).getBytes(CHARSET));
  }
}
