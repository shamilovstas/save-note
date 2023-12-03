package com.shamilovstas.text_encrypt.notes.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.shamilovstas.text_encrypt.ComposeNoteFragment
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.databinding.FragmentNoteListBinding
import com.shamilovstas.text_encrypt.notes.domain.Note
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotesListFragment : Fragment() {

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
        initViews(binding!!)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun onClickNoteItem(note: Note) {
        findNavController().navigate(R.id.action_from_list_to_compose, bundleOf("note_id" to note.id))
    }

    private fun onClickDeleteNoteItem(item: Note) {
        viewModel.deleteNote(item)
    }

    private fun onClickShareNoteItem(item: Note) {
        TODO()
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

    private fun effect(effects: NotesListEffects) = when(effects) {
        is NotesListEffects.NoteContentCopied -> {
            Snackbar.make(binding!!.root, getString(R.string.message_note_text_copied), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun navigateToComposeNoteScreen() {
        findNavController().navigate(R.id.action_from_list_to_compose)
    }

    private fun render(state: NotesListScreenState) = with(binding!!) {
        adapter.submitList(state.notes)
    }
}