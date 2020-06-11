package com.kirille.lifepriority

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem

class BookmarksActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null){
            val fragment = BookmarksFragment()
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.content_favorite_frame, fragment)
            ft.commit()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dialog_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            R.id.action_back_home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivityForResult(intent, 0)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
