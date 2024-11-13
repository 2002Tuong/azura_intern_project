package com.screentheme.app.data.remote.config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface ConfigModel

fun ConfigModel.config(key: String, default: Long): RemoteConfigLongDelegate {
    return RemoteConfigLongDelegate(key, default)
}

fun ConfigModel.config(key: String, default: Int): RemoteConfigIntDelegate {
    return RemoteConfigIntDelegate(key, default)
}

fun ConfigModel.config(key: String, default: String): RemoteConfigStringDelegate {
    return RemoteConfigStringDelegate(key, default)
}

fun ConfigModel.config(key: String, default: Double): RemoteConfigDoubleDelegate {
    return RemoteConfigDoubleDelegate(key, default)
}

fun ConfigModel.config(key: String, default: Boolean): RemoteConfigBooleanDelegate {
    return RemoteConfigBooleanDelegate(key, default)
}


abstract class RemoteConfigDelegate<out T : Any>
internal constructor(private val key: String, private val default: T) : ReadOnlyProperty<ConfigModel, T> {

    operator fun provideDelegate(thisRef: ConfigModel, property: KProperty<*>): ReadOnlyProperty<ConfigModel, T> {
        RemoteConfig.register(thisRef.javaClass, key, default)
        return this
    }
}

class RemoteConfigBooleanDelegate internal constructor(private val key: String, default: Boolean)
    : ReadOnlyProperty<ConfigModel, Boolean>, RemoteConfigDelegate<Boolean>(key, default) {

    override fun getValue(thisRef: ConfigModel, property: KProperty<*>): Boolean {
        return FirebaseRemoteConfig.getInstance().getBoolean(key)
    }
}

class RemoteConfigIntDelegate internal constructor(private val key: String, default: Int)
    : ReadOnlyProperty<ConfigModel, Int>, RemoteConfigDelegate<Int>(key, default) {

    override fun getValue(thisRef: ConfigModel, property: KProperty<*>): Int {
        return FirebaseRemoteConfig.getInstance().getLong(key).toInt()
    }
}

class RemoteConfigLongDelegate internal constructor(private val key: String, default: Long)
    : ReadOnlyProperty<ConfigModel, Long>, RemoteConfigDelegate<Long>(key, default) {

    override fun getValue(thisRef: ConfigModel, property: KProperty<*>): Long {
        return FirebaseRemoteConfig.getInstance().getLong(key)
    }
}

class RemoteConfigStringDelegate internal constructor(private val key: String, default: String)
    : ReadOnlyProperty<ConfigModel, String>, RemoteConfigDelegate<String>(key, default) {

    override fun getValue(thisRef: ConfigModel, property: KProperty<*>): String {
        return FirebaseRemoteConfig.getInstance().getString(key)
    }
}

class RemoteConfigDoubleDelegate internal constructor(private val key: String, default: Double)
    : ReadOnlyProperty<ConfigModel, Double>, RemoteConfigDelegate<Double>(key, default) {

    override fun getValue(thisRef: ConfigModel, property: KProperty<*>): Double {
        return FirebaseRemoteConfig.getInstance().getDouble(key)
    }
}