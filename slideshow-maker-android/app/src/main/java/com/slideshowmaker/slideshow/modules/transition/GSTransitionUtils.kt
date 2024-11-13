package com.slideshowmaker.slideshow.modules.transition

import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.modules.transition.transition.*
import com.slideshowmaker.slideshow.utils.RawResourceReader
import com.slideshowmaker.slideshow.utils.extentions.orFalse
import kotlin.random.Random

object GSTransitionUtils {

    enum class TransitionType {
        RANDOM,
        ANGULAR,
        BOUNCE,
        BOW_TIE_HORIZONTAL,
        BOW_TIE_VERTICAL,
        BUTTERFLY_WAVE,
        CANNABIS_LEAF,
        CIRCLE_CROP,
        CIRCLE,
        CIRCLE_OPEN,
        COLOR_PHASE,
        COLOR_DISTANCE,
        CRAZY_PARAMETRIC,
        CROSS_HATCH,
        CROSS_WARP,
        CUBE,
        DIRECTION_WIPE,
        DIRECTION_WARP,
        DOOM_SCREEN,
        DOOR_WAY,
        DREAMY,
        FADE_GRAY_SCALE,
        FLY_EYE,
        GLITCH,
        GRID_FLIP,
        IN_HEART,
        INVERTED_PAGE_CURL,
        KALE_IDO_SCOPE,
        MORPH,
        MOSAIC,
        MULTIPLY_BLEND,
        PER_LIN,
        PIN_WHEEL,
        PIXEL_IZE,
        POLAR_FUN,
        POLKA_DOTS,
        RADIAL,
        RANDOM_SQUARE,
        RIPPLE,
        ROTATE_SCALE_FADE,
        SIMPLE_ZOOM,
        SQUARE_WIRE,
        SQUEEZE,
        SWAP,
        SWIRL,
        UNDULATING_BURN,
        WATER_DROP,
        WIND,
        WINDOW_BLIND,
        WINDOW_SLICE,
        WIPE_DOWN,
        WIPE_LEFT,
        WIPE_UP,
        WIPE_RIGHT,
        ZOOM_IN_CIRCLE
    }

    private fun getTransitionByType(transitionType: TransitionType): GSTransition {
        return when (transitionType) {
            TransitionType.RANDOM -> GSTransition(id = "random")
            TransitionType.POLKA_DOTS -> GSPolkaDotsTransition()
            TransitionType.WIPE_DOWN -> GSWipeDownTransition()
            TransitionType.ANGULAR -> AngularTransition()
            TransitionType.BOUNCE -> BounceTransition()
            TransitionType.BOW_TIE_HORIZONTAL -> BowTieHorizontalTransition()
            TransitionType.BOW_TIE_VERTICAL -> BowTieVerticalTransition()
            TransitionType.BUTTERFLY_WAVE -> ButterflyWaveTransition()
            TransitionType.CANNABIS_LEAF -> CannabisLeafTransition()
            TransitionType.CIRCLE_CROP -> CircleCropTransition()
            TransitionType.CIRCLE -> CircleTransition()
            TransitionType.CIRCLE_OPEN -> CircleOpenTransition()
            TransitionType.COLOR_PHASE -> ColorPhaseTransition()
            TransitionType.COLOR_DISTANCE -> ColorDistanceTransition()
            TransitionType.CRAZY_PARAMETRIC -> CrazyParametricTransition()
            TransitionType.CROSS_HATCH -> CrossHatchTransition()
            TransitionType.CROSS_WARP -> CrossWarpTransition()
            TransitionType.CUBE -> CubeTransition()
            TransitionType.DIRECTION_WIPE -> DirectionWipeTransition()
            TransitionType.DIRECTION_WARP -> DirectionWarpTransition()
            TransitionType.DOOM_SCREEN -> DoomScreenTransition()
            TransitionType.DOOR_WAY -> DoorWayTransition()
            TransitionType.DREAMY -> DreamyTransition()
            TransitionType.FADE_GRAY_SCALE -> FadeGrayScaleTransition()
            TransitionType.FLY_EYE -> FlyEyeTransition()
            TransitionType.GLITCH -> GlitchTransition()
            TransitionType.GRID_FLIP -> GridFlipTransition()
            TransitionType.IN_HEART -> GSInHeartTransition()
            TransitionType.INVERTED_PAGE_CURL -> GSInvertedPageCurlTransition()
            TransitionType.KALE_IDO_SCOPE -> GSKaleIdoScopeTransition()
            TransitionType.MORPH -> GSMorphTransition()
            TransitionType.MOSAIC -> GSMosaicTransition()
            TransitionType.MULTIPLY_BLEND -> GSMultiplyBlendTransition()
            TransitionType.PER_LIN -> GSPerLinTransition()
            TransitionType.PIN_WHEEL -> GSPinWheelTransition()
            TransitionType.PIXEL_IZE -> GSPixelIzeTransition()
            TransitionType.POLAR_FUN -> GSPolarFunTransition()
            TransitionType.RADIAL -> GSRadialTransition()
            TransitionType.RANDOM_SQUARE -> GSRandomSquareTransition()
            TransitionType.RIPPLE -> GSRippleTransition()
            TransitionType.ROTATE_SCALE_FADE -> GSRotateScaleFadeTransition()
            TransitionType.SIMPLE_ZOOM -> GSSimpleZoomTransition()
            TransitionType.SQUARE_WIRE -> GSSquareWireTransition()
            TransitionType.SQUEEZE -> GSSqueezeTransition()
            TransitionType.SWAP -> GSSwapTransition()
            TransitionType.SWIRL -> GSSwirlTransition()
            TransitionType.UNDULATING_BURN -> GSUndulatingBurnTransition()
            TransitionType.WATER_DROP -> GSWaterDropTransition()
            TransitionType.WIND -> GSWindTransition()
            TransitionType.WINDOW_BLIND -> GSWindowBlindTransition()
            TransitionType.WINDOW_SLICE -> GSWindowSliceTransition()
            TransitionType.WIPE_LEFT -> GSWipeLeftTransition()
            TransitionType.WIPE_UP -> GSWipeUpTransition()
            TransitionType.WIPE_RIGHT -> GSWipeRightTransition()
            TransitionType.ZOOM_IN_CIRCLE -> GSZoomCircleTransition()
        }
    }

    fun createFragmentShaderCode(transitionCodeId: Int): String {
        return RawResourceReader.readTextFileFromRawResource(
            VideoMakerApplication.getContext(),
            transitionCodeId
        )
    }

    fun <T> getRandomItems(list: List<T>, n: Int): List<T> {
        if (n <= 0) {
            throw IllegalArgumentException("N must be a positive number.")
        }

        val resList = mutableListOf<T>()
        for (i in 0 until n) {
            val randomElement = list.random(Random)
            resList.add(randomElement)
        }

        return resList
    }

    fun getRandomTransitions(n: Int): List<GSTransition> {
        val gsTransitionList =
            getGSTransitionList().filter { it.id !in GSTransition.RANDOM_IDS }
        return getRandomItems(gsTransitionList, n)
    }

    fun getRandomTransitions(ids: List<String>, n: Int): List<GSTransition> {
        val filteredTransitions = getGSTransitionList().filter { it.id in ids }
        if (filteredTransitions.isEmpty())
            return getRandomTransitions(n)
        return getRandomItems(filteredTransitions, n)
    }


    fun getGSTransitionList(): ArrayList<GSTransition> {
        var gsTransitionList = mutableListOf<GSTransition>()
        val transitionConfig = RemoteConfigRepository.supportTransitionConfig.orEmpty()
        for (value in TransitionType.values()) {
            gsTransitionList.add(getTransitionByType(value))
        }
        val filteredTransitionList = if (transitionConfig.isEmpty()) gsTransitionList else
            gsTransitionList.filter { transitionConfig.any { item -> it.id == item.id } }
                .map { item ->
                    item.apply {
                        isPro = transitionConfig.firstOrNull { it.id == item.id }?.isPro.orFalse()
                        thumbnailUrl =
                            transitionConfig.firstOrNull { it.id == item.id }?.thumbnailUrl.orEmpty()
                        isWatchAds = !isPro && transitionConfig.firstOrNull { it.id == item.id }?.watchVideoAds.orFalse()
                    }
                }.sortedBy { item -> transitionConfig.indexOfFirst { it.id == item.id } }
        return ArrayList(filteredTransitionList)
    }

    fun getAllGSTransitionList(): ArrayList<GSTransition> {
        val resTransition = mutableListOf<GSTransition>()
        resTransition.add(GSTransition(id = "random_1", transitionName = "Random 1"))
        if (!RemoteConfigRepository.randomTransitionConfig.random2?.ids.isNullOrEmpty()) {
            resTransition.add(
                GSTransition(
                    id = "random_2",
                    transitionName = "Random 2",
                    isPro = RemoteConfigRepository.randomTransitionConfig.random2?.isPro.orFalse()
                )
            )
        }
        return ArrayList(resTransition.plus(getGSTransitionList()))
    }
}