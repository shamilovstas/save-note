package com.shamilovstas.text_encrypt.notes.compose

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.device.DeviceInteraction.Companion.setScreenOrientation
import androidx.test.espresso.device.EspressoDevice.Companion.onDevice
import androidx.test.espresso.device.action.ScreenOrientation
import androidx.test.espresso.device.rules.ScreenOrientationRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.utils.launchFragmentInHiltContainer
import org.junit.Rule
import org.junit.Test

class TestComposeNoteFragment {

    @get:Rule
    val screenOrientationRule: ScreenOrientationRule = ScreenOrientationRule(ScreenOrientation.PORTRAIT)

    @Test
    fun shouldNotBlankInputFieldsWhenScreenIsRotated() {
        launchFragmentInHiltContainer<ComposeNoteFragment>(ComposeNoteFragment.composeArgs())
        val expectedContent = "Hello world"
        val expectedDescription = "Description"
        val contextEditTextMatcher = onView(withId(R.id.edit_text))
        val descriptionEditTextMatcher = onView(withId(R.id.description_edit_text))
        contextEditTextMatcher.perform(typeText(expectedContent))
        descriptionEditTextMatcher.perform(typeText(expectedDescription))
        onDevice().setScreenOrientation(ScreenOrientation.LANDSCAPE)
        contextEditTextMatcher.check(matches(withText(expectedContent)))
        descriptionEditTextMatcher.check(matches(withText(expectedDescription)))
    }
}