package com.example.targetscan

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View.*
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.targetscan.databinding.ActivityRocordDetailBinding
import java.io.File


class RecordDetail : AppCompatActivity() {
    lateinit var binding : ActivityRocordDetailBinding
    lateinit var outputImage: File
    lateinit var imageUri : Uri
    var displayText = ""
    val disciplineList = mutableListOf<String>("Small-bore Rifle Shooting")
    var flg = false
    var imgName = ""
    lateinit var vectors :String
    lateinit var parsedVectors:Array<String>
    lateinit var originalImg:Bitmap
    lateinit var scores :String
    var arrowToggle = true
    var targetNum = 0
    private lateinit var positionPoint:Array<String>
    private var renderedFlg = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imgName = intent.getStringExtra("name").toString()
        binding = ActivityRocordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarRecordDetail)
        supportActionBar?.title = "Record Details"

        outputImage = File(externalCacheDir,imgName)
        if (outputImage.exists()){
            imageUri =
                FileProvider.getUriForFile(this,"com.example.cameraalbumtest.fileprovider",outputImage)

            binding.imageDetail.setImageURI(imageUri)
            val inputStream = contentResolver.openInputStream(imageUri)
            originalImg = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (originalImg.height>originalImg.width){
                originalImg = rotateBitmap(originalImg,270)
            }
            binding.imageDetail.setImageBitmap(originalImg)

        }

        else{
            val intent = Intent(this,MonthSelect::class.java)
            startActivity(intent)
            finish()
        }

        displayInfo()

        setSeekBar()
        setToggle()

        binding.backButton.setOnClickListener{
            val alert: AlertDialog.Builder =AlertDialog.Builder(this)
            alert.setTitle("Delete entry")
            alert.setMessage("Are you sure you want to delete?")
            alert.setPositiveButton(android.R.string.yes, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    deleteRecord(imgName)
                    finish()
                }
            })
            alert.setNegativeButton(android.R.string.no,
                DialogInterface.OnClickListener { dialog, which -> // close dialog
                    dialog.cancel()
                })
            alert.show()
        }
        binding.editCommentButton.setOnClickListener {
            intent = Intent(this,EditComment::class.java)
            intent.putExtra("imgName",imgName)
            startActivity(intent)
        }
        binding.processButton.setOnClickListener {
            val intent = Intent(this,PhotoProcess::class.java)
            intent.putExtra("ImgName",imgName)
            if (flg){
                intent.putExtra("editonly",true)
            }
            startActivity(intent)
            finish()
        }

    }

    @SuppressLint("Range")
    private fun displayData(imgName:String){
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",4)
        val db = dbHelper.readableDatabase

        val cursor = db.query("ShootingRecords",null,"filename = ?",
            arrayOf<String>(imgName),null,null,null)
        if (cursor.moveToFirst()){
            do{
                displayText=""
                displayText+="Discipline: "
                displayText+= disciplineList[cursor.getInt(cursor.getColumnIndex("discipline"))]
                displayText+="\nDate: "
                displayText+=imgName.slice(1..10)
//                displayText+="\nID: "
//                displayText+=imgName.slice(11..13)
                displayText+="\n"
                displayText+="Comment: \n"
                displayText+=cursor.getString(cursor.getColumnIndex("comment"))
                targetNum = cursor.getInt(cursor.getColumnIndex("targetNum"))
                vectors = cursor.getString(cursor.getColumnIndex("vectors"))
                parsedVectors = vectors.split(".").toTypedArray()
                binding.InformationCollect.text=displayText
                scores = cursor.getString(cursor.getColumnIndex("scores"))

                positionPoint= cursor.getString(cursor.getColumnIndex("positions")).split(".").toTypedArray()
//                binding.ScoreCollect.text = cursor.getString(cursor.getColumnIndex("scores"))
//                Log.d("wu",cursor.getString(cursor.getColumnIndex("vectors")))
            } while (cursor.moveToNext())
        }
        db.close()
        cursor.close()
    }

    private fun displayInfo(){
        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        val comment = access.getString(imgName,"")
        if (comment!=getString(R.string.processed_text)){
            var str = ""
            str += "Discipline: ${disciplineList[imgName.slice(0..0).toInt()]}\n"
            str += "Date: ${imgName.slice(1..10)}\n"
//            str += "ID: ${imgName.slice(11..13)}\n"

            str += "Comment: \n${comment}"
            binding.InformationCollect.text=str
            binding.seekBar.visibility=GONE
            binding.seekbarIndicator.visibility= GONE
        }
        else {
            binding.processButton.text="edit"
            binding.seekBar.visibility= VISIBLE
            binding.seekbarIndicator.visibility= VISIBLE
            flg=true
            displayData(imgName)
        }
    }
    override fun onResume() {
        super.onResume()
        displayInfo()
        val seek = binding.seekBar
        if (seek.progress>1){
            changeImage(parsedVectors[seek.progress-2],seek.progress-2)
        }
        else if (seek.progress==0){
            binding.imageDetail.setImageBitmap(originalImg)
        }
        else{
            binding.arrowScoreSwitch.visibility= VISIBLE
            if (arrowToggle){
                getArrowImage(vectors,scores)
            }
            else{
                getScoreImage(vectors,scores)
            }
        }
    }

    private fun deleteRecord(imgName:String){
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",4)
        val db = dbHelper.writableDatabase
        val cursor = db.query("ShootingRecords",null,"filename = ?",
            arrayOf<String>(imgName),null,null,null)
        if (cursor.count>0){
            db.delete("ShootingRecords","filename = ?",arrayOf<String>(imgName))
        }
        db.close()

        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = access.edit()
        editor.remove(imgName)
        editor.apply()

        outputImage = File(
            externalCacheDir,
            imgName
        )
        if (outputImage.exists()) {
            outputImage.delete()
        }
    }
    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height,
            matrix, true)
        bitmap.recycle() // 将不再需要的Bitmap对象回收 return rotatedBitmap
        return rotatedBitmap
    }

    private fun changeImage(vector:String,index:Int){
        if ( vector.slice(0..2)=="888" || renderedFlg){
            binding.singleTargetViewSwitch.visibility= if (vector.slice(0..2)=="888") INVISIBLE else VISIBLE
            val certainPosition = positionPoint[index].split(",")
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }
            val py = Python.getInstance()
            Log.d("wu",positionPoint[index])
            val content = contentResolver.openInputStream(imageUri)!!.use { it.readBytes() }
            val bytes = py.getModule("imageProcess").callAttr("cropImg",content,certainPosition[0].toInt(),certainPosition[1].toInt(),certainPosition[2].toInt()).toJava(ByteArray::class.java)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageDetail.setImageBitmap(bitmap)
            binding.imageDetail.scaleType = ImageView.ScaleType.FIT_CENTER
        }else{
            if(!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }
            val py = Python.getInstance()
            val bytes = py.getModule("dataAnalysis").callAttr("drawCircleGraph",vector).toJava(ByteArray::class.java)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            binding.imageDetail.setImageBitmap(bitmap)
        }
    }

    private fun getArrowImage(vector:String,score:String){
        if(!Python.isStarted()){
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()
        val bytes = py.getModule("dataAnalysis").callAttr("drawArrowGraph",vector,score).toJava(ByteArray::class.java)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imageDetail.setImageBitmap(rotateBitmap(bitmap,270))
        binding.imageDetail.scaleType=ImageView.ScaleType.CENTER_CROP
    }

    private fun getScoreImage(vector:String,score:String){
        if(!Python.isStarted()){
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()
        val bytes = py.getModule("dataAnalysis").callAttr("drawScoreGraph",vector,score).toJava(ByteArray::class.java)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imageDetail.setImageBitmap(rotateBitmap(bitmap,270))
        binding.imageDetail.scaleType=ImageView.ScaleType.CENTER_CROP
    }

    private fun setSeekBar(){
        val seek = binding.seekBar
        seek.max = targetNum+1
        seek?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                if (seek.progress>1){
                    binding.arrowScoreSwitch.visibility= INVISIBLE
                    binding.singleTargetViewSwitch.visibility = VISIBLE
                    binding.seekbarIndicator.text = "Position "+(seek.progress-1).toString()
                }
                else if (seek.progress==0){
                    binding.arrowScoreSwitch.visibility= INVISIBLE
                    binding.seekbarIndicator.text = getString(R.string.full_target_paper)
                    binding.singleTargetViewSwitch.visibility = INVISIBLE
                }
                else{
                    binding.arrowScoreSwitch.visibility= VISIBLE
                    binding.singleTargetViewSwitch.visibility = INVISIBLE
                    if (arrowToggle){
                        binding.seekbarIndicator.text = "Arrow Graph"
                    }
                    else{
                        binding.seekbarIndicator.text = "Score Graph"
                    }
                }
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }
            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
//                Toast.makeText(this@RecordDetail,
//                    "Progress is: " + seek.progress ,
//                    Toast.LENGTH_SHORT).show()
                if (seek.progress>1){
                    changeImage(parsedVectors[seek.progress-2],seek.progress-2)
                }
                else if (seek.progress==0){
                    binding.imageDetail.setImageBitmap(originalImg)
                }
                else{
                    binding.arrowScoreSwitch.visibility= VISIBLE
                    if (arrowToggle){
                        getArrowImage(vectors,scores)
                    }
                    else{
                        getScoreImage(vectors,scores)
                    }
                }
            }
        })
    }
    private fun setToggle(){
        val toggle = binding.arrowScoreSwitch
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // The toggle is enabled
                getScoreImage(vectors,scores)
                toggle.text="Score"
                arrowToggle=false
                binding.seekbarIndicator.text = "Score Graph"
            } else {
                // The toggle is disabled
                getArrowImage(vectors,scores)
                toggle.text="Arrow"
                arrowToggle=true
                binding.seekbarIndicator.text = "Arrow Graph"
            }
        }

        val toggle2 = binding.singleTargetViewSwitch
        val seek = binding.seekBar
        toggle2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // The toggle is enabled

                toggle2.text=getString(R.string.graphDisplaySwitch2)
                renderedFlg=true
                changeImage(parsedVectors[seek.progress-2],seek.progress-2)
            } else {
                // The toggle is disabled
                toggle2.text=getString(R.string.graphDisplaySwitch1)
                renderedFlg=false
                changeImage(parsedVectors[seek.progress-2],seek.progress-2)

            }
        }
    }
}