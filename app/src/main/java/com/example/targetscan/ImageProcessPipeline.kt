package com.example.targetscan

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType.CV_8UC1
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.core.times
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc


class ImageProcessPipeline (private val originalImg:Bitmap, val targetNum:Int){

    private var originalImgMat : Mat = Mat()
    private var outputImgMat :Mat = Mat()
    private var outputImg : Bitmap = originalImg.copy(originalImg.config,true)
    private val SCALED_WIDTH = 300
    private val CROPPED_SCALED_WIDTH=300
    private var EXTRA_RADIUS_ORIGINAL=0
    private val EXTRA_RADIUS=30
    init {
        if (OpenCVLoader.initDebug()){
            Utils.bitmapToMat(originalImg,originalImgMat)
            EXTRA_RADIUS_ORIGINAL = 20*(originalImgMat.width()/300)
            preProcess()
        }
        else{
            Log.d("wu","Failed to configure Opencv.")
        }
    }

    fun preProcess(){
        if (OpenCVLoader.initDebug()){
            Imgproc.cvtColor(originalImgMat,outputImgMat, Imgproc.COLOR_BGR2GRAY)
            outputImgMat.convertTo(outputImgMat,CV_8UC1)
            Log.d("wu",outputImgMat.size().toString())
            var outputImage2 = Mat()
//            Core.copyMakeBorder(outputImgMat,outputImgMat,EXTRA_RADIUS_ORIGINAL,EXTRA_RADIUS_ORIGINAL,EXTRA_RADIUS_ORIGINAL,EXTRA_RADIUS_ORIGINAL,Core.BORDER_REPLICATE)
            Imgproc.adaptiveThreshold(outputImgMat,outputImgMat,255.0,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY,199,5.0)
//            var kernel = Mat.ones(5,5,CV_8UC1)
//            Core.subtract(MatOfDouble(255.0),outputImgMat,outputImgMat)
//            Imgproc.morphologyEx(outputImgMat,outputImgMat,Imgproc.MORPH_CLOSE,kernel)
//            Imgproc.medianBlur(outputImgMat,outputImgMat,25)
        }
    }

    fun resize2Width(w:Double,img:Mat):Mat{
        val scalePercent = w/img.width()
        val returnImg : Mat = Mat()
        Imgproc.resize(img,returnImg,Size(), scalePercent, scalePercent,Imgproc.INTER_AREA)
        return returnImg
    }

    fun simpleEllipse2Circle(img:Mat,a:Double,b:Double,angle:Double){
        val scale = a/b
        val m = Imgproc.getRotationMatrix2D(Point(img.width()/2.0,img.height()/2.0),angle,1.0)
        var scaleMatrix = Mat(2, 2, m.type())
        scaleMatrix.put(0,0,1.0)
        scaleMatrix.put(1,1,scale)

    }

    fun returnImg():Bitmap{
        if (OpenCVLoader.initDebug()) {
            Utils.matToBitmap(outputImgMat, outputImg)
        }
        return outputImg
    }

}