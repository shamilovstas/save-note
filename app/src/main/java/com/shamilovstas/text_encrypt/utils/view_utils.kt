package com.shamilovstas.text_encrypt.utils

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

fun Snackbar.setIcon(drawable: Drawable, @ColorInt colorTint: Int, direction: IconDirection = IconDirection.Start): Snackbar {
    return this.apply {
        val textView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)

        drawable.setTint(colorTint)
        drawable.setTintMode(PorterDuff.Mode.SRC_ATOP)
        if (direction == IconDirection.Start) {
            textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        }
    }
}

fun Snackbar.setIcon(@DrawableRes drawableRes: Int, @ColorRes colorResTint: Int, direction: IconDirection = IconDirection.Start): Snackbar {
    return this.setIcon(ContextCompat.getDrawable(this.context, drawableRes)!!, ContextCompat.getColor(this.context, colorResTint), direction)
}

enum class IconDirection {
    Start, End
}