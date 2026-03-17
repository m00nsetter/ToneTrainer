package dev.moonsetter.shengcat.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.moonsetter.shengcat.data.db.dao.PracticeResultDao
import dev.moonsetter.shengcat.data.db.dao.TranslationDao
import dev.moonsetter.shengcat.data.db.entity.PracticeResultEntity
import dev.moonsetter.shengcat.data.db.entity.TranslationEntity

@TypeConverters(Converters::class)
@Database(
    version = 2,
    entities = [
        TranslationEntity::class,
        PracticeResultEntity::class
    ],
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getTranslationDao(): TranslationDao
    abstract fun getPracticeResultDao(): PracticeResultDao
}