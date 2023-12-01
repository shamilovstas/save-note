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

    fun encrypt(password: String, text: String?) = viewModelScope.launch {
        try {
            val note = withContext(Dispatchers.IO) {
                interactor.save(text, password)
            }
            _state.value = _state.value.copy(processedContent = note.content, error = CryptoFeatureError.Clear)
            _effects.emit(ComposeScreenEffect.ComposeComplete(note))
        } catch (e: ClearTextIsEmpty) {
            _state.value = _state.value.copy(processedContent = "", error = CryptoFeatureError.TextIsEmpty)
        }
    }

    fun onTextChange() {
        _state.value = _state.value.copy(error = CryptoFeatureError.Clear)
    }
}

data class EncryptScreenState(
    val processedContent: String = "",
    val error: CryptoFeatureError = CryptoFeatureError.Clear
)

sealed class ComposeScreenEffect {
    data class ComposeComplete(val data: Note) : ComposeScreenEffect()
}

sealed class CryptoFeatureError {
    data object Clear : CryptoFeatureError()
    data object TextIsEmpty : CryptoFeatureError()
}