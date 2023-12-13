package com.shamilovstas.text_encrypt.notes

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shamilovstas.text_encrypt.notes.repository.NotesDao
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ComposeNoteTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var robot: ComposeNoteRobot

    @Inject
    lateinit var dao: NotesDao

    @Before
    fun init() {
        hiltRule.inject()
        robot = ComposeNoteRobot()
    }

    @Test
    fun shouldSaveNoteWhenEnteredMessage(): Unit = runBlocking {

        val noteContent = "Hello world"
        val password = "qwerty"
        robot.apply {
            startScreen()
            writeNoteContent(noteContent)
            pressSave()
            typePassword(password)
        }

        assertTrue(dao.getAllNotes().toList().isNotEmpty())
    }
}
