package com.nagel.wordnotification.data.dictionaries.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nagel.wordnotification.data.accounts.room.entities.AccountDbEntity
import com.nagel.wordnotification.data.dictionaries.entities.Dictionary

/**
 * CREATE TABLE "dictionaries" (
"id_dictionaries"	INTEGER NOT NULL,
"id_author"	INTEGER NOT NULL UNIQUE,
"name"	TEXT NOT NULL,
"date_created"	INTEGER NOT NULL,
"id_folder"	INTEGER NOT NULL,
"mode"	INTEGER NOT NULL,
PRIMARY KEY("id_dictionaries" AUTOINCREMENT),
FOREIGN KEY("id_author") REFERENCES "accounts"("id_author"),
FOREIGN KEY("id_folder") REFERENCES "folders"("id_folder")
);
 */
@Entity(
    tableName = "dictionaries",
    indices = [
        Index("name", unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = AccountDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_author"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
class DictionaryDbEntity(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val idDictionaries: Long,
    @ColumnInfo(name = "id_author") val idAuthor: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "date_created") val dateCreated: Long,
    @ColumnInfo(name = "id_folder") val idFolder: Long,
    @ColumnInfo(name = "id_mode") val mode: Long,
    @ColumnInfo(name = "included") val included: Boolean,
) {

    fun toDictionary(): Dictionary {
        return Dictionary(
            idDictionaries = idDictionaries,
            idAuthor = idAuthor,
            name = name,
            dateCreated = dateCreated,
            idFolder = idFolder,
            mode = mode,
            include = included
        )
    }

    companion object {

        fun createDictionary(nameDictionary: String, idFolder: Long, idAuthor: Long, included: Boolean) =
            DictionaryDbEntity(
                idDictionaries = 0,
                idAuthor = idAuthor,
                dateCreated = System.currentTimeMillis(),
                name = nameDictionary,
                idFolder = idFolder,
                mode = 0L,
                included = included
            )
    }
}