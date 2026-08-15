package com.example.data.repository

import com.example.data.local.ProjectDao
import com.example.data.model.ProjectEntity
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val projectCount: Flow<Int> = projectDao.getProjectsCount()

    fun getProjectById(id: Long): Flow<ProjectEntity?> = projectDao.getProjectByIdFlow(id)

    suspend fun getProjectDirect(id: Long): ProjectEntity? = projectDao.getProjectById(id)

    fun getProjectsByType(type: String): Flow<List<ProjectEntity>> = projectDao.getProjectsByType(type)

    suspend fun insertProject(project: ProjectEntity): Long = projectDao.insertProject(project)

    suspend fun updateProject(project: ProjectEntity) = projectDao.updateProject(project)

    suspend fun deleteProject(project: ProjectEntity) = projectDao.deleteProject(project)

    suspend fun deleteProjectById(id: Long) = projectDao.deleteProjectById(id)
}
