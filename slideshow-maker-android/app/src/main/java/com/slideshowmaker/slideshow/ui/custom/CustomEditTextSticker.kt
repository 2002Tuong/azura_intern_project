package com.slideshowmaker.slideshow.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import com.slideshowmaker.slideshow.utils.DimenUtils
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.roundToInt
import kotlin.math.sqrt

class CustomEditTextSticker:AppCompatEditText {

    private var density = 0f

    private var inputText = ""
    private var hintText = "Type your text"
    private var normalTextSize = 24f
    private var normalTextPaint = Paint()

    private val dotBoundPaint = Paint()

    private val inputTextPaint = Paint()
    private var mainTextSize = 24f

    private var padding = 4f

    private var dotHeight = 1f


    private var buttonRadius = 12f
    private val btnPaint = Paint()

    private var translationX = 0f
    private var translationY = 0f
    private var scaleValue = 1f
    private var mRotate = 0f

    private val rotateBtnRegion = Region()

    private var touchPointX = 0f
    private var touchPointY = 0f

    private var isRotateMode = false

    private var centerPointX = 0f
    private var centerPointY = 0f

    private var mOriginWidth = 0f
    private var mOriginHeight = 0f
    private val mFloatArray = FloatArray(9)

    private var lines = ArrayList<String>()
    constructor(context: Context) : super(context) {
        initAttrs(null)
    }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        initAttrs(attributes)
    }


    private fun initAttrs(attributes: AttributeSet?) {
        density = DimenUtils.density(context)
        movementMethod = null
        normalTextSize*=density
        mainTextSize*=density
        padding*=density
        dotHeight*=density
        buttonRadius*=density
        setBackgroundColor(Color.TRANSPARENT)

        dotBoundPaint.apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(5f*density,5f*density),0f)
            strokeWidth = dotHeight
        }

        inputTextPaint.apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.FILL
            textSize = mainTextSize
            alpha = 255
        }

        normalTextPaint.apply {
            color = Color.WHITE
            isAntiAlias = true
            style = Paint.Style.FILL
            textSize = normalTextSize

        }

        btnPaint.apply {
            color = Color.GREEN
            isAntiAlias = true
            style = Paint.Style.FILL
        }


        addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                inputText = s.toString()
                requestLayout()
            }

        })


    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if(inputText.isNotEmpty()) {

           // val w = (getTextWidth(mMainText, mMainTextPaint)+mPadding*4+4*mButtonRadius).roundToInt()
          //  val h = ((getTextHeight(mMainText, mMainTextPaint)*1.75f)+mButtonRadius*2).roundToInt()
            val height = getHeightForText()+4*buttonRadius
            val width = getWidthForText()+4*(padding+buttonRadius)

            setMeasuredDimension(width.roundToInt(),height.roundToInt())
        } else {
            val width = (getTextWidth(hintText, normalTextPaint)+padding*4+4*buttonRadius).roundToInt()
            val height =((getTextHeight(hintText, normalTextPaint)*1.75f)+buttonRadius*2).roundToInt()
            setMeasuredDimension(width,height)
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawMainText(canvas)
        drawDotBound(canvas)

        canvas.save()
        drawDeleteIcon(canvas)
        drawRotateButton(canvas)
        canvas.restore()
    }

    private fun drawMainText(canvas: Canvas?) {
        if(inputText.isNotEmpty()) {
            val offsetX = buttonRadius*2+padding
            var curHeight = buttonRadius+padding
            for(text in lines) {
                val lineHeight = getTextHeight("ky", inputTextPaint)
                curHeight+=lineHeight
                canvas?.drawText(text, offsetX, curHeight, inputTextPaint)
            }

        } else {
            drawHintText(canvas)
        }
    }


    private fun drawHintText(canvas: Canvas?) {
        drawText(canvas, hintText, normalTextPaint)
    }

    private fun drawText(canvas: Canvas?, text:String, paint: Paint) {
        val offsetX = buttonRadius*2+padding
        canvas?.drawText(text, offsetX, height*2/3f, paint)

    }

    private fun drawDotBound(canvas: Canvas?) {
        val offset = dotHeight+buttonRadius
        canvas?.drawRect(offset,offset, width.toFloat()-offset,height.toFloat()-offset, dotBoundPaint)
    }

    private fun drawDeleteIcon(canvas: Canvas?) {
        canvas?.drawCircle(buttonRadius/scaleValue, buttonRadius/scaleValue, buttonRadius/scaleValue, btnPaint)
    }

    private fun drawRotateButton(canvas: Canvas?) {
        val drawPath = Path().apply {
            addCircle(width-(buttonRadius/scaleValue),height-(buttonRadius/scaleValue), (buttonRadius/scaleValue), Path.Direction.CCW)
            getBoundRegion(rotateBtnRegion, this)
        }
        canvas?.drawPath(drawPath, btnPaint)
    }

    private fun getTextWidth(text:String, paint: Paint):Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.width().toFloat()
    }
    private fun getTextHeight(text:String, paint: Paint):Float {
        val rect = Rect()
        paint.getTextBounds(text, 0, text.length, rect)
        return rect.height().toFloat()
    }

    var presentRotation = 0f
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_DOWN) {
            isRotateMode = event?.x?.let { rotateBtnRegion.contains(it.roundToInt(), event.y.roundToInt()) } == true
            if(isRotateMode) {
                presentRotation = rotation
            }
            touchPointX = event.rawX
            touchPointY = event.rawY
        } else  if(event?.action == MotionEvent.ACTION_MOVE) {
            if(isRotateMode) {
                onRotate(event.rawX, event.rawY)
            } else {
                motionView(event.rawX, event.rawY)
            }

        } else if(event?.action == MotionEvent.ACTION_UP) {
            isRotateMode = false
            presentRotation = rotation
        }

        return true
    }

    private fun motionView(rawX:Float, rawY:Float) {
        translationX += (rawX-touchPointX)
        translationY += (rawY-touchPointY)
        touchPointX = rawX
        touchPointY = rawY
        translationX = translationX
        translationY = translationY

    }

    private fun onRotate(rawX:Float, rawY:Float) {
        val location = IntArray(2)
        getLocationOnScreen(location)
        centerPointX = (rawX-location[0])/2f
        centerPointY = (rawY-location[1])/2f
       /* val dx = rawX-mCenterX
        val dy = rawY-mCenterY
        val degree = atan(dy/dx)*180/ PI
        rotation = degree.toFloat()*/
        val deltaDegree = (atan((rawY-centerPointY)/(rawX-centerPointX))- atan((touchPointY-centerPointY)/(touchPointX-centerPointX)))*180/ PI
        rotation = presentRotation+deltaDegree.toFloat()


        val originCross = sqrt(width*width.toFloat()+height*height)
        val deltaX = rawX-location[0]
        val deltaY = rawY-location[1]
        val newCross = sqrt((deltaX*deltaX)+(deltaY*deltaY))
        var newScaleValue = newCross/originCross
        //if(newScale <= 0.5) newScale = 0.5f
        scaleValue= newScaleValue
       scaleX = scaleValue
        scaleY = scaleValue
        //requestLayout()
        invalidate()
    }

    private fun containSpecialCharacter(text:String):Boolean {
        val regex = "[q|y|p|g|j]".toRegex()
        return regex.containsMatchIn(text)
    }

    private fun showKeyboard() {
        requestFocus()
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager!!.toggleSoftInputFromWindow(applicationWindowToken, InputMethodManager.SHOW_FORCED, 1)
    }

    private fun getBoundRegion(region: Region, path: Path) {
        val boundRecF = RectF()
        path.computeBounds(boundRecF, true)
        region.setPath(path, Region(boundRecF.left.toInt(), boundRecF.top.toInt(), boundRecF.right.toInt(), boundRecF.bottom.toInt()))
    }

    private fun getHeightForText():Int {
       var totalLine = 1
        var curLine = ""
        var outHeight = 0f
        lines.clear()
        for(c in inputText) {
            if(c.toString() == "\n") {
                totalLine++
                lines.add(curLine)
                outHeight += (getTextHeight("qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM", inputTextPaint))
                curLine=""
            } else {
                curLine = "$curLine$c"
            }
        }
        lines.add(curLine)
        outHeight += getTextHeight("qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM", inputTextPaint)
        return outHeight.roundToInt()
    }

    private fun getWidthForText():Int {
        var maxWidth = 0f
        for(text in lines) {
            if(getTextWidth(text, inputTextPaint) >= maxWidth) {
                maxWidth = getTextWidth(text, inputTextPaint)
            }
        }
        return maxWidth.roundToInt()
    }

    fun setToCenter(parentW:Int, parentH:Int) {

    }

    fun changeFonts(fontId:Int) {
        inputTextPaint.typeface = ResourcesCompat.getFont(context, fontId)
        requestLayout()
        invalidate()
    }
}