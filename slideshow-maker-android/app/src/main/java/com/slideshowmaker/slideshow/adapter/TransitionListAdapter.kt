package com.slideshowmaker.slideshow.adapter

import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.models.GSTransitionDataModel
import com.slideshowmaker.slideshow.modules.transition.GSTransitionUtils
import com.slideshowmaker.slideshow.modules.transition.transition.GSTransition
import com.slideshowmaker.slideshow.ui.base.BaseAdapter
import com.slideshowmaker.slideshow.ui.base.BaseViewHolder
import kotlinx.android.synthetic.main.item_view_gs_transition_list.view.*

class TransitionListAdapter(private val onSelectTransitionCallback: (GSTransitionDataModel) -> Unit) :
    BaseAdapter<GSTransitionDataModel>() {

    init {
        addGSTransitionData(GSTransitionUtils.getAllGSTransitionList())
    }

    override fun doGetViewType(position: Int): Int = R.layout.item_view_gs_transition_list

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val itemView: MaterialCardView = holder.itemView as MaterialCardView
        val transitionItem = _itemArray[position]

        itemView.transitionNameLabel.text = transitionItem.gsTransition.transitionName
        if (transitionItem.selected) {
            itemView.layoutThemeIcon.setBackgroundResource(R.drawable.background_selected_transition)
        } else {
            itemView.layoutThemeIcon.setBackgroundResource(R.drawable.background_transparent)
        }
        itemView.proBadge.isVisible = transitionItem.gsTransition.isPro && !SharedPreferUtils.proUser
        itemView.setOnClickListener {
            onSelectTransitionCallback.invoke(transitionItem)
        }
        /*view.adBadge.isVisible =
            item.gsTransition.isWatchAds && !item.gsTransition.isPro && !SharedPrefUtils.isProUser*/
        transitionItem.gsTransition.transitionName
        Glide.with(itemView.context).load(transitionItem.gsTransition.getThumbnail()).into(itemView.imagePreview)
    }

    private fun addGSTransitionData(gsTransitionList: ArrayList<GSTransition>) {
        _itemArray.clear()
        notifyDataSetChanged()
        for (gsTransition in gsTransitionList) {
            _itemArray.add(GSTransitionDataModel(gsTransition))
        }
        notifyDataSetChanged()
    }

    fun highlightItem(gsTransition: GSTransition) {
        for (item in _itemArray) {
            item.selected = item.gsTransition.id == gsTransition.id
        }
        notifyDataSetChanged()
    }

}