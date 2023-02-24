package com.example.targetscan

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.targetscan.databinding.ActivitySettingBinding
import java.io.File

class Setting : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding
    private var ascendingOrder = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Setting"

        setOrderSwitch()
        binding.deleteButton.setOnClickListener {
            val alert: AlertDialog.Builder = AlertDialog.Builder(this)
            alert.setTitle("Delete entry")
            alert.setMessage("Are you sure you want to delete?")
            alert.setPositiveButton(android.R.string.yes, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    deleteEverything()
                }
            })
            alert.setNegativeButton(android.R.string.no,
                DialogInterface.OnClickListener { dialog, which -> // close dialog
                    dialog.cancel()
                })
            alert.show()
        }
    }

    private fun setOrderSwitch(){
        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        ascendingOrder = access.getBoolean("ascendingOrder",false)

        val toggle = binding.orderSwitch
        toggle.isChecked=ascendingOrder
        toggle.text = if (ascendingOrder){
            "Ascending"
        }else{
            "Descending"
        }
        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // The toggle is enabled
                ascendingOrder = true
                toggle.text="Ascending"
            } else {
                // The toggle is disabled
                ascendingOrder = false
                toggle.text = "Descending"
            }
            val editor = access.edit()
            editor.putBoolean("ascendingOrder",ascendingOrder)
            editor.apply()
        }
    }

    private fun deleteEverything(){
        var photoList = externalCacheDir?.list()
        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = access.edit()
        editor.putInt("totalNum",0)
        editor.apply()
        if (photoList.isNullOrEmpty()){
            return
        }
        else{
            for (photo in photoList){
                // delete file
                val outputImage = File(
                    externalCacheDir,
                    photo
                )
                if (outputImage.exists()) {
                    outputImage.delete()
                }

                // delete database
                val dbHelper = MyDatabaseHelper(this,"TargetScan.db",3)
                val db = dbHelper.writableDatabase
                val cursor = db.query("ShootingRecords",null,"filename = ?",
                    arrayOf<String>(photo),null,null,null)
                if (cursor.count>0){
                    db.delete("ShootingRecords","filename = ?",arrayOf<String>(photo))
                }
                db.close()

                // delete xml

                val editor = access.edit()
                editor.remove(photo)
                editor.apply()
            }
        }
        Toast.makeText(this, "All records has been deleted.", Toast.LENGTH_SHORT).show()
        return
    }
}