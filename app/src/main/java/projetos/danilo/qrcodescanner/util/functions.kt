package projetos.danilo.qrcodescanner.util

import android.content.Context
import android.widget.Toast
import projetos.danilo.qrcodescanner.R

fun unrecognizdCode( context: Context, callbackClear: ()-> Unit = {}){
    Toast.makeText(context,
        context.getString(R.string.unrecognized_code),
        Toast.LENGTH_SHORT).show()

    callbackClear()
}