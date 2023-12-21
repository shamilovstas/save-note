package com.shamilovstas.text_encrypt.notes.compose

import android.app.DownloadManager
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamilovstas.text_encrypt.files.FileInteractor
import com.shamilovstas.text_encrypt.notes.domain.Attachment
import com.shamilovstas.text_encrypt.notes.domain.EncryptedMessageMalformed
import com.shamilovstas.text_encrypt.notes.domain.FileEncryptor
import com.shamilovstas.text_encrypt.notes.domain.Note
import com.shamilovstas.text_encrypt.notes.domain.NotesInteractor
import com.shamilovstas.text_encrypt.utils.getFilename
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.crypto.BadPaddingException
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class ComposeNoteViewModel @Inject constructor(
    private val notesInteractor: NotesInteractor,
    private val fileInteractor: FileInteractor,
    private val fencryptor: FileEncryptor
) : ViewModel() {

    private val _effect = MutableSharedFlow<ImportMessageScreenEffect>()
    val effect: SharedFlow<ImportMessageScreenEffect> = _effect

    private val _state = MutableStateFlow(ComposeNoteScreenState())
    val state: StateFlow<ComposeNoteScreenState> = _state

    fun onPasswordEntered(password: String) = viewModelScope.launch {
        val cipherState = state.value.cipherState
        val note = state.value.note
        try {
            if (cipherState == CipherState.Decrypted) {
                notesInteractor.save(note, password)
                _effect.emit(ImportMessageScreenEffect.NoteSavedMessage)
                _effect.emit(ImportMessageScreenEffect.ComposeComplete)
            } else {
                val processedNote = notesInteractor.decrypt(note, password)
                _state.update {
                    it.copy(note = processedNote, previousPassword = password, cipherState = CipherState.Decrypted)
                }
            }
        } catch (e: EncryptedMessageMalformed) {
            _effect.emit(ImportMessageScreenEffect.MalformedEncryptedMessage)
        } catch (e: IllegalArgumentException) {
            _effect.emit(ImportMessageScreenEffect.MalformedEncryptedMessage)
        } catch (e: BadPaddingException) {
            _effect.emit(ImportMessageScreenEffect.WrongPassword)
        }
    }

    fun loadNote(noteId: Long) = viewModelScope.launch {
        val note = withContext(Dispatchers.IO) { notesInteractor.getNote(noteId) }
        _state.update { it.copy(note = note, cipherState = CipherState.Encrypted) }
        _effect.emit(ImportMessageScreenEffect.RequestPassword(state.value.previousPassword))
    }

    fun saveNote(content: String, description: String = "") = viewModelScope.launch {
        val cipherState = state.value.cipherState
        val note = state.value.note.copy(content = content, description = description)
        _state.update { it.copy(note = note) }
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

    fun decryptNote(encryptedContent: String?) = viewModelScope.launch {
        val note = state.value.note.copy(content = encryptedContent!!)
        _state.update { it.copy(note = note) }
        _effect.emit(ImportMessageScreenEffect.RequestPassword(state.value.previousPassword))
    }

    fun import(fileUri: Uri, contentResolver: ContentResolver)  = viewModelScope.launch {
        val note = contentResolver.openInputStream(fileUri).use {
            return@use fileInteractor.import(requireNotNull(it))
        }
        _state.update { it.copy(note = note) }
    }

    fun setCipherMode(cipherMode: CipherState) {
        _state.update { it.copy(cipherState = cipherMode) }
    }

    fun addAttachment(uri: Uri, contentResolver: ContentResolver) {
        val note = state.value.note
        val attachment = Attachment(noteId = note.id, uri = uri, filename = uri.getFilename(contentResolver))
        val newNote = note.copy(attachments = note.attachments + attachment)
        _state.update { it.copy(note = newNote) }
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