package com.daasuu.gpuv.egl.more_filter.filters;

import com.daasuu.gpuv.egl.filter.GlFilter;
import com.daasuu.gpuv.egl.more_filter.FilterUtilsKt;

public class GlMirrorFilter extends GlFilter {
    private static final String FRAGMENT_SHADER = "\n            precision mediump float;\n\n            varying highp vec2 vTextureCoord;\n            uniform lowp sampler2D sTexture;\n\n            uniform highp float verticalMirror;\n            uniform highp float horizontalMirror;\n            void main() {\n                highp vec2 p = vTextureCoord;\n\n                if (verticalMirror < 0.0) {\n                    if (p.x > -verticalMirror) {\n                        p.x = -2.0 * verticalMirror - p.x;\n                    }\n                } else if (verticalMirror > 0.0) {\n                    if (p.x < verticalMirror) {\n                        p.x = 2.0 * verticalMirror - p.x;\n                    }\n                }\n                if (horizontalMirror < 0.0) {\n                    if (p.y > -horizontalMirror) {\n                        p.y = -2.0 * horizontalMirror - p.y;\n                    }\n                } else if (horizontalMirror > 0.0) {\n                    if (p.y < horizontalMirror) {\n                        p.y = 2.0 * horizontalMirror - p.y;\n                    }\n                }\n\n                if (p.x > 1.0 || p.x < 0.0 || p.y > 1.0 || p.y < 0.0) {\n                    gl_FragColor = vec4(0.0);\n                } else {\n                    gl_FragColor = texture2D(sTexture, p);\n                }\n            }\n        ";
    private final float horizontalMirror;
    private final float verticalMirror;

/*    @Metadata(mo21616bv = {1, 0, 3}, mo21617d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002J\u0006\u0010\u0005\u001a\u00020\u0006J\u0006\u0010\u0007\u001a\u00020\u0006J\u0006\u0010\b\u001a\u00020\u0006J\u0006\u0010\t\u001a\u00020\u0006R\u000e\u0010\u0003\u001a\u00020\u0004XT¢\u0006\u0002\n\u0000¨\u0006\n"}, mo21618d2 = {"Leditor/video/motion/fast/slow/filter/filters/MirrorFilter$Companion;", "", "()V", "FRAGMENT_SHADER", "", "bottomToTop", "Leditor/video/motion/fast/slow/filter/filters/MirrorFilter;", "leftToRight", "rightToLeft", "topToBottom", "app_worldRelease"}, mo21619k = 1, mo21620mv = {1, 1, 15})
    *//* compiled from: MirrorFilter.kt *//*
    public static final class Companion {
        private Companion() {
        }

        public *//* synthetic *//* Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final MirrorFilter leftToRight() {
            return new MirrorFilter(-0.5f, 0.0f);
        }

        @NotNull
        public final MirrorFilter rightToLeft() {
            return new MirrorFilter(0.0f, -0.5f);
        }

        @NotNull
        public final MirrorFilter topToBottom() {
            return new MirrorFilter(0.0f, 0.5f);
        }

        @NotNull
        public final MirrorFilter bottomToTop() {
            return new MirrorFilter(-0.5f, 0.0f);
        }
    }*/

    public GlMirrorFilter() {
        this(0.0f, -0.5f);
        filterName = "Mirror";
    }

    public static GlMirrorFilter bottomToTop() {
        GlMirrorFilter glMirrorFilter = new GlMirrorFilter(-0.5f, 0.0f);
        glMirrorFilter.filterName = "MirV1";
        return glMirrorFilter;
    }

    public static GlMirrorFilter topToBottom() {
        GlMirrorFilter glMirrorFilter = new GlMirrorFilter(0.0f, 0.5f);
        glMirrorFilter.filterName = "MirV2";
        return glMirrorFilter;
    }

    public static GlMirrorFilter rightToLeft() {
        GlMirrorFilter glMirrorFilter = new GlMirrorFilter(0.0f, -0.5f);
        glMirrorFilter.filterName = "MirH1";
        return glMirrorFilter;
    }

    public static GlMirrorFilter leftToRight() {
        GlMirrorFilter glMirrorFilter = new GlMirrorFilter(-0.5f, 0.0f);
        glMirrorFilter.filterName = "MirH2";
        return glMirrorFilter;
    }

    public static GlMirrorFilter moreMirror() {
        GlMirrorFilter glMirrorFilter = new GlMirrorFilter(-0.5f, 0.5f);
        glMirrorFilter.filterName = "MirM";
        return glMirrorFilter;
    }

    public GlMirrorFilter(float f, float f2) {
        super("attribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying highp vec2 vTextureCoord;\nvoid main() {\ngl_Position = aPosition;\nvTextureCoord = aTextureCoord.xy;\n}\n", FRAGMENT_SHADER);
        this.verticalMirror = f;
        this.horizontalMirror = f2;
    }

    public void onDraw() {
        FilterUtilsKt.setFloat(getHandle("verticalMirror"), this.verticalMirror);
        FilterUtilsKt.setFloat(getHandle("horizontalMirror"), this.horizontalMirror);
    }
}
