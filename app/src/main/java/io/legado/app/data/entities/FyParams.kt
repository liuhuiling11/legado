package io.legado.app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fyParams")
data class FyParams(

    @PrimaryKey
    @ColumnInfo(defaultValue = "")
    var name:String="",

    var value:String ="",

    var info: String ="",

    var type:String =""
){
    override fun equals(other: Any?): Boolean {
        if (other is FyParams) {
            return other.name == name
        }
        return false
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
