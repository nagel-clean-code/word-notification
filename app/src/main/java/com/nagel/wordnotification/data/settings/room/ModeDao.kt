package com.nagel.wordnotification.data.settings.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity

@Dao
interface ModeDao {
    @Query("SELECT * FROM modes WHERE id = :idMode")
    suspend fun getModeById(idMode: Long): ModeDbEntity?

    @Query("SELECT * FROM modes")
    suspend fun getModes(): List<ModeDbEntity>?

    @Update(entity = ModeDbEntity::class)
    suspend fun update(mode: ModeDbEntity)

    @Insert(entity = ModeDbEntity::class)
    suspend fun saveMode(accountDbEntity: ModeDbEntity): Long

    @Query("SELECT * FROM modes WHERE id_dictionary = :idDictionary and selected_mode = :selectedMode")
    fun getMode(idDictionary: Long, selectedMode: String): ModeDbEntity?


}