package com.techpro.parallax.wallpaper.gl;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;


import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class GlUtils {
    public static final int MAX_DATA_BYTES = 10240;

    public static int loadShader(String shaderCode, int type) {
        int[] iArr = new int[1];
        int glCreateShader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(glCreateShader, shaderCode);
        GLES20.glCompileShader(glCreateShader);
        GLES20.glGetShaderiv(glCreateShader, 35713, iArr, 0);
        if (iArr[0] == 0) {
            return 0;
        }
        return glCreateShader;
    }

    public static int glVersion(@NonNull Context context) {
        Object systemService = context.getSystemService(Context.ACTIVITY_SERVICE);
        return ((ActivityManager) systemService).getDeviceConfigurationInfo().reqGlEsVersion;
    }

    public static int loadProgram(@NonNull String strVSource, @NonNull String strFSource) {
        int a2 = loadShader(strVSource, GLES20.GL_VERTEX_SHADER);
        int a3 = loadShader(strFSource, GLES20.GL_FRAGMENT_SHADER);
        int glCreateProgram = GLES20.glCreateProgram();
        int[] iArr = new int[1];
        if (a2 == 0 || a3 == 0) {
            return 0;
        }
        GLES20.glAttachShader(glCreateProgram, a2);
        GLES20.glAttachShader(glCreateProgram, a3);
        GLES20.glLinkProgram(glCreateProgram);
        GLES20.glGetProgramiv(glCreateProgram, 35714, iArr, 0);
        if (iArr[0] <= 0) {
            return 0;
        }
        GLES20.glDeleteShader(a2);
        GLES20.glDeleteShader(a3);
        return glCreateProgram;
    }

    public static int loadTexture(@NonNull Bitmap img) {
        int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
        float f = 9729;
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, MAX_DATA_BYTES, f);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, 10241, f);
        float f2 = 33071;
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, 10242, f2);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, 10243, f2);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, img, 0);
        return textureHandle[0];
    }

    public static boolean supportES2(@NonNull Context context) {
        return glVersion(context) >= 131072;
    }


    public static FloatBuffer asFloatBuffer(float [] array) {
        FloatBuffer buffer = newFloatBuffer(array.length);
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }

    public static FloatBuffer newFloatBuffer(int size) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.position(0);
        return buffer;
    }
}
