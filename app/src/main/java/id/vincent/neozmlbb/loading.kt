package id.vincent.neozmlbb

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.vincent.neozmlbb.MainActivity
import id.vincent.neozmlbb.R

class loading : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var logo: ImageView
    private val handler = Handler(Looper.getMainLooper())
    private var progressStatus = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading)

        logo = findViewById(R.id.logo)
        progressBar = findViewById(R.id.loading)

        // Enable edge-to-edge if needed
        enableEdgeToEdge()

        // Show the ProgressBar while loading
        progressBar.visibility = View.VISIBLE

        // Start updating the progress bar
        updateProgressBar()
    }

    private fun updateProgressBar() {
        val totalDuration = 1000 // Total duration in milliseconds
        val updateInterval = 40 // Update interval in milliseconds
        val totalUpdates = totalDuration / updateInterval // Total number of updates

        // Post a delayed task to update progress
        val updateRunnable = object : Runnable {
            override fun run() {
                if (progressStatus < 100) {
                    progressStatus += (100 / totalUpdates).toInt()
                    progressBar.progress = progressStatus
                    handler.postDelayed(this, updateInterval.toLong())
                } else {
                    launchNextActivity()
                }
            }
        }

        // Start updating the progress bar
        handler.post(updateRunnable)
    }

    private fun launchNextActivity() {
        handler.post {
            progressBar.visibility = View.GONE // Hide the ProgressBar when done

            val intent = if (isFirstTime()) {
                Intent(this, MainActivity2::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
            finish() // Close this activity
        }
    }

    private fun isFirstTime(): Boolean {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val isFirstTime = sharedPref.getBoolean("isFirstTime", true)
        if (isFirstTime) {
            with(sharedPref.edit()) {
                putBoolean("isFirstTime", false)
                apply()
            }
        }
        return isFirstTime
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Clean up handler callbacks
    }
}
