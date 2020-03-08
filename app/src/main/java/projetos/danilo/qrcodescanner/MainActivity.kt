package projetos.danilo.qrcodescanner

import android.Manifest
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
import projetos.danilo.qrcodescanner.util.enableFlash
import projetos.danilo.qrcodescanner.util.unrecognizdCode

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    val REQUEST_CODE_CAMERA = 182 //inteiro aleatorio
    val REQUEST_CODE_FULLSCREEN = 184 /* Inteiro aleatório */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askCameraPermission()
        lastResultVerification()

        ib_clear.setOnClickListener {
            clearContent()
        }
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
            unrecognizdCode(this, { clearContent() })
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
        callbackClick: () -> Unit = {}
    ) {

        btn_open.text = label
        btn_open.visibility = if (status) View.VISIBLE else View.GONE
        btn_open.setOnClickListener { callbackClick() }
    }

    private fun lastResultVerification() {
        val result = Database.getSavedResult(this)
        if (result != null) {
            processBarcodeResult(result.text, result.barcodeFormat.name)
        }
    }

    fun openFullscreen(view: View){
        /*
         * Padrão Cláusula de Guarda - Sem permissão
         * de câmera: não abre atividade.
         * */
//        if( !EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA) ){
//            return
//        }

        unlockCamera()

        /*
         * A linha de código abaixo é necessária para
         * que não haja o risco de uma exception caso
         * o usuário abra o aplicativo e já ative a
         * tela de fullscreen.
         * */
        val value = if(ib_flash_light.tag == null) false else (ib_flash_light.tag as Boolean)

        val i = Intent(this, FullscreenActivity::class.java)
        i.putExtra(Database.KEY_IS_LIGHTENED, value)
        startActivityForResult(i, REQUEST_CODE_FULLSCREEN)
    }

    /*
     * Como o lock não é mantido, o método abaixo é
     * necessário para que o usuário veja o unlock
     * ocorrendo, caso esteja a câmera como lock.
     * */
    private fun unlockCamera(){
        ib_lock.tag = true
        lockUnlock()
    }

    /*
     * Ativa e desativa a luz de flash do celular caso esteja
     * disponível no device.
     * */
    fun flashLight(view: View? = null){
        /*
         * Utilizando a propriedade tag de Button para salvar o
         * valor atual do status de luz de flash.
         * */
        val value = if(ib_flash_light.tag == null)
            true
        else
            !(ib_flash_light.tag as Boolean)
        ib_flash_light.tag = value /* Sempre o inverso do valor de entrada. */

        if(value){
            z_xing_scanner.enableFlash(this, true)
            //todo: mudar a cor
            //ib_flash_light.setImageResource(R.drawable.ic_flashlight_white_24dp)
        }
        else{
            z_xing_scanner.enableFlash(this, false)
            //todo: mudar a cor
            //ib_flash_light.setImageResource(R.drawable.ic_flashlight_off_white_24dp)
        }
    }

    /*
     * Função responsável por mudar o status de funcionamento
     * do algoritmo de interpretação de código de barra
     * lido, incluindo a mudança do ícone de apresentação,
     * ao usuário, de status do algoritmo de interpretação de
     * código. Note que a luz e botão de flash não deve funcionar
     * se a CameraPreview estiver parada, stopped.
     * */
    fun lockUnlock(view: View? = null){
        /*
         * Utilizando a propriedade tag de Button para salvar o
         * valor atual do lock de leitura de código, assim não
         * temos a necessidade de trabalho com uma nova variável
         * de instância somente para manter este valor.
         * */
        val value = if(ib_lock.tag == null)
            true
        else
            !(ib_lock.tag as Boolean)
        ib_lock.tag = value /* Sempre o inverso do valor de entrada. */

        if( value ){
            /*
             * Para funcionar deve ser invocado antes do
             * stopCameraPreview().
             * */
            turnOffFlashlight()

            /*
             * Parar com a verificação de códigos de barra com
             * a câmera.
             * */
            z_xing_scanner.stopCameraPreview()
            //todo: mudar a cor
           // ib_lock.setImageResource(R.drawable.ic_lock_white_24dp)
            ib_flash_light.isEnabled = false
        }
        else{
            /*
             * Retomar a verificação de códigos de barra com
             * a câmera.
             * */
            z_xing_scanner.resumeCameraPreview(this)
            //todo: mudar a cor
            //ib_lock.setImageResource(R.drawable.ic_lock_open_white_24dp)
            ib_flash_light.isEnabled = true
        }
    }

    /*
     * Método necessário, pois não faz sentido deixar a luz
     * de flash ligada quando a tela não mais está lendo
     * códigos, está travada. Método somente invocado quando
     * o lock de tela ocorre.
     * */
    private fun turnOffFlashlight(){
        ib_flash_light.tag = true
        flashLight()
    }

    fun clearContent(view: View? = null) {
        resultadoLeitura.text = getString(R.string.nothing_read)
        processBarcodeType(false)
        setButtonOpenAction(status = false)
        Database.saveResult(this)
    }

}

