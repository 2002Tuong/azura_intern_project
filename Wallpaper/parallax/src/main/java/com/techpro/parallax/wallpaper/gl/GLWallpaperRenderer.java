package com.techpro.parallax.wallpaper.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.techpro.parallax.wallpaper.model.ParallaxModel;
import com.techpro.parallax.wallpaper.model.Resolution;
import com.techpro.parallax.wallpaper.orientationProvider.OrientationProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLWallpaperRenderer implements GLSurfaceView.Renderer {

    private GLWallpaper glWallpaper;
    public float[] viewMatrix = new float[16];
    public float[] mMVPMatrix = new float[16];
    public float[] projectionMatrix = new float[16];
    private final Resolution resolution = new Resolution();
    private final PointF ratio = new PointF();
    private final PointF translationRatio = new PointF();
    private boolean needUpdate = true;
    private final List<ParallaxModel> parallaxModels = new ArrayList<>();
    private final WeakReference<Context> mContext;
    public final float[] rotation = new float[3];
    private OrientationProvider provider;
    private float maxX = (float) (Math.PI / 3);
    private float maxY = (float) (2 * Math.PI / 3);
    private int lastDegrees = 0;
    private int maxDegrees = toDegrees(maxX);

    Set4DImageCallBack set4DImageCallBack;

    public GLWallpaperRenderer(Context context) {
        glWallpaper = new GLWallpaper();
        mContext = new WeakReference<>(context);
    }

    public void set4DImageSuccessListener(Set4DImageCallBack set4DImageCallBack) {
        this.set4DImageCallBack = set4DImageCallBack;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glWallpaper.initGl();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        resolution.setWidth(width);
        resolution.setHeight(height);
        setImageResolution(width, height);
        f();
    }

    public void setImageResolution(int width, int height) {
        float max = resolution.getMax() * 1.28f;
        float f = 6.0f * max;
        ratio.set(resolution.getWidth() / f, resolution.getHeight() / f);
        translationRatio.set(resolution.getWidth() / max, resolution.getHeight() / max);
        float r = width * 1f / height;
        Matrix.frustumM(this.projectionMatrix, 0, -ratio.x, ratio.x, -ratio.y, ratio.y, 1.0f, 6.0f);
//        Matrix.frustumM(this.projectionMatrix, 0, -r, r, -1, 1, 3.0f, 7.0f);
        Matrix.multiplyMM(this.mMVPMatrix, 0, this.projectionMatrix, 0, this.viewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // clear screen
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        if (needUpdate) {
            needUpdate = false;
            glWallpaper.clearTextures();
            for (int i = 0; i < parallaxModels.size(); i++) {
                ParallaxModel model = parallaxModels.get(i);
                Bitmap bitmap;
                if (model.getBitmap() != null) {
                    bitmap = model.getBitmap();
                } else {
                    bitmap = model.getUrl().startsWith("content://") ? loadBitmapFromStorage(model.getUrl())
                            : (model.getUrl().startsWith("/") ? loadBitmapFromFile(model.getUrl())
                            : (model.getUrl().contains("parallax") ? loadBitmapFromUrl(model.getUrl())
                            : loadBitmapFromAssets(model.getUrl())));
                }
                if (bitmap != null) {
                    glWallpaper.loadTexture(i, bitmap);
                } else {
                    if (set4DImageCallBack != null) {
                        set4DImageCallBack.loadImageError();
                        return;
                    }
                }
                Log.d("Step", i + "");
            }
            if (glWallpaper.size() == parallaxModels.size() && set4DImageCallBack != null) {
                set4DImageCallBack.loadImageSuccess();
            }
        }

        provider.getEulerAngles(rotation);
        rotation[2] = Math.min(Math.max(rotation[2], -maxX), maxX);
        int degrees = toDegrees(rotation[2]);
        if (Math.abs(lastDegrees) == maxDegrees && lastDegrees == -degrees) {
            rotation[2] = -rotation[2];
            lastDegrees = -degrees;
        } else {
            lastDegrees = degrees;
        }
        draw(gl);
        f();
    }

    private void draw(GL10 gl) {
        for (int i = 0; i < parallaxModels.size(); i++) {
            ParallaxModel wall = parallaxModels.get(i);
            Integer textureId = glWallpaper.getTexture(i);
            if (textureId != null) {
                float[] trans = translate(wall.getPowerX(), wall.getPowerY());
                gl.glActiveTexture(GLES20.GL_TEXTURE0);
                gl.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
                GLES20.glUniform1i(glWallpaper.inputImageTexture, 0);
                GLES20.glUniformMatrix4fv(glWallpaper.locationMatrix, 1, false, trans, 0);
                gl.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            }
        }
    }

    private int toDegrees(float rad) {
        return (int) (180 * rad / Math.PI);
    }

    public final float[] translate(float multiX, float multiY) {
        float[] fArr3 = new float[16];
        float[] fArr4 = new float[16];
        Matrix.setIdentityM(fArr3, 0);
        float y = -rotation[1] * (1 - translationRatio.y) * multiY * 0.5F;
        float x = rotation[2] * (1 - translationRatio.x) * multiX * 0.5F;
        Matrix.translateM(fArr3, 0, x, y, 0.0f);
        Matrix.multiplyMM(fArr4, 0, mMVPMatrix, 0, fArr3, 0);
        return fArr4;
    }

    public void needUpdate() {
        needUpdate = true;
    }

    private void f() {
        // Set the camera position (View matrix)
        Matrix.setLookAtM(this.viewMatrix, 0, 0.0f, 0.0f, 6f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMM(this.mMVPMatrix, 0, this.projectionMatrix, 0, this.viewMatrix, 0);
    }

    private Bitmap loadBitmapFromStorage(String uri) {
        Context context = mContext.get();
        if (context != null) {
            InputStream ip = null;
            try {
                ip = context.getContentResolver().openInputStream(Uri.parse(uri));
                return BitmapFactory.decodeStream(ip);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (ip != null) {
                    try {
                        ip.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    private Bitmap loadBitmapFromAssets(String url) {
        Context context = mContext.get();
        if (context != null) {
            InputStream ip = null;
            try {
                ip = context.getAssets().open(url);
                return BitmapFactory.decodeStream(ip);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (ip != null) {
                    try {
                        ip.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    private Bitmap loadBitmapFromFile(String path) {
        InputStream ip = null;
        try {
            ip = new FileInputStream(path);
            return BitmapFactory.decodeStream(ip);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ip != null) {
                try {
                    ip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private Bitmap loadBitmapFromUrl(String url) {
        Context context = mContext.get();
        if (context != null) {
            try {
                return Glide.with(context)
                        .asBitmap()
                        .load(url)
                        .submit()
                        .get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setImage(ParallaxModel... wallpapers) {
        this.parallaxModels.clear();
        this.parallaxModels.addAll(Arrays.asList(wallpapers));
        needUpdate();
    }

    public void setImage(List<ParallaxModel> wallpapers) {
        this.parallaxModels.clear();
        this.parallaxModels.addAll(wallpapers);
        needUpdate();
    }

    public void destroy() {
        glWallpaper.destroy();
    }

    public void setProvider(OrientationProvider provider) {
        this.provider = provider;
    }

}
