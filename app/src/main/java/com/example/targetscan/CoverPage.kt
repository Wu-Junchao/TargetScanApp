package com.example.targetscan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.example.targetscan.databinding.ActivityCoverPageBinding

class CoverPage : AppCompatActivity() {
    lateinit var binding: ActivityCoverPageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        binding = ActivityCoverPageBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar3)
        supportActionBar?.title = "Welcome to TargetScan : )"

        binding.newBtn.setOnClickListener {
            val intent = Intent(this,FillInformation::class.java)
            startActivity(intent)
        }
        binding.recordBtn.setOnClickListener {
            val intent = Intent(this,DaySelect::class.java)
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
    }
}