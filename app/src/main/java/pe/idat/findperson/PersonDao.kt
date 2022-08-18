package pe.idat.findperson

import androidx.room.*

@Dao
interface PersonDao {

    @Insert
    fun insertDB(person:PersonEntity)

    @Update
    fun updateDB(person: PersonEntity)

    @Delete
    fun deleteDB(person: PersonEntity)

    @Query("SELECT * FROM Persons WHERE personId IN (:ID)")
    fun findByIdDB(ID: Long): PersonEntity

    @Query("SELECT * FROM persons")
    fun findAll(): MutableList<PersonEntity>

    @Query("SELECT max(personId) FROM Persons")
    fun maxId(): Long
}