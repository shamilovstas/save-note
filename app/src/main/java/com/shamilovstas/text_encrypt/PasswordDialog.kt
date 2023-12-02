package com.shamilovstas.text_encrypt

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.shamilovstas.text_encrypt.databinding.DialogPasswordBinding


class PasswordDialog : DialogFragment() {


    companion object {
        const val REQUEST_PASSWORD_FRAGMENT = "req_password_fragment"
    }

    private var binding: DialogPasswordBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogPasswordBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.let { binding ->

            val editTextAction: (Editable?) -> Unit = {
                binding.tvPasswordDialogError.visibility = View.GONE
            }

            binding.etPassword.doAfterTextChanged(editTextAction)
            binding.etPasswordConfirm.doAfterTextChanged(editTextAction)

            binding.btnPasswordDialogConfirm.setOnClickListener {
                val password = binding.etPassword.text
                val passwordConfirmation = binding.etPasswordConfirm.text
                if (password.isNullOrBlank()) {
                    binding.tvPasswordDialogError.visibility = View.VISIBLE
                    binding.tvPasswordDialogError.text = getString(R.string.error_password_empty)
                    return@setOnClickListener
                } else if (passwordConfirmation == null || password.toString() != passwordConfirmation.toString()) {
                    binding.tvPasswordDialogError.visibility = View.VISIBLE
                    binding.tvPasswordDialogError.text = getString(R.string.error_password_do_not_match)
                    return@setOnClickListener
                } else {
                    binding.tvPasswordDialogError.visibility = View.GONE
                    returnResult(PasswordDialogResult.PasswordResult(password.toString()))
                }
            }

            binding.btnPasswordDialogCancel.setOnClickListener {
                returnResult(PasswordDialogResult.DismissResult)
            }
        }
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