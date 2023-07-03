package com.nagel.wordnotification.data.dictionaries.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary
import com.nagel.wordnotification.data.dictionaries.entities.Word

@Entity(
    tableName = "words",
    foreignKeys = [
        ForeignKey(
            entity = DictionaryDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_dictionary"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
class WordDbEntity(
    @ColumnInfo(name = "id_word") @PrimaryKey(autoGenerate = true) val idWord: Long,
    @ColumnInfo(name = "id_dictionary") val idDictionaries: Long,
    @ColumnInfo(name = "text_first") val textFirst: String,
    @ColumnInfo(name = "text_last") val textLast: String,
    val learned: Boolean,
) {


    fun toWord(): Word {
        return Word(
            idWord = idWord,
            textFirst = textFirst,
            textLast = textLast,
            learned = learned
        )
    }

    companion object {

        fun createWordDbEntity(textFirst: String, textLast: String, idDictionaries: Long) =
            WordDbEntity(
                idWord = 0,
                idDictionaries = idDictionaries,
                learned = false,
                textFirst = textFirst,
                textLast = textLast
            )
    }
}