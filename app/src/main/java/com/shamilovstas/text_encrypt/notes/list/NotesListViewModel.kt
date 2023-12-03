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
import kotlinx.coroutines.withContext
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

    fun deleteNote(item: Note) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            interactor.deleteNote(item)
        }
    }

}

data class NotesListScreenState(
    val notes: List<Note> = listOf()
)