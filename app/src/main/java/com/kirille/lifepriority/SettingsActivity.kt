package com.kirille.lifepriority

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar


class SettingsActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null){
            val fragment = SettingsFragment()
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.content_frame, fragment)
            ft.commit()
        }

    }
}
