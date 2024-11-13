package com.techpro.parallax.wallpaper.gl;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class GLWallpaper {

    protected int program;
    protected int position;
    protected int inputImageTexture;
    protected int inputTextureCoordinate;
    protected int locationMatrix;
    protected int maskTexture;
    protected int gyro;
    private FloatBuffer vertexBuffer;
    private FloatBuffer vertexBuffer2;
    public static final float[] G = {-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f};
    public static final float[] F = {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f};
    private Map<Integer, Integer> data = new HashMap<>();

    public GLWallpaper() {
        vertexBuffer = GlUtils.asFloatBuffer(G);
        vertexBuffer2 = GlUtils.asFloatBuffer(F);
    }

    private final String GL_VERTEX_SHADER = "attribute vec4 position;" +
            "\nuniform mat4 locationMatrix;" +
            "\nattribute vec2 inputTextureCoordinate;" +
            "\nvarying vec2 textureCoordinate;" +
            "\nvoid main() {" +
            "\n    gl_Position = locationMatrix * position;" +
            "\n    textureCoordinate = inputTextureCoordinate;" +
            "\n}";
    private final String FRAGMENT_SHADER_CODE = "precision mediump float;" +
            "\nuniform sampler2D inputImageTexture;" +
            "\nvarying vec2 textureCoordinate;" +
            "\nuniform sampler2D maskTexture;" +
            "\nuniform vec2 gyro;" +
            "\nvoid main() {" +
            "\n    vec4 mapColor = texture2D(maskTexture, textureCoordinate);" +
            "\n    vec2 displacement = vec2(gyro * mapColor.g);" +
            "\n    gl_FragColor = texture2D(inputImageTexture, textureCoordinate + displacement);" +
            "\n    if(gl_FragColor.a == 0.0) {" +
            "\n        discard;" +
            "\n    }" +
            "\n}";

    public void initGl() {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(1, 771);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        program = GlUtils.loadProgram(GL_VERTEX_SHADER, FRAGMENT_SHADER_CODE);
        position = GLES20.glGetAttribLocation(program, "position");
        inputImageTexture = GLES20.glGetUniformLocation(program, "inputImageTexture");
        inputTextureCoordinate = GLES20.glGetAttribLocation(program, "inputTextureCoordinate");
        locationMatrix = GLES20.glGetUniformLocation(program, "locationMatrix");
        maskTexture = GLES20.glGetUniformLocation(program, "maskTexture");
        gyro = GLES20.glGetUniformLocation(program, "gyro");
        GLES20.glUseProgram(program);
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(position, 2, 5126, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(position);
        GLES20.glEnableVertexAttribArray(gyro);
        vertexBuffer2.position(0);
        GLES20.glVertexAttribPointer(inputTextureCoordinate, 2, 5126, false, 0, vertexBuffer2);
        GLES20.glEnableVertexAttribArray(inputTextureCoordinate);
    }

    public synchronized void loadTexture(int position, Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled())
            data.put(position, GlUtils.loadTexture(bitmap));
    }

    public synchronized void clearTextures() {
        for (Map.Entry<Integer, Integer> entry : data.entrySet()) {
            GLES20.glDeleteTextures(1, new int[]{entry.getValue()}, 0);
        }
        data.clear();
    }

    public Integer getTexture(int position) {
        return data.get(position);
    }

    public int size() {
        return data.size();
    }

    public void destroy() {
        GLES20.glDeleteProgram(program);
        clearTextures();
    }
}
