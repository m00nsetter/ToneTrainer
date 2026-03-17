package dev.moonsetter.shengcat.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.moonsetter.shengcat.data.db.AppDatabase
import dev.moonsetter.shengcat.data.db.dao.TranslationDao
import dev.moonsetter.shengcat.data.db.dao.PracticeResultDao
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "shengcat_db")
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Singleton
    @Provides
    fun provideTranslation(db: AppDatabase): TranslationDao {
        return db.getTranslationDao()
    }

    @Singleton
    @Provides
    fun providePracticeResult(db: AppDatabase): PracticeResultDao {
        return db.getPracticeResultDao()
    }
}