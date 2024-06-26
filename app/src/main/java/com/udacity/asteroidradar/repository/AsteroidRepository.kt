package com.udacity.asteroidradar.repository

import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.asDomainModel
import com.udacity.asteroidradar.api.formatDate
import com.udacity.asteroidradar.api.strToDate
import com.udacity.asteroidradar.asDatabaseModel
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class AsteroidRepository(private val database: AsteroidDatabase) {
//    val asteroidsLivedata: LiveData<List<Asteroid>> = database.asteroidDao.getAsteroidsLiveData().map {
//        it.asDomainModel()
//    }


    //val asteroids: List<Asteroid>? = database.asteroidDao.getAsteroids()?.asDomainModel()

    suspend fun getAsteroids(): List<Asteroid>? {
        return withContext(Dispatchers.IO) {
            database.asteroidDao.getAsteroids()?.asDomainModel()
        }

    }

    suspend fun insertAsteroid(asteroid: Asteroid) {
        withContext(Dispatchers.IO) {
            database.asteroidDao.insertAll(asteroid.asDatabaseModel())
        }
    }

    suspend fun deleteAsteroid(asteroid: Asteroid) {
        withContext(Dispatchers.IO) {
            database.asteroidDao.delete(asteroid.asDatabaseModel())
        }
    }

    suspend fun cacheAsteroids() {
        withContext(Dispatchers.IO) {
            val asteroids = AsteroidApi.retrofitService.getAsteroidsForNextSevenDays()
            asteroids.asDomainModel().map { asteroid ->
                database.asteroidDao.insertAll(asteroid.asDatabaseModel())
            }
        }
    }

    suspend fun cleanupOldAsteroids() {
        withContext(Dispatchers.IO) {
            val asteroids = database.asteroidDao.getAsteroids()
            asteroids?.map { asteroid ->
                if (strToDate(asteroid.closeApproachDate)?.before(strToDate(formatDate(Calendar.getInstance().time))) == true) {
                    database.asteroidDao.delete(asteroid)
                }
            }
        }
    }

}