package com.example.targetscan

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.contentValuesOf
import androidx.core.view.isVisible
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
    private var iterate = 0

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
        supportActionBar?.title = "Photo Process"


        outputImage = File(externalCacheDir,imgName)
        if (outputImage.exists()){
            imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                FileProvider.getUriForFile(this,"com.example.cameraalbumtest.fileprovider",outputImage)
            }else{
                Uri.fromFile(outputImage)
            }
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
        binding.confirmEditedResult.setOnClickListener {
            if (binding.allScoreWrap.visibility == INVISIBLE){

                binding.confirmEditedResult.text="Waiting..."

                imageProcessWrap(imageUri)

                binding.allScoreWrap.visibility= VISIBLE
                binding.targetNumWrap.visibility= VISIBLE
                binding.confirmEditedResult.text="confirm"
            }
            else{
                confirmResult()
            }
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
        binding.scoreName1.setOnClickListener {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }

            val py = Python.getInstance()
            val bytes =py.getModule("imageProcess").callAttr("getCertainOriginalImageCut",0).toJava(ByteArray::class.java)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageViewProcess.setImageBitmap(bitmap)
        }
        binding.scoreName2.setOnClickListener {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }

            val py = Python.getInstance()
            val bytes =py.getModule("imageProcess").callAttr("getCertainOriginalImageCut",1).toJava(ByteArray::class.java)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageViewProcess.setImageBitmap(bitmap)
        }
        binding.scoreName3.setOnClickListener {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }

            val py = Python.getInstance()
            val bytes =py.getModule("imageProcess").callAttr("getCertainOriginalImageCut",2).toJava(ByteArray::class.java)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageViewProcess.setImageBitmap(bitmap)
        }
        binding.scoreName4.setOnClickListener {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }

            val py = Python.getInstance()
            val bytes =py.getModule("imageProcess").callAttr("getCertainOriginalImageCut",3).toJava(ByteArray::class.java)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageViewProcess.setImageBitmap(bitmap)
        }
        binding.scoreName5.setOnClickListener {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }

            val py = Python.getInstance()
            val bytes =py.getModule("imageProcess").callAttr("getCertainOriginalImageCut",4).toJava(ByteArray::class.java)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageViewProcess.setImageBitmap(bitmap)
        }
        binding.scoreName6.setOnClickListener {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }

            val py = Python.getInstance()
            val bytes =py.getModule("imageProcess").callAttr("getCertainOriginalImageCut",5).toJava(ByteArray::class.java)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageViewProcess.setImageBitmap(bitmap)
        }
        binding.scoreName7.setOnClickListener {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }

            val py = Python.getInstance()
            val bytes =py.getModule("imageProcess").callAttr("getCertainOriginalImageCut",6).toJava(ByteArray::class.java)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageViewProcess.setImageBitmap(bitmap)
        }
        binding.scoreName8.setOnClickListener {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }

            val py = Python.getInstance()
            val bytes =py.getModule("imageProcess").callAttr("getCertainOriginalImageCut",7).toJava(ByteArray::class.java)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageViewProcess.setImageBitmap(bitmap)
        }
        binding.scoreName9.setOnClickListener {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }

            val py = Python.getInstance()
            val bytes =py.getModule("imageProcess").callAttr("getCertainOriginalImageCut",8).toJava(ByteArray::class.java)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageViewProcess.setImageBitmap(bitmap)
        }
        binding.scoreName10.setOnClickListener {
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }

            val py = Python.getInstance()
            val bytes =py.getModule("imageProcess").callAttr("getCertainOriginalImageCut",9).toJava(ByteArray::class.java)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageViewProcess.setImageBitmap(bitmap)
        }
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
        db.insert("ShootingRecords",null,values)

        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = access.edit()
        editor.putString(imgName,"Processed")
        editor.apply()
    }
}