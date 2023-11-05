package com.nagel.wordnotification.data.dictionaries.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nagel.wordnotification.data.dictionaries.room.entities.DictionaryDbEntity
import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM dictionaries WHERE id_author = :accountId")   //TODO JOIN
    fun getMyDictionaries(accountId: Long): Flow<List<DictionaryDbEntity>?>
//
//    @Update(entity = DictionaryDbEntity::class)
//    suspend fun updateUsername(account: AccountUpdateUsernameTuple)

    @Insert(entity = DictionaryDbEntity::class)
    suspend fun saveDictionary(accountDbEntity: DictionaryDbEntity): Long

    @Query("SELECT * FROM dictionaries WHERE id_author = :accountId AND name = :name")
    suspend fun getDictionaryByName(name: String, accountId: Long): DictionaryDbEntity?

    @Query("SELECT * FROM dictionaries WHERE id = :idDictionary")
    suspend fun getDictionaryById(idDictionary: Long): DictionaryDbEntity?

    @Query("UPDATE dictionaries SET id_mode=:idMode WHERE id = :idDictionary")
    suspend fun setIdModeInDictionary(idMode: Long, idDictionary: Long)

    @Insert(entity = WordDbEntity::class)
    suspend fun addWord(wordDbEntity: WordDbEntity): Long

    @Update(entity = WordDbEntity::class)
    suspend fun updateWord(wordDbEntity: WordDbEntity)
    @Query("UPDATE dictionaries SET included = :include WHERE id=:idDictionary")
    suspend fun setInclude(idDictionary: Long, include: Boolean)

    @Query("DELETE FROM words WHERE id_word =:idWord")
    suspend fun deleteWord(idWord: Long): Int

    @Query("DELETE FROM dictionaries WHERE id =:idDictionary")
    suspend fun deleteDictionary(idDictionary: Long): Int
//    fun deleteWord(idWord: Long): Flow<Int>

    @Query("SELECT * FROM words WHERE id_dictionary = :idDictionary")
    suspend fun getWords(idDictionary: Long): List<WordDbEntity>

    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<WordDbEntity>

//    @Query("SELECT * FROM accounts WHERE id = :accountId")
//    fun getById(accountId: Long): Flow<AccountDbEntity?>
}