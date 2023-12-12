package com.shamilovstas.text_encrypt.importdata

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.base.ToolbarFragment
import com.shamilovstas.text_encrypt.databinding.FragmentImportMessageBinding
import com.shamilovstas.text_encrypt.notes.compose.CipherState
import com.shamilovstas.text_encrypt.showPasswordDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImportMessageFragment : ToolbarFragment() {

    private var binding: FragmentImportMessageBinding? = null
    private val viewModel by viewModels<ImportMessageViewModel>()

    companion object {

        private const val KEY_FILE_URI = "fileUri"
        private const val KEY_MODE = "key_mode"
        fun fileImportArgs(uri: Uri): Bundle {
            return bundleOf(
                KEY_FILE_URI to uri,
                KEY_MODE to CipherState.Encrypted.ordinal
            )
        }

        fun composeArgs(): Bundle {
            return bundleOf(KEY_MODE to CipherState.Decrypted.ordinal)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!requireArguments().containsKey(KEY_MODE)) {
            throw IllegalStateException("Mode not provided")
        }

        val cipherMode = CipherState.entries[requireArguments().getInt(KEY_MODE)]
        viewModel.setCipherMode(cipherMode)
        val fileUri = arguments?.getParcelable<Uri>(KEY_FILE_URI)

        if (fileUri != null) {
            viewModel.import(fileUri, requireActivity().contentResolver)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentImportMessageBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() = with(binding!!) {

        editText.doAfterTextChanged {
            btnDecryptNote.isEnabled = !it.isNullOrEmpty()
        }

        btnDecryptNote.setOnClickListener {
            val encryptedContent = editText.text?.toString()
            viewModel.decryptNote(encryptedContent)
        }

        btnSaveImportedNote.setOnClickListener {
            val description = descriptionEditText.text?.toString() ?: ""
            viewModel.saveNote(editText.text?.toString() ?: "", description = description)
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
                viewModel.effect.collect {
                    effect(it)
                }
            }
        }
    }

    private fun effect(effect: ImportMessageScreenEffect) {
        when(effect) {
            is ImportMessageScreenEffect.ComposeComplete -> {
                findNavController().navigateUp()
            }
            is ImportMessageScreenEffect.RequestPassword -> {
                childFragmentManager.showPasswordDialog(
                    this,
                    "password_dialog",
                    onResult = {viewModel.onPasswordEntered(it)})
            }
            is ImportMessageScreenEffect.NoteSavedMessage -> {
                Snackbar.make(binding!!.root, R.string.note_saved, Snackbar.LENGTH_SHORT).show()
            }
            is ImportMessageScreenEffect.ReturnToNoteList -> {
                findNavController().navigate(R.id.action_from_message_import_to_list)
            }
            is ImportMessageScreenEffect.WrongPassword -> {
                Snackbar.make(binding!!.root, R.string.wrong_password, Snackbar.LENGTH_SHORT).show()
            }
            is ImportMessageScreenEffect.MalformedEncryptedMessage -> {
                Snackbar.make(binding!!.root, R.string.message_malformed, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun render(state: ImportMessageScreenState) = with(binding!!) {
        if (state.cipherState == CipherState.Encrypted) {
            btnDecryptNote.visibility = View.VISIBLE
            groupImportControls.visibility = View.GONE
            tilEditText.hint = getString(R.string.import_content_hint)
        } else {
            btnDecryptNote.visibility = View.GONE
            groupImportControls.visibility = View.VISIBLE
            tilEditText.hint = getString(R.string.enter_message)
            tilDescriptionText.visibility = View.VISIBLE
        }

        if (state.content != null) {
            editText.setText(state.content)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}