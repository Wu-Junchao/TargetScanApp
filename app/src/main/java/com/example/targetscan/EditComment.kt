package com.example.targetscan

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.contentValuesOf
import com.example.targetscan.databinding.EditCommentBinding
class EditComment : AppCompatActivity() {
    lateinit var binding:EditCommentBinding
    lateinit var myDateList : MutableList<String>
    var imgName:String = ""
    var comment:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar3)

        //restart Parameter
        imgName = intent.getStringExtra("imgName").toString()
        supportActionBar?.title = "Edit Existing Record"
        myDateList = mutableListOf<String>("Rifle shoot","Test")

        showInfo(imgName)

        binding.confirmBtn.setOnClickListener {
            comment = binding.commentInput.text.toString()
            if (comment!!.length>250){
                Toast.makeText(this, "Comment length exceeds 250 chars.", Toast.LENGTH_SHORT).show()
            }
            else{
                val access = getSharedPreferences("data", Context.MODE_PRIVATE)
                if (access.getString(imgName,"")=="Processed"){
                    add2Database(imgName,comment)
                }
                else{
                    val editor = access.edit()
                    editor.putString(imgName,comment)
                    editor.apply()
                }
                finish()
            }
        }
        binding.cancelBtn.setOnClickListener {
            finish()
        }
    }

    private fun add2Database(imgName:String, comment:String){
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",3)
        val db = dbHelper.writableDatabase
        val values = contentValuesOf("comment" to comment)
        val cursor = db.query("ShootingRecords",null,"filename = ?",
            arrayOf<String>(imgName),null,null,null)
        if (cursor.count>0){
            db.update("ShootingRecords",values,"filename = ?",arrayOf<String>(imgName))
        }
        db.close()
    }

    @SuppressLint("Range")
    private fun showInfo(imgName:String){
        binding.disciplineText.text = "Discipline: ${myDateList[imgName.slice(0..0).toInt()]}"
        binding.dateText.text = imgName.slice(1..10)
        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        if (access.getString(imgName,"")=="Processed"){
            binding.commentInput.setText(getComment(imgName))
        }
        else{
            binding.commentInput.setText(access.getString(imgName,""))
        }

    }

    @SuppressLint("Range")
    private fun getComment(imgName:String):String{
        val dbHelper = MyDatabaseHelper(this,"TargetScan.db",3)
        val db = dbHelper.readableDatabase

        var comment=""
        val cursor = db.query("ShootingRecords",null,"filename = ?",
            arrayOf<String>(imgName),null,null,null)
        if (cursor.moveToFirst()){
            do{
                comment = cursor.getString(cursor.getColumnIndex("comment"))
//                Log.d("wu",displayText)
            } while (cursor.moveToNext())
        }
        db.close()
        cursor.close()
        return comment
    }
}