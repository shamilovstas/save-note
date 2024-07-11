package com.shamilovstas.text_encrypt

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.shamilovstas.text_encrypt.base.AggregatedFragmentFactory
import com.shamilovstas.text_encrypt.base.AppBarConfigurationProvider
import com.shamilovstas.text_encrypt.notes.compose.ComposeNoteFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AppBarConfigurationProvider {

    private lateinit var appBarConfiguration: AppBarConfiguration
    @Inject lateinit var fragmentFactory: AggregatedFragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.fragmentFactory = fragmentFactory
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_container) as NavHostFragment
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navController = navHostFragment.navController
        val topLevel = setOf(
            R.id.notes_list, R.id.import_dashboard
        )
        appBarConfiguration = AppBarConfiguration(topLevel, drawerLayout)
        findViewById<NavigationView>(R.id.nav_view).setupWithNavController(navController)

        checkForImportedFile(navController, intent)
    }

    private fun checkForImportedFile(navController: NavController, intent: Intent?) {
        if (intent == null) return

        if (intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data ?: return

            navController.navigate(R.id.action_import_file, ComposeNoteFragment.fileImportArgs(uri))
        }
    }

    override fun provideAppBarConfiguration(): AppBarConfiguration {
        return appBarConfiguration
    }
}