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
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import by.dzmitry_lakisau.month_year_picker_dialog.MonthYearPickerDialog
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    private var dateRecord = ArrayList<DateRecord>()
    private var year = "2023"
    private var month = "02"
    private lateinit var calendar:Calendar

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
        calendar = Calendar.getInstance()
        year = calendar.get(Calendar.YEAR).toString()
        month = formatMonth(calendar.get(Calendar.MONTH))
        setupAdapter(year,month)

        initializeDatabase()

        binding.fab.setOnClickListener{
            val intent = Intent(this,FillInformation::class.java)
            startActivity(intent)
        }

        binding.monthSelector.setOnClickListener {

            val dialog = MonthYearPickerDialog.Builder(
                context = this,
                themeResId = R.style.Style_MonthYearPickerDialog,
                onDateSetListener = { y, m ->
                    // To something
                    year = y.toString()
                    month = formatMonth(m)
                    Log.d("wu",year)
                    Log.d("wu",month)
                    setupAdapter(year,month)
                },
                selectedYear = calendar.get(Calendar.YEAR),
                selectedMonth = calendar.get(Calendar.MONTH)

            )
                .setMinMonth(Calendar.JANUARY)
                .setMinYear(2022)
                .setMaxMonth(calendar.get(Calendar.MONTH))
                .setMaxYear(calendar.get(Calendar.YEAR))
                .build()
            dialog.setTitle("")
            dialog.setCustomTitle(layoutInflater.inflate(R.layout.header, null))
            MonthPickerDialogFragment.newInstance(dialog)
                .showNow(supportFragmentManager, MonthPickerDialogFragment::class.java.simpleName)
        }
    }

    override fun onResume() {
        super.onResume()
        setupAdapter(year,month)
    }

    private fun initializeDatabase(){
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",3)
        dbHelper.writableDatabase
    }

    private fun setupAdapter(year:String,month:String){
        // Read all images name
        var photoList = externalCacheDir?.list()
        var dateCorr = mutableMapOf<String,Int>()
        if (photoList.isNullOrEmpty()){
            photoList = arrayOf<String>()
        }
        else{
            for (photo in photoList){
                if (photo.slice(1..4) == year && photo.slice(6..7)==month){
                    if (dateCorr.containsKey(photo.slice(1..10))){
                        dateCorr[photo.slice(1..10)]=dateCorr[photo.slice(1..10)]!!+1
                    }
                    else{
                        dateCorr[photo.slice(1..10)]=1
                    }
                }

            }
        }

        binding.shootingHistory.isVisible = !dateCorr.isNullOrEmpty()
        binding.noRecordText.isVisible = dateCorr.isNullOrEmpty()

        dateRecord=ArrayList<DateRecord>()
        for (i in dateCorr.keys){
            dateRecord.add(DateRecord(i, androidx.appcompat.R.drawable.abc_ic_go_search_api_material,dateCorr[i]!!))
        }
        val layoutManager = LinearLayoutManager(this)
        binding.shootingHistory.layoutManager = layoutManager
        val adapter = DateRecordAdapter(dateRecord)
        binding.shootingHistory.adapter = adapter
    }

    private fun formatMonth(month:Int):String{
        val m = month+1
        val back =if (m in 1..9){
            "0$m"
        } else{
            "$m"
        }
        return back
    }
}