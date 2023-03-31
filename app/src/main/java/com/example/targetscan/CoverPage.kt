package com.example.targetscan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.targetscan.CoverPage.Companion.REQUIRED_PERMISSIONS
import com.example.targetscan.databinding.ActivityCoverPageBinding

class CoverPage : AppCompatActivity() {
    lateinit var binding: ActivityCoverPageBinding
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityCoverPageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar3)
        supportActionBar?.title = "Welcome to TargetScan : )"

        // Request camera permissions
        if (allPermissionsGranted()) {

        } else {
            ActivityCompat.requestPermissions(
                this, CoverPage.REQUIRED_PERMISSIONS, CoverPage.REQUEST_CODE_PERMISSIONS
            )
        }

        if(!Python.isStarted()){
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        // Initialize shared preference XML
        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = access.edit()

        if (!access.contains("totalNum") ){
            editor.putInt("totalNum",0)
            editor.apply()
        }
        if (!access.contains("ascendingOrder")){
            editor.putBoolean("ascendingOrder",false)
            editor.apply()
        }
        binding.newBtn.setOnClickListener {
            val intent = Intent(this,FillInformation::class.java)
            startActivity(intent)
        }
        binding.recordBtn.setOnClickListener {
            val intent = Intent(this,MonthSelect::class.java)
            startActivity(intent)
        }
        binding.settingBtn.setOnClickListener {
            val intent = Intent(this,Setting::class.java)
            startActivity(intent)
        }
        binding.analysisBtn.setOnClickListener {
            val intent = Intent(this,DataAnalysis::class.java)
            startActivity(intent)
        }
        initializeDatabase()
    }
    private fun initializeDatabase(){
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",4)
        dbHelper.writableDatabase
    }
}