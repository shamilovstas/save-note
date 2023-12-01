package com.shamilovstas.text_encrypt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.shamilovstas.text_encrypt.notes.list.NotesListFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, NotesListFragment())
            .commit()
    }
}