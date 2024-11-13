package com.example.videoart.batterychargeranimation.extension

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