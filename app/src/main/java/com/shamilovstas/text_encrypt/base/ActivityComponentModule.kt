package com.shamilovstas.text_encrypt.base

import androidx.activity.result.ActivityResultRegistry
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import com.shamilovstas.text_encrypt.base.fragmentfactory.FragmentKey
import com.shamilovstas.text_encrypt.base.fragmentfactory.ImportDashboardFragmentFactory
import com.shamilovstas.text_encrypt.importdata.ImportDashboardFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(ActivityComponent::class)
interface ActivityComponentModule {

    @Binds
    @IntoMap
    @FragmentKey(ImportDashboardFragment::class)
    fun bindImportDashboardFragmentFactory(factory: ImportDashboardFragmentFactory): FragmentFactory

    companion object {
        @Provides
        fun provideActivityResultRegistry(activity: FragmentActivity): ActivityResultRegistry {
            return activity.activityResultRegistry
        }
    }
}