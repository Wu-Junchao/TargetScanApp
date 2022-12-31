package com.example.targetscan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.targetscan.databinding.ActivityRocordDetailBinding

class RecordDetail : AppCompatActivity() {
    lateinit var binding : ActivityRocordDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRocordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val position = intent.getIntExtra("position",-1)
        Log.d("wu", position.toString())
        binding.shootID.text=position.toString()
    }
}