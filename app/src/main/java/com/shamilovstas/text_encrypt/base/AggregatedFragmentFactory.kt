package com.shamilovstas.text_encrypt.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class AggregatedFragmentFactory @Inject constructor(private val fragmentMap: Map<Class<out Fragment>, @JvmSuppressWildcards FragmentFactory>) :
    FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        val fragmentClass = Class.forName(className)
        if (fragmentMap.containsKey(fragmentClass)) {
            return fragmentMap[fragmentClass]!!.instantiate(classLoader, className)
        }
        return super.instantiate(classLoader, className)
    }
}