package com.daasuu.gpuv.composer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.*;
import android.util.Log;
import android.view.Surface;

import com.daasuu.gpuv.egl.EglUtil;
import com.daasuu.gpuv.egl.GlFramebufferObject;
import com.daasuu.gpuv.egl.GlPreviewFilter;
import com.daasuu.gpuv.egl.GlSurfaceTexture;
import com.daasuu.gpuv.egl.filter.GlFilter;
import com.daasuu.gpuv.egl.more_filter.FilterTimable;
import com.daasuu.gpuv.player.EffectData;
import com.daasuu.gpuv.player.StickerDrawerData;
import com.daasuu.gpuv.player.StickerInfo;


import java.io.File;
import java.util.ArrayList;

import static android.opengl.GLES20.*;


// Refer : https://android.googlesource.com/platform/cts/+/lollipop-release/tests/tests/media/src/android/media/cts/OutputSurface.java

/**
 * Holds state associated with a Surface used for MediaCodec decoder output.
 * <p>
 * The (width,height) constructor for this class will prepare GL, create a SurfaceTexture,
 * and then create a Surface for that SurfaceTexture.  The Surface can be passed to
 * MediaCodec.configure() to receive decoder output.  When a frame arrives, we latch the
 * texture with updateTexImage, then render the texture with GL to a pbuffer.
 * <p>
 * The no-arg constructor skips the GL preparation step and doesn't allocate a pbuffer.
 * Instead, it just creates the Surface and SurfaceTexture, and when a frame arrives
 * we just draw it on whatever surface is current.
 * <p>
 * By default, the Surface will be using a BufferQueue in asynchronous mode, so we
 * can potentially drop frames.
 */
class DecoderSurface implements SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "DecoderSurface";
    private static final boolean VERBOSE = false;
    private EGLDisplay eglDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext eglContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;
    private Surface surface;
    private Object frameSyncObject = new Object();     // guards frameAvailable
    private boolean frameAvailable;
    private GlFilter filter;

    private int texName;

    private GlSurfaceTexture previewTexture;

    private GlFramebufferObject filterFramebufferObject;
    private GlPreviewFilter previewShader;
    private GlFilter normalShader;
    private GlFramebufferObject framebufferObject;

    private float[] MVPMatrix = new float[16];
    private float[] ProjMatrix = new float[16];
    private float[] MMatrix = new float[16];
    private float[] VMatrix = new float[16];
    private float[] STMatrix = new float[16];


    private Rotation rotation = Rotation.NORMAL;
    private Size outputResolution;
    private Size inputResolution;
    private FillMode fillMode = FillMode.PRESERVE_ASPECT_FIT;
    private FillModeCustomItem fillModeCustomItem;
    private boolean flipVertical = false;
    private boolean flipHorizontal = false;

    private ArrayList<EffectData> mListEffectData;
    private ArrayList<StickerInfo> mListStickerInfo;

    private ArrayList<StickerDrawerData> mListStickerData = new ArrayList<>();

    private Context context;


    /**
     * Creates an DecoderSurface using the current EGL context (rather than establishing a
     * new one).  Creates a Surface that can be passed to MediaCodec.configure().
     */
    DecoderSurface(GlFilter filter) {
        this.filter = filter;
        setup();
    }

    DecoderSurface(Context context, ArrayList<EffectData> listEffectData, ArrayList<StickerInfo> listStickerInfo) {
        this.mListEffectData = listEffectData;
        this.mListStickerInfo = listStickerInfo;
        this.context = context;
        setup();
    }

    DecoderSurface(Context context,GlFilter filter, ArrayList<StickerInfo> listStickerInfo) {
        this.mListStickerInfo = listStickerInfo;
        this.filter = filter;
        this.context = context;
        setup();
    }

    private EffectData getEffectFromDuration(Long duration) {
        for (EffectData effect : mListEffectData) {
            if (effect.getStartTime() < duration && effect.getEndTime() > duration) {
                return effect;
            }
        }
        return null;
    }

    /**
     * Creates instances of TextureRender and SurfaceTexture, and a Surface associated
     * with the SurfaceTexture.
     */
    private void setup() {

        // Even if we don't access the SurfaceTexture after the constructor returns, we
        // still need to keep a reference to it.  The Surface doesn't retain a reference
        // at the Java level, so if we don't either then the object can get GCed, which
        // causes the native finalizer to run.

        // if (VERBOSE) Log.d(TAG, "textureID=" + filter.getTextureId());
        // surfaceTexture = new SurfaceTexture(filter.getTextureId());

        // This doesn't work if DecoderSurface is created on the thread that CTS started for
        // these test cases.
        //
        // The CTS-created thread has a Looper, and the SurfaceTexture constructor will
        // create a Handler that uses it.  The "frame available" message is delivered
        // there, but since we're not a Looper-based thread we'll never see it.  For
        // this to do anything useful, DecoderSurface must be created on a thread without
        // a Looper, so that SurfaceTexture uses the main application Looper instead.
        //
        // Java language note: passing "this" out of a constructor is generally unwise,
        // but we should be able to get away with it here.

        filter.setup();
        framebufferObject = new GlFramebufferObject();
        normalShader = new GlFilter();
        normalShader.setup();

        for (StickerInfo stickerInfo : mListStickerInfo) {

            StickerDrawer stickerDrawer =new StickerDrawer();

            stickerDrawer.prepare(BitmapFactory.decodeFile(stickerInfo.getPath()));

            mListStickerData.add(new StickerDrawerData(stickerInfo.getStartTime() , stickerInfo.getEndTime() , stickerDrawer));
        }


        final int[] args = new int[1];

        GLES20.glGenTextures(args.length, args, 0);
        texName = args[0];

        // SurfaceTextureを生成
        previewTexture = new GlSurfaceTexture(texName);
        previewTexture.setOnFrameAvailableListener(this);
        surface = new Surface(previewTexture.getSurfaceTexture());

        GLES20.glBindTexture(previewTexture.getTextureTarget(), texName);
        // GL_TEXTURE_EXTERNAL_OES
        //OpenGlUtils.setupSampler(previewTexture.getTextureTarget(), GL_LINEAR, GL_NEAREST);
        EglUtil.setupSampler(previewTexture.getTextureTarget(), GL_LINEAR, GL_NEAREST);

        GLES20.glBindTexture(GL_TEXTURE_2D, 0);

        // GL_TEXTURE_EXTERNAL_OES
        previewShader = new GlPreviewFilter(previewTexture.getTextureTarget());
        previewShader.setup();
        filterFramebufferObject = new GlFramebufferObject();


        Matrix.setLookAtM(VMatrix, 0,
                0.0f, 0.0f, 5.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        );

        GLES20.glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0);


    }


    /**
     * Discard all resources held by this class, notably the EGL context.
     */
    void release() {
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglDestroySurface(eglDisplay, eglSurface);
            EGL14.eglDestroyContext(eglDisplay, eglContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(eglDisplay);
        }
        surface.release();
        previewTexture.release();
        // this causes a bunch of warnings that appear harmless but might confuse someone:
        //  W BufferQueue: [unnamed-3997-2] cancelBuffer: BufferQueue has been abandoned!
        //surfaceTexture.release();
        eglDisplay = EGL14.EGL_NO_DISPLAY;
        eglContext = EGL14.EGL_NO_CONTEXT;
        eglSurface = EGL14.EGL_NO_SURFACE;
        if (filter != null) {
            filter.release();
            filter = null;
        }

        surface = null;
        previewTexture = null;
    }

    /**
     * Returns the Surface that we draw onto.
     */
    Surface getSurface() {
        return surface;
    }

    /**
     * Latches the next buffer into the texture.  Must be called from the thread that created
     * the DecoderSurface object, after the onFrameAvailable callback has signaled that new
     * data is available.
     */
    void awaitNewImage() {
        final int TIMEOUT_MS = 10000;
        synchronized (frameSyncObject) {
            while (!frameAvailable) {
                try {
                    // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                    // stalling the test if it doesn't arrive.
                    frameSyncObject.wait(TIMEOUT_MS);
                    if (!frameAvailable) {
                        // TODO: if "spurious wakeup", continue while loop
                        throw new RuntimeException("Surface frame wait timed out");
                    }
                } catch (InterruptedException ie) {
                    // shouldn't happen
                    throw new RuntimeException(ie);
                }
            }
            frameAvailable = false;
        }
        // Latch the data.
        //  GlUtils.checkGlError("before updateTexImage");
        previewTexture.updateTexImage();
        previewTexture.getTransformMatrix(STMatrix);
    }


    /**
     * Draws the data from SurfaceTexture onto the current EGL surface.
     */

    private float count = 0;

    void drawImage(Long duration) {
        count += 1 / 60f;
        framebufferObject.enable();
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());
/*        effectData = this.filter
        if (effectData != null) {
            GlFilter ft = effectData.getFilter();
            if (filter == null) {
                filter = ft;
                filter.setup();
            } else if (!ft.filterName.equals(filter.filterName)) {
                filter.release();
                filter = null;
                filter = ft;
                filter.setup();
            }
            filter.setFrameSize(outputResolution.getWidth(), outputResolution.getHeight());
        } else {
            if (filter != null) {
                filter.release();
                filter = null;
            }
        }*/
        if (filter != null) {
            filterFramebufferObject.enable();
            GLES20.glViewport(0, 0, filterFramebufferObject.getWidth(), filterFramebufferObject.getHeight());
        }

        GLES20.glClear(GL_COLOR_BUFFER_BIT);

        Matrix.multiplyMM(MVPMatrix, 0, VMatrix, 0, MMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, ProjMatrix, 0, MVPMatrix, 0);

        float scaleDirectionX = flipHorizontal ? -1 : 1;
        float scaleDirectionY = flipVertical ? -1 : 1;

        float scale[];
        switch (fillMode) {
            case PRESERVE_ASPECT_FIT:
                scale = FillMode.getScaleAspectFit(rotation.getRotation(), inputResolution.getWidth(), inputResolution.getHeight(), outputResolution.getWidth(), outputResolution.getHeight());

                // Log.d(TAG, "scale[0] = " + scale[0] + " scale[1] = " + scale[1]);

                Matrix.scaleM(MVPMatrix, 0, scale[0] * scaleDirectionX, scale[1] * scaleDirectionY, 1);
                if (rotation != Rotation.NORMAL) {
                    Matrix.rotateM(MVPMatrix, 0, -rotation.getRotation(), 0.f, 0.f, 1.f);
                }
                break;
            case PRESERVE_ASPECT_CROP:
                scale = FillMode.getScaleAspectCrop(rotation.getRotation(), inputResolution.getWidth(), inputResolution.getHeight(), outputResolution.getWidth(), outputResolution.getHeight());
                Matrix.scaleM(MVPMatrix, 0, scale[0] * scaleDirectionX, scale[1] * scaleDirectionY, 1);
                if (rotation != Rotation.NORMAL) {
                    Matrix.rotateM(MVPMatrix, 0, -rotation.getRotation(), 0.f, 0.f, 1.f);
                }
                break;
            case CUSTOM:
                if (fillModeCustomItem != null) {
                    Matrix.translateM(MVPMatrix, 0, fillModeCustomItem.getTranslateX(), -fillModeCustomItem.getTranslateY(), 0f);
                    scale = FillMode.getScaleAspectCrop(rotation.getRotation(), inputResolution.getWidth(), inputResolution.getHeight(), outputResolution.getWidth(), outputResolution.getHeight());

                    if (fillModeCustomItem.getRotate() == 0 || fillModeCustomItem.getRotate() == 180) {
                        Matrix.scaleM(MVPMatrix,
                                0,
                                fillModeCustomItem.getScale() * scale[0] * scaleDirectionX,
                                fillModeCustomItem.getScale() * scale[1] * scaleDirectionY,
                                1);
                    } else {
                        Matrix.scaleM(MVPMatrix,
                                0,
                                fillModeCustomItem.getScale() * scale[0] * (1 / fillModeCustomItem.getVideoWidth() * fillModeCustomItem.getVideoHeight()) * scaleDirectionX,
                                fillModeCustomItem.getScale() * scale[1] * (fillModeCustomItem.getVideoWidth() / fillModeCustomItem.getVideoHeight()) * scaleDirectionY,
                                1);
                    }

                    Matrix.rotateM(MVPMatrix, 0, -(rotation.getRotation() + fillModeCustomItem.getRotate()), 0.f, 0.f, 1.f);

//                    Log.d(TAG, "inputResolution = " + inputResolution.getWidth() + " height = " + inputResolution.getHeight());
//                    Log.d(TAG, "out = " + outputResolution.getWidth() + " height = " + outputResolution.getHeight());
//                    Log.d(TAG, "rotation = " + rotation.getRotation());
//                    Log.d(TAG, "scale[0] = " + scale[0] + " scale[1] = " + scale[1]);


                }
            default:
                break;
        }


        previewShader.draw(texName, MVPMatrix, STMatrix, 1f);

        if (filter != null) {
            // 一度shaderに描画したものを、fboを利用して、drawする。drawには必要なさげだけど。
            framebufferObject.enable();
            GLES20.glClear(GL_COLOR_BUFFER_BIT);
            if (filter instanceof FilterTimable) {
                ((FilterTimable) filter).setTime(count);
            }
            filter.draw(filterFramebufferObject.getTexName(), framebufferObject);
        }


        ////////////////////////////////////////////////////////////////////////////////////

        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());

        GLES20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        normalShader.draw(framebufferObject.getTexName(), null);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        for (StickerDrawerData stickerData: mListStickerData) {
            if (stickerData.getStartOffset() <= duration && stickerData.getEndOffset() >= duration) {
                stickerData.getStickerDrawer().drawFrame();
            }
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture st) {
        if (VERBOSE) Log.d(TAG, "new frame available");
        synchronized (frameSyncObject) {
            if (frameAvailable) {
                throw new RuntimeException("frameAvailable already set, frame could be dropped");
            }
            frameAvailable = true;
            frameSyncObject.notifyAll();
        }
    }

    void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }


    void setOutputResolution(Size resolution) {
        this.outputResolution = resolution;
    }

    void setFillMode(FillMode fillMode) {
        this.fillMode = fillMode;
    }

    void setInputResolution(Size resolution) {
        this.inputResolution = resolution;
    }

    void setFillModeCustomItem(FillModeCustomItem fillModeCustomItem) {
        this.fillModeCustomItem = fillModeCustomItem;
    }

    void setFlipVertical(boolean flipVertical) {
        this.flipVertical = flipVertical;
    }

    void setFlipHorizontal(boolean flipHorizontal) {
        this.flipHorizontal = flipHorizontal;
    }

    void completeParams() {
        int width = outputResolution.getWidth();
        int height = outputResolution.getHeight();
        framebufferObject.setup(width, height);
        normalShader.setFrameSize(width, height);

        filterFramebufferObject.setup(width, height);
        previewShader.setFrameSize(width, height);
        // MCLog.d("onSurfaceChanged width = " + width + " height = " + height + " aspectRatio = " + scaleRatio);
        Matrix.frustumM(ProjMatrix, 0, -1f, 1f, -1, 1, 5, 7);
        Matrix.setIdentityM(MMatrix, 0);

        if (filter != null) {
            filter.setFrameSize(width, height);
        }

    }
}
