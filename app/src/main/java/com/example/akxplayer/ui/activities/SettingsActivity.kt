package com.example.akxplayer.ui.activities

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.example.akxplayer.R

class SettingsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var mSpinnerTheme: Spinner
    private lateinit var mSpinnerStartPage: Spinner
    private lateinit var settings: SharedPreferences
    private var isSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settings = getSharedPreferences("AppSettingsPref", 0)
        val startPage = settings.getInt("startPage", 0)
        val theme = settings.getInt("theme", 0)

        mSpinnerTheme = findViewById(R.id.spinner_theme)
        mSpinnerStartPage = findViewById(R.id.spinner_start_page)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mSpinnerTheme.setSelection(theme)
        mSpinnerStartPage.setSelection(startPage)
        mSpinnerTheme.onItemSelectedListener = this
        mSpinnerStartPage.onItemSelectedListener = this
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        if (!isSelected) {
            isSelected = true
            return
        }
        when (p0?.id) {
            R.id.spinner_theme -> {
                settings.edit().putInt("theme", position).apply()
                updateTheme(position)
            }
            R.id.spinner_start_page ->
                settings.edit().putInt("startPage", position).apply()
        }
    }

    private fun updateTheme(value: Int) {
        when (value) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.menu_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_done -> finish()
            else -> return false
        }
        return true
    }
}
