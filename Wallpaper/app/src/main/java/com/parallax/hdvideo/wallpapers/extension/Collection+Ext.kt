package com.parallax.hdvideo.wallpapers.extension

import android.content.pm.PackageManager

fun <T> MutableList<T>.removeFirst(filter: ((T) -> Boolean)) : T? {
    val iterator = iterator()
    var current: T? = null
    while (iterator.hasNext()) {
        current = iterator.next()
        if (filter(current)) {
            iterator.remove()
            break
        } else {
            current = null
        }
    }
    return current
}

fun <K, V> Map<K,V>.firstValue(key: K?) : V? {
    for (entry in entries) {
        if (entry.key == key) {
            return entry.value
        }
    }
    return null
}
fun <K, V> Map<K,V>.firstKey(value: V?) : K? {
    for (entry in entries) {
        if (entry.key == value) {
            return entry.key
        }
    }
    return null
}

fun <K, V> Map<K, V>.getAsPair(position : Int) : Pair<K, V> {
    val list = toList()
    return list[position]
}

val IntArray.permissionGranted: Boolean get() = !any { it != PackageManager.PERMISSION_GRANTED }
val IntArray.permissionDenied: Boolean get() = !any { it != PackageManager.PERMISSION_DENIED }

public fun Array<String>.startsWith(element: String): Boolean {
    for (it in this) {
        if (element.startsWith(it, ignoreCase = true)) {
            return true
        }
    }
    return false
}

public fun Collection<String>.startsWith(element: String): Boolean {
    for (it in this) {
        if (element.startsWith(it, ignoreCase = true)) {
            return true
        }
    }
    return false
}
