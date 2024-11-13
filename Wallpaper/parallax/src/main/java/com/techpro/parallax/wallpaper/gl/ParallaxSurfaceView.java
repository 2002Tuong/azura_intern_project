package com.techpro.parallax.wallpaper.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.techpro.parallax.wallpaper.model.ParallaxModel;
import com.techpro.parallax.wallpaper.orientationProvider.OrientationProvider;

import java.util.List;

public class ParallaxSurfaceView extends GLSurfaceView {

    private GLWallpaperRenderer renderer;
    private OrientationProvider orientationProvider;

    Set4DImageCallBack set4DImageCallBack;

    public ParallaxSurfaceView(Context context) {
        this(context, null);
    }

    public ParallaxSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void set4DImageSuccessListener(Set4DImageCallBack set4DImageCallBack) {
        this.set4DImageCallBack = set4DImageCallBack;
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        setPreserveEGLContextOnPause(true);
        renderer = new GLWallpaperRenderer(context);
        setRenderer(renderer);
        orientationProvider = OrientationProvider.bestProvider(context);
        renderer.setProvider(orientationProvider);
    }

    @Override
    public void onResume() {
        super.onResume();
        orientationProvider.start();
    }

    public void reload() {
        renderer.needUpdate();
    }

    public void setImage(ParallaxModel... wallpapers) {
        if (wallpapers.length > 0)
            renderer.setImage(wallpapers);
    }

    public void setImage(List<ParallaxModel> wallpapers, Boolean isPreview) {
        if (!wallpapers.isEmpty()) {
            renderer.setImage(wallpapers);

            if (!isPreview) {
                renderer.set4DImageSuccessListener(new Set4DImageCallBack() {

                    @Override
                    public void loadImageSuccess() {
                        if (set4DImageCallBack != null) {
                            set4DImageCallBack.loadImageSuccess();
                            Log.d("Callback", "set 4D Image success");
                        }
                    }

                    @Override
                    public void loadImageError() {
                        if (set4DImageCallBack != null) {
                            set4DImageCallBack.loadImageError();
                            Log.d("Callback", "set 4D Image error");
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        orientationProvider.stop();
    }

    public void clearSensorListener(){
        orientationProvider.unregisterListener();
    }

    public void destroy() {
        renderer.destroy();
    }

    @Override
    public SurfaceHolder getHolder() {
        return super.getHolder();
    }
}
