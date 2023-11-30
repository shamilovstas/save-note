package com.shamilovstas.text_encrypt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.shamilovstas.text_encrypt.databinding.ActivityMainBinding
import kotlin.io.encoding.Base64

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val textEncryptor = TextEncryptor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.button.setOnClickListener {
            if (binding.swEnc.isChecked) {
                // Encryption is enabled
                val clearText = binding.editText.text.toString()
                val encText = textEncryptor.encrypt(clearText, "qwerty")
                binding.editText.text.clear()
                binding.editText.setText(encText)
                Log.d("TextEncryptor", "Encrypted text: $encText")
            } else {
                val encText = binding.editText.text.toString()
                val clearText = textEncryptor.decrypt(encText, "qwerty")
                binding.editText.text.clear()
                binding.editText.setText(clearText)
                Log.d("TextEncryptor", "Decrypted text: $clearText")
            }
        }

        binding.swEnc.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.text = "Encrypt"
            } else {
                buttonView.text = "Decrypt"
            }
        }
    }
}