package com.slideshowmaker.slideshow.utils.extentions

import android.util.Log

inline fun <T> MutableList<T>.moveItemToFirstPosition(predicate: (T) -> Boolean) {
    for (element in this.withIndex()) {
        if (predicate(element.value)) {
            removeAt(element.index)
            add(0, element.value)
            break
        }
    }
}

inline fun <T> List<T>.moveItemToPosition(position: Int, predicate: (T) -> Boolean): List<T> {
    for (element in this.withIndex()) {
        if (predicate(element.value)) {
            return this.toMutableList().apply {
                removeAt(element.index)
                add(position - 1, element.value)
            }.toList()
        }
    }
    return this
}