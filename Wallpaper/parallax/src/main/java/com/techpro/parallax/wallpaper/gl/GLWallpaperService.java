package com.techpro.parallax.wallpaper.gl;

import android.content.Context;
import android.service.wallpaper.WallpaperService;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.techpro.parallax.wallpaper.model.ParallaxModel;

import java.util.ArrayList;
import java.util.List;

public abstract class GLWallpaperService extends WallpaperService {

    public static final String TAG = "GLWallpaperService";
    private List<GLEngine> listEngines = new ArrayList<>();

    @Override
    public Engine onCreateEngine() {
        return new GLEngine();
    }

    public void reload() {
        for (GLEngine engine : listEngines) {
            engine.reload();
        }
    }

    public abstract List<ParallaxModel> getImage(boolean isPreview);

    class GLEngine extends Engine {

        protected GLSurfaceView surfaceView;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            listEngines.add(this);
            surfaceView = new GLSurfaceView(GLWallpaperService.this);
            Log.d(TAG, "onCreate " + isPreview());
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            reload();
        }

        protected void reload() {
            if (surfaceView != null) surfaceView.setImage(getImage(isPreview()), isPreview());
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                Log.d(TAG, "onResume " + isPreview());
                surfaceView.onResume();
            } else {
                Log.d(TAG, "onPause " + isPreview());
                surfaceView.onPause();
            }
        }

        @Override
        public void onDestroy() {
            listEngines.remove(this);
            super.onDestroy();
            Log.d(TAG, "onDestroy " + isPreview());
            surfaceView.destroy();
        }

        private class GLSurfaceView extends ParallaxSurfaceView {

            public GLSurfaceView(Context context) {
                super(context);
            }

            public GLSurfaceView(Context context, AttributeSet attrs) {
                super(context, attrs);
            }

            @Override
            public SurfaceHolder getHolder() {
                return getSurfaceHolder();
            }
        }
    }

}
