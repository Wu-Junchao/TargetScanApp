package com.example.targetscan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.targetscan.databinding.ActivityRocordDetailBinding

class RecordDetail : AppCompatActivity() {
    lateinit var binding : ActivityRocordDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imgName = intent.getStringExtra("name")
        binding = ActivityRocordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarRecordDetail)
        supportActionBar?.title = "Record Details";

        val position = intent.getIntExtra("position",-1)
//        Log.d("wu", position.toString())
        binding.shootID.text=imgName.toString()
    }

    private fun getData(imgName:String){

    }
}