package pe.idat.findperson

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(PersonEntity::class), version = 1)
abstract class PersonDatabase: RoomDatabase() {

    abstract fun PersonDao(): PersonDao
}