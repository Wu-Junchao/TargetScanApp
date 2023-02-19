package com.example.targetscan

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.targetscan.databinding.ActivityFillInformationBinding
import java.io.File
import java.util.Calendar
import java.util.Date

private var selectedIndex:Int = 0
class FillInformation : AppCompatActivity() {
    lateinit var binding:ActivityFillInformationBinding
    lateinit var myDateList:MutableList<String>
    lateinit var outputImage: File
    var nextID=1

    private fun dateFormat(year:Int, month: Int, day:Int):String{
        var m = if (month in 1..9){
            "0$month"
        } else{
            "$month"
        }
        var d = if (day in 1..9){
            "0$day"
        } else{
            "$day"
        }
        return "$year-${m}-${d}"
    }

    private fun removeEmptyPhoto(){
        val access = getSharedPreferences("data", Context.MODE_PRIVATE)

        var photoList = externalCacheDir?.list()
        if (photoList.isNullOrEmpty()){
            return
        }
        else{
            for (photo in photoList){
                if (!access.contains(photo)){
                    outputImage = File(externalCacheDir,photo)
                    if (outputImage.exists()){
                        outputImage.delete()
                    }
                }
            }
        }
        return
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFillInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar3)

        //restart Parameter
        val index = intent.getIntExtra("index",0)
        var year =intent.getIntExtra("year",1111)
        var month = intent.getIntExtra("month",1)
        var day = intent.getIntExtra("day",1)
        var comment = intent.getStringExtra("comment")
        binding.commentInput.setText(comment)

        supportActionBar?.title = "Entering information"
        myDateList = mutableListOf<String>("Rifle shoot","Test")
        initSpinner(index)
        initDateSelector(year,month-1,day)

        binding.confirmBtn.setOnClickListener {
            year = binding.dateInput.year
            month = binding.dateInput.month+1
            day = binding.dateInput.dayOfMonth
            comment = binding.commentInput.text.toString()
            removeEmptyPhoto()
            if (comment!!.length>250){
                Toast.makeText(this, "Comment length exceeds 250 chars.", Toast.LENGTH_SHORT).show()
            }
            else{
                intent = Intent(this,TakePhoto2::class.java)
                intent.putExtra("index", selectedIndex)
                intent.putExtra("year", year)
                intent.putExtra("month", month)
                intent.putExtra("day", day)
                intent.putExtra("comment",comment!!.trim())
                Toast.makeText(this, "$year $month $day", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            }
        }
        binding.cancelBtn.setOnClickListener {
            val year = binding.dateInput.year
            val month = binding.dateInput.month+1
            val day = binding.dateInput.dayOfMonth
            removeEmptyPhoto()

            intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
     private fun initDateSelector(year:Int,month:Int,day:Int){
         val c = Calendar.getInstance()
         c.set(2020,0,1)
         binding.dateInput.minDate= c.timeInMillis
         binding.dateInput.maxDate=Date().time
         if (year!=1111) {
             binding.dateInput.updateDate(year, month, day)
         }
     }

    private fun initSpinner(index:Int){
        var starAdapter = ArrayAdapter<String>(this,R.layout.item_select,myDateList)
        starAdapter.setDropDownViewResource(R.layout.item_dropdown)
        var sp = binding.disciplineSelect
        sp.prompt="choose a discipline"
        sp.adapter=starAdapter
        sp.setSelection(index)
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