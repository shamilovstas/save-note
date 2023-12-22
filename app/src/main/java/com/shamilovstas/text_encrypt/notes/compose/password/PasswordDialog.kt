package com.shamilovstas.text_encrypt.notes.compose.password

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.databinding.DialogPasswordBinding


class PasswordDialog : DialogFragment() {


    companion object {
        const val REQUEST_PASSWORD_FRAGMENT = "req_password_fragment"
        const val PREVIOUS_PASSWORD = "previous_password"
        const val DIALOG_MODE = "dialog_mode"
    }

    private var binding: DialogPasswordBinding? = null
    private lateinit var mode: PasswordDialogMode
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogPasswordBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mode = getMode(arguments)
        val previousPassword = arguments?.getString(PREVIOUS_PASSWORD, null)

        binding?.let { binding ->

            if (previousPassword != null) {
                setupPreviousPasswordViews(previousPassword)
            }
            val editTextAction: (Editable?) -> Unit = {
                binding.tvPasswordDialogError.visibility = View.GONE
            }

            if (mode is PasswordDialogMode.EnterPassword) {
                binding.tilPasswordConfirm.visibility = View.GONE
            } else {
                binding.etPasswordConfirm.doAfterTextChanged(editTextAction)
            }

            binding.etPassword.doAfterTextChanged(editTextAction)

            binding.btnPasswordDialogConfirm.setOnClickListener {
                onPasswordConfirmClicked()
            }

            binding.btnPasswordDialogCancel.setOnClickListener {
                returnResult(PasswordDialogResult.DismissResult)
            }
        }
    }

    private fun setupPreviousPasswordViews(previousPassword: String?) = with(binding!!) {
        cbUseInitialPassword.visibility = View.VISIBLE
        cbUseInitialPassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etPassword.setText(previousPassword)
                etPasswordConfirm.setText(previousPassword)
            } else {
                etPassword.text?.clear()
                etPasswordConfirm.text?.clear()
            }
        }
    }

    private fun getMode(arguments: Bundle?): PasswordDialogMode {
        return if (arguments == null) {
            PasswordDialogMode.CreatePassword
        } else {
            PasswordDialogMode.getMode(
                arguments.getString(DIALOG_MODE, PasswordDialogMode.CreatePassword.key)
            )
        }
    }

    private fun onPasswordConfirmClicked() = with(binding!!) {

        val password = etPassword.text
        val passwordConfirmation = etPasswordConfirm.text

        if (password.isNullOrBlank()) {
            tvPasswordDialogError.visibility = View.VISIBLE
            tvPasswordDialogError.text = getString(R.string.error_password_empty)
            return
        }
        val isCreatingPassword = mode is PasswordDialogMode.CreatePassword
        val arePasswordsMatch = passwordConfirmation == null || password.toString() != passwordConfirmation.toString()

        if (isCreatingPassword && arePasswordsMatch) {
            tvPasswordDialogError.visibility = View.VISIBLE
            tvPasswordDialogError.text = getString(R.string.error_password_do_not_match)
            return
        }

        tvPasswordDialogError.visibility = View.GONE
        returnResult(PasswordDialogResult.PasswordResult(password.toString()))
    }

    private fun returnResult(result: PasswordDialogResult) {
        parentFragmentManager.setFragmentResult(REQUEST_PASSWORD_FRAGMENT, result.toBundle())
        dialog?.dismiss()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}

sealed class PasswordDialogResult {
    abstract fun toBundle(): Bundle
    data class PasswordResult(val password: String) : PasswordDialogResult() {

        companion object {
            const val KEY = "password_result"
        }

        override fun toBundle() = bundleOf(KEY to password)
    }

    data object DismissResult : PasswordDialogResult() {

        const val KEY = "dismiss_result"
        override fun toBundle() = bundleOf(KEY to true)
    }
}

sealed class PasswordDialogMode(val key: String) {

    companion object {
        private const val KEY_CREATE_PASSWORD = "key_create_password"
        private const val KEY_ENTER_PASSWORD = "key_enter_password"

        fun getMode(key: String): PasswordDialogMode {
            return when (key) {
                KEY_CREATE_PASSWORD -> CreatePassword
                KEY_ENTER_PASSWORD -> EnterPassword
                else -> throw IllegalArgumentException("Unknown mode key: $key")
            }
        }
    }

    data object CreatePassword : PasswordDialogMode(KEY_CREATE_PASSWORD)
    data object EnterPassword : PasswordDialogMode(KEY_ENTER_PASSWORD)
}