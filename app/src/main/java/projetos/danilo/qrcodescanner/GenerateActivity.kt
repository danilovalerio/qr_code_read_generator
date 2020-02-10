package projetos.danilo.qrcodescanner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_generator_qr_code.*

class GenerateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generator_qr_code)

        btnGenerate.setOnClickListener {
            gerarQRCode()
        }
    }

    private fun gerarQRCode(){
        val txt: String = editText.text.toString()
        val multiFormatWriter = MultiFormatWriter()
        val barcodeEncoder = BarcodeEncoder()

        try {
            val bitMatrix: BitMatrix =
                multiFormatWriter.encode(txt, BarcodeFormat.QR_CODE,400, 400)
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            ivQRCode.setImageBitmap(bitmap)
        } catch (e: WriterException){
            e.printStackTrace()
        }


    }

    private fun inicializarComponentes(){

    }
}
