package com.example.claptofindphone.presenter.dialog

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.claptofindphone.R

class CustomRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var rate: Int = 0
    private var maxRate: Int = 5
    private var emptyStarDraw: Drawable? = null
    private var filledStarDraw: Drawable? = null
    private var emptyLastStarDraw: Drawable? = null
    private var filledLastStarDraw: Drawable? = null
    private var starSpace: Int = 0

    private var ratingChangeListener: RatingChangeListener? = null

    interface RatingChangeListener {
        fun onRatingChanged(rating: Int)
    }

    init {
        orientation = HORIZONTAL

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomRatingBar, defStyleAttr, 0)

        maxRate = typedArray.getInt(R.styleable.CustomRatingBar_maxRating, 5)
        emptyStarDraw = typedArray.getDrawable(R.styleable.CustomRatingBar_emptyStarDrawable)
        filledStarDraw = typedArray.getDrawable(R.styleable.CustomRatingBar_filledStarDrawable)
        emptyLastStarDraw = typedArray.getDrawable(R.styleable.CustomRatingBar_emptyLastStarDrawable)
        filledLastStarDraw = typedArray.getDrawable(R.styleable.CustomRatingBar_filledLastStarDrawable)
        starSpace = typedArray.getDimensionPixelSize(R.styleable.CustomRatingBar_starSpacing, 0)

        typedArray.recycle()

        setupRatingViews()
    }

    private fun setupRatingViews() {
        removeAllViews()

        for (i in 0 until maxRate - 1) {
            val starImageView = createStarView(i)
            starImageView.setImageDrawable(emptyStarDraw)
            addView(starImageView)

            // Apply spacing between stars
            if (starSpace > 0) {
                val layoutParams = starImageView.layoutParams as LayoutParams
                layoutParams.marginEnd = starSpace
                starImageView.layoutParams = layoutParams
            }
        }

        val lastStarImageView = createStarView(maxRate - 1)
        lastStarImageView.setImageDrawable(emptyLastStarDraw)
        addView(lastStarImageView)

        updateRating()
    }

    private fun createStarView(index: Int): ImageView {
        val starImageView = ImageView(context)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        starImageView.layoutParams = layoutParams
        starImageView.setOnClickListener {
            rate = index + 1
            updateRating()
            ratingChangeListener?.onRatingChanged(rate)
        }
        return starImageView
    }

    private fun updateRating() {
        for (i in 0 until childCount - 1) {
            val starImageView = getChildAt(i) as ImageView
            val imageRes = if (i < rate) filledStarDraw else emptyStarDraw
            starImageView.setImageDrawable(imageRes)
        }

        val lastStarImageView = getChildAt(childCount - 1) as ImageView
        val imageRes = if (rate == maxRate) filledLastStarDraw else emptyLastStarDraw
        lastStarImageView.setImageDrawable(imageRes)
    }

    fun setRatingChangeListener(listener: RatingChangeListener) {
        this.ratingChangeListener = listener
    }

    fun setRating(rating: Int) {
        this.rate = rating
        updateRating()
    }

    fun setMaxRating(maxRating: Int) {
        this.maxRate = maxRating
        setupRatingViews()
    }

    fun setEmptyStarDrawable(drawable: Drawable) {
        emptyStarDraw = drawable
        setupRatingViews()
    }

    fun setFilledStarDrawable(drawable: Drawable) {
        filledStarDraw = drawable
        setupRatingViews()
    }

    fun setEmptyLastStarDrawable(drawable: Drawable) {
        emptyLastStarDraw = drawable
        setupRatingViews()
    }

    fun setFilledLastStarDrawable(drawable: Drawable) {
        filledLastStarDraw = drawable
        setupRatingViews()
    }

    fun setStarSpacing(spacing: Int) {
        this.starSpace = spacing
        setupRatingViews()
    }
}