package com.daasuu.gpuv.egl.more_filter.filters;

import com.daasuu.gpuv.egl.filter.GlFilter;
import com.daasuu.gpuv.egl.more_filter.FilterUtilsKt;
import com.daasuu.gpuv.egl.more_filter.Orientation;

public class GlTvShopFilter extends GlFilter {

    private static final String FRAGMENT_SHADER = "\nprecision highp float;\nvarying vec2 vTextureCoord;\nuniform sampler2D sTexture;\nuniform int orientation;\nvoid main() {\n    gl_FragColor = texture2D(sTexture, fract(vTextureCoord*exp2(ceil(-log2(orientation == 1 ? vTextureCoord.x : vTextureCoord.y)))));\n}";
    private int orientation;

   // @Metadata(mo21616bv = {1, 0, 3}, mo21617d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002¢\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004XT¢\u0006\u0002\n\u0000¨\u0006\u0005"}, mo21618d2 = {"Leditor/video/motion/fast/slow/filter/filters/TvShopFilter$Companion;", "", "()V", "FRAGMENT_SHADER", "", "app_worldRelease"}, mo21619k = 1, mo21620mv = {1, 1, 15})
    /* compiled from: TvShopFilter.kt */

    public GlTvShopFilter() {
        super("attribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying highp vec2 vTextureCoord;\nvoid main() {\ngl_Position = aPosition;\nvTextureCoord = aTextureCoord.xy;\n}\n", FRAGMENT_SHADER);
        filterName = "Tv Show";
    }

    public void onDraw() {
        super.onDraw();
        FilterUtilsKt.setInteger(getHandle("orientation"), this.orientation);
    }

    public void setFrameSize(int i, int i2) {
        this.orientation = Orientation.INSTANCE.getOrientation(i, i2);
    }

}
