package com.shamilovstas.text_encrypt.notes.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamilovstas.text_encrypt.notes.domain.Note
import com.shamilovstas.text_encrypt.notes.domain.NotesInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val interactor: NotesInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(NotesListScreenState())
    val state: StateFlow<NotesListScreenState> = _state


    fun loadNotes() = viewModelScope.launch {
        val flow = interactor.getAllNotes()
        flow.flowOn(Dispatchers.IO).collect {
            _state.value = _state.value.copy(notes = it)
        }
    }

}

data class NotesListScreenState(
    val notes: List<Note> = listOf()
)