package com.shamilovstas.text_encrypt.importdata

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamilovstas.text_encrypt.notes.domain.EncryptedMessageMalformed
import com.shamilovstas.text_encrypt.files.FileInteractor
import com.shamilovstas.text_encrypt.notes.compose.CipherState
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
    private val notesInteractor: NotesInteractor,
    private val fileInteractor: FileInteractor
) : ViewModel() {

    private val _effect = MutableSharedFlow<ImportMessageScreenEffect>()
    val effect: SharedFlow<ImportMessageScreenEffect> = _effect

    private val _state = MutableStateFlow(ImportMessageScreenState())
    val state: StateFlow<ImportMessageScreenState> = _state

    fun onPasswordEntered(password: String) = viewModelScope.launch {
        val cipherState = state.value.cipherState
        val content = requireNotNull(state.value.content)
        val note = Note(content = content, description = state.value.description)
        try {
            val processedNote =  if (cipherState == CipherState.Decrypted) {
                notesInteractor.save(note, password)
             } else {
                notesInteractor.decrypt(note, password)
            }

            if (cipherState == CipherState.Encrypted) {
                _state.value = _state.value.copy(content = processedNote.content, cipherState = CipherState.Decrypted)
            } else {
                _effect.emit(ImportMessageScreenEffect.NoteSavedMessage)
                _effect.emit(ImportMessageScreenEffect.ComposeComplete)
            }
        } catch (e: EncryptedMessageMalformed) {
            _effect.emit(ImportMessageScreenEffect.MalformedEncryptedMessage)
        } catch (e: IllegalArgumentException) {
            _effect.emit(ImportMessageScreenEffect.MalformedEncryptedMessage)
        } catch (e: BadPaddingException) {
            _effect.emit(ImportMessageScreenEffect.WrongPassword)
        }
    }

    fun saveNote(content: String, description: String = "") = viewModelScope.launch {
        val cipherState = state.value.cipherState
        _state.value = _state.value.copy(content = content, description = description)
        if (cipherState == CipherState.Decrypted) {
            _effect.emit(ImportMessageScreenEffect.RequestPassword)
        } else {
            val note = Note(content = content, description = description)
            notesInteractor.save(note)
            _effect.emit(ImportMessageScreenEffect.NoteSavedMessage)
            _effect.emit(ImportMessageScreenEffect.ReturnToNoteList)
        }
    }

    fun discard() = viewModelScope.launch {
        _state.value = _state.value.copy(content = null, cipherState = CipherState.Encrypted)
    }

    fun decryptNote(encryptedContent: String?) = viewModelScope.launch {
        _state.value = _state.value.copy(content = encryptedContent!!)
        _effect.emit(ImportMessageScreenEffect.RequestPassword)
    }

    fun import(fileUri: Uri, contentResolver: ContentResolver) {
        val note = contentResolver.openInputStream(fileUri).use {
            return@use fileInteractor.import(requireNotNull(it))
        }
        _state.value = _state.value.copy(content = note.content)
    }

    fun setCipherMode(cipherMode: CipherState) {
        _state.value = _state.value.copy(cipherState = cipherMode)
    }
}

sealed class ImportMessageScreenEffect {
    data object RequestPassword : ImportMessageScreenEffect()
    data object ComposeComplete: ImportMessageScreenEffect()
    data object ReturnToNoteList : ImportMessageScreenEffect()
    data object NoteSavedMessage : ImportMessageScreenEffect()
    data object WrongPassword: ImportMessageScreenEffect()
    data object MalformedEncryptedMessage: ImportMessageScreenEffect()
}