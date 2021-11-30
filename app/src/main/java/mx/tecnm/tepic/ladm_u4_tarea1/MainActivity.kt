package mx.tecnm.tepic.ladm_u4_tarea1

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val siPermiso = 98 //Asignación de permisos
    var hilo = Hilo(this) //Declaración de Hilo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Sentencia que nos sirve para verificar si los permisos han sido otorgados
        if((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)) {
                    //Si lo anterior fue cumplido -> Solicita el permiso con la siguiente línea
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CALL_LOG,android.Manifest.permission.CALL_PHONE),siPermiso)
        } else {
            //Si lo anterior fue cumplido exitosamente pasa a las siguientes dos funciones
            recuperar() //Recupera el hilo
            hilo.start() //Inicializa el hilo para que este se este actualizacon la información debida
        }

        //Llamado a llamar() desde el botón
        llamar.setOnClickListener {
            llamar()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        //Compara/Valida si el requestCode == siPermiso para poder otorgar los permisos debidos
        if (requestCode == siPermiso){
            Toast.makeText(this,"PERMISOS DE LLAMAR Y LEER LLAMADAS OTORGADO", Toast.LENGTH_LONG).show()
            recuperar() //Recupera el hilo
            hilo.start() //Inicializa el hilo para que este se este actualizacon la información debida
        }
    }

    //Read de la lista de llamadas
    fun recuperar() {
        var cursor = contentResolver.query(CallLog.Calls.CONTENT_URI,null,null,null,"date DESC")
        var registros = ArrayList<String>()

        if(cursor!!.moveToFirst()){
            var num = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            var tipo = cursor.getColumnIndex(CallLog.Calls.TYPE)

            do{
                var data = "LLAMADA DE:\nTELEFONO: ${cursor.getString(num)} \nTIPO: ${cursor.getString(tipo)}"
                registros.add(data)
            }while(cursor.moveToNext()) //Cursor seguirá desplazandose

            llamadas.adapter = ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, registros)
        }
    }

    //Realizar Llamadas
    private fun llamar(){
        val num = telefono.text.toString()
        val intent = Intent(Intent.ACTION_CALL)

        intent.data = Uri.parse("tel:$num")
        startActivity(intent)
    }

}

//Creación de hilo para poder refrescar la lista que contienen las llamadas.

class Hilo (p: MainActivity) : Thread() {
    private var puntero = p //Creación de varibale con asignación del main activity

    override fun run() {
        super.run()

        while (true) { //Recupera la lista de llamadas para su actualización
            sleep(1000)
            puntero.runOnUiThread { //Permite manipular la lista que tenemos en MainActivity
                puntero.recuperar()
            }
        }
    }
}