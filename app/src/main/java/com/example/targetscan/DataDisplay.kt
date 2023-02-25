package com.example.targetscan

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.SeekBar
import android.widget.Toast
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.targetscan.databinding.ActivityDataDisplayBinding
import me.moallemi.tools.daterange.localdate.rangeTo
import java.time.LocalDate
import java.util.Calendar

class DataDisplay : AppCompatActivity() {
    private lateinit var binding: ActivityDataDisplayBinding
    private var xDateAgo = 14
    private var isMonth = false
    private var xNum = 25
    private var typeFlg = false // all targets in one target is false, by position is true
    private var rangeFlg = false    // by week is false, by num is true
    private lateinit var access :SharedPreferences
    private var index = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.title = "Data analysis"

        access = getSharedPreferences("data", Context.MODE_PRIVATE)
        setTypeToggle()
        setRangeSeekBar()
        setIndexSeekBar()
        setRangeToggle()
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.processHeatmapButton.setOnClickListener {
            if (typeFlg){
                displayByCertainTarget(xNum,rangeFlg,xDateAgo,isMonth,index)
            }
            else{
                displayByAllTargets(xNum,rangeFlg,xDateAgo,isMonth)
            }
        }
    }

    private fun getAllFilesInCertainDateRange(xDaysAgo:Int,isMonth :Boolean = false):Array<String>{
        var photoList = externalCacheDir?.list()
        var returnList = arrayOf<String>()
        if (photoList.isNullOrEmpty()){
            return returnList
        }
        else{
            val calendar = Calendar.getInstance()
            val endTime = LocalDate.of(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH))
            if (isMonth){
                calendar.add(Calendar.MONTH,-xDaysAgo)
            }
            else{
                calendar.add(Calendar.DAY_OF_YEAR,-xDaysAgo)
            }
            var startTime = LocalDate.of(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH))

            if (xDaysAgo==42){
                startTime = LocalDate.of(2022,1,1)
            }
//            Log.d("wu",startTime.toString())
            for (photo in photoList){
                val date = photo.slice(1..10)
                val year = date.slice(0..3).toInt()
                val month = date.slice(5..6).toInt()
                val day = date.slice(8..9).toInt()

                val fileDate = LocalDate.of(year,month,day)
                if (fileDate in startTime..endTime){
                    if (access.getString(photo,"")==getString(R.string.processed_text) && checkTargetNum(photo)){
                        returnList+=photo
                    }
                }
            }
        }
        return returnList
    }

    private fun getAllFilesInCertainNumRange(recordNum:Int):Array<String>{
        var photoList = externalCacheDir?.list()
        var returnList = arrayOf<String>()
        if (photoList.isNullOrEmpty()){
            return returnList
        }
        else{
            val size = photoList.size
            var index = size-1
            var acc = 0
            while (index>=0 && (acc<=recordNum || recordNum==42)){
//                Log.d("wu",photoList[index])
                if (access.getString(photoList[index],"")==getString(R.string.processed_text) && checkTargetNum(photoList[index])){
                    returnList+=photoList[index]
                    index-=1
                    acc+=1
                }else{
                    index-=1
                }
            }
            if (index<=0 && acc<recordNum){
                if (recordNum!=42){
                    Toast.makeText(this, "Only has ${acc} valid records.", Toast.LENGTH_SHORT).show()
                }

            }
        }
        return returnList
    }

    @SuppressLint("Range")
    private fun getVectorFromDatabase(imgName:String):String{
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",4)
        val db = dbHelper.readableDatabase
        var vectors=""
        val cursor = db.query("ShootingRecords",null,"filename = ?",
            arrayOf<String>(imgName),null,null,null)
        if (cursor.moveToFirst()){
            do{
                val targetNum = cursor.getInt(cursor.getColumnIndex("targetNum"))
                vectors = cursor.getString(cursor.getColumnIndex("vectors"))
                val parsedVectors = vectors.split(".").toTypedArray()
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return vectors
    }

    @SuppressLint("Range")
    private fun checkTargetNum(imgName:String):Boolean{
        // only use 10 target record when display by position
        if (!typeFlg){
            return true
        }
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",4)
        var targetNum = 0
        val db = dbHelper.readableDatabase
        var vectors=""
        val cursor = db.query("ShootingRecords",null,"filename = ?",
            arrayOf<String>(imgName),null,null,null)
        if (cursor.moveToFirst()){
            do{
                targetNum = cursor.getInt(cursor.getColumnIndex("targetNum"))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return targetNum==10
    }

    @SuppressLint("Range")
    private fun getCertainVectorFromDatabase(imgName:String,index: Int):String{
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",4)
        val db = dbHelper.readableDatabase
        var vector = ""
        val cursor = db.query("ShootingRecords",null,"filename = ?",
            arrayOf<String>(imgName),null,null,null)
        if (cursor.moveToFirst()){
            do{
                val targetNum = cursor.getInt(cursor.getColumnIndex("targetNum"))
                val vectors = cursor.getString(cursor.getColumnIndex("vectors"))
                val parsedVectors = vectors.split(".").toTypedArray()
                if (index<targetNum){
                    vector = parsedVectors[index]
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return vector
    }

    private fun displayByAllTargets(recordNum:Int = 25,rangeFlg:Boolean = false,xDaysAgo:Int =14 ,isMonth: Boolean = false){
        var fileCollect= arrayOf<String>()
        var vectorCollect=""
        if (rangeFlg){
            // true -> by num
            if (recordNum==0){
                return
            }
            else{
                fileCollect=getAllFilesInCertainNumRange(xNum)
                var singleVector = ""
                for (file in fileCollect){
                    singleVector = getVectorFromDatabase(file)
                    if (singleVector.isNotEmpty()){
                        vectorCollect+= "$singleVector."
                    }
                }
                if (vectorCollect.isNotEmpty()){
                    vectorCollect = vectorCollect.dropLast(1)
                }
                getHeatmapWithVectors(vectorCollect)
            }
        }
        else{
            if (xDaysAgo==0){
                return
            }
            else{
                fileCollect=getAllFilesInCertainDateRange(xDaysAgo,isMonth)
                var singleVector = ""
                for (file in fileCollect){
                    singleVector = getVectorFromDatabase(file)
                    if (singleVector.isNotEmpty()){
                        vectorCollect+= "$singleVector."
                    }
                }
                if (vectorCollect.isNotEmpty()){
                    vectorCollect = vectorCollect.dropLast(1)
                }
                getHeatmapWithVectors(vectorCollect)
            }
        }
    }

    private fun displayByCertainTarget(recordNum:Int = 25,rangeFlg:Boolean = false,xDaysAgo:Int =14 ,isMonth: Boolean = false,index:Int){
        var fileCollect= arrayOf<String>()
        var vectorCollect=""
        if (rangeFlg){
            // true -> by num
            if (recordNum==0){
                return
            }
            else{
                fileCollect=getAllFilesInCertainNumRange(xNum)
                var singleVector = ""
                for (file in fileCollect){
                    singleVector = getCertainVectorFromDatabase(file,index)
                    if (singleVector.isNotEmpty()){
                        vectorCollect+= "$singleVector."
                    }
                }
                if (vectorCollect.isNotEmpty()){
                    vectorCollect = vectorCollect.dropLast(1)
                }
                getHeatmapWithVectors(vectorCollect)
            }
        }
        else{
            if (xDaysAgo==0){
                return
            }
            else{
                fileCollect=getAllFilesInCertainDateRange(xDaysAgo,isMonth)
                var singleVector = ""
                for (file in fileCollect){
                    singleVector = getCertainVectorFromDatabase(file,index)
                    if (singleVector.isNotEmpty()){
                        vectorCollect+="$singleVector."
                    }
                }
                if (vectorCollect.isNotEmpty()){
                    vectorCollect = vectorCollect.dropLast(1)
                }
                getHeatmapWithVectors(vectorCollect,index)
            }
        }
    }

    private fun getHeatmapWithVectors(vectors:String,location:Int = -1){
        if(!Python.isStarted()){
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()
        val bytes = py.getModule("dataAnalysis").callAttr("drawRealHeatmap",vectors,location).toJava(ByteArray::class.java)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.heatmapImg.setImageBitmap(bitmap)
    }

    private fun setTypeToggle(){
        val toggle = binding.typeSwitch
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // The toggle is enabled
                typeFlg = true
                binding.indexSeekbarWrap.visibility = VISIBLE
                toggle.text="By position"
            } else {
                // The toggle is disabled
                typeFlg=false
                binding.indexSeekbarWrap.visibility = INVISIBLE
                toggle.text = "All together"
            }
        }
    }

    private fun setRangeSeekBar(){
        val seek = binding.rangeSeekbar
        seek?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (seek.progress){
                    0 -> {
                        if (rangeFlg){
                            xNum = 10
                        }
                        else{
                            xDateAgo = 7
                            isMonth=false
                        }

                    }
                    1 -> {
                        if (rangeFlg){
                            xNum = 25
                        }
                        else{
                            xDateAgo = 14
                            isMonth = false
                        }
                    }
                    2 -> {
                        if (rangeFlg){
                            xNum = 50
                        }
                        else{
                            xDateAgo = 1
                            isMonth = true
                        }
                    }
                    3-> {
                        if (rangeFlg){
                            xNum = 100
                        }
                        else{
                            xDateAgo = 3
                            isMonth = true
                        }
                    }
                    else->{
                        // All records
                        if (rangeFlg){
                            xNum = 42
                        }
                        else{
                            xDateAgo = 42
                            isMonth = false
                        }
                    }
                }
                updateIndicator()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
            })
    }

    private fun setIndexSeekBar(){
        val seek = binding.indexSeekbar
        seek?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.indexSeekbarIndicator.text = "Position ${seek.progress+1}"

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                index = seek.progress
            }
        })
    }

    private fun setRangeToggle(){
        val toggle = binding.rangeSwitch
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // The toggle is enabled
                rangeFlg=true
                toggle.text="By record number"
            } else {
                // The toggle is disabled
                rangeFlg = false
                toggle.text = "by date range"
                binding.rangeSeekbarIndicator.text = ""
            }
            updateIndicator()
        }
    }

    private fun updateIndicator(){
        if (rangeFlg){
            when (xNum){
                10 -> {
                    binding.rangeSeekbarIndicator.text = "10 records"
                    binding.rangeSeekbar.progress = 0
                }
                25 -> {
                    binding.rangeSeekbarIndicator.text = "25 records"
                    binding.rangeSeekbar.progress = 1
                }
                50 -> {
                    binding.rangeSeekbarIndicator.text = "50 records"
                    binding.rangeSeekbar.progress = 2
                }
                100 -> {
                    binding.rangeSeekbarIndicator.text = "100 records"
                    binding.rangeSeekbar.progress = 3
                }
                else -> {
                    binding.rangeSeekbarIndicator.text = "All records"
                    binding.rangeSeekbar.progress = 4
                }
            }
        }
        else{
            when (xDateAgo){
                7 -> {
                    binding.rangeSeekbarIndicator.text = "1 week"
                    binding.rangeSeekbar.progress = 0
                }
                14 -> {
                    binding.rangeSeekbarIndicator.text = "2 weeks"
                    binding.rangeSeekbar.progress = 1
                }
                1 -> {
                    binding.rangeSeekbarIndicator.text = "1 month"
                    binding.rangeSeekbar.progress = 2
                }
                3 -> {
                    binding.rangeSeekbarIndicator.text = "3 months"
                    binding.rangeSeekbar.progress = 3
                }
                else -> {
                    binding.rangeSeekbarIndicator.text = "All records"
                    binding.rangeSeekbar.progress = 4
                }
            }
        }
    }
}