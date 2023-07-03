package com.nagel.wordnotification.data

abstract class Storage {

    abstract fun getWordItem(pos: Int): Pair<String, String>

    abstract fun saveWordItem(word: Pair<String, String>)

    abstract fun getCount(): Int
}