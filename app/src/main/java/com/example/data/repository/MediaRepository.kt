package com.example.data.repository

import com.example.data.local.MediaAssetDao
import com.example.data.model.MediaAssetEntity
import kotlinx.coroutines.flow.Flow

class MediaRepository(private val mediaAssetDao: MediaAssetDao) {
    val allAssets: Flow<List<MediaAssetEntity>> = mediaAssetDao.getAllAssets()
    val assetCount: Flow<Int> = mediaAssetDao.getAssetCount()

    fun getAssetsByCategory(category: String): Flow<List<MediaAssetEntity>> =
        mediaAssetDao.getAssetsByCategory(category)

    fun getAssetsByCharacter(character: String): Flow<List<MediaAssetEntity>> =
        mediaAssetDao.getAssetsByCharacter(character)

    suspend fun insertAsset(asset: MediaAssetEntity): Long = mediaAssetDao.insertAsset(asset)

    suspend fun deleteAsset(asset: MediaAssetEntity) = mediaAssetDao.deleteAsset(asset)

    suspend fun deleteAssetById(id: Long) = mediaAssetDao.deleteAssetById(id)
}
