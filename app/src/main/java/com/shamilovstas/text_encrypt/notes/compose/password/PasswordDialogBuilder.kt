package com.shamilovstas.text_encrypt.notes.compose.password

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner

class PasswordDialogBuilder(
    private val fragmentManager: FragmentManager,
    private val lifecycleOwner: LifecycleOwner
) {

    private var tag: String? = null
    private var onResult: (String) -> Unit = {}
    private var onDismiss: () -> Unit = {}
    private val args = Bundle()


    fun tag(tag: String): PasswordDialogBuilder {
        this.tag = tag
        return this
    }

    fun onResult(onResult: (String) -> Unit): PasswordDialogBuilder {
        this.onResult = onResult
        return this
    }

    fun onDismiss(onDismiss: () -> Unit): PasswordDialogBuilder {
        this.onDismiss = onDismiss
        return this
    }

    fun previousPassword(previousPassword: String?) : PasswordDialogBuilder {
        this.args.putString(PasswordDialog.PREVIOUS_PASSWORD, previousPassword)
        return this
    }

    fun mode(mode: PasswordDialogMode): PasswordDialogBuilder {
        this.args.putString(PasswordDialog.DIALOG_MODE, mode.key)
        return this
    }

    fun show() {
        fragmentManager.clearFragmentResult(PasswordDialog.REQUEST_PASSWORD_FRAGMENT)
        fragmentManager.setFragmentResultListener(PasswordDialog.REQUEST_PASSWORD_FRAGMENT, lifecycleOwner) { _, bundle ->
            val isDismissed = bundle.getBoolean(PasswordDialogResult.DismissResult.KEY, false)

            if (isDismissed) {
                onDismiss()
            } else {
                val password = bundle.getString(PasswordDialogResult.PasswordResult.KEY)
                    ?: throw IllegalStateException("Password cannot be null")
                onResult(password)
            }
        }
        val dialog = PasswordDialog()
        dialog.arguments = args
        dialog.show(fragmentManager, tag)
    }
}