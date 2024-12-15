package com.nagel.wordnotification.data.googledisk.files.google

import android.content.Context
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.nagel.wordnotification.Constants.DICTIONARY_NAME_WITH_FORMAT
import com.nagel.wordnotification.data.googledisk.accounts.google.getGoogleLastSignedInAccount
import com.nagel.wordnotification.data.googledisk.exeptions.SourceExceptionMapper
import com.nagel.wordnotification.data.googledisk.files.FilesSource
import com.nagel.wordnotification.data.googledisk.files.google.enity.ReadFileResult
import com.nagel.wordnotification.domain.googledisk.files.RemoteFile
import com.nagel.wordnotification.domain.googledisk.utils.ReadFileException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

/** https://developers.google.com/drive/api/guides/search-files?hl=ru - документация */
@Singleton
class GoogleFilesSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sourceExceptionMapper: SourceExceptionMapper,
) : FilesSource {

    private var driveService: Drive? = null

    /**
     * Обязательно должен быть вызыван перед загрузкой
     */
    private fun initAccount() {
        if (driveService != null) return
        val googleAccount = getGoogleLastSignedInAccount(context)
        val credential = GoogleAccountCredential.usingOAuth2(context, setOf(DriveScopes.DRIVE_FILE))
        credential.selectedAccount = googleAccount.account
        driveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("Drive API Migration")
            .build()
    }


    override suspend fun getFiles(): List<RemoteFile> = sourceExceptionMapper.wrap {
        initAccount()
        val result = driveService?.files()?.list()?.execute()
        val filesResult =
            result?.files?.map { RemoteFile(it.id, it.name, it.getSize()) } ?: emptyList()
        return@wrap filesResult
    }

    override suspend fun delete(file: RemoteFile) = sourceExceptionMapper.wrap {
        initAccount()
        driveService?.files()?.delete(file.id)
        return@wrap
    }

    private fun getLastFile(folderId: String): com.google.api.services.drive.model.File? {
        val result = driveService?.files()?.list()
            ?.setQ("name='$DICTIONARY_NAME_WITH_FORMAT' and '$folderId' in parents")
            ?.execute()
        val filesResult = result?.files ?: emptyList()
        return filesResult.getOrNull(0)
    }

    override suspend fun upload(file: File) = sourceExceptionMapper.wrap {
        initAccount()
        val readFileResult = tryReadFile(file)
        upload(readFileResult)
    }

    private fun tryReadFile(file: File): ReadFileResult {
        try {
            initAccount()
            return ReadFileResult(file.name, file.readBytes())
        } catch (e: Exception) {
            throw ReadFileException(e)
        }
    }

    /**
     * Doc - https://github.com/mesadhan/google-drive-app
     */
    private fun upload(readFileResult: ReadFileResult) {
        var folderId = getFolderId()
        if (folderId == null) {
            folderId = createFolder()
        }
        val lasFile = if (readFileResult.fileName == DICTIONARY_NAME_WITH_FORMAT) {
            getLastFile(folderId)
        } else {
            null
        }
        val metadata2 = com.google.api.services.drive.model.File()
            .setName(readFileResult.fileName)
        val contentStream = ByteArrayContent(null, readFileResult.bytes)

        val files = driveService?.files()!!
        try {
            val googleFile = if (lasFile == null) {
                val metadata1 = com.google.api.services.drive.model.File()
                    .setName(readFileResult.fileName)
                    .setParents(Collections.singletonList(folderId))
                files.create(metadata1)
                    .setFields("id, parents")
                    .execute()
                    ?: throw IOException("Null result when requesting file creation.")
            } else {
                lasFile
            }
            val result = files.update(googleFile.id, metadata2, contentStream)?.execute()
            Log.d("MyUploadStrategy:", result.toString())
        } catch (e: Exception) {
            Log.e("MyUploadStrategy:", "ошибка", e)
            e.printStackTrace()
        }
    }

    private fun getFolderId(): String? {
        val result = driveService?.files()?.list()
            ?.setQ("mimeType='application/vnd.google-apps.folder'")
            ?.setSpaces("drive")
            ?.setPageToken(null)
            ?.execute()
        return result?.files?.filter {
            it.name == FOLDER_NAME
        }?.getOrNull(0)?.id
    }

    private fun createFolder(): String {
        val metadata: com.google.api.services.drive.model.File =
            com.google.api.services.drive.model.File()
                .setName(FOLDER_NAME)
                .setMimeType("application/vnd.google-apps.folder")

        val files = driveService?.files()!!
        try {
            val file = files.create(metadata)
                .setFields("id")
                .execute()
                ?: throw IOException("Null result when requesting folder creation.")
            return file.id
        } catch (e: Exception) {
            Log.e("MyUploadStrategy:", "ошибка", e)
            e.printStackTrace()
        }
        return FOLDER_NAME
    }

    companion object {
        private const val FOLDER_NAME = "Notifier"
    }
}