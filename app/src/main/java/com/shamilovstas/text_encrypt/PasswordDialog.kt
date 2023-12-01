package com.shamilovstas.text_encrypt

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.shamilovstas.text_encrypt.databinding.DialogPasswordBinding


class PasswordDialog : DialogFragment() {


    companion object {
        const val REQUEST_PASSWORD_FRAGMENT = "req_password_fragment"
        const val BUNDLE_PASSWORD_RESULT = "bundle_password_result"
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
                    returnResult(password.toString())
                }
            }
        }
    }

    private fun returnResult(password: String) {
        parentFragmentManager.setFragmentResult(REQUEST_PASSWORD_FRAGMENT, bundleOf(BUNDLE_PASSWORD_RESULT to password))
        dialog?.dismiss()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

}