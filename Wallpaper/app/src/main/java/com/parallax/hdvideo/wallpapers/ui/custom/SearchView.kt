package com.parallax.hdvideo.wallpapers.ui.custom

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.extension.isInvisible
import com.parallax.hdvideo.wallpapers.utils.dp2Px
import com.parallax.hdvideo.wallpapers.utils.dpToPx

class SearchView : CardView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mEditText: EditText
    private lateinit var mTextView: TextView
    private lateinit var mButton: ImageView
    private lateinit var mClearButton: ImageButton
    private var latestText: String = ""
    var minNumberOfSearchable = 1
    var onSearchCallback: ((String) -> Unit)? = null
    var currentHashTag: String? = null
    var textChangedLiveData: MutableLiveData<String>? = null
    var onClickEvent: (() -> Unit)? = null
    private var latestTextVisible: String? = null
    val textColor: Int = Color.parseColor("#222222")
    var text: String?
        get() = mEditText.text?.run {
            toString().trim()
        }
        set(value) {
            removeTextChangedListener(textWatcherObject)
            mEditText.setText(value)
            mTextView.text = value
            addTextChangedListener(textWatcherObject)
            currentHashTag = null
            latestTextVisible = value
            mClearButton.isHidden = value.isNullOrEmpty()
        }

    private fun init(context: Context, attrs: AttributeSet?) {
        val textParam = MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        mEditText = EditText(ContextThemeWrapper(context, R.style.EditTextSearchViewStyle), null, 0)
        mEditText.setTextColor(context.getColor(R.color.white))
        mEditText.typeface = ResourcesCompat.getFont(this.context, R.font.bevietnampro_regular)
        mEditText.hint = context.getString(R.string.search)
        mEditText.setPadding(
            dpToPx(52f),
            0,
            dpToPx(40f),
            0
        )
        addView(mEditText, textParam)
        mTextView = TextView(ContextThemeWrapper(context, R.style.TextViewSearchViewStyle), null, 0)
        mTextView.hint = context.getString(R.string.search)
        mTextView.setTextColor(context.getColor(R.color.white))
        mTextView.typeface = ResourcesCompat.getFont(this.context, R.font.bevietnampro_regular)
        mTextView.setPadding(
            dpToPx(52f),
            0,
            dpToPx(40f),
            0
        )
        addView(mTextView, textParam)
        mButton = ImageView(context)
        val outTypedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outTypedValue, true)
//        button.setBackgroundResource(outValue.resourceId)
        mButton.scaleType = ImageView.ScaleType.CENTER_INSIDE
        mButton.setPadding(dpToPx(8f), dpToPx(4f), 0, dpToPx(4f))
        mButton.setImageResource(R.drawable.ic_search_re)
        val layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT).apply {
            topMargin = dpToPx(8f)
            bottomMargin = dpToPx(8f)
        }
        layoutParams.gravity = Gravity.START
        addView(mButton, layoutParams)
        mClearButton = ImageButton(context)
        mClearButton.setImageResource(R.drawable.ic_cancel_re)
        mClearButton.setBackgroundResource(outTypedValue.resourceId)
        addView(mClearButton, LayoutParams(dpToPx(36f), ViewGroup.LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setPadding(0,0, dpToPx(8f),0)
            }
        )
        mClearButton.isHidden = true
        radius = dp2Px(24f)
        cardElevation = 0f
        setCardBackgroundColor(context.getColor(R.color.bg_color_search))
        addTextChangedListener(textWatcherObject)
        setOnClickListener {
            setShowKeyboard(true)
            onClickEvent?.invoke()
            val txt = text?.also {
                textChangedLiveData?.value = it
            }
            mClearButton.isHidden = txt.isNullOrEmpty()
        }
        mEditText.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                startSearching()
                true
            } else false
        }
        mEditText.isHidden = true
        mClearButton.setOnClickListener {
            setEditText("")
            it.isHidden = true
            textChangedLiveData?.value = ""
            setShowKeyboard(true)
            onClickEvent?.invoke()
        }
        attrs?.also {
            val array = context.obtainStyledAttributes(attrs, R.styleable.SearchView)
            text = array.getString(R.styleable.SearchView_text) ?: ""
            array.recycle()
        }
    }


    private val inputMethodManager: InputMethodManager
        get() =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    private fun startSearching() {
        latestText = currentHashTag ?: text ?: ""
        if (latestText.length >= minNumberOfSearchable) {
            latestTextVisible = mEditText.text?.toString() ?: ""
            setShowKeyboard(false)
            handler.removeCallbacks(runnable)
            onSearchCallback?.invoke(latestText)
        }
    }

    fun setShowKeyboard(isShow: Boolean) {
        if (isShow) {
            mEditText.isEnabled = true
            mEditText.isHidden = false
            val txt = mEditText.text?.toString() ?: ""
            mEditText.isFocusable = true
            mEditText.isFocusableInTouchMode = true
            mEditText.requestFocus()
            showKeyboard()
            mEditText.setSelection(txt.length)
            mTextView.isHidden = true
        } else {
            mTextView.isHidden = false
            mTextView.text = text
            mEditText.isEnabled = false
            mEditText.isHidden = true
            if (!latestTextVisible.isNullOrEmpty()) {
               // setEditText(lastTextVisible)
            }
            hiddenKeyboard()
            mTextView.isHidden = false
        }
        alterSearchViewForSearch(isShow)
    }

    private fun setEditText(text: String?) {
        removeTextChangedListener(textWatcherObject)
        mEditText.setText(text)
        mTextView.text = text
        addTextChangedListener(textWatcherObject)
    }

    fun updateTextSearch(hashTag: HashTag) {
        val textSearch = hashTag.hashtag ?: hashTag.name
        this.text = if (hashTag.name.isNullOrEmpty()) textSearch else hashTag.name
        currentHashTag = textSearch
        if (textSearch != null) {
            latestText = textSearch
        }
    }

    private fun reset() {
        text = ""
        textWatcherObject.onTextChanged(text, 0, 0, 0)
        setShowKeyboard(true)
    }

    fun addTextChangedListener(textWatcher: TextWatcher) {
        mEditText.addTextChangedListener(textWatcher)
    }

    fun removeTextChangedListener(textWatcher: TextWatcher) {
        mEditText.removeTextChangedListener(textWatcher)
    }

    private fun hiddenKeyboard() {
//        inputMethod.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        inputMethodManager.hideSoftInputFromWindow(mEditText.windowToken, 0)
        mEditText.clearFocus()
    }

    private fun showKeyboard() {
        inputMethodManager.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT)
//        } else
//        inputMethod.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    fun alterSearchViewForSearch(searching : Boolean) {
        mButton.isInvisible = searching
        val rightPaddingOfEditText = mEditText.paddingRight
        val paddingRight = if(searching) dpToPx(16f) else dpToPx(52f)
        mEditText.setPadding(paddingRight, 0, rightPaddingOfEditText, 0)
        mTextView.setPadding(paddingRight, 0, rightPaddingOfEditText, 0)
    }

    private val runnable = Runnable {
        val txt = this.text ?: ""
        latestText = txt
        textChangedLiveData?.postValue(txt)
    }

    private val textWatcherObject = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            currentHashTag = null
            val txt = text
            mTextView.text = txt
            mTextView.isHidden = true
            mClearButton.isHidden = txt.isNullOrEmpty()
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, 200)
        }
    }

    override fun onDetachedFromWindow() {
//        Reflector.fixInputMethodManager(context)
        handler.removeCallbacks(runnable)
        textChangedLiveData = null
        onSearchCallback = null
        super.onDetachedFromWindow()
    }
}