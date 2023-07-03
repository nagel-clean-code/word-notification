package com.nagel.wordnotification.data.dictionaries.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.data.dictionaries.room.entities.DictionaryDbEntity
import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DictionaryDao {
//    @Query("SELECT id, password FROM accounts WHERE email = :email")
//    suspend fun findByEmail(email: String): AccountSignInTuple?
//
//    @Update(entity = DictionaryDbEntity::class)
//    suspend fun updateUsername(account: AccountUpdateUsernameTuple)

    @Insert(entity = DictionaryDbEntity::class)
    suspend fun saveDictionary(accountDbEntity: DictionaryDbEntity): Long

    @Query("SELECT * FROM dictionaries WHERE id = :accountId AND name = :name")
    suspend fun getDictionary(name: String, accountId: Long): DictionaryDbEntity?

    @Insert(entity = WordDbEntity::class)
    suspend fun addWord(wordDbEntity: WordDbEntity): Long

    @Query("SELECT * FROM words WHERE id_dictionary = :idDictionary")
    suspend fun getWords(idDictionary: Long): List<WordDbEntity>

//    @Query("SELECT * FROM accounts WHERE id = :accountId")
//    fun getById(accountId: Long): Flow<AccountDbEntity?>
}