package com.example.mymusicapp.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mymusicapp.db.entity.MusicEntity

@Dao
interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addMusic(musicEntity: MusicEntity)

    @Update
    fun editMusic(musicEntity: MusicEntity)

    @Delete
    fun deleteMusic(musicEntity: MusicEntity)

    @Query("SELECT * FROM musicentity")
    fun listmusics(): List<MusicEntity>
}