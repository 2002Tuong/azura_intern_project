package com.parallax.hdvideo.wallpapers.ui.details

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.parallax.hdvideo.wallpapers.R
import java.util.*

open class ViewHolderCommon(val view: ViewBinding) : RecyclerView.ViewHolder(view.root) {
    var previewView: View? = null
    private val patternDate = "E, dd MMM"
    private val patternHour = "HH:mm"
    private val patternYear = "EEEE, dd MMM"
    private val calendar = Calendar.getInstance()

    fun addPreviewView(state: PopupWindowAdapter.ScreenState, cardView: ViewGroup) {
        if (state == PopupWindowAdapter.ScreenState.PREVIEW_IMAGE) {
            if (previewView != null) {
                cardView.removeView(previewView)
            }
            return
        }
        if (previewView != null) {
            cardView.removeView(previewView)
        }
        val idLayout = if (state == PopupWindowAdapter.ScreenState.PREVIEW_LOCK) {
            R.layout.preview_lock_screen
        } else R.layout.layout_preview
        val previewView = LayoutInflater.from(view.root.context)
            .inflate(idLayout, cardView, false)
        cardView.addView(
            previewView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        this.previewView = previewView
    }

    fun setTimeTextView(vh: ViewHolderCommon) {
        val hour = DateFormat.format(patternHour, calendar)
        val date = DateFormat.format(patternDate, calendar)
        val year = DateFormat.format(patternYear, calendar)
        vh.setText(hour, date, year)
    }

    fun setText(formatHour: CharSequence, formatDate: CharSequence, formatYear: CharSequence) {
        itemView.findViewById<TextView>(R.id.previewHour)?.text = formatHour
        itemView.findViewById<TextView>(R.id.previewDate)?.text = formatDate
    }

}