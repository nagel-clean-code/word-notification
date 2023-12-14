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
    override suspend fun saveModeSettings(data: ModeSettingsDto): Long {
        Log.d("saveModeSettings", "idDictionary: ${data.idDictionary}")
        val dto = ModeDbEntity.createMode(data)
        val mode = modeDao.getMode(data.idDictionary, dto.selectedMode)
        mutex.withLock {
            val idMode = if (mode == null) {
                modeDao.saveMode(dto)
            } else {
                dto.idMode = mode.idMode
                modeDao.update(dto)
                dto.idMode
            }
            dictionary.setIdModeInDictionary(idMode, data.idDictionary)
            return idMode
        }
    }

    override suspend fun getModeSettingsById(idMode: Long): ModeDbEntity? {
        mutex.withLock {
            return modeDao.getModeById(idMode)
        }
    }

    override suspend fun getModes(): List<ModeSettingsDto>? {
        mutex.withLock {
            return modeDao.getModes()?.map { it.toMode() }
        }
    }

}