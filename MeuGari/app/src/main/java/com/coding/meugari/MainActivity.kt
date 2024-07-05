package com.coding.meugari

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
        try {
            val pi: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            findViewById<TextView>(R.id.lbVersion).text = getString(R.string.app_label_version, pi.versionName);
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            handler.post {
                startActivity(Intent(this, HomeActivity::class.java).apply {})
                finish()
            }
        }
    }
}