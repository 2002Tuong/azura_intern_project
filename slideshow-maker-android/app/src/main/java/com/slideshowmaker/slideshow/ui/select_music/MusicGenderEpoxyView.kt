package com.slideshowmaker.slideshow.ui.select_music

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.utils.KotlinEpoxyHolder

@EpoxyModelClass(layout = R.layout.item_view_gender_list)
abstract class MusicGenderEpoxyView :
    EpoxyModelWithHolder<MusicGenderEpoxyView.Holder>() {

    @EpoxyAttribute
    lateinit var gender: String

    @EpoxyAttribute
    var selected: Boolean = false

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var toggleClickListener: View.OnClickListener? = null

    override fun bind(holder: Holder) {
        super.bind(holder)
        holder.tvGender.text = gender
        holder.root.setBackgroundResource(if (selected) R.drawable.background_rectangle_radius_40 else R.drawable.background_rectangle_radius)
        holder.tvGender.setTextColor(
            ContextCompat.getColor(
                holder.root.context,
                if (selected) R.color.orange_900 else R.color.greyscale_500
            )
        )
        holder.root.setOnClickListener(toggleClickListener)
    }

    class Holder : KotlinEpoxyHolder() {
        val root: LinearLayout by bind(R.id.rootLayout)
        val tvGender: TextView by bind(R.id.tvGender)
    }
}