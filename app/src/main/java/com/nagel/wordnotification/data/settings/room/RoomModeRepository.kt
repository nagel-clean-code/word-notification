package com.nagel.wordnotification.data.settings.room

import android.util.Log
import com.nagel.wordnotification.data.settings.SettingsRepository
import com.nagel.wordnotification.data.settings.entities.ModeSettingsDto
import com.nagel.wordnotification.data.settings.room.entities.ModeDbEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomModeRepository @Inject constructor(
    private val modeDao: ModeDao,
) : SettingsRepository {

    override suspend fun saveModeSettings(data: ModeSettingsDto) {
        Log.d("saveModeSettings", "idDictionary: ${data.idDictionary}")
        modeDao.getModeByIdDictionary(data.idDictionary).collect() {
            val dto = ModeDbEntity.createMode(data)
            if (it == null) {
                modeDao.saveMode(dto)
            } else {
                dto.idMode = it.idMode
                modeDao.update(dto)
            }
        }
    }

    override suspend fun getModeSettings(idDictionary: Long): Flow<ModeDbEntity?> {
        return modeDao.getModeByIdDictionary(idDictionary)
    }

}