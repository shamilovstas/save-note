package com.shamilovstas.text_encrypt.notes.compose

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
import com.shamilovstas.text_encrypt.PasswordDialog
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.base.ToolbarFragment
import com.shamilovstas.text_encrypt.databinding.FragmentComposeNoteBinding
import com.shamilovstas.text_encrypt.showPasswordDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ComposeNoteFragment : ToolbarFragment() {

    private var binding: FragmentComposeNoteBinding? = null
    private val viewModel by viewModels<ComposeNoteViewModel>()

    companion object {

        private const val KEY_FILE_URI = "fileUri"
        private const val KEY_MODE = "key_mode"
        private const val KEY_NOTE_ID = "note_id"
        fun fileImportArgs(uri: Uri): Bundle {
            return bundleOf(
                KEY_FILE_URI to uri,
                KEY_MODE to CipherState.Encrypted.ordinal
            )
        }

        fun composeArgs(): Bundle {
            return bundleOf(KEY_MODE to CipherState.Decrypted.ordinal)
        }

        fun importMessageArgs(): Bundle {
            return bundleOf(KEY_MODE to CipherState.Encrypted.ordinal)
        }

        fun loadByIdArgs(noteId: Int): Bundle {
            return bundleOf(
                KEY_MODE to CipherState.Encrypted.ordinal,
                KEY_NOTE_ID to noteId
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!requireArguments().containsKey(KEY_MODE)) {
            throw IllegalStateException("Mode not provided")
        }

        val cipherMode = CipherState.entries[requireArguments().getInt(KEY_MODE)]
        viewModel.setCipherMode(cipherMode)

        when {
            requireArguments().containsKey(KEY_FILE_URI) -> {
                val fileUri = requireArguments().getParcelable<Uri>(KEY_FILE_URI)!!
                viewModel.import(fileUri, requireActivity().contentResolver)
            }

            requireArguments().containsKey(KEY_NOTE_ID) -> {
                val noteId = requireArguments().getInt(KEY_NOTE_ID)
                viewModel.loadNote(noteId)
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentComposeNoteBinding.inflate(inflater, container, false)
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
        when (effect) {
            is ImportMessageScreenEffect.ComposeComplete -> {
                findNavController().navigateUp()
            }

            is ImportMessageScreenEffect.RequestPassword -> {
                childFragmentManager.showPasswordDialog(
                    this,
                    "password_dialog",
                    onResult = { viewModel.onPasswordEntered(it) },
                    args = bundleOf(PasswordDialog.PREVIOUS_PASSWORD to effect.previousPassword)
                )
            }

            is ImportMessageScreenEffect.NoteSavedMessage -> {
                Snackbar.make(binding!!.root, R.string.note_saved, Snackbar.LENGTH_SHORT).show()
            }

            is ImportMessageScreenEffect.ReturnToNoteList -> {
                findNavController().navigate(R.id.action_from_message_import_to_note_list)
            }

            is ImportMessageScreenEffect.WrongPassword -> {
                Snackbar.make(binding!!.root, R.string.wrong_password, Snackbar.LENGTH_SHORT).show()
            }

            is ImportMessageScreenEffect.MalformedEncryptedMessage -> {
                Snackbar.make(binding!!.root, R.string.message_malformed, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun render(state: ComposeNoteScreenState) = with(binding!!) {
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

        if (state.content != null && state.content != editText.text?.toString()) {
            editText.setText(state.content)
        }
        if (state.description != descriptionEditText.text?.toString()) {
            descriptionEditText.setText(state.description)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}