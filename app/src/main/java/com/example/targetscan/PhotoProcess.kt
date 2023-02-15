package com.example.targetscan

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.contentValuesOf
import androidx.core.graphics.rotationMatrix
import androidx.core.view.isVisible
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.targetscan.databinding.ActivityPhotoProcessBinding
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
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
    lateinit private var image :ImageProcessPipeline

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
        binding.confirmEditedResult.setOnClickListener {
            if (binding.allScoreWrap.visibility == INVISIBLE){
                if (binding.targetNumInput.text.isNotBlank() && binding.targetNumInput.text.toString().toInt() in 1..10){
                    targetNum=binding.targetNumInput.text.toString().toInt()
                    binding.confirmEditedResult.text="Waiting..."

                    imageProcessWrap(imageUri)



                    // Start process
//                    if (OpenCVLoader.initDebug()){
//                        val inputStream2 = contentResolver.openInputStream(imageUri)
//                        var originalImg2 = BitmapFactory.decodeStream(inputStream2)
//                        inputStream2?.close()
//
//                        image = ImageProcessPipeline(originalImg2,targetNum)
//                        binding.imageViewProcess.setImageBitmap(image.returnImg())
//                        Log.d("wu","successfully")
//                    }
//                    else{
//                        Log.d("wu","failed to configure opencv")
//                    }
                    binding.allScoreWrap.visibility= VISIBLE

                    binding.confirmEditedResult.text="confirm"
                }
                else{
                    Toast.makeText(this, "The number of targets should between 1 and 10.", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                confirmResult()
            }
        }
    }

    private fun imageProcessWrap(imgUri: Uri){
        val inputStream = contentResolver.openInputStream(imgUri)
        var originalImg2 = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if(!Python.isStarted()){
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()
        val call = py.getModule("hello").callAttr("getArray") //("getData")
        Log.d("wu",call.toString())

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

        // Add to SQLite database
        imgName?.let { comment?.let { it1 -> add2Database(it,scoreList, it1,index,year,month,day,targetNum) } }

        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
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