package com.shamilovstas.text_encrypt.utils

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

fun Uri.getFilename(contentResolver: ContentResolver): String {
    var filename = ""

    if (this.scheme == "file") {
        val lastIndex = this.path!!.lastIndexOf("/")
        filename = this.path?.substring(lastIndex + 1) ?: ""

        //TODO remove
    } else {
        contentResolver.query(this, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            filename = cursor.getString(nameIndex)
        }
    }

    return filename
}