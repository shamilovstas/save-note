package com.shamilovstas.text_encrypt.notes.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamilovstas.text_encrypt.notes.domain.ClearTextIsEmpty
import com.shamilovstas.text_encrypt.notes.domain.Note
import com.shamilovstas.text_encrypt.notes.domain.NotesInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ComposeNoteViewModel @Inject constructor(
    private val interactor: NotesInteractor
) : ViewModel() {

    private val _state = MutableStateFlow(EncryptScreenState())
    val state: StateFlow<EncryptScreenState> = _state
    private val _effects = MutableSharedFlow<ComposeScreenEffect>()
    val effects: SharedFlow<ComposeScreenEffect> = _effects

    private fun encrypt(note: Note, password: String) = viewModelScope.launch {
        try {
            val newNote = withContext(Dispatchers.IO) {
                interactor.save(note, password)
            }
            _effects.emit(ComposeScreenEffect.ComposeComplete(newNote))
        } catch (e: ClearTextIsEmpty) {
            _state.value = _state.value.copy(note = note.copy(content = ""), error = CryptoFeatureError.TextIsEmpty)
        }
    }

    fun onPasswordEntered(password: String) {
        val currentState = state.value.state
        val note = state.value.note
        if (currentState == ComposeScreenState.Encrypted) {
            decrypt(note, password)
        } else {
            encrypt(note, password)
        }
    }

    private fun decrypt(note: Note, password: String) = viewModelScope.launch {
        val decryptedNote = interactor.decrypt(note, password)
        _state.value = state.value.copy(note = decryptedNote, state = ComposeScreenState.Clear)
    }

    fun onTextChange() {
        _state.value = _state.value.copy(error = CryptoFeatureError.Clear)
    }

    fun loadNote(noteId: Int) = viewModelScope.launch {
        val note = withContext(Dispatchers.IO) { interactor.getNote(noteId) }
        _state.value = _state.value.copy(note = note, state = ComposeScreenState.Encrypted)
        _effects.emit(ComposeScreenEffect.RequestPassword)
    }

    fun saveNote(text: String) = viewModelScope.launch {
        val note = state.value.note.copy(content = text)
        _state.value = _state.value.copy(note = note, state = ComposeScreenState.Clear)
        _effects.emit(ComposeScreenEffect.RequestPassword)
    }

    fun onPasswordDialogDismissed() = viewModelScope.launch {
        if (state.value.state == ComposeScreenState.Encrypted) {
            _effects.emit(ComposeScreenEffect.ComposeCancelled)
        }
    }
}

data class EncryptScreenState(
    val note: Note = Note(),
    val state: ComposeScreenState = ComposeScreenState.Clear,
    val error: CryptoFeatureError = CryptoFeatureError.Clear
)

sealed class ComposeScreenEffect {
    data class ComposeComplete(val data: Note) : ComposeScreenEffect()
    data object ComposeCancelled : ComposeScreenEffect()
    data object RequestPassword : ComposeScreenEffect()
}

sealed class CryptoFeatureError {
    data object Clear : CryptoFeatureError()
    data object TextIsEmpty : CryptoFeatureError()
}

enum class ComposeScreenState {
    Clear, Encrypted
}