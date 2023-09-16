package fr.farfagames.warcode

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

import android.content.Intent
import android.app.Activity
import android.Manifest
import android.widget.Toast
import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import com.google.android.gms.vision.Detector

class ScanCodeActivity : AppCompatActivity() {

    companion object {
        const val CODE_KEY = "code key"
        private const val CAMERA_REQUEST_CODE = 23
    }

    private lateinit var scanSurfaceView: SurfaceView
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_code2)

        scanSurfaceView = findViewById(R.id.scan_surface_view)

        initBarcodeDetector()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (cameraPermissionGranted(requestCode,grantResults)){
            finish()
            overridePendingTransition(0,0)
            startActivity(intent)
            overridePendingTransition(0,0)
        }else{
            Toast.makeText(this,"Vous devez activer votre caméra pour scanner les code barres.",
                Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun cameraPermissionGranted(requestCode: Int, grantResults: IntArray): Boolean {
        return requestCode == CAMERA_REQUEST_CODE
                && grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun initBarcodeDetector() {
        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        initCameraSource()
        initScanSurfaceView()

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode>{
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems

                if(barcodes.isNotEmpty()){
                    barcodes.forEach{_,barcode->
                        if(barcode.displayValue.isNotEmpty()){
                            onCodeScanned(barcode.displayValue)
                        }
                    }
                }
            }

        })
    }

private fun onCodeScanned(value: String) {

    val intent = Intent()
    intent.putExtra(CODE_KEY, value)
    setResult(Activity.RESULT_OK, intent)
    MainActivity.SharedData.ValCodeScan = value

    finish() // Assurez-vous que l'activité se termine ici
}

    private fun initScanSurfaceView() {
        scanSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (ActivityCompat.checkSelfPermission(
                        this@ScanCodeActivity,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    cameraSource.start(holder)
                } else {
                    ActivityCompat.requestPermissions(
                        this@ScanCodeActivity,
                        arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE
                    )
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                // Vous pouvez ajouter un code pour gérer les changements de surface si nécessaire
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                // Vous pouvez ajouter un code pour gérer la destruction de la surface si nécessaire
                cameraSource.release()
            }
        })
    }

    private fun initCameraSource() {
        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true)
            .build()
    }
}