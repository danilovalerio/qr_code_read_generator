package projetos.danilo.qrcodescanner

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.android.synthetic.main.activity_generator_qr_code.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val activity: Activity = this

        btnScan.setOnClickListener { v ->
                val integrator: IntentIntegrator = IntentIntegrator(activity)
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                integrator.setCameraId(0)
                integrator.initiateScan()
        }
        val intent = Intent(this, GenerateActivity::class.java)

        startActivity(intent)

//        btnGenerate.setOnClickListener {
//            startActivity(intent)
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(result != null){
            if(result.contents != null){
                alert(result.contents)
            } else {
                alert("Scan cancelado")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun alert(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    }

}
