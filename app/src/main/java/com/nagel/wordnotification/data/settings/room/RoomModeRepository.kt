package com.nagel.wordnotification.data.settings.room

import android.util.Log
import com.nagel.wordnotification.data.dictionaries.room.DictionaryDao
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomModeRepository @Inject constructor(
    private val modeDao: ModeDao,
    private val dictionary: DictionaryDao
) : SettingsRepository {

    private val mutex = Mutex()
    override suspend fun saveModeSettings(data: ModeSettingsDto) {
        Log.d("saveModeSettings", "idDictionary: ${data.idDictionary}")
        val mode = modeDao.getModeByIdDictionary(data.idDictionary)
        val dto = ModeDbEntity.createMode(data)
        mutex.withLock {
            if (mode == null) {
                val idMode = modeDao.saveMode(dto)
                dictionary.setIdModeInDictionary(idMode, data.idDictionary)
            } else {
                dto.idMode = mode.idMode
                modeDao.update(dto)
            }
        }
    }

    override suspend fun getModeSettings(idDictionary: Long): ModeDbEntity? {
        mutex.withLock {
            val r = modeDao.getModeByIdDictionary(idDictionary)
            return r
        }
    }

}