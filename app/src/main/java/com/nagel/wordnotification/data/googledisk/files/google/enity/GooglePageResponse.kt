package com.nagel.wordnotification.data.googledisk.files.google.enity

data class GooglePageResponse(
    val nextPageToken: String?,
    val files: List<GoogleFileResponse>,
)