package com.example.videoart.batterychargeranimation.ui.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.videoart.batterychargeranimation.R

class CustomRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var rating: Int = 0
    private var maxRating: Int = 5
    private var emptyStarDrawable: Drawable? = null
    private var filledStarDrawable: Drawable? = null
    private var emptyLastStarDrawable: Drawable? = null
    private var filledLastStarDrawable: Drawable? = null
    private var starSpacing: Int = 0

    private var ratingChangeListener: RatingChangeListener? = null

    interface RatingChangeListener {
        fun onRatingChanged(rating: Int)
    }

    init {
        orientation = HORIZONTAL

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomRatingBar, defStyleAttr, 0)

        maxRating = typedArray.getInt(R.styleable.CustomRatingBar_maxRating, 5)
        emptyStarDrawable = typedArray.getDrawable(R.styleable.CustomRatingBar_emptyStarDrawable)
        filledStarDrawable = typedArray.getDrawable(R.styleable.CustomRatingBar_filledStarDrawable)
        emptyLastStarDrawable = typedArray.getDrawable(R.styleable.CustomRatingBar_emptyLastStarDrawable)
        filledLastStarDrawable = typedArray.getDrawable(R.styleable.CustomRatingBar_filledLastStarDrawable)
        starSpacing = typedArray.getDimensionPixelSize(R.styleable.CustomRatingBar_starSpacing, 0)

        typedArray.recycle()

        setupRatingViews()
    }

    private fun setupRatingViews() {
        removeAllViews()

        for (i in 0 until maxRating - 1) {
            val starView = createStarView(i)
            starView.setImageDrawable(emptyStarDrawable)
            addView(starView)

            // Apply spacing between stars
            if (starSpacing > 0) {
                val layoutParams = starView.layoutParams as LayoutParams
                layoutParams.marginEnd = starSpacing
                starView.layoutParams = layoutParams
            }
        }

        val lastStarView = createStarView(maxRating - 1)
        lastStarView.setImageDrawable(emptyLastStarDrawable)
        addView(lastStarView)

        updateRating()
    }

    private fun createStarView(index: Int): ImageView {
        val starView = ImageView(context)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        starView.layoutParams = layoutParams
        starView.setOnClickListener {
            rating = index + 1
            updateRating()
            ratingChangeListener?.onRatingChanged(rating)
        }
        return starView
    }

    private fun updateRating() {
        for (i in 0 until childCount - 1) {
            val starView = getChildAt(i) as ImageView
            val drawable = if (i < rating) filledStarDrawable else emptyStarDrawable
            starView.setImageDrawable(drawable)
        }

        val lastStarView = getChildAt(childCount - 1) as ImageView
        val drawable = if (rating == maxRating) filledLastStarDrawable else emptyLastStarDrawable
        lastStarView.setImageDrawable(drawable)
    }

    fun setRatingChangeListener(listener: RatingChangeListener) {
        this.ratingChangeListener = listener
    }

    fun setRating(rating: Int) {
        this.rating = rating
        updateRating()
    }

    fun setMaxRating(maxRating: Int) {
        this.maxRating = maxRating
        setupRatingViews()
    }

    fun setEmptyStarDrawable(drawable: Drawable) {
        emptyStarDrawable = drawable
        setupRatingViews()
    }

    fun setFilledStarDrawable(drawable: Drawable) {
        filledStarDrawable = drawable
        setupRatingViews()
    }

    fun setEmptyLastStarDrawable(drawable: Drawable) {
        emptyLastStarDrawable = drawable
        setupRatingViews()
    }

    fun setFilledLastStarDrawable(drawable: Drawable) {
        filledLastStarDrawable = drawable
        setupRatingViews()
    }

    fun setStarSpacing(spacing: Int) {
        this.starSpacing = spacing
        setupRatingViews()
    }
}