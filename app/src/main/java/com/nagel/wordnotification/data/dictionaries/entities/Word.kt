package com.nagel.wordnotification.data.dictionaries.entities

import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity
import com.nagel.wordnotification.presentation.addingwords.worddetails.widget.model.ShowStepsWordDto
import com.nagel.wordnotification.utils.GlobalFunction

data class Word(
    val idDictionary: Long,
    var textFirst: String,
    var textLast: String,
    var allNotificationsCreated: Boolean = false,
    var learnStep: Int = 0,
    var lastDateMention: Long = THERE_IS_NO_DATE_MENTION,
    var uniqueId: Int = GlobalFunction.generateUniqueId()
) {
    var idWord: Long = 0

    //использую только как буфер
    var nextDate: Long? = null
    var mode: ModeDbEntity? = null

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

    fun fullCopyWord(): Word {
        val newWord = this.copy()
        newWord.idWord = this.idWord
        return newWord
    }

    companion object {
        const val THERE_IS_NO_DATE_MENTION = -1L
    }
}