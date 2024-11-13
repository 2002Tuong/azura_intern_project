package com.parallax.hdvideo.wallpapers.ui.custom.bottommenu

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.ui.custom.bottommenu.ViewHelper.getThemeAccentColor
import com.parallax.hdvideo.wallpapers.ui.custom.bottommenu.ViewHelper.updateDrawableColor

class BubbleToggleView : RelativeLayout {
    private var bubbleToggle: BubbleToggleData? = null

    /**
     * Get the current state of the view
     *
     * @return the current state
     */
    var active = false
        private set
    private var iconView: ImageView? = null
    private var titleView: TextView? = null
    private var badgeView: TextView? = null
    private var animDuration = 0
    private var alwaysShowShape = false
    private var maximumTitleWidth = 0f
    private var measuredTitleWidth = 0f

    /**
     * Constructors
     */
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }
    /////////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////////
    /**
     * Initialize
     *
     * @param context current context
     * @param attrs   custom attributes
     */
    private fun init(context: Context, attrs: AttributeSet?) {
        //initialize default component
        var title: String = "Title"
        var iconDraw: Drawable? = null
        var shapeDraw: Drawable? = null
        var shapeColor = Int.MIN_VALUE
        var colorActive = getThemeAccentColor(context)
        var colorInactive = ContextCompat.getColor(context, R.color.default_inactive_color)
        var titleSize = context.resources.getDimension(R.dimen.default_nav_item_text_size)
        maximumTitleWidth = context.resources.getDimension(R.dimen.default_nav_item_title_max_width)
        var iconWidthValue = context.resources.getDimension(R.dimen.default_icon_size)
        var iconHeightValue = context.resources.getDimension(R.dimen.default_icon_size)
        var internalPadding = context.resources.getDimension(R.dimen.default_nav_item_padding)
            .toInt()
        var titlePadding = context.resources
            .getDimension(R.dimen.default_nav_item_text_padding).toInt()
        var textSizeOfBadge = context.resources
            .getDimension(R.dimen.default_nav_item_badge_text_size).toInt()
        var colorOfBadgeBackground =
            ContextCompat.getColor(context, R.color.default_badge_background_color)
        var textColorOfBadge = ContextCompat.getColor(context, R.color.default_badge_text_color)
        var badgeText: String? = null
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.BubbleToggleView, 0, 0)
            try {
                iconDraw =
                    ta.getDrawable(R.styleable.BubbleToggleView_bt_icon)
                iconWidthValue = ta.getDimension(R.styleable.BubbleToggleView_bt_iconWidth, iconWidthValue)
                iconHeightValue = ta.getDimension(R.styleable.BubbleToggleView_bt_iconHeight, iconHeightValue)
                shapeDraw = ta.getDrawable(R.styleable.BubbleToggleView_bt_shape)
                shapeColor = ta.getColor(R.styleable.BubbleToggleView_bt_shapeColor, shapeColor)
                alwaysShowShape =
                    ta.getBoolean(R.styleable.BubbleToggleView_bt_showShapeAlways, false)
                title = ta.getString(R.styleable.BubbleToggleView_bt_title).toString()
                titleSize = ta.getDimension(R.styleable.BubbleToggleView_bt_titleSize, titleSize)
                colorActive = ta.getColor(R.styleable.BubbleToggleView_bt_colorActive, colorActive)
                colorInactive =
                    ta.getColor(R.styleable.BubbleToggleView_bt_colorInactive, colorInactive)
                active = ta.getBoolean(R.styleable.BubbleToggleView_bt_active, false)
                animDuration =
                    ta.getInteger(R.styleable.BubbleToggleView_bt_duration, DEFAULT_ANIM_DURATION)
                internalPadding = ta.getDimension(
                    R.styleable.BubbleToggleView_bt_padding,
                    internalPadding.toFloat()
                ).toInt()
                titlePadding = ta.getDimension(
                    R.styleable.BubbleToggleView_bt_titlePadding,
                    titlePadding.toFloat()
                ).toInt()
                textSizeOfBadge = ta.getDimension(
                    R.styleable.BubbleToggleView_bt_badgeTextSize,
                    textSizeOfBadge.toFloat()
                ).toInt()
                colorOfBadgeBackground = ta.getColor(
                    R.styleable.BubbleToggleView_bt_badgeBackgroundColor,
                    colorOfBadgeBackground
                )
                textColorOfBadge =
                    ta.getColor(R.styleable.BubbleToggleView_bt_badgeTextColor, textColorOfBadge)
                badgeText = ta.getString(R.styleable.BubbleToggleView_bt_badgeText)
            } finally {
                ta.recycle()
            }
        }

        //set the default icon
        if (iconDraw == null) iconDraw = ContextCompat.getDrawable(context, R.drawable.default_icon_re)

        //set the default shape
        if (shapeDraw == null) shapeDraw =
            ContextCompat.getDrawable(context, R.drawable.transition_background_draw)

        //create a default bubble item
        bubbleToggle = BubbleToggleData()
        bubbleToggle!!.iconDraw = iconDraw
        bubbleToggle!!.shapeDraw = shapeDraw
        bubbleToggle!!.title = title
        bubbleToggle!!.sizeOfTitle = titleSize
        bubbleToggle!!.titlePadding = titlePadding
        bubbleToggle!!.shapeColor = shapeColor
        bubbleToggle!!.activeColor = colorActive
        bubbleToggle!!.inactiveColor = colorInactive
        bubbleToggle!!.iconWidthValue = iconWidthValue
        bubbleToggle!!.iconHeightValue = iconHeightValue
        bubbleToggle!!.internalPadding = internalPadding
        bubbleToggle!!.badgeText = badgeText
        bubbleToggle!!.colorOfBadgeBackground = colorOfBadgeBackground
        bubbleToggle!!.colorOfBadgeText = textColorOfBadge
        bubbleToggle!!.textSizeOfBadge = textSizeOfBadge.toFloat()

        //set the gravity
        gravity = Gravity.CENTER

        //set the internal padding
        setPadding(
            bubbleToggle!!.internalPadding,
            bubbleToggle!!.internalPadding,
            bubbleToggle!!.internalPadding,
            bubbleToggle!!.internalPadding
        )
        post {
            //make sure the padding is added
            setPadding(
                bubbleToggle!!.internalPadding,
                bubbleToggle!!.internalPadding,
                bubbleToggle!!.internalPadding,
                bubbleToggle!!.internalPadding
            )
        }
        createBubbleItemView(context)
        setInitialState(active)
    }

    /**
     * Create the components of the bubble item view [.iconView] and [.titleView]
     *
     * @param context current context
     */
    private fun createBubbleItemView(context: Context) {

        //create the nav icon
        iconView = ImageView(context)
        iconView!!.id = ViewCompat.generateViewId()
        val lpIcon = LayoutParams(
            bubbleToggle!!.iconWidthValue.toInt(), bubbleToggle!!.iconHeightValue.toInt()
        )
        lpIcon.addRule(CENTER_VERTICAL, TRUE)
        iconView!!.layoutParams = lpIcon
        iconView!!.setImageDrawable(bubbleToggle!!.iconDraw)

        //create the nav title
        titleView = TextView(context)
        val paramsTitle = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        paramsTitle.addRule(CENTER_VERTICAL, TRUE)
        paramsTitle.addRule(
            END_OF,
            iconView!!.id
        )
        titleView!!.layoutParams = paramsTitle
        titleView!!.isSingleLine = true
        titleView!!.setTextColor(bubbleToggle!!.activeColor)
        titleView!!.text = bubbleToggle!!.title
        titleView!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, bubbleToggle!!.sizeOfTitle)
        //get the current measured title width
        titleView!!.visibility = VISIBLE
        //update the margin of the text view
        titleView!!.setPadding(
            bubbleToggle!!.titlePadding,
            0,
            bubbleToggle!!.titlePadding,
            0
        )
        //measure the content width
        titleView!!.measure(0, 0) //must call measure!
        measuredTitleWidth = titleView!!.measuredWidth.toFloat() //get width
        //limit measured width, based on the max width
        if (measuredTitleWidth > maximumTitleWidth) measuredTitleWidth = maximumTitleWidth

        //change the visibility
        titleView!!.visibility = GONE
        addView(iconView)
        addView(titleView)
        updateBadge(context)

        //set the initial state
        setInitialState(active)
    }

    /**
     * Adds or removes the badge
     */
    private fun updateBadge(context: Context) {

        //remove the previous badge view
        if (badgeView != null) removeView(badgeView)
        if (bubbleToggle!!.badgeText == null) return

        //create badge
        badgeView = TextView(context)
        val paramsBadge = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        paramsBadge.addRule(ALIGN_TOP, iconView!!.id)
        paramsBadge.addRule(ALIGN_END, iconView!!.id)
        badgeView!!.layoutParams = paramsBadge
        badgeView!!.isSingleLine = true
        badgeView!!.setTextColor(bubbleToggle!!.colorOfBadgeText)
        badgeView!!.text = bubbleToggle!!.badgeText
        badgeView!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, bubbleToggle!!.textSizeOfBadge)
        badgeView!!.gravity = Gravity.CENTER
        val bgDrawable = ContextCompat.getDrawable(context, R.drawable.bg_badge_white)
        updateDrawableColor(bgDrawable, bubbleToggle!!.colorOfBadgeBackground)
        badgeView!!.background = bgDrawable
        val badgePadding = context.resources
            .getDimension(R.dimen.default_nav_item_badge_padding).toInt()
        //update the margin of the text view
        badgeView!!.setPadding(badgePadding, 0, badgePadding, 0)
        //measure the content width
        badgeView!!.measure(0, 0)
        if (badgeView!!.measuredWidth < badgeView!!.measuredHeight) badgeView!!.width =
            badgeView!!.measuredHeight
        addView(badgeView)
    }
    /////////////////////////////////
    // PUBLIC METHODS
    ////////////////////////////////
    /**
     * Updates the Initial State
     *
     * @param isActive current state
     */
    fun setInitialState(isActive: Boolean) {
        //set the background
        background = bubbleToggle!!.shapeDraw
        if (isActive) {
            updateDrawableColor(iconView!!.drawable, bubbleToggle!!.activeColor)
            this.active = true
            titleView!!.visibility = VISIBLE
            if (background is TransitionDrawable) {
                val trans = background as TransitionDrawable
                trans.startTransition(0)
            } else {
                if (!alwaysShowShape && bubbleToggle!!.shapeColor != Int.MIN_VALUE) updateDrawableColor(
                    bubbleToggle!!.shapeDraw, bubbleToggle!!.shapeColor
                )
            }
        } else {
            updateDrawableColor(iconView!!.drawable, bubbleToggle!!.inactiveColor)
            this.active = false
            titleView!!.visibility = GONE
            if (!alwaysShowShape) {
                if (background !is TransitionDrawable) {
                    background = null
                } else {
                    val trans = background as TransitionDrawable
                    trans.resetTransition()
                }
            }
        }
    }

    /**
     * Toggles between Active and Inactive state
     */
    fun toggle() {
        if (!active) activate() else deactivate()
    }

    /**
     * Set Active state
     */
    fun activate() {
        updateDrawableColor(iconView!!.drawable, bubbleToggle!!.activeColor)
        active = true
        titleView!!.visibility = VISIBLE
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = animDuration.toLong()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            titleView!!.width = (measuredTitleWidth * value).toInt()
            //end of animation
        }
        animator.start()
        if (background is TransitionDrawable) {
            val trans = background as TransitionDrawable
            trans.startTransition(animDuration)
        } else {
            //if not showing Shape Always and valid shape color present, use that as tint
            if (!alwaysShowShape && bubbleToggle!!.shapeColor != Int.MIN_VALUE) updateDrawableColor(
                bubbleToggle!!.shapeDraw, bubbleToggle!!.shapeColor
            )
            background = bubbleToggle!!.shapeDraw
        }
    }

    /**
     * Set Inactive State
     */
    private fun deactivate() {
        updateDrawableColor(iconView!!.drawable, bubbleToggle!!.inactiveColor)
        active = false
        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.duration = animDuration.toLong()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            titleView!!.width = (measuredTitleWidth * value).toInt()
            //end of animation
            if (value <= 0.0f) titleView!!.visibility = GONE
        }
        animator.start()
        if (background is TransitionDrawable) {
            val trans = background as TransitionDrawable
            trans.reverseTransition(animDuration)
        } else {
            if (!alwaysShowShape) background = null
        }
    }

    /**
     * Sets the [Typeface] of the [.titleView]
     *
     * @param typeface to be used
     */
    fun setTitleTypeface(typeface: Typeface?) {
        titleView!!.typeface = typeface
    }

    /**
     * Updates the measurements and fits the view
     *
     * @param maxWidth in pixels
     */
    fun updateMeasurements(maxWidth: Int) {
        var marginLeft = 0
        var marginRight = 0
        val titleViewLayoutParams = titleView!!.layoutParams
        if (titleViewLayoutParams is LayoutParams) {
            marginLeft = titleViewLayoutParams.rightMargin
            marginRight = titleViewLayoutParams.leftMargin
        }
        val newTitleWidth = ((maxWidth
                - (paddingRight + paddingLeft)
                - (marginLeft + marginRight)
                - (bubbleToggle!!.iconWidthValue.toInt()))
                + titleView!!.paddingRight + titleView!!.paddingLeft)

        //if the new calculate title width is less than current one, update the titleView specs
        if (newTitleWidth > 0 && newTitleWidth < measuredTitleWidth) {
            measuredTitleWidth = titleView!!.measuredWidth.toFloat()
        }
    }

    /**
     * Set value to the Badge's
     *
     * @param value as String, null to hide
     */
    fun setBadgeText(value: String?) {
        bubbleToggle!!.badgeText = value
        updateBadge(context)
    }

    companion object {
        private val TAG = "BNI_View"
        private val DEFAULT_ANIM_DURATION = 200
    }
}