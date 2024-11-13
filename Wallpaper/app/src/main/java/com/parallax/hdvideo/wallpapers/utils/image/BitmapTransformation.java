package com.parallax.hdvideo.wallpapers.utils.image;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.Util;

import java.security.MessageDigest;

public abstract class BitmapTransformation implements Transformation<Bitmap> {

  @NonNull
  @Override
  public final Resource<Bitmap> transform(@NonNull Context context, @NonNull Resource<Bitmap> resource,
                                          int outWidth, int outHeight) {
    if (!Util.isValidDimensions(outWidth, outHeight)) {
      throw new IllegalArgumentException(
        "Cannot apply transformation on width: " + outWidth + " or height: " + outHeight
          + " less than or equal to zero and not Target.SIZE_ORIGINAL");
    }
    BitmapPool pool = Glide.get(context).getBitmapPool();
    Bitmap bitmapToTransform = resource.get();
    int targetWidthValue = outWidth == Target.SIZE_ORIGINAL ? bitmapToTransform.getWidth() : outWidth;
    int targetHeightValue = outHeight == Target.SIZE_ORIGINAL ? bitmapToTransform.getHeight() : outHeight;
    Bitmap transformedBitmap = transform(context.getApplicationContext(), pool, bitmapToTransform, targetWidthValue, targetHeightValue);

    final Resource<Bitmap> res;
    if (bitmapToTransform.equals(transformedBitmap)) {
      res = resource;
    } else {
      res = BitmapResource.obtain(transformedBitmap, pool);
    }
    return res;
  }

  void setCanvasBitmapDensity(@NonNull Bitmap toTransform, @NonNull Bitmap canvasBitmap) {
    canvasBitmap.setDensity(toTransform.getDensity());
  }

  protected abstract Bitmap transform(@NonNull Context context, @NonNull BitmapPool pool,
                                      @NonNull Bitmap toTransform, int outWidth, int outHeight);

  @Override
  public abstract void updateDiskCacheKey(@NonNull MessageDigest messageDigest);

  @Override
  public abstract boolean equals(Object o);

  @Override
  public abstract int hashCode();
}
