package com.shamilovstas.text_encrypt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.shamilovstas.text_encrypt.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private val textEncryptor = TextEncryptor()

    private var binding: FragmentMainBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(PasswordDialog.REQUEST_PASSWORD_FRAGMENT, this) { key, bundle ->
            val password = bundle.getString(PasswordDialog.BUNDLE_PASSWORD_RESULT)
                ?: throw IllegalStateException("Password cannot be null")
            Log.d("TextEncryptor", "Password: $password")
            doAction(password)
        }
    }

    private fun initViews() {
        binding?.saveButton?.setOnClickListener {
            PasswordDialog().show(childFragmentManager, "password_dialog")
        }
    }

    private fun doAction(password: String) {
        binding?.let {
            if (it.sw.isChecked) {
                val clearText = it.editText.text.toString()
                val encText = textEncryptor.encrypt(clearText, password)
                it.editText.text.clear()
                it.editText.setText(encText)
                Log.d("TextEncryptor", "Encrypted text: $encText")
            } else {
                val clearText = it.editText.text.toString()
                val encText = textEncryptor.decrypt(clearText, password)
                it.editText.text.clear()
                it.editText.setText(encText)
                Log.d("TextEncryptor", "Decrypted text: $encText")
            }
        }
    }
}