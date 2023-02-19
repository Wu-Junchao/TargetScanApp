package com.example.targetscan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.example.targetscan.databinding.ActivityTakePhoto1Binding
import java.io.File

class TakePhoto1 : AppCompatActivity() {
    lateinit var binding: ActivityTakePhoto1Binding
    val takePhoto =1
    lateinit var imageUri : Uri
    lateinit var outputImage: File
    lateinit var discplineList:MutableList<String>
    var nextID=1

    private fun dateFormat(year:Int, month: Int, day:Int):String{
        var m = if (month in 1..9){
            "0$month"
        } else{
            "$month"
        }
        var d = if (day in 1..9){
            "0$day"
        } else{
            "$day"
        }
        return "$year-${m}-${d}"
    }

    private fun int2ThreeDigits(number:Int):String{
        return when (number) {
            in 1..9 -> {
                "00$number"
            }
            in 10..99 -> {
                "0$number"
            }
            in 100..999 -> {
                "$number"
            }
            else -> {
                "000"
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakePhoto1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar2)
        supportActionBar?.title = "Add new record"

        discplineList = mutableListOf<String>("Rifle shoot","Test")
        val index = intent.getIntExtra("index",0)
        val year =intent.getIntExtra("year",2023)
        val month = intent.getIntExtra("month",1)
        val day = intent.getIntExtra("day",1)
        val comment = intent.getStringExtra("comment")
        binding.info1.text="Discipline: ${discplineList[index]}"
        binding.info2.text="Date: $year.$month.$day"
        binding.info3.text = "Comment: $comment"
        val concatDate = "$index${dateFormat(year,month,day)}"
        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = access.edit()
        var totalNum = access.getInt("totalNum",-1)

        while (access.getString("$concatDate${int2ThreeDigits(nextID)}.jpg","404")!="404"){
            nextID+=1
        }

        binding.takePhotoBtn.setOnClickListener {
            outputImage = File(externalCacheDir,"$index${dateFormat(year,month,day)}${int2ThreeDigits(nextID)}.jpg")
            if (outputImage.exists()){
                outputImage.delete()
            }
            outputImage.createNewFile()
            imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                FileProvider.getUriForFile(this,"com.example.cameraalbumtest.fileprovider",outputImage)
            }else{
                Uri.fromFile(outputImage)
            }
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
            startActivityForResult(intent,takePhoto)
        }

        binding.cancelPhotoBtn.setOnClickListener {
            outputImage = File(externalCacheDir,"$index${dateFormat(year,month,day)}${nextID-1}.jpg")
            if (outputImage.exists()){
                outputImage.delete()
            }
            val intent = Intent(this,FillInformation::class.java)
            intent.putExtra("index", index)
            intent.putExtra("year", year)
            intent.putExtra("month", month)
            intent.putExtra("day", day)
            intent.putExtra("comment",comment)
            startActivity(intent)
            finish()
        }

        binding.confirmPhotoBtn.setOnClickListener {
            editor.putString("$index${dateFormat(year,month,day)}${int2ThreeDigits(nextID)}.jpg","NotYetProcessed")
            totalNum+=1
            editor.putInt("totalNum",totalNum)
            editor.apply()
            val intent = Intent(this,PhotoProcess::class.java)
            intent.putExtra("index", index)
            intent.putExtra("year", year)
            intent.putExtra("month", month)
            intent.putExtra("day", day)
            intent.putExtra("comment",comment)
            intent.putExtra("ImgName","$index${dateFormat(year,month,day)}${int2ThreeDigits(nextID)}.jpg")
            startActivity(intent)
            finish()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            takePhoto -> {
                if (resultCode == Activity.RESULT_OK) {
// 将拍摄的照片显示出来
                    val bitmap = BitmapFactory.decodeStream(contentResolver.
                    openInputStream(imageUri))
                    binding.imageView.setImageBitmap(rotateIfRequired(bitmap))
                    binding.confirmPhotoBtn.isVisible=true
                    binding.takePhotoBtn.text="Retake photo"
                } }
        } }
    private fun rotateIfRequired(bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(outputImage.path)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
            else -> bitmap
        } }
    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height,
            matrix, true)
        bitmap.recycle() // 将不再需要的Bitmap对象回收 return rotatedBitmap
        return rotatedBitmap
    }
}