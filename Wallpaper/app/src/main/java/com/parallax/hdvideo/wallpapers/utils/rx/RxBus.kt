

package com.parallax.hdvideo.wallpapers.utils.rx

import android.os.Handler
import android.os.Looper
import com.google.gson.internal.Primitives
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

object RxBus {

    const val STATUS_CODE = "STATUS_CODE"
    const val DISMISS_FRAGMENT_DETAIL = "DISMISS_FRAGMENT_DETAIL"
    const val NOTIFICATION = "NOTIFICATION"

    private val handler = Handler(Looper.getMainLooper())
    private val hashMap = HashMap<String,  Disposable>()
//    val listKClass = mutableListOf<KClass<*>>()
    private val publishSubject: PublishSubject<Any> by lazy {
        val bs = PublishSubject.create<Any>()
        bs.subscribeOn(Schedulers.io())
        bs.observeOn(AndroidSchedulers.mainThread())
        bs
    }

    fun push(data: Any, delay: Long = 0) {
        if (delay > 0) {
            Timer().schedule(object : TimerTask(){
                override fun run() {
                    publishSubject.onNext(data)
                }

            }, delay)
        } else {
            publishSubject.onNext(data)
        }
    }

    // Chỉ dành cho đăng ký một nơi
    // khi đăng ký nơi khác cùng clazz thì cũ sẽ bị huỷ
/*    fun <T: Any> subscribe(clazz: KClass<T>, callback: ((T) -> Unit)? = null) {
        unregister(clazz.java)
        listKClass.add(clazz)
        map[clazz.java.basicName] = subject.ofType(clazz.java).subscribe{onNext ->
            callback?.invoke(onNext)
        }
    }*/
//    fun unregister(clazz: Class<*>) {
//        map.remove(clazz.name)?.dispose()
//    }

    // Đăng ký nhiều lần với tên khác nhau
    fun <T: Any> subscribe(name: String, clazz: KClass<T> , callback: ((T) -> Unit)? = null) {
        unregister(name)
        val disposable = publishSubject.ofType(clazz.java).subscribe { onNext ->
            handler.post {  callback?.invoke(onNext) }
        }
        hashMap[name] = disposable
    }

    fun unregister(name: String) {
        hashMap.remove(name)?.dispose()
    }

    fun <T> cast(classOfT: Class<T>, data: Any): T? {
        return Primitives.wrap(classOfT).cast(data)
    }
}
