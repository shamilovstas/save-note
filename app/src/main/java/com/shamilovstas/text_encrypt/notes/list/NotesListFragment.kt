package com.shamilovstas.text_encrypt.notes.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.base.ToolbarFragment
import com.shamilovstas.text_encrypt.databinding.FragmentNoteListBinding
import com.shamilovstas.text_encrypt.importdata.ComposeNoteFragment
import com.shamilovstas.text_encrypt.notes.domain.Note
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotesListFragment : ToolbarFragment() {

    private val viewModel by viewModels<NotesListViewModel>()
    private var binding: FragmentNoteListBinding? = null
    private val adapter = NotesAdapter(
        onClickListener = ::onClickNoteItem,
        onDeleteClickListener = ::onClickDeleteNoteItem,
        onShareClickListener = ::onClickShareNoteItem,
        onCopyClickListener = ::onClickCopyNote
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNoteListBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(binding!!)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun onClickNoteItem(note: Note) {
        findNavController().navigate(R.id.action_from_list_to_note_detail, ComposeNoteFragment.loadByIdArgs(note.id))
    }

    private fun onClickDeleteNoteItem(item: Note) {
        viewModel.deleteNote(item)
    }

    val createDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        viewModel.exportNote(uri, requireActivity().contentResolver)
    }
    private fun onClickShareNoteItem(item: Note) {
        viewModel.onClickShareNoteItem(item)
    }

    private fun onClickCopyNote(item: Note) {
        viewModel.copyNote(requireContext(), item)
    }

    private fun initViews(binding: FragmentNoteListBinding) {
        binding.recyclerNoteList.adapter = adapter
        binding.buttonAddNote.setOnClickListener {
            navigateToComposeNoteScreen()
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
        viewModel.loadNotes()
    }

    private fun effect(effect: NotesListEffects) = when(effect) {
        is NotesListEffects.NoteContentCopied -> {
            Snackbar.make(binding!!.root, getString(R.string.message_note_text_copied), Snackbar.LENGTH_SHORT).show()
        }
        is NotesListEffects.CreatePublicFile -> {
            createDocument.launch(effect.filename)
        }
        is NotesListEffects.NoteExported -> {
            Snackbar.make(binding!!.root, getString(R.string.note_exported, effect.filename), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun navigateToComposeNoteScreen() {
        findNavController().navigate(R.id.action_from_list_to_compose, ComposeNoteFragment.composeArgs())
    }

    private fun render(state: NotesListScreenState) = with(binding!!) {
        adapter.submitList(state.notes)
    }
}