package com.example.supdevinciweatherapp.geocoding.repository.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.supdevinciweatherapp.geocoding.entity.GeoCodingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [GeoCodingEntity::class], version = 1)
abstract class GeoCityRoomDatabase : RoomDatabase() {
    abstract fun cityDao(): GeoCodingDatabaseDao
    companion object {
        @Volatile
        private var INSTANCE: GeoCityRoomDatabase? = null
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): GeoCityRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GeoCityRoomDatabase::class.java,
                    "city_database"
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not part of this codelab.
                    .fallbackToDestructiveMigration()
                    .addCallback(WordDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
        private class WordDatabaseCallback(private val scope: CoroutineScope) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.cityDao())
                    }
                }
            }
        }
        suspend fun populateDatabase(geoCodingDatabaseDao: GeoCodingDatabaseDao) {
            geoCodingDatabaseDao.deleteAll()
        }
    }
}