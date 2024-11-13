package com.daasuu.gpuv.egl.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.daasuu.gpuv.egl.filter.*
import com.daasuu.gpuv.egl.more_filter.filters.*

object Utils {
    fun createViewFilter(videoFilterType: VideoFilterType): GlFilter {
        return when (videoFilterType) {

            VideoFilterType.DEFAULT ->  GlFilter()
            /*VideoFilterType.LOOK_UP_TABLE_SAMPLE ->   GlLookUpTableFilter(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.a1
                )
            )*/
            VideoFilterType.BILATERAL_BLUR ->  GlBilateralFilter()
            VideoFilterType.BOX_BLUR ->  GlBoxBlurFilter()
            //VideoFilterType.BRIGHTNESS ->  GlBrightnessFilter().apply { setBrightness(0.2f) }
            VideoFilterType.BULGE_DISTORTION ->  GlBulgeDistortionFilter()
            VideoFilterType.CGA_COLORSPACE ->  GlCGAColorspaceFilter()
            //VideoFilterType.CONTRAST ->  GlContrastFilter().apply { setContrast(2.5f) }
            VideoFilterType.CROSSHATCH ->  GlCrosshatchFilter()
            VideoFilterType.EXPOSURE ->  GlExposureFilter()
            //VideoFilterType.GAMMA ->   GlGammaFilter().apply { setGamma(2f) }
            VideoFilterType.GAUSSIAN_FILTER ->   GlGaussianBlurFilter()
            VideoFilterType.GRAY_SCALE ->   GlGrayScaleFilter()
            VideoFilterType.HALFTONE ->   GlHalftoneFilter()
            VideoFilterType.HAZE ->   GlHazeFilter()
            //VideoFilterType.HIGHLIGHT_SHADOW ->   GlHighlightShadowFilter()
            //VideoFilterType.HUE ->   GlHueFilter()
            VideoFilterType.INVERT ->   GlInvertFilter()

            VideoFilterType.LUMINANCE ->   GlLuminanceFilter()
            VideoFilterType.LUMINANCE_THRESHOLD ->   GlLuminanceThresholdFilter()
            //VideoFilterType.MONOCHROME ->   GlMonochromeFilter()
            //VideoFilterType.OPACITY ->   GlOpacityFilter()
            //VideoFilterType.OVERLAY ->   GlOverlayFilter()
            VideoFilterType.PIXELATION ->   GlPixelationFilter()
            VideoFilterType.POSTERIZE ->   GlPosterizeFilter()
            VideoFilterType.RGB ->   GlRGBFilter().apply { setRed(0f) }
            //VideoFilterType.SATURATION ->   GlSaturationFilter()
            VideoFilterType.SEPIA ->   GlSepiaFilter()
            //VideoFilterType.SHARP ->   GlSharpenFilter().apply { sharpness = 4f }
            VideoFilterType.SOLARIZE ->   GlSolarizeFilter()
            VideoFilterType.SPHERE_REFRACTION ->   GlSphereRefractionFilter().apply { setRadius(0.25f)
                setCenterX(0.5f)
                setCenterY(0.5f)
                setAspectRatio(1f)}
            VideoFilterType.SWIRL ->   GlSwirlFilter()
            //VideoFilterType.TONE_CURVE_SAMPLE ->   GlToneCurveFilter()
            VideoFilterType.TONE ->   GlToneFilter()
            VideoFilterType.VIBRANCE ->   GlVibranceFilter().apply { setVibrance(3f) }
            VideoFilterType.VIGNETTE ->   GlVignetteFilter()
            //VideoFilterType.WATERMARK ->   GlWatermarkFilter()
            //VideoFilterType.WEAK_PIXEL ->   GlWeakPixelInclusionFilter()
            /*       VideoFilterType.WHITE_BALANCE ->   GlWhiteBalanceFilter().apply {
                       setTemperature(2400f)
                       setTint(2f)
                   }*/
            VideoFilterType.ZOOM_BLUR ->   GlZoomBlurFilter()
            //VideoFilterType.BITMAP_OVERLAY_SAMPLE ->   GlBitmapOverlaySample()
            VideoFilterType.ANAGLYPH ->   GlAnaglyphFilter()

            VideoFilterType.TV_SHOW ->   GlTvShopFilter()
            VideoFilterType.ASCIART ->   GlAsciArtFilter()
            VideoFilterType.BEVELED -> GlBeveledFilter()
            VideoFilterType.BINARY_GLITCH -> GlBinaryGlitchEffectFilter()
            VideoFilterType.BLACK_BODY -> GlBlackBodyFilter()
            VideoFilterType.BLEACH -> GlBleachFilter()
            VideoFilterType.BLUE -> GlBlueFilter()
            VideoFilterType.BOKEH -> GlBokehFilter()
            //VideoFilterType.BW_LIGHT -> GlBwLightFilter()
            VideoFilterType.BW_STROBE -> GlBwStrobeFilter()
            //VideoFilterType.COLOR_BALANCE -> GlColorBalanceFilter()
            //VideoFilterType.COLOR_WHEEL -> GlColorWheelFilter()
            VideoFilterType.COMMODORE -> GlCommodoreFilter()
           // VideoFilterType.CONVOLUTION -> GlConvolutionFilter()
            VideoFilterType.CROSS_HATCHING -> GlCrosshatchingFilter()
            VideoFilterType.CROSS_STITCHING -> GlCrossStitchingFilter()
            VideoFilterType.CRT -> GlCrtFilter()
            VideoFilterType.DAWN_BRINGER -> GlDawnbringerFilter()
            VideoFilterType.DISPERSION -> GlDispersionFilter()
            VideoFilterType.DROSTE -> GlDrosteFilter()
            VideoFilterType.DRUNK -> GlDrunkFilter()
            //VideoFilterType.EDGES -> GlEdgesFilter()
            VideoFilterType.FALSE_COLOR -> GlFalseColorFilter()
            VideoFilterType.FIRE -> GlFireFilter()
            VideoFilterType.FISH_EYE -> GlFisheyeFilter()
            VideoFilterType.FIXED_TONE -> GlFixedToneFilter()
            VideoFilterType.FRESNEL -> GlFresnelFilter()
            VideoFilterType.FROSTED -> GlFrostedGlassFilter()
            VideoFilterType.GAME_BOY -> GlGameboyFilter()
            VideoFilterType.GLITCH -> GlGlitchEffect()
            //VideoFilterType.GLOOMY -> GlGloomyFilter()
            VideoFilterType.HIGH_SPEED -> GlHighSpeedFilter()
            //VideoFilterType.KUWAHARA -> GlKuwaharaFilter()
            VideoFilterType.LAPLACE -> GlLaplaceFilter()
            VideoFilterType.LOW_QUALITY -> GlLowQualityFilter()
            //VideoFilterType.LSD -> GlLsdFilter()
            VideoFilterType.MATRIX -> GlMatrixFilter()
            VideoFilterType.MIRROR -> GlMirrorFilter.leftToRight()
            VideoFilterType.MIRROR_01 -> GlMirrorFilter.rightToLeft()
            VideoFilterType.MIRROR_02 -> GlMirrorFilter.topToBottom()
            VideoFilterType.MIRROR_03 -> GlMirrorFilter.bottomToTop()
            VideoFilterType.MIRROR_04 -> GlMirrorFilter.moreMirror()
            VideoFilterType.MOLTEN -> GlMoltenGoldFilter()
            VideoFilterType.NIGHT_VISION -> GlNightVisionFilter()
            //VideoFilterType.NOISY_MIRROR -> GlNoisyMirrorEffect()
            VideoFilterType.OLD_MOVIE -> GlOldMovieFilter()
            VideoFilterType.ORANGE_TEAL -> GlOrangeTealFilter()
           // VideoFilterType.PLASMA -> GlPlasmaFilter()
            VideoFilterType.POLYGON -> GlPolygonsFilter()
            VideoFilterType.POSTERIZATION -> GlPosterizationFilter()
           // VideoFilterType.PSYCHEDELIC -> GlPsychedelicFilter()
            VideoFilterType.RADIAL_BLUR -> GlRadialBlurFilter()
            VideoFilterType.RAIN -> GlRainFilter()
            VideoFilterType.SEVENTY -> GlSeventyFilter()
           // VideoFilterType.SKETCH -> GlSketchFilter()
            VideoFilterType.SMOOTH_TONE -> GlSmoothToneFilter()
            VideoFilterType.SNOW -> GlSnowFilter()
            //VideoFilterType.SOBER -> GlSoberEdgeFilter()
            VideoFilterType.SOLARIZATION -> GlSolarizationFilter()
            VideoFilterType.SPIRALS -> GlSpiralsFilter()
            VideoFilterType.SPLIT -> GlSplitColorFilter()
            VideoFilterType.SPY_GLASS -> GlSpyGlassFilter()
            VideoFilterType.THERMAL -> GlThermalFilter()
            VideoFilterType.TILES -> GlTilesFilter()
            VideoFilterType.ULTRA_VIOLET -> GlUltravioletFilter()
            VideoFilterType.WARP -> GlWarpFilter()
            VideoFilterType.WAVY -> GlWavyFilter()
            VideoFilterType.WISP -> GlWispFilter()
        }
    }

    fun getVideoFilterType(): Array<VideoFilterType> {
        return VideoFilterType.values()
    }

    fun getBitmapFromAsset(path: String, context: Context): Bitmap {
        val inputStream = context.assets.open(path)
        return BitmapFactory.decodeStream(inputStream)
    }

    fun getLookupFilter(context: Context, lookupType: LookupType) :GlLookUpTableFilter {
        val bitmap = getBitmapFromAsset("luts/$lookupType.jpg", context)
        return GlLookUpTableFilter(bitmap)
    }


}