package com.parallax.hdvideo.wallpapers.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.utils.dpToPx


/**
 * Created by DatTV on 12,July,2020
 * trandatbkhn@gmail.com
 * Copyright © 2020 Dat Tran. All rights reserved.
 */
class RotationImageView : View {

    private var angleRota = 90f
    private var valueAnimator: ValueAnimator? = null
    private var matrix = Matrix()
    private var curBitmap: Bitmap? = null
    private var sizeImage = 0
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }



    override fun onSaveInstanceState(): Parcelable? {
        // Obtain any state that our super class wants to save.
        val parentState = super.onSaveInstanceState()

        // Wrap our super class's state with our own.
        val curState = SavedState(parentState)
        curState.index = this.angleRota
        curState.name = "this.index"

        // Return our state along with our super class's state.
         return curState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RotationImageView)
            val drawableId = typedArray.getResourceId(R.styleable.RotationImageView_image, R.drawable.ic_loading_re)
            sizeImage = typedArray.getDimensionPixelOffset(R.styleable.RotationImageView_size, 0)
            if (drawableId != 0) {
                curBitmap = getBitmapFromDrawable(context, drawableId)
            }
            typedArray.recycle()
        }
        if (sizeImage <=0) {
            sizeImage = dpToPx(48f)
        }
        setImage(curBitmap ?: getBitmapFromDrawable(context, R.drawable.ic_loading_re))
        start()
    }

    fun setImage(bitmap: Bitmap) {
        if (sizeImage > 0) {
            this.curBitmap = Bitmap.createScaledBitmap(bitmap, sizeImage, sizeImage, true)
        } else {
            this.curBitmap = bitmap
        }
        invalidate()
    }

    fun setImage(@DrawableRes resId: Int) {
        curBitmap = getBitmapFromDrawable(context, resId)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        curBitmap?.also {
            matrix.reset()
            matrix.postTranslate(-it.width / 2f, -it.height / 2f) // Centers image
            matrix.postRotate(angleRota)
            matrix.postTranslate(width / 2f, height / 2f)
            canvas.drawBitmap(it, matrix, null)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }

    fun start() {
        stop()
        val valueAnimator1 = ValueAnimator.ofFloat(0f, 360f).setDuration(1000)
        valueAnimator1.repeatCount = ValueAnimator.INFINITE
        valueAnimator1.repeatMode = ValueAnimator.RESTART
        valueAnimator1.interpolator = LinearInterpolator()
        valueAnimator1.addUpdateListener { animation: ValueAnimator ->
            setAngle(animation.animatedValue as Float)
        }
        this.valueAnimator = valueAnimator1
        valueAnimator1.start()
    }

    fun release() {
        stop()
        matrix.reset()
        curBitmap = null
        valueAnimator = null
    }

    fun stop() {
        valueAnimator?.removeAllUpdateListeners()
        valueAnimator?.cancel()
    }

    fun setAngle(angle: Float) {
        this.angleRota = angle
        invalidate()
    }

    companion object {

        fun getBitmapFromDrawable(context: Context, @DrawableRes drawableId: Int): Bitmap {
            val drawable = AppCompatResources.getDrawable(context, drawableId)
            return if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else if (drawable is VectorDrawableCompat || drawable is VectorDrawable) {
                val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            } else {
                throw IllegalArgumentException("RotationImageView unsupported drawable type")
            }
        }
    }

    private class SavedState : BaseSavedState {
        var name: String? = null
        var index = 0f

        internal constructor(superState: Parcelable?) : super(superState) {}
        private constructor(`in`: Parcel) : super(`in`) {
            name = `in`.readString()
            index = `in`.readFloat()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(name)
            out.writeFloat(index)
        }
    }
}