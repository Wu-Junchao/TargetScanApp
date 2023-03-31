package com.example.targetscan

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View.*
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.contentValuesOf
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.targetscan.databinding.ActivityPhotoProcess2Binding
import java.io.File
import java.util.Timer
import kotlin.math.abs

class PhotoProcess : AppCompatActivity() {
    private lateinit var binding:ActivityPhotoProcess2Binding
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
    private lateinit var mTask:MyAsyncTask
    private lateinit var timerTask :TimerTask
    private var working = false
    private var vectorCollect :Array<String> = arrayOf<String>()
    private var resultCollect:Array<Int> = arrayOf<Int>()
    private var base = 0
    private var scores = arrayOf(String())
    private lateinit var positionCollect : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotoProcess2Binding.inflate(layoutInflater)
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
            scores = getWhat(imgName!!,"scores",",")
            positionCollect = getComment(imgName!!,"positions")
            targetNum =scores.size
            binding.targetNumInput.text = targetNum.toString()
            val vectorsHold = getWhat(imgName!!,"vectors",".")
            for (i in 0 until targetNum) {
                scoreTextList[i].setText(scores[i])
                vectorCollect+=vectorsHold[i]
            }
        }
        mTask = MyAsyncTask()
        timerTask = TimerTask()
        binding.confirmEditedResult.setOnClickListener {
            if (!working) {
                if (binding.allScoreWrap.visibility == INVISIBLE) {
                    mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    timerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                } else {
                    confirmResult()
                }
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
            targetNum =py.getModule("imageProcess").callAttr("getTargetNum").toJava(String::class.java).toInt()
            positionCollect = py.getModule("imageProcess").callAttr("getCenterLocation").toJava(String::class.java)
            Log.d("wu",positionCollect)
            for (i in 0 until targetNum){
                vectorCollect += py.getModule("imageProcess").callAttr("getCertainVector",i).toJava(String::class.java)
            }

            runOnUiThread {
                // Stuff that updates the UI
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding.targetNumInput.text = targetNum.toString()
                binding.imageViewProcess.setImageBitmap(bitmap)
                for (i in 0 until targetNum) {
                    val score = py.getModule("imageProcess").callAttr("getCertainScore", i)
                        .toJava(String::class.java).toInt()
                    if (score==-999){
                        resultCollect+=0
                        scoreTextList[i].setText("0")
                    }else{
                        val result = 10 + score
                        resultCollect+=result
                        scoreTextList[i].setText(result.toString())
                    }
                }
                for (i in targetNum..9){
                    scoreTextList[i].visibility= INVISIBLE
                }
            }
        }
        else{
            runOnUiThread{
                binding.targetNumInput.text = "0"
                targetNum=0
            }
        }

    }
    private fun confirmResult(){
        var scoreTextList = arrayOf<EditText>(binding.score1,binding.score2,binding.score3,binding.score4,binding.score5,binding.score6,binding.score7,binding.score8,binding.score9,binding.score10)
        targetNum = binding.targetNumInput.text.toString().toInt()
        if (targetNum <1 || targetNum>10){
            Toast.makeText(this, "target number should between 0 .. 10", Toast.LENGTH_SHORT).show()
            scoreList = arrayOf<Int>()
            return
        }
        for (i in 0 until targetNum){
            if (scoreTextList[i].text.isBlank()){
                Toast.makeText(this, "Position ${i+1}'s score is blank.", Toast.LENGTH_SHORT).show()
                scoreList = arrayOf<Int>()
                return
            }
            var score=scoreTextList[i].text.toString().toInt()
//            Log.d("wu",score.toString())
            if (score in 6..10){
                if (!editonly && i>=resultCollect.size){
                    vectorCollect+="888,888"
                }
                else if (editonly && i>=scores.size){
                    vectorCollect+="888,888"
                }else{
                    if (!editonly && abs(score-resultCollect[i])>1){
                        vectorCollect[i]="888,888"
                    }
                    else if (editonly && abs(score-scores[i].toInt())>1){
                        vectorCollect[i]="888,888"
                    }
                }
                scoreList+=score
            }
            else if (score == 0){
                if (!editonly && i>=resultCollect.size){
                    vectorCollect+="999,999"
                }
                else if (editonly && i>=scores.size){
                    vectorCollect+="999,999"
                }
                else {
                    vectorCollect[i] = "999,999"
                }
                scoreList += score
            }
            else{
                Toast.makeText(this, "Position ${i+1}'s score is not in 6..10 or 0", Toast.LENGTH_SHORT).show()
                scoreList = arrayOf<Int>()
                return
            }
        }

        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        comment = access.getString(imgName,"")
        if (comment==getString(R.string.processed_text)){
            comment = getComment(imgName!!)
        }
        if (comment.isNullOrBlank()){
            comment = ""
        }
        // Add to SQLite database
        imgName?.let { comment?.let { it1 -> add2Database(it,scoreList, it1,index,year,month,day,targetNum,vectorCollect.sliceArray(0 until targetNum),positionCollect) } }
        finish()
    }

    private fun list2Str(scoreList: Array<Int>):String{
        var output = ""
        for (i in scoreList){
            output+=i.toString()
            output+=","
        }
        if (output.isNotEmpty()){
            output = output.dropLast(1)
        }
        Log.d("wu",output)
        return output
    }

    private fun list2Str2(scoreList: Array<String>):String{
        var output = ""
        for (i in scoreList){
            output+=i
            output+="."
        }
        if (output.isNotEmpty()){
            output = output.dropLast(1)
        }
        Log.d("wu",output)
        return output
    }

    private fun add2Database(imgName:String, scoreList: Array<Int>, comment:String, index:Int, year:Int, month: Int, day:Int,targetNum:Int,vectorCollect:Array<String>,positionCollect:String){
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",4)
        val db = dbHelper.writableDatabase
        val values = contentValuesOf("filename" to imgName,"discipline" to index,"year" to year,"month" to month,"day" to day,"comment" to comment,"targetNum" to targetNum,"scores" to list2Str(scoreList),"vectors" to list2Str2(vectorCollect),"positions" to positionCollect)
        val cursor = db.query("ShootingRecords",null,"filename = ?",
            arrayOf<String>(imgName),null,null,null)
        if (cursor.count>0){
            db.delete("ShootingRecords","filename = ?",arrayOf<String>(imgName))
        }
        db.insert("ShootingRecords",null,values)

        db.close()

        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = access.edit()
        editor.putString(imgName,getString(R.string.processed_text))
        editor.apply()
    }

    @SuppressLint("Range")
    private fun getComment(imgName:String,desiredKey:String = "comment" ):String{
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",4)
        val db = dbHelper.readableDatabase

        val cursor = db.query("ShootingRecords",null,"filename = ?",
            arrayOf<String>(imgName),null,null,null)
        if (cursor.moveToFirst()){
            do{
                comment = cursor.getString(cursor.getColumnIndex(desiredKey))
//                Log.d("wu",displayText)
            } while (cursor.moveToNext())
        }
        db.close()
        cursor.close()
        return comment.toString()
    }
    @SuppressLint("Range")
    private fun getWhat(imgName:String,what:String,split:String):Array<String>{
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",4)
        val db = dbHelper.readableDatabase

        var scores=""
        val cursor = db.query("ShootingRecords",null,"filename = ?",
            arrayOf<String>(imgName),null,null,null)
        if (cursor.moveToFirst()){
            do{
                scores = cursor.getString(cursor.getColumnIndex(what))
//                Log.d("wu",displayText)
            } while (cursor.moveToNext())
        }
        db.close()
        cursor.close()
        return scores.split(split).toTypedArray()
    }

    override fun onDestroy() {
        super.onDestroy()
        mTask.cancel(true)
        timerTask.cancel(true)
    }
    private fun setButtons(){
        binding.processLaterButton.setOnClickListener {
            mTask.cancel(true)
            timerTask.cancel(true)
            finish()
        }

        val seek = binding.imageProcessSeekBar
        seek?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                if (seek.progress>0){
                    binding.imageProcessSeekBarIndicator.text = "Position "+seek.progress.toString()
                }
                else{
                    binding.imageProcessSeekBarIndicator.text = getString(R.string.full_target_paper)
                }
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }
            override fun onStopTrackingTouch(seek: SeekBar) {
                if (seek.progress>0){
                    val i=seek.progress-1
                    if(Python.isStarted() && !editonly){
                        val py = Python.getInstance()
                        val bytes =py.getModule("imageProcess").callAttr("getCertainOriginalImageCut",i).toJava(ByteArray::class.java)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

//                        val vector = py.getModule("imageProcess").callAttr("getCertainVector",i).toJava(String::class.java)
//                        Log.d("wu",vector)
                        binding.imageViewProcess.setImageBitmap(bitmap)
                    }
                    else{
                        Toast.makeText(this@PhotoProcess, "The target image is not available.", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    val py = Python.getInstance()
                    val bytes = py.getModule("imageProcess").callAttr("getLabeledWholeTargetPaper")
                        .toJava(ByteArray::class.java)

                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    binding.imageViewProcess.setImageBitmap(bitmap)
                }
            }
        })
    }
    inner class MyAsyncTask :AsyncTask<String, Int, Int>(){
        private var startTime :Long = 0
        private var endTime :Long = 0
        override fun onPreExecute() {
            super.onPreExecute()
            binding.confirmEditedResult.text="Waiting..."
            working=true
            startTime = System.currentTimeMillis()

        }
        override fun doInBackground(vararg params: String?): Int {

            imageProcessWrap(imageUri)
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            timerTask.cancel(true)
            endTime = System.currentTimeMillis()
//            binding.targetNumText.text= "Time used: "+((endTime-startTime)/1000).toString()
            Log.d("wu","Time used: "+((endTime-startTime)/1000).toString())
            working=false
            binding.imageProcessSeekBar.max=targetNum
            binding.allScoreWrap.visibility= VISIBLE
            binding.targetNumWrap.visibility= if (targetNum!=0) VISIBLE else INVISIBLE
            binding.confirmEditedResult.text="confirm"
            binding.processLaterButton.visibility= GONE
            binding.imageProcessSeekBarIndicator.visibility= VISIBLE
            binding.imageProcessSeekBar.visibility = VISIBLE
        }

        override fun onCancelled() {
            super.onCancelled()
            timerTask.cancel(true)
            binding.confirmEditedResult.text="process"
            working=false
        }
    }

    inner class TimerTask :AsyncTask<String, Int, Int>(){
        override fun onPreExecute() {
            super.onPreExecute()
            base=0
            binding.hintText.visibility= VISIBLE
        }
        override fun doInBackground(vararg params: String?): Int {
            while (true){
                runOnUiThread {
                    binding.hintText.text ="Time used: ${base.toString()}s."

                }
                Thread.sleep(1_000)
                base+=1
            }
            return 0
        }

        override fun onPostExecute(result: Int?) {
            super.onPostExecute(result)
            binding.hintText.visibility= INVISIBLE
            base=0
        }

        override fun onCancelled() {
            super.onCancelled()
            binding.hintText.visibility= INVISIBLE
            base=0
        }
    }
}