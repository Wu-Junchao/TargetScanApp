package com.example.targetscan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.targetscan.databinding.ActivityMainBinding

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    private var dateRecord = ArrayList<DateRecord>()
    private var year = "2023"
    private var month = "02"

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.settings -> Toast.makeText(this, "You click settings, TODO", Toast.LENGTH_SHORT).show()
        }
        return true
    }

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
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {

        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        setSupportActionBar(binding.toolbar)

        supportActionBar?.title = "Shooting Records"

        // Initialize shared preference XML
        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = access.edit()

        if (!access.contains("totalNum") ){
            editor.putInt("totalNum",0)
            editor.apply()
        }

        setupAdapter(access)

        initializeDatabase()

        binding.fab.setOnClickListener{
            val intent = Intent(this,FillInformation::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        setupAdapter(access)
    }

    private fun initializeDatabase(){
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",2)
        dbHelper.writableDatabase
    }

    private fun setupAdapter(access: SharedPreferences){
        // Read all images name
        var photoList = externalCacheDir?.list()
        var DateCorr = mutableMapOf<String,Int>()
        if (photoList.isNullOrEmpty()){
            photoList = arrayOf<String>()
        }
        else{
            for (photo in photoList){
                if (photo.slice(1..4) == year && photo.slice(6..7)==month){
                    if (DateCorr.containsKey(photo.slice(1..10))){
                        DateCorr[photo.slice(1..10)]=DateCorr[photo.slice(1..10)]!!+1
                    }
                    else{
                        DateCorr[photo.slice(1..10)]=1
                    }
                }

            }
        }


        dateRecord=ArrayList<DateRecord>()
        for (i in DateCorr.keys){
            dateRecord.add(DateRecord(i, androidx.appcompat.R.drawable.abc_ic_go_search_api_material,DateCorr[i]!!))
        }
        val layoutManager = LinearLayoutManager(this)
        binding.shootingHistory.layoutManager = layoutManager
        val adapter = DateRecordAdapter(dateRecord)
        binding.shootingHistory.adapter = adapter
    }
}