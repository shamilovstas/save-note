package com.shamilovstas.text_encrypt.base.fragmentfactory

import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.shamilovstas.text_encrypt.importdata.ImportDashboardFragment
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class ImportDashboardFragmentFactory @Inject constructor(
    private val activityResultRegistry: ActivityResultRegistry
): FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return ImportDashboardFragment(activityResultRegistry)
    }
}