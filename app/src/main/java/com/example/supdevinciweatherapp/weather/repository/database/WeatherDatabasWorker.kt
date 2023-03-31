package com.example.supdevinciweatherapp.weather.repository.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.supdevinciweatherapp.weather.entity.WeatherCityEntity
import com.example.supdevinciweatherapp.weather.repository.database.WeatherDatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [WeatherCityEntity::class], version = 2)
abstract class CityRoomDatabase : RoomDatabase() {
    abstract fun cityDao(): WeatherDatabaseDao
    companion object {
        @Volatile
        private var INSTANCE: CityRoomDatabase? = null
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): CityRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CityRoomDatabase::class.java,
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
        suspend fun populateDatabase(weatherCityDao: WeatherDatabaseDao) {
            weatherCityDao.deleteAll()
            var city = WeatherCityEntity("Hello")
            weatherCityDao.insert(city)
            city = WeatherCityEntity("World!")
            weatherCityDao.insert(city)
        }
    }
}