package com.screentheme.app.utils.helpers

import android.content.Context
import android.os.Build
import java.util.Locale

const val PERMISSION_READ_CONTACTS = 5
const val GENERIC_PERM_HANDLER = 100
const val REQUEST_CODE_SET_DEFAULT_DIALER = 1007
const val REQUEST_CODE_WRITE_SETTINGS = 123

fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
fun isOreoMr1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
fun isSPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

fun isXiaomiDevice() = "xiaomi" == Build.MANUFACTURER.toLowerCase(Locale.ROOT)

val dummyContacts: List<Map<String, String>> = listOf(
    mapOf("contact_name" to "John Doe", "phone_number" to "+1 (123) 456-7890"),
    mapOf("contact_name" to "Jane Smith", "phone_number" to "+1 (987) 654-3210"),
    mapOf("contact_name" to "Alice Johnson", "phone_number" to "+1 (555) 555-5555"),
    mapOf("contact_name" to "Bob Anderson", "phone_number" to "+1 (999) 999-9999"),
    mapOf("contact_name" to "Michael Brown", "phone_number" to "+1 (111) 222-3333"),
    mapOf("contact_name" to "Emily Davis", "phone_number" to "+1 (444) 555-6666"),
    mapOf("contact_name" to "Sophia Wilson", "phone_number" to "+1 (777) 888-9999"),
    mapOf("contact_name" to "Matthew Thompson", "phone_number" to "+1 (222) 333-4444"),
)