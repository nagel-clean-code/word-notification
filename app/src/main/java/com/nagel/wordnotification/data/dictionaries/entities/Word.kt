package com.nagel.wordnotification.data.dictionaries.entities

import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity
import com.nagel.wordnotification.presentation.addingwords.worddetails.widget.model.ShowStepsWordDto
import com.nagel.wordnotification.utils.GlobalFunction
import java.util.Date

data class Word(
    val idDictionary: Long,
    var textFirst: String,
    var textLast: String,
    var allNotificationsCreated: Boolean = false,
    var learnStep: Int = 0,
    var lastDateMention: Long = Date().time,
    var uniqueId: Int = GlobalFunction.generateUniqueId()
) {
    var idWord: Long = 0

    //использую только как буфер
    var nextDate: Long? = null
    var mode: ModeDbEntity? = null

    var notifications: List<NotificationHistoryItem>? = null

    fun getLastDateMentionOrNull(): Long? {
        return if (lastDateMention == THERE_IS_NO_DATE_MENTION) null else lastDateMention
    }

    fun markWordAsLearned(): Word {
        allNotificationsCreated = true
        lastDateMention = THERE_IS_NO_DATE_MENTION
        return this
    }

    fun isWordLearned() = allNotificationsCreated

    fun toShowStepsWordDto(modeSettingsDto: ModeSettingsDto): ShowStepsWordDto {
        return ShowStepsWordDto(
            modeSettingsDto,
            allNotificationsCreated,
            learnStep,
            lastDateMention
        )
    }

    fun toDbEntity(): WordDbEntity {
        return WordDbEntity(
            idWord,
            idDictionary,
            textFirst,
            textLast,
            learnStep,
            lastDateMention,
            uniqueId,
            allNotificationsCreated
        )
    }

    fun fullCopyWord(
        learnStep: Int? = null,
        lastDateMention: Long? = null,
        allNotificationsCreated: Boolean? = null
    ): Word {
        val newWord = this.copy()
        newWord.idWord = this.idWord
        learnStep?.let {
            newWord.learnStep = learnStep
        }
        lastDateMention?.let {
            newWord.lastDateMention = lastDateMention
        }
        allNotificationsCreated?.let {
            newWord.allNotificationsCreated = allNotificationsCreated
        }
        return newWord
    }

    companion object {
        const val THERE_IS_NO_DATE_MENTION = -1L

        fun createEmptyWord(): Word = Word(-1, "", "")
    }
}