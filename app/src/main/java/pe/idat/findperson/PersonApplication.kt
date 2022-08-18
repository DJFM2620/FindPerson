package pe.idat.findperson

import android.app.Application
import androidx.room.Room

//Con esta clase llamaremos a las operaciones de la BD
class PersonApplication: Application() {

    /*
        Con esto se puede llamar a cualquier funcion que este dentro de este bloque de codigo sin necesidad
        de instanciar un objeto desde cualquier punto
        Patron(Singleton)
    */
    companion object {
        lateinit var  database: PersonDatabase
    }

    override fun onCreate() {
        super.onCreate()

        //Cargamos la var database
        database = Room.databaseBuilder(this, PersonDatabase::class.java, "PersonDatabase").build()
    }
}