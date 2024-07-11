package com.shamilovstas.text_encrypt.importdata

import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.shamilovstas.text_encrypt.R
import com.shamilovstas.text_encrypt.utils.createActivityResultRegistry
import com.shamilovstas.text_encrypt.utils.launchFragmentInHiltContainer
import io.mockk.mockk
import org.junit.Test

class TestImportDashboardFragment {

    @Test
    fun shouldNotCrashWhenAFilePickDialogCancelled() {
        val registry = createActivityResultRegistry(null)
        val mockNavController = mockk<NavController>()
        launchFragmentInHiltContainer<ImportDashboardFragment>(factory = {
            ImportDashboardFragment(registry).also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(fragment.requireView(), mockNavController)
                    }
                }
            }
        })
        onView(withId(R.id.btn_open_import_file)).perform(click())
    }
}