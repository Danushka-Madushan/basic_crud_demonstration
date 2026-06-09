package com.dm.firebase

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Force Light Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val window = this.window // Use 'this.window' if inside an Activity
        val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false // false = White text/icons, true = Dark text/icons

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Keep root padding at 0 so the red block stays pinned to the absolute top glass
            v.setPadding(systemBars.left, 0, systemBars.right, 0)

            // Calculate 16dp of extra breathing room in pixels
            val extraPaddingInPx = (16 * v.resources.displayMetrics.density).toInt()

            // Set the spacer height to: Physical Status Bar + Your Extra Padding
            val statusBarSpacer = v.findViewById<View>(R.id.status_bar_spacer)
            statusBarSpacer?.layoutParams = statusBarSpacer.layoutParams?.apply {
                height = systemBars.top + extraPaddingInPx
            }

            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.itemActiveIndicatorColor = ContextCompat.getColorStateList(this, R.color.white)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // 2. Set Up the Item Selection Change Listener using a 'when' statement
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_add_jobs -> {
                    replaceFragment(AddJobsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}