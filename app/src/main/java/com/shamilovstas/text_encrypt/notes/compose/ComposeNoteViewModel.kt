package com.shamilovstas.text_encrypt.notes.compose

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamilovstas.text_encrypt.files.FileInteractor
import com.shamilovstas.text_encrypt.notes.domain.Attachment
import com.shamilovstas.text_encrypt.notes.domain.EncryptedMessageMalformed
import com.shamilovstas.text_encrypt.notes.domain.Note
import com.shamilovstas.text_encrypt.notes.domain.NotesInteractor
import com.shamilovstas.text_encrypt.utils.getFilename
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.crypto.BadPaddingException
import javax.inject.Inject

@HiltViewModel
class ComposeNoteViewModel @Inject constructor(
    private val notesInteractor: NotesInteractor,
    private val fileInteractor: FileInteractor
) : ViewModel() {

    private val _effect = MutableSharedFlow<ImportMessageScreenEffect>()
    val effect: SharedFlow<ImportMessageScreenEffect> = _effect

    private val _state = MutableStateFlow(ComposeNoteScreenState())
    val state: StateFlow<ComposeNoteScreenState> = _state

    fun onPasswordEntered(password: String) = viewModelScope.launch {
        val cipherState = state.value.cipherState
        val content = requireNotNull(state.value.content)
        val id = state.value.existingId
        val note = Note(id = id, content = content, description = state.value.description)
        try {
            if (cipherState == CipherState.Decrypted) {
                notesInteractor.save(note, password)
                _effect.emit(ImportMessageScreenEffect.NoteSavedMessage)
                _effect.emit(ImportMessageScreenEffect.ComposeComplete)
            } else {
                val processedNote = notesInteractor.decrypt(note, password)
                _state.value =
                    _state.value.copy(content = processedNote.content, previousPassword = password, cipherState = CipherState.Decrypted)
            }
        } catch (e: EncryptedMessageMalformed) {
            _effect.emit(ImportMessageScreenEffect.MalformedEncryptedMessage)
        } catch (e: IllegalArgumentException) {
            _effect.emit(ImportMessageScreenEffect.MalformedEncryptedMessage)
        } catch (e: BadPaddingException) {
            _effect.emit(ImportMessageScreenEffect.WrongPassword)
        }
    }

    fun loadNote(noteId: Int) = viewModelScope.launch {
        val note = withContext(Dispatchers.IO) { notesInteractor.getNote(noteId) }
        _state.value = _state.value.copy(
            existingId = noteId,
            content = note.content,
            description = note.description,
            cipherState = CipherState.Encrypted
        )
        _effect.emit(ImportMessageScreenEffect.RequestPassword(state.value.previousPassword))
    }

    fun saveNote(content: String, description: String = "") = viewModelScope.launch {
        val cipherState = state.value.cipherState
        _state.value = _state.value.copy(content = content, description = description)
        if (cipherState == CipherState.Decrypted) {
            _effect.emit(ImportMessageScreenEffect.RequestPassword(state.value.previousPassword))
        } else {
            saveImportedNote(content, description)
        }
    }

    private suspend fun saveImportedNote(content: String, description: String = "") {
        val note = Note(content = content, description = description)
        notesInteractor.save(note)
        _effect.emit(ImportMessageScreenEffect.NoteSavedMessage)
        _effect.emit(ImportMessageScreenEffect.ReturnToNoteList)
    }

    fun discard() = viewModelScope.launch {
        _state.value = _state.value.copy(content = null, cipherState = CipherState.Encrypted)
    }

    fun decryptNote(encryptedContent: String?) = viewModelScope.launch {
        _state.value = _state.value.copy(content = encryptedContent!!)
        _effect.emit(ImportMessageScreenEffect.RequestPassword(state.value.previousPassword))
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

    fun addAttachment(uri: Uri, contentResolver: ContentResolver) {
        val attachment = Attachment(uri, uri.getFilename(contentResolver))
        _state.update { it.copy(attachments = it.attachments + attachment) }
    }
}

sealed class ImportMessageScreenEffect {
    data class RequestPassword(val previousPassword: String? = null) : ImportMessageScreenEffect()
    data object ComposeComplete : ImportMessageScreenEffect()
    data object ReturnToNoteList : ImportMessageScreenEffect()
    data object NoteSavedMessage : ImportMessageScreenEffect()
    data object WrongPassword : ImportMessageScreenEffect()
    data object MalformedEncryptedMessage : ImportMessageScreenEffect()
}