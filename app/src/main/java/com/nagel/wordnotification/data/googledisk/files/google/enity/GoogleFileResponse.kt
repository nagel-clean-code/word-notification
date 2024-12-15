package com.nagel.wordnotification.data.googledisk.files.google.enity

import com.nagel.wordnotification.domain.googledisk.files.RemoteFile

data class GoogleFileResponse(
    val id: String,
    val name: String,
    val size: Long
) {

    fun toRemoteFile() = RemoteFile(
        id = id,
        fileName = name,
        size = size,
    )
}