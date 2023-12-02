package com.shamilovstas.text_encrypt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.shamilovstas.text_encrypt.databinding.FragmentComposeNoteBinding
import com.shamilovstas.text_encrypt.notes.compose.ComposeNoteViewModel
import com.shamilovstas.text_encrypt.notes.compose.ComposeScreenEffect
import com.shamilovstas.text_encrypt.notes.compose.CryptoFeatureError
import com.shamilovstas.text_encrypt.notes.compose.EncryptScreenState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ComposeNoteFragment : Fragment() {


    private val viewModel by viewModels<ComposeNoteViewModel>()

    private var binding: FragmentComposeNoteBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentComposeNoteBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(PasswordDialog.REQUEST_PASSWORD_FRAGMENT, this) { _, bundle ->
            val password = bundle.getString(PasswordDialog.BUNDLE_PASSWORD_RESULT)
                ?: throw IllegalStateException("Password cannot be null")
            Log.d("TextEncryptor", "Password: $password")
            viewModel.encrypt(password, getMessage())
        }
    }

    private fun getMessage(): String? {
        return binding?.editText?.text?.toString()
    }

    private fun initViews() {
        binding?.saveButton?.setOnClickListener {
            PasswordDialog().show(childFragmentManager, "password_dialog")
        }

        binding?.editText?.doAfterTextChanged {
            viewModel.onTextChange()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    render(it)
                }
            }

            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect {
                    effect(it)
                }
            }
        }
    }

    private fun effect(it: ComposeScreenEffect) {
        when (it) {
            is ComposeScreenEffect.ComposeComplete -> {
                Snackbar.make(binding!!.root, getString(R.string.note_created), Snackbar.LENGTH_SHORT).show()
                //finish()
            }
        }
    }


    private fun render(state: EncryptScreenState) = with(binding!!) {
        if (state.error != CryptoFeatureError.Clear) {
            showError(state.error)
        }
        editText.setText(state.processedContent)
    }

    private fun showError(error: CryptoFeatureError) {
        val messageRes: Int
        when (error) {
            is CryptoFeatureError.TextIsEmpty -> messageRes = R.string.message_is_empty
            else -> return
        }
        Snackbar.make(binding!!.root, messageRes, LENGTH_SHORT).show()
    }
}