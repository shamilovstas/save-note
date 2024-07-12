package com.shamilovstas.text_encrypt.notes.compose

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.base.ToolbarFragment
import com.shamilovstas.text_encrypt.databinding.FragmentComposeNoteBinding
import com.shamilovstas.text_encrypt.notes.compose.password.PasswordDialogBuilder
import com.shamilovstas.text_encrypt.notes.domain.Attachment
import com.shamilovstas.text_encrypt.notes.domain.CleanerLifecycleObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ComposeNoteFragment : ToolbarFragment() {

    private var binding: FragmentComposeNoteBinding? = null
    private val viewModel by viewModels<ComposeNoteViewModel>()
    private val attachmentsAdapter = AttachmentAdapter(
        onAttachmentClick = ::onAttachmentClicked
    )
    @Inject
    lateinit var cleanerObserver: CleanerLifecycleObserver

    private val pickFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        viewModel.addAttachment(uri, requireActivity().contentResolver)
    }

    private val saveFile = registerForActivityResult(ActivityResultContracts.CreateDocument("image/*")) { uri ->
        if (uri == null) return@registerForActivityResult
        viewModel.saveAttachment(uri, requireActivity().contentResolver)
    }

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

        fun loadByIdArgs(noteId: Long): Bundle {
            return bundleOf(
                KEY_MODE to CipherState.Encrypted.ordinal,
                KEY_NOTE_ID to noteId
            )
        }
    }

    private fun onAttachmentClicked(attachment: Attachment) {

        if (attachment.isDecrypted) {
            viewModel.prepareAttachmentForSaving(attachment)
            saveFile.launch(attachment.filename)
        }
    }

    private fun parseArguments() {
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
                val noteId = requireArguments().getLong(KEY_NOTE_ID)
                viewModel.loadNote(noteId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cleanerObserver.init(this.lifecycle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentComposeNoteBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        parseArguments()
    }

    private fun initViews() = with(binding!!) {
        rvAttachments.adapter = attachmentsAdapter

        btnAddAttachment.setOnClickListener {
            pickFile.launch(arrayOf("*/*"))
        }

        editText.doAfterTextChanged {
            btnDecryptNote.isEnabled = !it.isNullOrEmpty()
            it?.let { viewModel.setContent(it.toString()) }
        }

        descriptionEditText.doAfterTextChanged {
            it?.let { viewModel.setDescription(it.toString()) }
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
            repeatOnLifecycle(Lifecycle.State.CREATED) {
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
                PasswordDialogBuilder(childFragmentManager, this)
                    .tag("password_dialog")
                    .onResult { viewModel.onPasswordEntered(it) }
                    .previousPassword(effect.previousPassword)
                    .mode(effect.dialogMode)
                    .show()
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

            is ImportMessageScreenEffect.UnknownFiletype -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.corrupted_file_dialog_title)
                    .setMessage(R.string.corrupted_file_dialog_message)
                    .setPositiveButton(R.string.corrupted_file_ok_button) { dialog, _ ->
                        dialog.dismiss()
                        findNavController().navigateUp()
                    }
                    .create().show()
            }

            is ImportMessageScreenEffect.DownloadedAttachment -> {
                Snackbar.make(binding!!.root, getString(R.string.attachment_saved, effect.filename), Snackbar.LENGTH_LONG)
//                    .setAction(R.string.action_open_attachment) {
//                        val intent = Intent()
//                        intent.setDataAndType(effect.uri, "image/*")
//                        intent.action = Intent.ACTION_VIEW
//                        startActivity(Intent.createChooser(intent, getString(R.string.dialog_title_open_with)))
//                    }
                    .show()

            }
        }
    }

    private fun render(state: ComposeNoteScreenState) = with(binding!!) {
        btnAddAttachment.isVisible = state.canAddAttachments
        if (state.cipherState == CipherState.Encrypted) {
            btnDecryptNote.visibility = View.VISIBLE
            btnSaveImportedNote.visibility = View.GONE
            tilEditText.hint = getString(R.string.import_content_hint)
        } else {
            btnDecryptNote.visibility = View.GONE
            btnSaveImportedNote.visibility = View.VISIBLE
            tilEditText.hint = getString(R.string.enter_message)
            tilDescriptionText.visibility = View.VISIBLE
        }

        if (state.note.content != editText.text?.toString()) {
            editText.setText(state.note.content)
        }
        if (state.note.description != descriptionEditText.text?.toString()) {
            descriptionEditText.setText(state.note.description)
        }
        attachmentsAdapter.submitList(state.note.attachments)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}