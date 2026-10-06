package dev.moonsetter.shengcat

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.moonsetter.shengcat.data.db.AppDatabase
import dev.moonsetter.shengcat.data.db.dao.PracticeResultDao
import dev.moonsetter.shengcat.data.db.entity.PracticeResultEntity
import dev.moonsetter.shengcat.data.repository.SyllableRepository
import dev.moonsetter.shengcat.model.PracticeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class InstrumentedTests {
    private lateinit var db: AppDatabase
    private lateinit var dao: PracticeResultDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = db.getPracticeResultDao()
    }

    @After
    fun closeDb() {
        if (::db.isInitialized) {
            db.close()
        }
    }

    @Test
    fun insertAndRetrieveResult() = runBlocking {

        val entity = PracticeResultEntity(
            id = 0,
            score = 7,
            totalQuestions = 10,
            practiceMode = PracticeMode.PRONUNCIATION,
            date = LocalDateTime.now()
        )

        dao.insert(entity)

        val results = dao.getAll().first { it.isNotEmpty() }

        assertEquals(1, results.size)
        assertEquals(7, results[0].score)
    }

    @Test
    fun resultsOrderedByDateDescending() = runBlocking {
        val older = PracticeResultEntity(
            0,
            10,
            10,
            PracticeMode.RECOGNITION,
            LocalDateTime.now().minusDays(1)
        )

        val newer = PracticeResultEntity(
            0,
            10,
            10,
            PracticeMode.PRONUNCIATION,
            LocalDateTime.now()
        )

        dao.insert(older)
        dao.insert(newer)

        val results = dao.getAll().first { it.size == 2 }

        assertEquals(10, results[0].score)
        assertEquals(10, results[1].score)
    }

    @Test
    fun deleteAllClearsTable() = runBlocking {
        dao.insert(
            PracticeResultEntity(
                0,
                3,
                10,
                PracticeMode.RECOGNITION,
                LocalDateTime.now()
            )
        )

        dao.insert(
            PracticeResultEntity(
                0,
                9,
                10,
                PracticeMode.PRONUNCIATION,
                LocalDateTime.now()
            )
        )

        dao.deleteAll()
        val results = dao.getAll().first()
        assertTrue(results.isEmpty())
    }

    @Test
    fun insertMultipleModesAndFilterByFlow() = runBlocking {
        dao.insert(
            PracticeResultEntity(
                0,
                4,
                10,
                PracticeMode.RECOGNITION,
                LocalDateTime.now().minusMinutes(5)
            )
        )

        dao.insert(
            PracticeResultEntity(
                0,
                6,
                10,
                PracticeMode.PRONUNCIATION,
                LocalDateTime.now()
            )
        )

        val results = dao.getAll().first { it.size == 2 }
        assertEquals(2, results.size)
    }

    @Test
    fun poolIsNotEmpty() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = SyllableRepository(context)
        val pool = repository.getPool()
        assertTrue(pool.isNotEmpty())
    }

    @Test
    fun poolContainsAllFourTones() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = SyllableRepository(context)
        val pool = repository.getPool()
        val tones = pool.map { it.toneNumber }.toSet()

        assertTrue(tones.contains(1))
        assertTrue(tones.contains(2))
        assertTrue(tones.contains(3))
        assertTrue(tones.contains(4))
    }
}