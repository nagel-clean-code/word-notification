package com.nagel.wordnotification.data.dictionaries.entities

import com.nagel.wordnotification.data.dictionaries.room.entities.WordDbEntity
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.presentation.addingwords.worddetails.widget.model.ShowStepsWordDto
import com.nagel.wordnotification.utils.GlobalFunction

data class Word(
    val idDictionary: Long,
    var textFirst: String,
    var textLast: String,
    var allNotificationsCreated: Boolean = false,
    var learnStep: Int = 0,
    var currentDateMention: Long = THERE_IS_NO_DATE_MENTION,
    var uniqueId: Int = GlobalFunction.generateUniqueId()
) {
    var idWord: Long = 0

    fun markWordAsLearned(): Word {
        allNotificationsCreated = true
        currentDateMention = THERE_IS_NO_DATE_MENTION
        return this
    }

    fun isWordLearned() = allNotificationsCreated

    fun toShowStepsWordDto(modeSettingsDto: ModeSettingsDto): ShowStepsWordDto {
        return ShowStepsWordDto(
            modeSettingsDto,
            allNotificationsCreated,
            learnStep,
            currentDateMention
        )
    }

    fun toDbEntity(): WordDbEntity {
        return WordDbEntity(
            idWord,
            idDictionary,
            textFirst,
            textLast,
            learnStep,
            currentDateMention,
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