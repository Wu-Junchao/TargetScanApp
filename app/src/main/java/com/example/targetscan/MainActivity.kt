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
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    private var shootList = ArrayList<ShootRecord>()

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

        supportActionBar?.title = "Shooting Records";

        // Initialize shared preference XML
        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = access.edit()

        if (!access.contains("totalNum") ){
            editor.putInt("totalNum",0)
            editor.apply()
        }
        var totalNum = access.getInt("totalNum",-1)

        // Read all images name
        var photoList = externalCacheDir?.list()
        var photoCorr = mutableMapOf<String,String>()
        if (photoList.isNullOrEmpty()){
            photoList = arrayOf<String>()
        }
        else{
            for (photo in photoList){
                photoCorr[photo] =access.getString(photo,"NotYetProcessed")!!
            }
        }

//        Log.d("wu", externalCacheDir?.list()?.get().toString())
        shootList=ArrayList<ShootRecord>()
        for (i in 0 until totalNum){
            shootList.add(ShootRecord(photoList[i], androidx.appcompat.R.drawable.abc_ic_go_search_api_material,photoCorr[photoList[i]]!!))
        }
        val layoutManager = LinearLayoutManager(this)
        binding.shootingHistory.layoutManager = layoutManager
        val adapter = ShootRecordAdapter(shootList)
        binding.shootingHistory.adapter = adapter

        initializeDatabase()

        binding.fab.setOnClickListener{
            val intent = Intent(this,FillInformation::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        var photoList = externalCacheDir?.list()
        var photoCorr = mutableMapOf<String,String>()
        if (photoList.isNullOrEmpty()){
            photoList = arrayOf<String>()
        }
        else{
            for (photo in photoList){
                photoCorr[photo] =access.getString(photo,"NotYetProcessed")!!
            }
        }
        var totalNum = access.getInt("totalNum",-1)
        shootList=ArrayList<ShootRecord>()
        for (i in 0 until totalNum){
            shootList.add(ShootRecord(photoList[i], androidx.appcompat.R.drawable.abc_ic_go_search_api_material,photoCorr[photoList[i]]!!))
        }
        val adapter = ShootRecordAdapter(shootList)
        binding.shootingHistory.adapter = adapter
    }

    private fun initializeDatabase(){
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",2)
        dbHelper.writableDatabase
    }



    fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}