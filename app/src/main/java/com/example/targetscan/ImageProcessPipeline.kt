package com.example.targetscan

import org.opencv.android.Utils
import org.opencv.core.Mat
import android.graphics.Bitmap
import android.util.Log
import org.opencv.imgproc.Imgproc
import org.opencv.android.OpenCVLoader

class ImageProcessPipeline (private val originalImg:Bitmap, val targetNum:Int){

    private var originalImgMat : Mat = Mat()
    private var outputImgMat :Mat = Mat()
    private var outputImg : Bitmap = originalImg.copy(originalImg.config,true)

    init {
        if (OpenCVLoader.initDebug()){
            Utils.bitmapToMat(originalImg,originalImgMat)
        }
        else{
            Log.d("wu","Failed to configure Opencv.")
        }
    }

    fun preProcess(){
        if (OpenCVLoader.initDebug()){
            Imgproc.cvtColor(originalImgMat,outputImgMat, Imgproc.COLOR_BGR2GRAY)
        }
    }

    fun returnImg():Bitmap{
        if (OpenCVLoader.initDebug()) {
            Utils.matToBitmap(outputImgMat, outputImg)
        }
        return outputImg
    }

}