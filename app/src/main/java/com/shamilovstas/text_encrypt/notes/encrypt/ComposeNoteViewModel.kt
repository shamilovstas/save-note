package com.shamilovstas.text_encrypt.notes.encrypt

import androidx.lifecycle.ViewModel
import com.shamilovstas.text_encrypt.TextEncryptor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ComposeNoteViewModel @Inject constructor(
    private val encryptor: TextEncryptor
): ViewModel() {

    private val _state = MutableStateFlow(EncryptScreenState())
    val state: StateFlow<EncryptScreenState> = _state


    fun encrypt(password: String, text: String?) {
        if (text.isNullOrEmpty()) {
            _state.value = _state.value.copy(error = CryptoFeatureError.TextIsEmpty)
        } else {
            val encrypted = encryptor.encrypt(password, text)
            _state.value = _state.value.copy(processedContent = encrypted, error = CryptoFeatureError.Clear)
        }
    }

    fun onTextChange(content: String) {
        _state.value = _state.value.copy(error = CryptoFeatureError.Clear)
    }
}

data class EncryptScreenState(
    val processedContent: String = "",
    val error: CryptoFeatureError = CryptoFeatureError.Clear
)

sealed class CryptoFeatureError {
    data object Clear: CryptoFeatureError()
    data object TextIsEmpty: CryptoFeatureError()
}