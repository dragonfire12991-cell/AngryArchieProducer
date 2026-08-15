package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MediaAssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaAssetDao {
    @Query("SELECT * FROM media_assets ORDER BY createdAt DESC")
    fun getAllAssets(): Flow<List<MediaAssetEntity>>

    @Query("SELECT * FROM media_assets WHERE category = :category ORDER BY createdAt DESC")
    fun getAssetsByCategory(category: String): Flow<List<MediaAssetEntity>>

    @Query("SELECT * FROM media_assets WHERE character = :character")
    fun getAssetsByCharacter(character: String): Flow<List<MediaAssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: MediaAssetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<MediaAssetEntity>)

    @Delete
    suspend fun deleteAsset(asset: MediaAssetEntity)

    @Query("DELETE FROM media_assets WHERE id = :id")
    suspend fun deleteAssetById(id: Long)

    @Query("SELECT COUNT(*) FROM media_assets")
    fun getAssetCount(): Flow<Int>
}
