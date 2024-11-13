package com.parallax.hdvideo.wallpapers.utils.other

import android.content.Context
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.parallax.hdvideo.wallpapers.utils.Logger

object Reflector {

    @Suppress("UNCHECKED_CAST")
    fun <T: Any>getDeclaredField(fieldOwner: Any?, field: String) : T? {
        if (fieldOwner == null) return null
        try {
            val file = fieldOwner.javaClass.getDeclaredField(field)
            if (!file.isAccessible) file.isAccessible = true
            return file.get(fieldOwner) as? T
        } catch (ignored: Exception) {
            Logger.e(ignored)
        }
        return null
    }

    fun invokeMethod(methodOwner: Any?, method: String, vararg arguments: TypedObject) {
        try {
            if (methodOwner == null) return
            val arrayOfClass =  Array<Class<*>?>(arguments.size, init = {null})
            val data =  Array<Any?>(arguments.size, init = {null})
            arguments.forEachIndexed {i, item ->
                arrayOfClass[i] = item.type
                data[i] = item.obj
            }
            val declareMethod = methodOwner.javaClass.getDeclaredMethod(method, *arrayOfClass)
            if (!declareMethod.isAccessible)
                declareMethod.isAccessible = true
            declareMethod.invoke(methodOwner, *data)
        } catch (ignored: Exception) {
            Logger.e("invokeMethod error ", method, ignored)
        }
    }

    fun fixInputMethodManager(context: Context?) {
        (context as? AppCompatActivity)?.run {
            invokeMethod(getSystemService(Context.INPUT_METHOD_SERVICE), "windowDismissed", TypedObject(window.decorView.windowToken, IBinder::class.java))
        }
    }

    class TypedObject(val obj: Any?, val type: Class<*>)
}