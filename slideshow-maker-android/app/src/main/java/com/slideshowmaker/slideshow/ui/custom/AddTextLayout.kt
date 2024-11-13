package com.slideshowmaker.slideshow.ui.custom

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.adapter.TextColorListAdapter
import com.slideshowmaker.slideshow.adapter.FontListAdapter
import com.slideshowmaker.slideshow.data.models.TextStickerAttrInfo
import com.slideshowmaker.slideshow.utils.DimenUtils
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.RawResourceReader
import kotlinx.android.synthetic.main.layout_view_add_text.view.*
import kotlinx.android.synthetic.main.layout_view_edit_text_color.view.*
import kotlinx.android.synthetic.main.layout_view_edit_text_fonts.view.*
import kotlinx.android.synthetic.main.layout_view_edit_text_style.view.*

@SuppressLint("ViewConstructor")
class AddTextLayout(context: Context?,  editTextSticker: EditTextSticker? = null) :
    LinearLayout(context) {
    private var curEditState = false
    private var MainEditTextSticker: EditTextSticker? = null
    private val textColorAdapter = TextColorListAdapter {
        MainEditTextSticker?.changeColor(it)
    }
    private val fontAdapter = FontListAdapter {
        onChangeFont(it)
    }
    private var editMode = EditMode.TEXT

    private var textStickerAttrInfo: TextStickerAttrInfo?=null

    init {
        editTextSticker?.let {
            MainEditTextSticker = it
            curEditState = true
            textStickerAttrInfo = it.getTextAttrData()
            setOnClickListener {

                editMode = EditMode.TEXT
                showKeyboard()

                updateIcon()
            }
        }
        initAttrs()
        updateIcon()

    }

    private fun initAttrs() {
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        inflate(context, R.layout.layout_view_add_text, this)
        textColorAdapter.setItemList(RawResourceReader.readTextColorFile())
        initAction()
        initView()
    }

    private fun initAction() {
        icKeyboard.setOnClickListener {
            editMode = EditMode.TEXT
            showKeyboard()

            updateIcon()
        }
        icColor.setOnClickListener {
            if (editMode == EditMode.COLOR) return@setOnClickListener
            hideKeyboard()
            editMode = EditMode.COLOR
            showChangeColorLayout()
            updateIcon()
        }
        icFonts.setOnClickListener {
            if (editMode == EditMode.FONTS) return@setOnClickListener
            editMode = EditMode.FONTS
            hideKeyboard()
            showChangeFontLayout()
            updateIcon()
        }
        icStyle.setOnClickListener {
            if (editMode == EditMode.STYLE) return@setOnClickListener
            editMode = EditMode.STYLE
            hideKeyboard()
            showChangeStyleLayout()
            updateIcon()
        }

    }

    fun showKeyboard() {
        editMode = EditMode.TEXT
        toolsDetails.removeAllViews()
        openKeyboard()
        MainEditTextSticker?.requestFocus()
        //hideKeyboard()

    }

    private fun initView() {
        if (MainEditTextSticker == null) {
            MainEditTextSticker = EditTextSticker(context, null).apply {
                id = View.generateViewId()
            }
        }

        val screenSize = DimenUtils.screenWidth(context)
        val videoPreviewScale = DimenUtils.videoPreviewScale()

        textContainer.layoutParams.width = (screenSize*videoPreviewScale).toInt()
        textContainer.layoutParams.height = (screenSize*videoPreviewScale).toInt()
        textContainer.addView(MainEditTextSticker)


    }
    private var autoShowKeyboardEnable = false
    fun onResume() {
        Logger.e("add text layout on resume")
        object :CountDownTimer(500,500){
            override fun onFinish() {
                if(autoShowKeyboardEnable)
                openKeyboard()
            }

            override fun onTick(millisUntilFinished: Long) {

            }

        }.start()

    }

    private fun showChangeFontLayout() {
        val editView = View.inflate(context, R.layout.layout_view_edit_text_fonts, null)
        showToolsView(editView)
        editView.fontsListView.adapter = fontAdapter
        editView.fontsListView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private fun showChangeColorLayout() {
        val editView = View.inflate(context, R.layout.layout_view_edit_text_color, null)
        showToolsView(editView)
        editView.textColorListView.apply {
            adapter = textColorAdapter
            layoutManager = LinearLayoutManager(editView.context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun showChangeStyleLayout() {
        val editView = View.inflate(context, R.layout.layout_view_edit_text_style, null)
        showToolsView(editView)
        editView.icTextAlignLeft.setOnClickListener {
            MainEditTextSticker?.changeAlign(EditTextSticker.AlignMode.LEFT)
        }
        editView.icTextAlignCenter.setOnClickListener {
            MainEditTextSticker?.changeAlign(EditTextSticker.AlignMode.CENTER)
        }
        editView.icTextAlignRight.setOnClickListener {
            MainEditTextSticker?.changeAlign(EditTextSticker.AlignMode.RIGHT)
        }
        editView.textStyleRegular.setOnClickListener {
            MainEditTextSticker?.changeTextStyle(Typeface.NORMAL)
        }
        editView.textStyleBold.setOnClickListener {
            MainEditTextSticker?.changeTextStyle(Typeface.BOLD)
        }
        editView.textStyleItalic.setOnClickListener {
            MainEditTextSticker?.changeTextStyle(Typeface.ITALIC)
        }
        editView.textStyleBoldItalic.setOnClickListener {
            MainEditTextSticker?.changeTextStyle(Typeface.BOLD_ITALIC)
        }
        editView.textStyleStrike.setOnClickListener {
            MainEditTextSticker?.changeTextFlag(Paint.STRIKE_THRU_TEXT_FLAG)
        }
        editView.textStyleUnderline.setOnClickListener {
            MainEditTextSticker?.changeTextFlag(Paint.UNDERLINE_TEXT_FLAG)
        }
    }

    private fun showToolsView(view: View) {
        toolsDetails.removeAllViews()
        toolsDetails.addView(view)
        playTranslationYAnimation(view)
    }

 /*   private fun openKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInputFromWindow(mMainTextSticker?.applicationWindowToken, InputMethodManager.SHOW_IMPLICIT, 1)
        mMainTextSticker?.requestFocus()

    }*/
    private fun openKeyboard() {
     autoShowKeyboardEnable = true
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)

    }

     fun hideKeyboard() {
 /*       if(context.currentFocus is EditText) {
            activity.currentFocus?.clearFocus()
        }*/
         //mMainTextSticker?.clearFocus()
         autoShowKeyboardEnable = false
        val inputMethodManager: InputMethodManager =
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }


/*    fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.hideSoftInputFromWindow(textContainer.applicationWindowToken, 0)

    }*/

    private fun playTranslationYAnimation(view: View) {
        val animator = AnimatorSet()
        animator.playTogether(
            ObjectAnimator.ofFloat(view, "alpha", 0.5f, 1f),
            ObjectAnimator.ofFloat(view, "translationY", 64f, 0f)
        )
        animator.duration = 250
        animator.interpolator = FastOutLinearInInterpolator()
        animator.start()
    }

    fun getEditTextView(): EditTextSticker? {
        if (MainEditTextSticker?.getMainText()!!.isNotEmpty()) {
            textContainer.removeView(MainEditTextSticker)
            return MainEditTextSticker
        } else {
            Toast.makeText(context, context.getString(R.string.text_editor_hint), Toast.LENGTH_LONG)
                .show()
            return null
        }
    }

    private fun onChangeFont(fontId: Int) {
        MainEditTextSticker?.changeFonts(fontId)
    }

    enum class EditMode {
        NONE, TEXT, FONTS, COLOR, STYLE
    }



    fun onBackPress(): EditTextSticker? {
        return if (curEditState) {
            textContainer.removeView(MainEditTextSticker)
            MainEditTextSticker
        } else {
            null
        }
    }

    fun onCancelEdit(): EditTextSticker? {
        hideKeyboard()
        return if(curEditState) {
            if(textStickerAttrInfo == null) return null
            textStickerAttrInfo?.let {
                MainEditTextSticker?.setAttr(it)
            }
            textContainer.removeView(MainEditTextSticker)
            MainEditTextSticker?.clearFocus()
            MainEditTextSticker
        } else {
            MainEditTextSticker?.clearFocus()
            null
        }
    }

    fun editState():Boolean=curEditState

    private fun updateIcon() {

        icKeyboard.setBackgroundColor(ContextCompat.getColor(context, R.color.greyscale900))
        icFonts.setBackgroundColor(ContextCompat.getColor(context, R.color.greyscale900))
        icColor.setBackgroundColor(ContextCompat.getColor(context, R.color.greyscale900))
        icStyle.setBackgroundColor(ContextCompat.getColor(context, R.color.greyscale900))
        when(editMode) {
            EditMode.TEXT -> {
                icKeyboard.setBackgroundColor(ContextCompat.getColor(context, R.color.greyscale900))
            }
            EditMode.FONTS -> {
                icFonts.setBackgroundColor(ContextCompat.getColor(context, R.color.greyscale900))
            }
            EditMode.STYLE -> {
                icStyle.setBackgroundColor(ContextCompat.getColor(context, R.color.greyscale900))
            }
            EditMode.COLOR -> {
                icColor.setBackgroundColor(ContextCompat.getColor(context, R.color.greyscale900))
            }
            else -> {}
        }
    }

}