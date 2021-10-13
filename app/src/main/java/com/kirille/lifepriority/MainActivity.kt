package com.kirille.lifepriority

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.forEach
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.kirille.lifepriority.databinding.ActivityMainBinding
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this);

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home, R.id.navigation_favorite, R.id.navigation_settings))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.menu.forEach { it ->
            it.setOnMenuItemClickListener {
                val toolbar = findViewById<Toolbar>(R.id.app_toolbar)
                if (it.itemId == R.id.navigation_home){
                    toolbar.title = resources.getString(R.string.app_name)
                } else {
                    toolbar.title = it.title
                }

                toolbar.menu?.clear()
                toolbar.navigationIcon = null

                it.isChecked

            }
        }

        val actionBar = supportActionBar!!
        actionBar.hide()

    }


    override fun onBackPressed() {
        if (binding.navView.selectedItemId == R.id.navigation_home) {
            super.onBackPressed();
            ActivityCompat.finishAffinity(this);
        } else {
            binding.navView.selectedItemId = R.id.navigation_home
            startActivity(intent);
        }
    }

}
