package pe.idat.findperson

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Persons")
data class PersonEntity(@PrimaryKey(autoGenerate = true) var personId:Long = 0,
                        var name:String,
                        var age:String = "",
                        var height:String = "",
                        var weight:String = "",
                        var photo:String = "",
                        var direction: String = ""){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersonEntity

        if (personId != other.personId) return false

        return true
    }

    override fun hashCode(): Int {
        return personId.hashCode()
    }
}
