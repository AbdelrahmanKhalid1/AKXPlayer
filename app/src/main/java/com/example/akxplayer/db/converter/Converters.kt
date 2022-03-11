package com.example.akxplayer.db.converter

import androidx.room.TypeConverter
import com.example.akxplayer.constants.RepeatMode
import com.google.gson.Gson

class Converters {

    @TypeConverter
    fun fromRepeatModeToString(repeatMode: RepeatMode): String = Gson().toJson(repeatMode).toString()
    @TypeConverter
    fun fromJsonToObj(string: String): RepeatMode = Gson().fromJson(string, RepeatMode::class.java)
}
