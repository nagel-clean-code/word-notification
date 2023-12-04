package com.nagel.wordnotification.data.dictionaries.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.nagel.wordnotification.data.dictionaries.entities.NotificationHistoryItem
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity


@Entity(
    tableName = "notification_history_items",
    foreignKeys = [
        ForeignKey(
            entity = WordDbEntity::class,
            parentColumns = ["id_word"],
            childColumns = ["id_word"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ModeDbEntity::class,
            parentColumns = ["id"],
            childColumns = ["id_mode"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
    ]
)
class NotificationHistoryDbEntity(
    @ColumnInfo(name = "id_notification") @PrimaryKey(autoGenerate = true) val idNotification: Long,
    @ColumnInfo(name = "id_word") val idWord: Long,
    @ColumnInfo(name = "date_mention") val dateMention: Long,
    @ColumnInfo(name = "id_mode") val idMode: Long,
    @ColumnInfo(name = "learn_step") val learnStep: Int,
) {


    fun toNotificationHistoryItem() =
        NotificationHistoryItem(idNotification, idWord, dateMention, idMode, learnStep)

    companion object {
        fun createNotificationHistoryDbEntity(notification: NotificationHistoryItem): NotificationHistoryDbEntity {
            notification.apply {
                return NotificationHistoryDbEntity(
                    0,
                    idWord,
                    dateMention,
                    idMode,
                    learnStep
                )
            }
        }
    }
}