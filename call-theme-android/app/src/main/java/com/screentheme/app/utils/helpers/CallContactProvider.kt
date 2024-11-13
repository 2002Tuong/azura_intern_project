package com.screentheme.app.utils.helpers

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import com.screentheme.app.R
import com.screentheme.app.models.CallContactModel

fun getCallContact(context: Context, call: Call?, callback: (CallContactModel) -> Unit) {
    val contactId = call?.details?.handle?.schemeSpecificPart ?: return

    val contentResolver = context.contentResolver
    val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contactId))

    var contactName = ""
    val cursor =
        contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_URI), null, null, null)

    if (cursor != null) {
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))

            val photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))

            val callContact = CallContactModel(contactName, photoUri ?: "", contactId, "")

            callback(callContact)

        } else {
            callback(CallContactModel(context.getString(R.string.unknown_caller), "", contactId, ""))
        }
        cursor.close()
    } else {
        callback(CallContactModel(context.getString(R.string.unknown_caller), "", contactId, ""))
    }

}