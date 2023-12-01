package com.shamilovstas.text_encrypt.notes.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.shamilovstas.text_encrypt.databinding.FragmentNoteListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotesListFragment : Fragment() {

    private val viewModel by viewModels<NotesListViewModel>()
    private var binding: FragmentNoteListBinding? = null
    private val adapter = NotesAdapter()
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

    private fun initViews(binding: FragmentNoteListBinding) {
        binding.recyclerNoteList.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    render(it)
                }
            }
        }
        viewModel.loadNotes()
    }

    private fun render(state: NotesListScreenState) = with(binding!!) {
        adapter.submitList(state.notes)
    }
}