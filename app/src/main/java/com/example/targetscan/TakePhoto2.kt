package com.example.targetscan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View.*
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
import androidx.camera.core.ImageCapture.FLASH_MODE_ON
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.core.content.FileProvider
import java.io.File
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.targetscan.MainActivity.Companion.REQUEST_CODE_PERMISSIONS
import com.example.targetscan.databinding.ActivityTakePhoto2Binding
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TakePhoto2 : AppCompatActivity() {
    lateinit var binding: ActivityTakePhoto2Binding
    lateinit var imageUri: Uri
    lateinit var outputImage: File
    lateinit var discplineList: MutableList<String>
    var nextID = 1
    private val pickImg = 100
    private var imageCapture: ImageCapture? = null

    private var index= 0
    private var year = 2023
    private var month = 1
    private var day = 1
    private lateinit var cameraExecutor: ExecutorService

    private fun dateFormat(year: Int, month: Int, day: Int): String {
        var m = if (month in 1..9) {
            "0$month"
        } else {
            "$month"
        }
        var d = if (day in 1..9) {
            "0$day"
        } else {
            "$day"
        }
        return "$year-${m}-${d}"
    }

    private fun int2ThreeDigits(number: Int): String {
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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().setFlashMode(FLASH_MODE_ON).setCaptureMode(
                CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview , imageCapture
                )

            } catch (exc: Exception) {
                Log.d("wu", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private fun takePhoto(img:File) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return


        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(img)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.d("wu", "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d("wu", msg)
                    binding.confirmPhotoBtn.visibility = VISIBLE
                    binding.rotatePhotoBtn.visibility= VISIBLE
                    binding.takePhotoBtn.text="Retake photo"
                    binding.cameraPreview.visibility=GONE
                    binding.imageView.visibility= VISIBLE
                    val inputStream = contentResolver.openInputStream(imageUri)
                    val originalImg = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    binding.imageView.setImageBitmap(rotateIfRequired(originalImg))
                    cameraProvider.unbindAll()
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakePhoto2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar2)
        supportActionBar?.title = "Add new record"

        discplineList = mutableListOf<String>("Rifle shoot")
        index = intent.getIntExtra("index", 0)
        year = intent.getIntExtra("year", 2023)
        month = intent.getIntExtra("month", 1)
        day = intent.getIntExtra("day", 1)
        val comment = intent.getStringExtra("comment")
        binding.info1.text = "Discipline: ${discplineList[index]}"
        binding.info2.text = "Date: $year.$month.$day"
        binding.info3.text = "Comment: $comment"
        val concatDate = "$index${dateFormat(year, month, day)}"
        val access = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = access.edit()
        var totalNum = access.getInt("totalNum", -1)

        binding.cameraPreview.scaleType = PreviewView.ScaleType.FIT_CENTER
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

//        while (access.getString("$concatDate${int2ThreeDigits(nextID)}.jpg", "404") != "404") {
//            nextID += 1
//        }
        nextID=findNextID(index,year, month, day)

        binding.takePhotoBtn.setOnClickListener {
            if (binding.takePhotoBtn.text.toString() == "Retake photo" && allPermissionsGranted()) {
                binding.cameraPreview.visibility= VISIBLE
                binding.imageView.visibility= GONE
                binding.confirmPhotoBtn.visibility = INVISIBLE
                binding.rotatePhotoBtn.visibility = INVISIBLE
                binding.takePhotoBtn.text="take photo"
                startCamera()

            }
            else{
                outputImage = File(
                    externalCacheDir,
                    "$index${dateFormat(year, month, day)}${int2ThreeDigits(nextID)}.jpg"
                )
                if (outputImage.exists()) {
                    outputImage.delete()
                }
                outputImage.createNewFile()
                imageUri =FileProvider.getUriForFile(this,"com.example.cameraalbumtest.fileprovider",outputImage)
                takePhoto(outputImage)
            }
        }

        binding.cancelPhotoBtn.setOnClickListener {
            outputImage =
                File(externalCacheDir, "$index${dateFormat(year, month, day)}${int2ThreeDigits(nextID)}.jpg")
            if (outputImage.exists()) {
                outputImage.delete()
            }
            val intent = Intent(this, FillInformation::class.java)
            intent.putExtra("index", index)
            intent.putExtra("year", year)
            intent.putExtra("month", month)
            intent.putExtra("day", day)
            intent.putExtra("comment", comment)
            startActivity(intent)
            finish()
        }

        binding.uploadBtn.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImg)
        }

        binding.confirmPhotoBtn.setOnClickListener {
            editor.putString(
                "$index${dateFormat(year, month, day)}${int2ThreeDigits(nextID)}.jpg",
                comment
            )
            totalNum += 1
            editor.putInt("totalNum", totalNum)
            editor.apply()
            val intent = Intent(this, PhotoProcess::class.java)
            intent.putExtra("index", index)
            intent.putExtra("year", year)
            intent.putExtra("month", month)
            intent.putExtra("day", day)
            intent.putExtra("comment", comment)
            intent.putExtra(
                "ImgName",
                "$index${dateFormat(year, month, day)}${int2ThreeDigits(nextID)}.jpg"
            )
            startActivity(intent)
            finish()
        }

        binding.rotatePhotoBtn.setOnClickListener {
            outputImage =
                File(externalCacheDir, "$index${dateFormat(year, month, day)}${int2ThreeDigits(nextID)}.jpg")
            if (!outputImage.exists()) {
                // do nothing
            }
            else{
                imageUri =FileProvider.getUriForFile(this,"com.example.cameraalbumtest.fileprovider",outputImage)
                val inputStream = contentResolver.openInputStream(imageUri)
                var originalImg = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                outputImage.delete()
                val rotatedImg = rotateBitmap(originalImg,90)
                outputImage.createNewFile()
                val fileOutputStream = FileOutputStream(outputImage)
                rotatedImg.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                binding.imageView.setImageBitmap(rotatedImg)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun rotateIfRequired(bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(outputImage.path)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
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

    private fun findNextID(index:Int,year:Int,month:Int,day: Int):Int{
        var photoList = externalCacheDir?.list()
        var result = 0
        if (photoList.isNullOrEmpty()){
            return 1
        }
        else{
            for (photo in photoList){
                val head = "$index${dateFormat(year, month, day)}"
                if (photo.slice(0..10) ==head && photo.slice(11..13).toInt()>result){
                    result=photo.slice(11..13).toInt()
                }
            }
        }
        return result+1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImg) {
            imageUri = data?.data!!
            val inputStream = contentResolver.openInputStream(imageUri)
            val originalImg = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            binding.imageView.setImageBitmap(originalImg)
            binding.confirmPhotoBtn.visibility = VISIBLE
            binding.rotatePhotoBtn.visibility = VISIBLE
            binding.takePhotoBtn.text="Retake photo"
            binding.cameraPreview.visibility= GONE
            binding.imageView.visibility= VISIBLE
            outputImage = File(
                externalCacheDir,
                "$index${dateFormat(year, month, day)}${int2ThreeDigits(nextID)}.jpg"
            )
            if (outputImage.exists()) {
                outputImage.delete()
            }
            outputImage.createNewFile()
            val fileOutputStream = FileOutputStream(outputImage)
            originalImg.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        }
    }
}