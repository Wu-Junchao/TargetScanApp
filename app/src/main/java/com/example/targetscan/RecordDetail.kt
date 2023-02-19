package com.example.targetscan

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.GONE
import android.view.View.INVISIBLE
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.example.targetscan.databinding.ActivityRocordDetailBinding
import java.io.File

class RecordDetail : AppCompatActivity() {
    lateinit var binding : ActivityRocordDetailBinding
    lateinit var outputImage: File
    lateinit var imageUri : Uri
    var displayText = ""
    val disciplineList = mutableListOf<String>("Rifle shoot","Test")
    var flg = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imgName = intent.getStringExtra("name")
        val imgProcessLabel = intent.getStringExtra("processLabel")
        binding = ActivityRocordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbarRecordDetail)
        supportActionBar?.title = "Record Details"

        outputImage = File(externalCacheDir,imgName!!)
        if (outputImage.exists()){
            imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                FileProvider.getUriForFile(this,"com.example.cameraalbumtest.fileprovider",outputImage)
            }else{
                Uri.fromFile(outputImage)
            }
            binding.imageDetail.setImageURI(imageUri)
        }
        else{
            val intent = Intent(this,FillInformation::class.java)
            startActivity(intent)
            finish()
        }

            if (imgProcessLabel!="Processed"){
                binding.InformationCollect.text="Not yet processed"
                binding.ScoreCollect.visibility= INVISIBLE
            }
            else {
//                binding.processButton.visibility=GONE
                binding.processButton.text="edit"
                flg=true
                displayData(imgName)
            }


        binding.backButton.setOnClickListener{
            intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
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
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",2)
        val db = dbHelper.readableDatabase

        val cursor = db.query("ShootingRecords",null,"filename = ?",
            arrayOf<String>(imgName),null,null,null)
        if (cursor.moveToFirst()){
            do{
                displayText+="Discipline: "
                displayText+= disciplineList[cursor.getInt(cursor.getColumnIndex("discipline"))]
                displayText+="\nDate: "
                displayText+=imgName.slice(1..10)
                displayText+="\nID: "
                displayText+=imgName.slice(11..13)
                displayText+="\n"
                displayText+="Comment: "
                displayText+=cursor.getString(cursor.getColumnIndex("comment"))
                binding.InformationCollect.text=displayText
                binding.ScoreCollect.text = cursor.getString(cursor.getColumnIndex("scores"))
//                Log.d("wu",displayText)
            } while (cursor.moveToNext())
        }
        db.close()
        cursor.close()
    }

}