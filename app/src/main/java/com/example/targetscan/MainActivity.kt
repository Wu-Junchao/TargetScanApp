package com.example.targetscan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.targetscan.databinding.ActivityMainBinding
import com.chaquo.python.PyException
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform


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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        val params = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        )
//        params.setMargins(0, getStatusBarHeight(), 0, 0)
//        binding.toolbar.layoutParams = params;
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