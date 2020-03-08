package projetos.danilo.qrcodescanner

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.activity_generator_qr_code.*
import kotlinx.android.synthetic.main.activity_main.*
import me.dm7.barcodescanner.zxing.ZXingScannerView

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler{


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askCameraPermission()

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            alert("Permissão não garantida!")
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.CAMERA)) {
                alert("Permissão não garantida!")
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                alert("we can request the permission!")
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    PackageManager.PERMISSION_GRANTED)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            alert("A permissão já foi concedida")

            btnScan.setOnClickListener { it ->

                //                scanButton(it)
            }
        }

//        ActivityCompat.requestPermissions(this, arrayOf<String>(
//            android.Manifest.permission.CAMERA), PackageManager.PERMISSION_GRANTED)

//        val activity: Activity = this
//
//        btnScan.setOnClickListener { v ->
//            val integrator: IntentIntegrator = IntentIntegrator(activity)
//            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
//            integrator.setCameraId(0)
//            integrator.initiateScan()
//        }

        //vai para activity que gera o qr code
//        val intent = Intent(this, GenerateActivity::class.java)
//
//        startActivity(intent)

//        btnGenerate.setOnClickListener {
//            startActivity(intent)
//        }
    }

    val activity = this

    fun scanButton(view: View) {
        var intentIntegrator = IntentIntegrator(this@MainActivity)
        intentIntegrator.setCameraId(0)
        intentIntegrator.setOrientationLocked(false)
        intentIntegrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var intentResult: IntentResult = IntentIntegrator.parseActivityResult(
            requestCode, resultCode, data
        )

        if(intentResult != null){
            if(intentResult.contents == null){
                resultadoLeitura.setText("Cancelado!")
            }
            else {
                resultadoLeitura.setText(intentResult.contents)
            }
        }

    }

    private fun alert(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

