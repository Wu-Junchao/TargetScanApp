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
import android.widget.CompoundButton
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
    val disciplineList = mutableListOf<String>("Rifle shoot","Test")
    var flg = false
    var imgName = ""
    var imgProcessLabel = ""
    lateinit var vectors :String
    lateinit var parsed_vectors:Array<String>
    lateinit var originalImg:Bitmap
    lateinit var scores :String
    var arrowToggle = true
    var targetNum = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imgName = intent.getStringExtra("name").toString()
        imgProcessLabel = intent.getStringExtra("processLabel").toString()
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
//            originalImg= rotateBitmap(originalImg,0)
            binding.imageDetail.setImageBitmap(originalImg)

        }

        else{
            val intent = Intent(this,MainActivity::class.java)
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
        }

    }

    @SuppressLint("Range")
    private fun displayData(imgName:String){
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",3)
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
                displayText+="Comment: "
                displayText+=cursor.getString(cursor.getColumnIndex("comment"))
                targetNum = cursor.getInt(cursor.getColumnIndex("targetNum"))
                vectors = cursor.getString(cursor.getColumnIndex("vectors"))
                parsed_vectors = vectors.split(".").toTypedArray()
                binding.InformationCollect.text=displayText
                scores = cursor.getString(cursor.getColumnIndex("scores"))
//                binding.ScoreCollect.text = cursor.getString(cursor.getColumnIndex("scores"))
                Log.d("wu",cursor.getString(cursor.getColumnIndex("vectors")))
            } while (cursor.moveToNext())
        }
        db.close()
        cursor.close()
    }

    private fun displayInfo(){
        if (imgProcessLabel!=getString(R.string.processed_text)){
            var str = ""
            str += "Discipline: ${disciplineList[imgName.slice(0..0).toInt()]}\n"
            str += "Date: ${imgName.slice(1..10)}\n"
//            str += "ID: ${imgName.slice(11..13)}\n"
            val access = getSharedPreferences("data", Context.MODE_PRIVATE)
            val comment = access.getString(imgName,"")
            str += "Comment: ${comment}"
            binding.InformationCollect.text=str
            binding.seekBar.visibility=GONE
            binding.seekbarIndicator.visibility= GONE
        }
        else {
            binding.processButton.text="edit"
            flg=true
            displayData(imgName)
        }
    }
    override fun onResume() {
        super.onResume()
        displayInfo()
    }

    private fun deleteRecord(imgName:String){
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",3)
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

    private fun changeImage(vector:String){
        if(!Python.isStarted()){
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()
        val bytes = py.getModule("dataAnalysis").callAttr("drawCircleGraph",vector).toJava(ByteArray::class.java)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imageDetail.setImageBitmap(bitmap)
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
                    binding.seekbarIndicator.setText("Position "+(seek.progress-1).toString())
                }
                else if (seek.progress==0){
                    binding.arrowScoreSwitch.visibility= INVISIBLE
                    binding.seekbarIndicator.text = "Full target paper"
                }
                else{
                    binding.arrowScoreSwitch.visibility= VISIBLE
                    if (arrowToggle){
                        binding.seekbarIndicator.text = "Arrow graph"
                    }
                    else{
                        binding.seekbarIndicator.text = "Score graph"
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
                    changeImage(parsed_vectors[seek.progress-2])
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
        toggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // The toggle is enabled
                getScoreImage(vectors,scores)
                toggle.text="Score"
                arrowToggle=false
                binding.seekbarIndicator.text = "Score graph"
            } else {
                // The toggle is disabled
                getArrowImage(vectors,scores)
                toggle.text="Arrow"
                arrowToggle=true
                binding.seekbarIndicator.text = "Arrow graph"
            }
        }
    }
}