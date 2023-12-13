package com.shamilovstas.text_encrypt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavHost
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.shamilovstas.text_encrypt.base.AppBarConfigurationProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class MainActivity : AppCompatActivity(), AppBarConfigurationProvider {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_container) as NavHostFragment
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navController = navHostFragment.navController
        val topLevel = setOf(
            R.id.notes_list, R.id.import_dashboard
        )
        appBarConfiguration = AppBarConfiguration(topLevel, drawerLayout)
        findViewById<NavigationView>(R.id.nav_view).setupWithNavController(navController)
    }

    override fun provideAppBarConfiguration(): AppBarConfiguration {
        return appBarConfiguration
    }
}