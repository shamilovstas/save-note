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
import javax.crypto.BadPaddingException
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
            _effects.emit(ComposeScreenEffect.TextIsEmpty)
        }
    }

    fun onPasswordEntered(password: String) {
        val currentState = state.value.cipherState
        val note = state.value.note
        if (currentState == CipherState.Encrypted) {
            _state.value = _state.value.copy(previousPassword = password)
            decrypt(note, password)
        } else {
            encrypt(note, password)
        }
    }

    private fun decrypt(note: Note, password: String) = viewModelScope.launch {
        try {
            val decryptedNote = interactor.decrypt(note, password)
            _state.value = state.value.copy(note = decryptedNote, cipherState = CipherState.Decrypted)
        } catch (e: BadPaddingException) {
            _effects.emit(ComposeScreenEffect.WrongPassword)
        }
    }

    fun onTextChange() {
    }

    fun loadNote(noteId: Int) = viewModelScope.launch {
        val note = withContext(Dispatchers.IO) { interactor.getNote(noteId) }
        _state.value = _state.value.copy(note = note, cipherState = CipherState.Encrypted)
        _effects.emit(ComposeScreenEffect.RequestPassword(state.value.previousPassword))
    }

    fun saveNote(text: String, description: String) = viewModelScope.launch {
        val note = state.value.note.copy(content = text, description = description)
        _state.value = _state.value.copy(note = note)
        _effects.emit(ComposeScreenEffect.RequestPassword(state.value.previousPassword))
    }

    fun onPasswordDialogDismissed() = viewModelScope.launch {
        if (state.value.cipherState == CipherState.Encrypted) {
            _effects.emit(ComposeScreenEffect.ComposeCancelled)
        }
    }
}

sealed class ComposeScreenEffect {
    data class ComposeComplete(val data: Note) : ComposeScreenEffect()
    data object ComposeCancelled : ComposeScreenEffect()
    data class RequestPassword(val previousPassword: String? = null) : ComposeScreenEffect()
    data object WrongPassword : ComposeScreenEffect()
    data object TextIsEmpty : ComposeScreenEffect()
}