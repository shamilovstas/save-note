package com.shamilovstas.text_encrypt.importdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamilovstas.text_encrypt.notes.domain.Note
import com.shamilovstas.text_encrypt.notes.domain.NotesInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.crypto.BadPaddingException
import javax.inject.Inject

@HiltViewModel
class ImportMessageViewModel @Inject constructor(
    private val interactor: NotesInteractor
) : ViewModel() {

    private val _effect = MutableSharedFlow<ImportMessageScreenEffect>()
    val effect: SharedFlow<ImportMessageScreenEffect> = _effect

    private val _state = MutableStateFlow(ImportMessageScreenState())
    val state: StateFlow<ImportMessageScreenState> = _state

    fun onPasswordEntered(password: String) = viewModelScope.launch {
        val note = Note(content = state.value.encryptedContent)
        try {
            val decrypted = interactor.decrypt(note, password)
            _state.value = _state.value.copy(decryptedContent = decrypted.content, importEncryptionState = ImportEncryptionState.Decrypted)
        } catch (e: IllegalArgumentException) {
            _effect.emit(ImportMessageScreenEffect.MalformedEncryptedMessage)
        } catch (e: BadPaddingException) {
            _effect.emit(ImportMessageScreenEffect.WrongPassword)
        }
    }

    fun saveNote() = viewModelScope.launch {
        val note = Note(content = state.value.encryptedContent)
        interactor.saveEncrypted(note)
        _effect.emit(ImportMessageScreenEffect.NoteSavedMessage)
        _effect.emit(ImportMessageScreenEffect.ReturnToNoteList)
    }

    fun discard() = viewModelScope.launch {
        _state.value = _state.value.copy(decryptedContent = null, importEncryptionState = ImportEncryptionState.Encrypted)
    }

    fun decryptNote(encryptedContent: String?) = viewModelScope.launch {
        _state.value = _state.value.copy(encryptedContent = encryptedContent!!)
        _effect.emit(ImportMessageScreenEffect.RequestPassword)
    }
}

data class ImportMessageScreenState(
    val encryptedContent: String = "",
    val decryptedContent: String? = null,
    val importEncryptionState: ImportEncryptionState = ImportEncryptionState.Encrypted,
    val isDecryptionPossible: Boolean = false
)

enum class ImportEncryptionState {
    Encrypted, Decrypted
}

sealed class ImportMessageScreenEffect {
    data object RequestPassword : ImportMessageScreenEffect()
    data object ReturnToNoteList : ImportMessageScreenEffect()
    data object NoteSavedMessage : ImportMessageScreenEffect()
    data object WrongPassword: ImportMessageScreenEffect()
    data object MalformedEncryptedMessage: ImportMessageScreenEffect()
}