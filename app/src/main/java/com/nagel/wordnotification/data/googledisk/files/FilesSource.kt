package com.nagel.wordnotification.data.googledisk.files

import com.nagel.wordnotification.domain.googledisk.files.RemoteFile
import java.io.File

interface FilesSource {

    suspend fun getFiles(): List<RemoteFile>

    suspend fun delete(file: RemoteFile)

    suspend fun upload(file: File)

}