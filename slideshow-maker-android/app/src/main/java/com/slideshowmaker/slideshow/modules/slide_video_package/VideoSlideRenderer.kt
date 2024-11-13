package com.slideshowmaker.slideshow.modules.slide_video_package

import android.graphics.Point
import android.media.MediaMetadataRetriever
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Size
import com.slideshowmaker.slideshow.modules.theme.SlideThemeDrawer
import com.slideshowmaker.slideshow.modules.theme.data.ThemeData
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class VideoSlideRenderer(val mVideoSlideGLView: VideoSlideGLView)  : GLSurfaceView.Renderer {



    private var themeDrawer: SlideThemeDrawer? = null
    private var videoDrawer: VideoDrawer? = null

    private var _themeData = ThemeData()
    val themeData get() = _themeData

    private val videoDataList = ArrayList<VideoDataForSlide>()

    private var totalDuration = 0L

    init {
        themeDrawer = SlideThemeDrawer(_themeData)
        videoDrawer = VideoDrawer()
    }

    fun initData(videoList: ArrayList<String>) {
        videoDataList.clear()
        for(path in videoList) {
            val videoDataForSlide = getDataForSlide(path)
            videoDataList.add(videoDataForSlide)
            totalDuration+=(videoDataForSlide.endOffset-videoDataForSlide.startOffset)
        }
    }

    fun playByProgress(percent:Float) {
        val tartTime = percent*totalDuration

    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        videoDrawer?.prepare()
        themeDrawer?.prepare()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        videoDrawer?.drawFrame()
        themeDrawer?.drawFrame()
    }

    fun changeTheme(themeData: ThemeData) {
        _themeData = themeData
        themeDrawer?.changeTheme(themeData)
    }

    fun changeVideo(videoPath:String, size: Size, point:Point) {
        videoDrawer?.changeVideo(videoPath, size, point)
    }

    private fun getDataForSlide(videoPath: String): VideoDataForSlide {
        val mediaData = MediaMetadataRetriever()
        mediaData.setDataSource(videoPath)
        val rotation = mediaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toInt()
        val videoWidth:Int
        val videoHeight:Int
        if (rotation == 90) {
            videoWidth = (mediaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) ?:"-1").toInt()
            videoHeight = (mediaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) ?: "-1").toInt()
        } else {
            videoWidth = (mediaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) ?: "-1").toInt()
            videoHeight = (mediaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) ?: "-1").toInt()
        }
        val size =Size(videoWidth, videoHeight)
        val len = (mediaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) ?: "-1").toLong()
        return VideoDataForSlide(videoPath, 0, len, size)
    }

}