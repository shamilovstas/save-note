package com.shamilovstas.text_encrypt.notes.list

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shamilovstas.text_encrypt.files.FileInteractor
import com.shamilovstas.text_encrypt.notes.domain.CopyNoteContentUseCase
import com.shamilovstas.text_encrypt.notes.domain.Note
import com.shamilovstas.text_encrypt.notes.domain.NotesInteractor
import com.shamilovstas.text_encrypt.utils.getFilename
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val NOTE_EXPORT_KEY = "note_export_key"

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val notesInteractor: NotesInteractor,
    private val copyNoteContentUseCase: CopyNoteContentUseCase,
    private val exportInteractor: FileInteractor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(NotesListScreenState())
    val state: StateFlow<NotesListScreenState> = _state
    private val _effects = MutableSharedFlow<NotesListEffects>()
    val effect: SharedFlow<NotesListEffects> = _effects


    fun loadNotes() = viewModelScope.launch {
        val flow = notesInteractor.getAllNotes()
        flow.flowOn(Dispatchers.IO).collect {
            _state.value = _state.value.copy(notes = it)
        }
    }

    fun deleteNote(item: Note) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            notesInteractor.deleteNote(item)
        }
    }

    fun copyNote(context: Context, note: Note) = viewModelScope.launch {
        copyNoteContentUseCase.perform(context, note)
        _effects.emit(NotesListEffects.NoteContentCopied)
    }

    fun exportNote(uri: Uri?, contentResolver: ContentResolver) = viewModelScope.launch {

        if (!savedStateHandle.contains(NOTE_EXPORT_KEY)) {
            throw IllegalStateException("No saved note to export")
        }
        val id: Int = savedStateHandle[NOTE_EXPORT_KEY]!!
        savedStateHandle.remove<Int>(NOTE_EXPORT_KEY)

        withContext(Dispatchers.IO) {
            val note = notesInteractor.getNote(id)

            contentResolver.openOutputStream(requireNotNull(uri)).use { outputStream ->
                if (outputStream == null) throw IllegalStateException("Couldn't write to file")
                exportInteractor.export(note, outputStream)

                withContext(Dispatchers.Main) {
                    _effects.emit(NotesListEffects.NoteExported(uri.getFilename(contentResolver)))
                }
            }
        }
    }

    fun onClickShareNoteItem(item: Note) = viewModelScope.launch {
        val filename = exportInteractor.createExportFilename(item)
        savedStateHandle[NOTE_EXPORT_KEY] = item.id
        _effects.emit(NotesListEffects.CreatePublicFile(filename))
    }

}

data class NotesListScreenState(
    val notes: List<Note> = listOf(),
    val exportedNote: Note? = null
)

sealed class NotesListEffects {
    data object NoteContentCopied : NotesListEffects()
    data class CreatePublicFile(val filename: String) : NotesListEffects()
    data class NoteExported(val filename: String): NotesListEffects()
}