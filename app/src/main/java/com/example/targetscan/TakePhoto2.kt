package com.example.targetscan

import android.Manifest
import android.app.Activity
import android.content.ContentValues
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
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
import androidx.camera.core.ImageCapture.FLASH_MODE_ON
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.example.targetscan.databinding.ActivityTakePhoto1Binding
import java.io.File
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.targetscan.MainActivity.Companion.REQUEST_CODE_PERMISSIONS
import com.example.targetscan.databinding.ActivityTakePhoto2Binding
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TakePhoto2 : AppCompatActivity() {
    lateinit var binding: ActivityTakePhoto2Binding
    val takePhoto = 1
    lateinit var imageUri: Uri
    lateinit var outputImage: File
    lateinit var discplineList: MutableList<String>
    var nextID = 1

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

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
                    binding.confirmPhotoBtn.isVisible=true
                    binding.takePhotoBtn.text="Retake photo"
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
        supportActionBar?.title = "Add new record";

        discplineList = mutableListOf<String>("Rifle shoot", "Test")
        val index = intent.getIntExtra("index", 0)
        val year = intent.getIntExtra("year", 2023)
        val month = intent.getIntExtra("month", 1)
        val day = intent.getIntExtra("day", 1)
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

        while (access.getString("$concatDate${int2ThreeDigits(nextID)}.jpg", "404") != "404") {
            nextID += 1
        }

        binding.takePhotoBtn.setOnClickListener {
            if (binding.takePhotoBtn.text.toString() == "Retake photo" && allPermissionsGranted()) {
                startCamera()
            }
            outputImage = File(
                externalCacheDir,
                "$index${dateFormat(year, month, day)}${int2ThreeDigits(nextID)}.jpg"
            )
            if (outputImage.exists()) {
                outputImage.delete()
            }
            outputImage.createNewFile()
            takePhoto(outputImage)

        }

        binding.cancelPhotoBtn.setOnClickListener {
            outputImage =
                File(externalCacheDir, "$index${dateFormat(year, month, day)}${nextID - 1}.jpg")
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

        binding.confirmPhotoBtn.setOnClickListener {
            editor.putString(
                "$index${dateFormat(year, month, day)}${int2ThreeDigits(nextID)}.jpg",
                "NotYetProcessed"
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
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}