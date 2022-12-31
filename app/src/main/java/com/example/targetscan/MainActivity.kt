package com.example.targetscan

import android.R.attr.left
import android.R.attr.right
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.targetscan.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    private val shootList = ArrayList<ShootRecord>()
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

        getSupportActionBar()?.setTitle("Shooting Records");

        repeat(20){
            shootList.add(ShootRecord("test", androidx.appcompat.R.drawable.abc_ic_go_search_api_material))
        }
        val layoutManager = LinearLayoutManager(this)
        binding.shootingHistory.layoutManager = layoutManager
        val adapter = ShootRecordAdapter(shootList)
        binding.shootingHistory.adapter = adapter

        binding.fab.setOnClickListener{
            val intent = Intent(this,FillInformation::class.java)
            startActivity(intent)
        }
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