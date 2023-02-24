package com.example.targetscan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.SharedPreferences
import com.example.targetscan.databinding.ActivityMonthSelectBinding


class MonthSelect : AppCompatActivity() {
    lateinit var binding:ActivityMonthSelectBinding
    private var shootList = ArrayList<ShootRecord>()
    private var date:String = ""

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
        binding = ActivityMonthSelectBinding.inflate(layoutInflater)
        val d = intent.getStringExtra("date")
        if (d.isNullOrEmpty()){
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        date = d.toString()

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.title = "Shooting Records : $date"

        // Initialize shared preference XML
        val access = getSharedPreferences("data", Context.MODE_PRIVATE)

        setupAdapter(access)

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

    private fun setupAdapter(access:SharedPreferences){
        // Read all images name
        var photoList = externalCacheDir?.list()
        var photoCorr = mutableMapOf<String,String>()
        if (photoList.isNullOrEmpty()){
            photoList = arrayOf<String>()
        }
        else{
            for (photo in photoList){
                if (photo.slice(1..4) == date.slice(0..3) && photo.slice(6..7)==date.slice(5..6) && photo.slice(9..10)==date.slice(8..9)){
                    val v = access.getString(photo,"")
                    if (v==getString(R.string.processed_text)){
                        photoCorr[photo] = v
                    }
                    else{
                        photoCorr[photo] = "Not yet processed."
                    }

                }
            }
        }

        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        if (!access.getBoolean("ascendingOrder",false)){
            photoCorr = photoCorr.toSortedMap(reverseOrder())
        }
        else{
            photoCorr = photoCorr.toSortedMap()
        }
        shootList=ArrayList<ShootRecord>()
        for (i in photoCorr.keys){
            shootList.add(ShootRecord(i, androidx.appcompat.R.drawable.abc_ic_go_search_api_material,photoCorr[i]!!))
        }
        val layoutManager = LinearLayoutManager(this)
        binding.shootingHistory.layoutManager = layoutManager
        val adapter = ShootRecordAdapter(shootList)
        binding.shootingHistory.adapter = adapter
    }
}
