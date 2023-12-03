package com.shamilovstas.text_encrypt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.shamilovstas.text_encrypt.databinding.FragmentComposeNoteBinding
import com.shamilovstas.text_encrypt.notes.compose.ComposeNoteViewModel
import com.shamilovstas.text_encrypt.notes.compose.ComposeScreenEffect
import com.shamilovstas.text_encrypt.notes.compose.ComposeScreenState
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
        arguments?.let {
            val noteId = it.getInt("note_id", 0)
            if (noteId != 0) {
                viewModel.loadNote(noteId)
            }
        }
    }

    private fun initViews() {
        binding?.saveButton?.setOnClickListener {
            viewModel.saveNote(binding!!.editText.text.toString())
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
        }

        viewLifecycleOwner.lifecycleScope.launch {
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
                Snackbar.make(binding!!.root, getString(R.string.note_saved), Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }

            is ComposeScreenEffect.RequestPassword -> {
                childFragmentManager.clearFragmentResult(PasswordDialog.REQUEST_PASSWORD_FRAGMENT)
                childFragmentManager.setFragmentResultListener(PasswordDialog.REQUEST_PASSWORD_FRAGMENT, this) { _, bundle ->
                    onPasswordDialogResult(bundle)
                }
                val dialog = PasswordDialog()
                dialog.arguments = bundleOf(PasswordDialog.PREVIOUS_PASSWORD to it.previousPassword)
                dialog.show(childFragmentManager, "password_dialog")
            }

            is ComposeScreenEffect.ComposeCancelled -> {
                findNavController().navigateUp()
            }
            is ComposeScreenEffect.TextIsEmpty -> {
                Snackbar.make(binding!!.root, R.string.message_is_empty, Snackbar.LENGTH_SHORT)
                    .setIcon(R.drawable.error, com.google.android.material.R.color.design_default_color_error)
                    .show()
            }
            is ComposeScreenEffect.WrongPassword -> {
                Snackbar.make(binding!!.root, R.string.wrong_password, Snackbar.LENGTH_SHORT)
                    .show()
            }

        }
    }

    private fun onPasswordDialogResult(bundle: Bundle) {
        val isDismissed = bundle.getBoolean(PasswordDialogResult.DismissResult.KEY, false)

        if (isDismissed) {
            viewModel.onPasswordDialogDismissed()
        } else {
            val password = bundle.getString(PasswordDialogResult.PasswordResult.KEY)
                ?: throw IllegalStateException("Password cannot be null")
            viewModel.onPasswordEntered(password)
        }
    }


    private fun render(state: EncryptScreenState) = with(binding!!) {
        if (state.state == ComposeScreenState.Encrypted) {
            saveButton.text = getString(R.string.action_decrypt)
        } else {
            saveButton.text = getString(R.string.action_save)
        }

        if (editText.text.toString() != state.note.content) {
            editText.setText(state.note.content)
        }
    }
}