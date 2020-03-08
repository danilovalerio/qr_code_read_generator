package projetos.danilo.qrcodescanner

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.activity_main.*
import me.dm7.barcodescanner.core.CameraUtils
import me.dm7.barcodescanner.zxing.ZXingScannerView
import projetos.danilo.qrcodescanner.util.Database
import projetos.danilo.qrcodescanner.util.unrecognizdCode

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    val REQUEST_CODE_CAMERA = 182 //inteiro aleatorio


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askCameraPermission()
    }

    override fun onResume() {
        super.onResume()
        z_xing_scanner.setResultHandler(this)
        z_xing_scanner.startCamera()
    }

    override fun onPause() {
        super.onPause()
        z_xing_scanner.stopCamera()

        val camera = CameraUtils.getCameraInstance()
        if (camera != null) {
            (camera as android.hardware.Camera).release()
        }
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

        if (intentResult != null) {
            if (intentResult.contents == null) {
                resultadoLeitura.setText("Cancelado!")
            } else {
                resultadoLeitura.setText(intentResult.contents)
            }
        }

    }

    private fun askCameraPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            alert("Permissão não garantida!")
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.CAMERA
                )
            ) {
                alert("Permissão não garantida!")
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                alert("we can request the permission!")
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CAMERA),
                    PackageManager.PERMISSION_GRANTED
                )

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            alert("A permissão já foi concedida")
        }
    }

    private fun alert(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun handleResult(result: Result?) {
//        z_xing_scanner.resumeCameraPreview(this)

        if (result == null) {
            unrecognizdCode(this)
            return
        }

        processBarcodeResult(
            result!!.text,
            result!!.barcodeFormat.name
        )
    }

    private fun processBarcodeResult(
        texto: String,
        barcodeFormatName: String
    ) {

        val result = Result(
            texto,
            texto.toByteArray(),//somente para ter algo
            arrayOf(),//somente para ter algo
            BarcodeFormat.valueOf(barcodeFormatName)
        )

        Database.saveResult(this, result)

        //atualizando interface
        resultadoLeitura.text = result.text
        processBarcodeType(true, result.barcodeFormat.name)
        processButtonOpen(result)

        z_xing_scanner.resumeCameraPreview(this)

    }

    private fun processBarcodeType(
        status: Boolean = false,
        barcode: String = ""
    ) {

        tipo_de_barra.text = getString(R.string.barcode_format) + barcode
        tipo_de_barra.visibility = if (status) View.VISIBLE else View.GONE
    }


    private fun processButtonOpen(result: Result) {
        when {
            URLUtil.isValidUrl(result.text) ->
                setButtonOpenAction(resources.getString(R.string.open_url)) {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(result.text)
                    startActivity(i)
                }
            Patterns.EMAIL_ADDRESS.matcher(result.text).matches() ->
                setButtonOpenAction(getString(R.string.open_email)) {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse("mailto:?body=${result.text}")
                    startActivity(i)
                }
            Patterns.PHONE.matcher(result.text).matches() ->
                setButtonOpenAction(getString(R.string.open_call)) {
                    val i = Intent(Intent.ACTION_DIAL)
                    i.data = Uri.parse("tel:${result.text}")
                    startActivity(i)
                }
            else -> setButtonOpenAction(status = false)
        }
    }

    private fun setButtonOpenAction(
         label: String = "",
     status: Boolean = true,
     callbackClick:()->Unit = {} ){

           btn_open.text = label
        btn_open.visibility = if( status ) View.VISIBLE else View.GONE
        btn_open.setOnClickListener { callbackClick() }
         }
}

