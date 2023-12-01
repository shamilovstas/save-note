package com.shamilovstas.text_encrypt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.shamilovstas.text_encrypt.databinding.ActivityMainBinding
import kotlin.io.encoding.Base64

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment())
            .commit()
    }
}