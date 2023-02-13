package com.example.targetscan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.contentValuesOf
import com.example.targetscan.databinding.ActivityPhotoProcessBinding
import java.io.File
import java.time.Month

class PhotoProcess : AppCompatActivity() {
    private lateinit var binding:ActivityPhotoProcessBinding
    lateinit var imageUri : Uri
    lateinit var outputImage: File
    private var index = 0
    private var year = 0
    private var month = 0
    private var day = 0
    private var comment :String? = null
    private var imgName :String? = null
    private var targetNum =10
    private var scoreList = arrayOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoProcessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        index = intent.getIntExtra("index",0)
        year =intent.getIntExtra("year",2023)
        month = intent.getIntExtra("month",1)
        day = intent.getIntExtra("day",1)
        comment = intent.getStringExtra("comment")
        imgName = intent.getStringExtra("ImgName")

        setSupportActionBar(binding.toolbarPhotoProcess)
        supportActionBar?.title = "Photo Process";

        outputImage = File(externalCacheDir,imgName)
        if (outputImage.exists()){
            imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                FileProvider.getUriForFile(this,"com.example.cameraalbumtest.fileprovider",outputImage)
            }else{
                Uri.fromFile(outputImage)
            }
            binding.imageViewProcess.setImageURI(imageUri)
        }
        else{
            val intent = Intent(this,FillInformation::class.java)
            startActivity(intent)
            finish()
        }

        binding.confirmEditedResult.setOnClickListener {
            confirmResult()
        }
    }

    private fun confirmResult(){
        var scoreTextList = arrayOf<EditText>(binding.score1,binding.score2,binding.score3,binding.score4,binding.score5,binding.score6,binding.score7,binding.score8,binding.score9,binding.score10)

        for (i in 0 until targetNum){
            var score=scoreTextList[i].text.toString().toInt()
//            Log.d("wu",score.toString())
            if (score in 0..10){
                scoreList+=score
            }
            else{
                Toast.makeText(this, "Position ${i+1}'s score is not in 0..10", Toast.LENGTH_SHORT).show()
                scoreList = arrayOf<Int>()
                return
            }
        }

        // Add to SQLite database
        imgName?.let { comment?.let { it1 -> add2Database(it,scoreList, it1,index,year,month,day,targetNum) } }

        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }

    private fun list2Str(scoreList: Array<Int>):String{
        var output = ""
        for (i in scoreList){
            output+=i.toString()
            output+=","
        }
        Log.d("wu",output)
        return output.dropLast(1)
    }
    private fun add2Database(imgName:String, scoreList: Array<Int>, comment:String, index:Int, year:Int, month: Int, day:Int,targetNum:Int){
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",2)
        val db = dbHelper.writableDatabase
        val values = contentValuesOf("filename" to imgName,"discipline" to index,"year" to year,"month" to month,"day" to day,"comment" to comment,"targetNum" to targetNum,"scores" to list2Str(scoreList))
        db.insert("ShootingRecords",null,values)

        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = access.edit()
        editor.putString(imgName,"Processed")
        editor.apply()
    }
}