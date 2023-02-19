package com.example.targetscan

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.contentValuesOf
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.targetscan.databinding.ActivityPhotoProcessBinding
import java.io.File

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
    private var editonly = false

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
        editonly = intent.getBooleanExtra("editonly",false)
        var scoreTextList = arrayOf<EditText>(binding.score1,binding.score2,binding.score3,binding.score4,binding.score5,binding.score6,binding.score7,binding.score8,binding.score9,binding.score10)


        setSupportActionBar(binding.toolbarPhotoProcess)
        supportActionBar?.title = "Photo Process"


        outputImage = File(externalCacheDir,imgName)
        if (outputImage.exists()){
            imageUri =FileProvider.getUriForFile(this,"com.example.cameraalbumtest.fileprovider",outputImage)
            val inputStream = contentResolver.openInputStream(imageUri)
            var originalImg = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            binding.imageViewProcess.setImageBitmap(originalImg)
        }
        else{
            val intent = Intent(this,FillInformation::class.java)
            startActivity(intent)
            finish()
        }

        binding.allScoreWrap.visibility=INVISIBLE
        binding.targetNumWrap.visibility= INVISIBLE
        if (editonly){
            binding.allScoreWrap.visibility= VISIBLE
            binding.targetNumWrap.visibility= VISIBLE
            binding.confirmEditedResult.text="confirm"
            binding.processLaterButton.visibility= GONE
            val scores = getScore(imgName!!)
            targetNum =scores.size
            binding.targetNumInput.setText(targetNum.toString())
            for (i in 0 until targetNum) {
                scoreTextList[i].setText(scores[i])
            }
        }
        binding.confirmEditedResult.setOnClickListener {
            if (binding.allScoreWrap.visibility == INVISIBLE){

                binding.confirmEditedResult.text="Waiting..."

                imageProcessWrap(imageUri)

                binding.allScoreWrap.visibility= VISIBLE
                binding.targetNumWrap.visibility= VISIBLE
                binding.confirmEditedResult.text="confirm"
                binding.processLaterButton.visibility= GONE
            }
            else{
                confirmResult()
            }
        }

        setButtons()
    }

    private fun imageProcessWrap(imgUri: Uri){
        var scoreTextList = arrayOf<EditText>(binding.score1,binding.score2,binding.score3,binding.score4,binding.score5,binding.score6,binding.score7,binding.score8,binding.score9,binding.score10)
        val content = contentResolver.openInputStream(imgUri)!!.use { it.readBytes() }

        if(!Python.isStarted()){
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()
        val resultSuccess = py.getModule("imageProcess").callAttr("main",content).toJava(Int::class.java)
        if (resultSuccess==0) {
            val bytes = py.getModule("imageProcess").callAttr("getLabeledWholeTargetPaper")
                .toJava(ByteArray::class.java)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            targetNum =
                py.getModule("imageProcess").callAttr("getTargetNum").toJava(String::class.java)
                    .toInt()
            binding.targetNumInput.setText(targetNum.toString())
            binding.imageViewProcess.setImageBitmap(bitmap)
            for (i in 0 until targetNum) {
                val result = 10 + py.getModule("imageProcess").callAttr("getCertainScore", i)
                    .toJava(String::class.java).toInt()
                scoreTextList[i].setText(result.toString())
            }
        }
        else{
            binding.targetNumInput.setText("0")
            targetNum=0
        }

    }
    private fun confirmResult(){
        var scoreTextList = arrayOf<EditText>(binding.score1,binding.score2,binding.score3,binding.score4,binding.score5,binding.score6,binding.score7,binding.score8,binding.score9,binding.score10)
        targetNum = binding.targetNumInput.text.toString().toInt()
        for (i in 0 until targetNum){
            if (scoreTextList[i].text.isBlank()){
                Toast.makeText(this, "Position ${i+1}'s score is blank.", Toast.LENGTH_SHORT).show()
                scoreList = arrayOf<Int>()
                return
            }
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
        if (comment.isNullOrBlank()){
            comment = ""
        }
        // Add to SQLite database
        imgName?.let { comment?.let { it1 -> add2Database(it,scoreList, it1,index,year,month,day,targetNum) } }

//        val intent = Intent(this,MainActivity::class.java)
//        startActivity(intent)
        finish()
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
        val cursor = db.query("ShootingRecords",null,"filename = ?",
            arrayOf<String>(imgName),null,null,null)
        if (cursor.count>0){
            db.delete("ShootingRecords","filename = ?",arrayOf<String>(imgName))
        }
        db.insert("ShootingRecords",null,values)

        db.close()

        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = access.edit()
        editor.putString(imgName,"Processed")
        editor.apply()
    }

    @SuppressLint("Range")
    private fun getScore(imgName:String):Array<String>{
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",2)
        val db = dbHelper.readableDatabase

        var scores=""
        val cursor = db.query("ShootingRecords",null,"filename = ?",
            arrayOf<String>(imgName),null,null,null)
        if (cursor.moveToFirst()){
            do{

                scores = cursor.getString(cursor.getColumnIndex("scores"))
//                Log.d("wu",displayText)
            } while (cursor.moveToNext())
        }
        db.close()
        cursor.close()
        return scores.split(",").toTypedArray()
    }

    private fun setButtons(){
        binding.processLaterButton.setOnClickListener {
            finish()
        }
        binding.fullImgButton.setOnClickListener {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }

            val py = Python.getInstance()
            val bytes = py.getModule("imageProcess").callAttr("getLabeledWholeTargetPaper")
                .toJava(ByteArray::class.java)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageViewProcess.setImageBitmap(bitmap)
        }
        var buttonList = arrayOf<Button>(binding.scoreName1,binding.scoreName2,binding.scoreName3,binding.scoreName4,binding.scoreName5,binding.scoreName6,binding.scoreName7,binding.scoreName8,binding.scoreName9,binding.scoreName10)
        for (i in buttonList.indices){
            buttonList[i].setOnClickListener {
                if(!Python.isStarted()){
                    Python.start(AndroidPlatform(this))
                }

                val py = Python.getInstance()
                val bytes =py.getModule("imageProcess").callAttr("getCertainOriginalImageCut",i).toJava(ByteArray::class.java)

                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding.imageViewProcess.setImageBitmap(bitmap)
            }
        }

    }
}