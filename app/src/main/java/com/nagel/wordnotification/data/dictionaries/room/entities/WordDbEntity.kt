package com.nagel.wordnotification.data.dictionaries.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.nagel.wordnotification.data.dictionaries.entities.Word
import com.nagel.wordnotification.utils.GlobalFunction
import java.util.UUID




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
    @ColumnInfo(name = "id_dictionary") val idDictionary: Long,
    @ColumnInfo(name = "text_first") val textFirst: String,
    @ColumnInfo(name = "text_last") val textLast: String,
    @ColumnInfo(name = "learn_step") val learnStep: Int,
    @ColumnInfo(name = "last_date_mention") val lastDateMention: Long,
    @ColumnInfo(name = "unique_id") val uniqueId: Int,
    val learned: Boolean,
) {


    fun toWord(): Word {
        val word = Word(
            idDictionary = idDictionary,
            textFirst = textFirst,
            textLast = textLast,
            learned = learned,
            learnStep = learnStep,
            lastDateMention = lastDateMention,
            uniqueId = uniqueId
        )
        word.idWord = idWord
        return word
    }

    companion object {

        fun createWordDbEntity(word: Word) =
            WordDbEntity(
                idWord = word.idWord,
                idDictionary = word.idDictionary,
                learned = word.learned,
                textFirst = word.textFirst,
                textLast = word.textLast,
                learnStep = word.learnStep,
                lastDateMention = word.lastDateMention,
                uniqueId = word.uniqueId
            )
    }
}