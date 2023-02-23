package com.example.targetscan

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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
        }
        else{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        displayInfo()


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
                displayText+="\nID: "
                displayText+=imgName.slice(11..13)
                displayText+="\n"
                displayText+="Comment: "
                displayText+=cursor.getString(cursor.getColumnIndex("comment"))

                displayText+="\nVectors: "
                displayText+=cursor.getString(cursor.getColumnIndex("vectors"))

                binding.InformationCollect.text=displayText
                binding.ScoreCollect.text = cursor.getString(cursor.getColumnIndex("scores"))
                Log.d("wu",cursor.getString(cursor.getColumnIndex("vectors")))
            } while (cursor.moveToNext())
        }
        db.close()
        cursor.close()
    }

    private fun displayInfo(){
        if (imgProcessLabel!=getString(R.string.processed_text)){
            var str = ""
            str += "Discipline: ${disciplineList[imgName.slice(0..0).toInt()]}\n\n"
            str += "Date: ${imgName.slice(1..10)}\n\n"
            str += "ID: ${imgName.slice(11..13)}\n\n"
            val access = getSharedPreferences("data", Context.MODE_PRIVATE)
            val comment = access.getString(imgName,"")
            str += "Comment: ${comment}"
            binding.InformationCollect.text=str
            binding.ScoreCollect.visibility= INVISIBLE
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

}