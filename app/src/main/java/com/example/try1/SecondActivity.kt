package com.example.try1

import android.Manifest.*
import android.Manifest.permission.*
import android.content.Intent
import android.content.pm.PackageManager.*
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log.*
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.widget.TextView
import android.widget.Toast.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.*
import java.util.*
import java.util.Arrays.sort

@Suppress("DEPRECATION")
class SecondActivity : AppCompatActivity() {
    var act2nd: SecondActivity? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    protected var camera: CameraDevice? = null; private var camID: String? = null
    protected var capReqBuild: CaptureRequest.Builder? = null; protected var captureSession: CameraCaptureSession? = null
    var rtnInt: Intent? = null
    private var presentMovAv = 0; private var oldMovAv = 0; private var olderMovAv = 0
    var timArr = Vector<Long>()
    private var cap = 0
    private var imgDim: Size? = null
    var startTime : Long = 0; var timeDuration = 45
    private var txV: TextureView? = null; var txtV: TextView? = null
    var flag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main2)
        txV = findViewById(R.id.Cam_Texture)
        if (BuildConfig.DEBUG && txV == null) {
            error("Assertion failed")
        }
        txtV = findViewById<View>(R.id.Instrct_Txt) as TextView
        act2nd = this
        rtnInt = Intent()
    }

    var surfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, w: Int, h: Int) {
            startCam()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, w: Int, h: Int) {}
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            if (!flag) {
                flag = true
                i(myTag2, "Starting Camera")
                startTime = System.currentTimeMillis()
            }
            var timeDiff = ((System.currentTimeMillis()-startTime)/ 1000).toInt()
            i(myTag2, "Time elapsed: $timeDiff seconds")
            val bMap = txV!!.bitmap
            val h = bMap!!.height
            val w = bMap.width

            val pX = IntArray(h * w)
            bMap.getPixels(pX, 0, w, w / 2, h / 2, w / 20, h / 20)
            var pixSum = 0
            val th1 = 20
            val th2 = 49
            for (it in 0 until h * w) {
                pixSum += (pX[it] shr 16 and 0xFF) // red pixels
            }
            if (cap == th1) {
                presentMovAv = pixSum
            } else if (cap in (th1 + 1) until th2) {
                presentMovAv = (presentMovAv * (cap - th1) + pixSum) / (cap - th1 + 1)
            } else if (cap >= th2) {
                presentMovAv = (presentMovAv * (th2 - th1) + pixSum) / (th2 - th1 + 1)
                if (oldMovAv > presentMovAv && oldMovAv > olderMovAv) {
                    timArr.add(System.currentTimeMillis())
                    if (timeDiff >= timeDuration) {
                        getHeartRt()
                    }
                }
            }
            cap++
            olderMovAv = oldMovAv
            oldMovAv = presentMovAv
        }
    }
    private val cameraStateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            this@SecondActivity.camera = camera
            previewCam()
        }

        override fun onDisconnected(camera: CameraDevice) {
            this@SecondActivity.camera!!.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            if (this@SecondActivity.camera != null) this@SecondActivity.camera!!.close()
            this@SecondActivity.camera = null
        }
    }

    protected fun startBackThread() {
        backgroundThread = HandlerThread("Background thread for camera")
        backgroundThread!!.start()
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    protected fun stopBackThread() {
        backgroundThread!!.quitSafely()
        try {
            backgroundThread!!.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun startCam() {
        val manager = this.getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            camID = manager.cameraIdList[0]
            val char = camID?.let { manager.getCameraCharacteristics(it) }
            val sscMap = char?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            imgDim = sscMap.getOutputSizes(SurfaceTexture::class.java)[0]
            if (checkSelfPermission(this, CAMERA) != PERMISSION_GRANTED && checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
                requestPermissions(this@SecondActivity, arrayOf(CAMERA), RequestCamPermission)
                return
            }
            manager.openCamera(camID!!, cameraStateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    protected fun previewUpdate() {
        if (null == camera) {
            e(myTag2, "Error! Preview can't be updated")
        }
        capReqBuild!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        capReqBuild!!.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH)
        try {
            captureSession!!.setRepeatingRequest(capReqBuild!!.build(), null, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun closeCam() {
        if (null != camera) {
            camera!!.close()
            camera = null
        }
    }

    private fun getHeartRt() {
        var m = 0
        val diffTime = LongArray(timArr.size - 1)
        for (it in 0 until timArr.size - 1) {
            diffTime[it] = timArr.get(it + 1) - timArr[it]
        }
        sort(diffTime)
        m = diffTime[diffTime.size / 2].toInt()
        heartbeatsPerMin = (60*1000)/m
        syncDB()
        onBackPressed()
    }

    private fun syncDB() {
        val txV = findViewById<View>(R.id.Instrct_Txt) as TextView
        ("Heart Rate = " + Companion.heartbeatsPerMin + " BPM").also { txV.text = it }
        rtnInt!!.putExtra("HEART_RATE_VALUE", Companion.heartbeatsPerMin.toString() + "")
        act2nd!!.setResult(requestCode1, rtnInt)
        makeText(this@SecondActivity, "Heartbeat recording completed, your heart rate is $heartbeatsPerMin BPM", LENGTH_LONG).show()
    }

    protected fun previewCam() {
        try {
            val surfaceTexture = txV!!.surfaceTexture!!
            surfaceTexture.setDefaultBufferSize(imgDim!!.width, imgDim!!.height)
            val surface = Surface(surfaceTexture)
            capReqBuild = camera!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            capReqBuild!!.addTarget(surface)
            camera!!.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    when (camera) {
                        null -> {
                            return
                        }
                        else -> {
                            captureSession = cameraCaptureSession
                            previewUpdate()
                        }
                    }
                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    makeText(this@SecondActivity, "Config changed", LENGTH_LONG).show()
                }
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == Companion.RequestCamPermission) {
            if (grantResults[0] == PERMISSION_DENIED) {
                makeText(this@SecondActivity, "Permission Error! Closing..", LENGTH_LONG).show()
                finish()
            }
        }
    }

    public override fun onStart() {
        super.onStart()
    }

    override fun onPause() {
        closeCam()
        stopBackThread()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        startBackThread()
        if (txV!!.isAvailable) {
            startCam()
        } else {
            txV!!.surfaceTextureListener = surfaceTextureListener
        }
    }

    public override fun onStop() {
        super.onStop()
    }

    companion object {
        // static variables and values
        private const val myTag2 = "Button2"
        private const val requestCode1 = 7777
        private const val RequestCamPermission = 1
        var heartbeatsPerMin = 0
    }
}