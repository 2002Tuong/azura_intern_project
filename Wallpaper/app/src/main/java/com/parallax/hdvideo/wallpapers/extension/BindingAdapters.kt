package com.parallax.hdvideo.wallpapers.extension

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.parallax.hdvideo.wallpapers.ui.custom.SearchView
import com.parallax.hdvideo.wallpapers.utils.other.GlideSupport

@BindingAdapter("adapter")
fun setAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>) {
    view.adapter = adapter
}
//
@BindingAdapter("image")
fun setImage(view: ImageView, url: String?) {
    GlideSupport.load(view, url)
}

@BindingAdapter("image")
fun setImage(view: ImageView, resource: Int) {
    Glide.with(view.context).load(resource).into(view)
}

@BindingAdapter("textChanged")
fun addTextChangedListener(view: SearchView,  liveData: MutableLiveData<String>?) {
    view.textChangedLiveData = liveData
}

@BindingAdapter("circleImage")
fun glideAsCircleImage(imageView: ImageView, url : String) {
    Glide.with(imageView.context).load(url).circleCrop().into(imageView)
}

@BindingAdapter("circleImage")
fun glideAsCircleImage(imageView: ImageView, resource : Int) {
    Glide.with(imageView.context).load(resource).circleCrop().into(imageView)
}