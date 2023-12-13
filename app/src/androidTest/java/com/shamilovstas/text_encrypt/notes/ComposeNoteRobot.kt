package com.shamilovstas.text_encrypt.notes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.launchFragmentInHiltContainer
import com.shamilovstas.text_encrypt.notes.compose.ComposeNoteFragment

open class ComposeNoteRobot {

    fun startScreen() {
        launchFragmentInHiltContainer<ComposeNoteFragment>(fragmentArgs = ComposeNoteFragment.composeArgs()) {
            this.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                if (viewLifecycleOwner != null) {
                    // The fragment’s view has just been created
                    navController.setGraph(R.navigation.trivia)
                    Navigation.setViewNavController(fragment.requireView(), navController)
                }
            }

        }
    }

    fun writeNoteContent(text: String) {
        noteContentEditText().perform(typeText(text))
    }

    fun pressSave() {
        saveButton().perform(click())
    }

    fun typePassword(password: String, passwordConfirmation: String = password) {
        passwordField().perform(typeText(password))
        passwordConfirmField().perform(typeText(passwordConfirmation))
        passwordOkButton().perform(click())
    }

    private fun passwordField(): ViewInteraction {
        return onView(withId(R.id.et_password)).inRoot(isDialog())
    }

    private fun passwordConfirmField(): ViewInteraction {
        return onView(withId(R.id.et_password_confirm)).inRoot(isDialog())
    }

    private fun passwordOkButton(): ViewInteraction {
        return onView(withId(R.id.btn_password_dialog_confirm)).inRoot(isDialog())
    }

    private fun noteContentEditText(): ViewInteraction {
        return onView(withId(R.id.edit_text))
    }

    private fun saveButton(): ViewInteraction {
        return onView(withId(R.id.btn_save_imported_note))
    }
}