package com.nagel.wordnotification.data.settings.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModeDao {
    @Query("SELECT * FROM modes WHERE id_dictionary = :dictionaryId")
    fun getModeByIdDictionary(dictionaryId: Long): Flow<ModeDbEntity?>
//
    @Update(entity = ModeDbEntity::class)
    suspend fun update(mode: ModeDbEntity)

    @Insert(entity = ModeDbEntity::class)
    suspend fun saveMode(accountDbEntity: ModeDbEntity): Long


}