package com.shamilovstas.text_encrypt.base

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.shamilovstas.text_encrypt.R

abstract class ToolbarFragment: Fragment() {

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val configurationProvider = requireActivity() as? AppBarConfigurationProvider ?: return

        val navController = findNavController()

        NavigationUI.setupWithNavController(toolbar, navController, configurationProvider.provideAppBarConfiguration())
    }
}