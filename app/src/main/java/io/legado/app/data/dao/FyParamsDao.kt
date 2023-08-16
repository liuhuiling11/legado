package io.legado.app.data.dao

import androidx.room.*
import io.legado.app.data.entities.FyParams

@Dao
interface FyParamsDao {


    @Query("select * from fyParams where name = :name")
    fun get(name: String): FyParams?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg rule: FyParams)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg rule: FyParams)

    @Delete
    fun delete(vararg rule: FyParams)

    @Query("update fyParams set value = :value where name = :name")
    fun updateValue(name: String, value: String)
}