package com.shamilovstas.text_encrypt.importdata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.databinding.FragmentImportBinding
import com.shamilovstas.text_encrypt.showPasswordDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImportMessageFragment : Fragment() {

    private var binding: FragmentImportBinding? = null
    private val viewModel by viewModels<ImportMessageViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentImportBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
    }

    private fun initViews() = with(binding!!) {

        btnDecryptNote.setOnClickListener {
            val encryptedContent = editText.text?.toString()
            viewModel.decryptNote(encryptedContent)
        }

        btnSaveImportedNote.setOnClickListener {
            viewModel.saveNote()
        }

        btnDiscardImportedNote.setOnClickListener {
            editText.text?.clear()
            viewModel.discard()
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
        }
    }

    private fun render(state: ImportMessageScreenState) = with(binding!!) {
        if (state.importEncryptionState == ImportEncryptionState.Encrypted) {
            btnDecryptNote.visibility = View.VISIBLE
            groupImportControls.visibility = View.GONE
            editText.isEnabled = true
        } else {
            btnDecryptNote.visibility = View.GONE
            groupImportControls.visibility = View.VISIBLE
            editText.isEnabled = false
        }

        if (state.decryptedContent != null) {
            editText.setText(state.decryptedContent)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}