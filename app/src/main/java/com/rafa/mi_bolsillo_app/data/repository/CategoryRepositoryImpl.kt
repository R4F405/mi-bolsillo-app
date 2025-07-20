package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.dao.CategoryDao
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de categorías.
 * Proporciona acceso a los datos de categorías a través de la capa de persistencia.
 * Utiliza el DAO de categorías para realizar operaciones CRUD y consultas específicas.
 */

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    override fun getUserDefinedCategories(): Flow<List<Category>> = categoryDao.getUserDefinedCategories()
    override fun getPredefinedCategories(): Flow<List<Category>> = categoryDao.getPredefinedCategories()
    override suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id)
    override suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)
    override suspend fun insertCategories(categories: List<Category>) = categoryDao.insertCategories(categories)
    override suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)
    override suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
}