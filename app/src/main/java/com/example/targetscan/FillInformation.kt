package com.example.targetscan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.targetscan.databinding.ActivityFillInformationBinding
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

private var selectedIndex:Int = 0
class FillInformation : AppCompatActivity() {
    lateinit var binding:ActivityFillInformationBinding
    lateinit var myDateList:MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFillInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar3)

        getSupportActionBar()?.setTitle("Entering information");
        myDateList = mutableListOf<String>("Rifle shoot","Test")
        initSpinner()
        initDateSelector()

        binding.confirmBtn.setOnClickListener {
            val year = binding.dateInput.year
            val month = binding.dateInput.month+1
            val day = binding.dateInput.dayOfMonth
            val comment = binding.commentInput.text.toString()

            intent = Intent(this,TakePhoto1::class.java)
            intent.putExtra("index", selectedIndex)
            intent.putExtra("year", year)
            intent.putExtra("month", month)
            intent.putExtra("day", day)
            intent.putExtra("comment",comment.trim())
            Toast.makeText(this, "$year $month $day", Toast.LENGTH_SHORT).show()
            startActivity(intent)
            finish()
        }
        binding.cancelBtn.setOnClickListener {
            intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
     private fun initDateSelector(){
         val c = Calendar.getInstance()
         c.set(2020,0,1)
         binding.dateInput.minDate= c.timeInMillis
         binding.dateInput.maxDate=Date().time

     }

    private fun initSpinner(){
        var starAdapter = ArrayAdapter<String>(this,R.layout.item_select,myDateList)
        starAdapter.setDropDownViewResource(R.layout.item_dropdown)
        var sp = binding.disciplineSelect
        sp.prompt="choose a discipline"
        sp.adapter=starAdapter
        sp.setSelection(0)
        sp.onItemSelectedListener=MySelectedListener()
    }

    class MySelectedListener : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            selectedIndex = p2
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            TODO("Not yet implemented")
        }


    }
}