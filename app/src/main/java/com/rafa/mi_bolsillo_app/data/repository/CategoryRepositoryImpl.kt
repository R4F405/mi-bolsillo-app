package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.dao.CategoryDao
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de categorías.
 *
 * Proporciona métodos para interactuar con la tabla de categorías en la base de datos.
 *
 */

@Singleton // Indicamos que Hilt debe proveer una única instancia de este repositorio
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories() // Obtener todas las categorías
    override fun getUserDefinedCategories(): Flow<List<Category>> = categoryDao.getUserDefinedCategories() // Obtener todas las categorías definidas por el usuario
    override fun getPredefinedCategories(): Flow<List<Category>> = categoryDao.getPredefinedCategories() // Obtener todas las categorías predefinidas
    override suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id) // Obtener una categoría por su ID
    override suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category) // Insertar una nueva categoría
    override suspend fun insertCategories(categories: List<Category>) = categoryDao.insertCategories(categories) // Insertar varias categorías
    override suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category) // Actualizar una categoría
    override suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category) // Eliminar una categoría
}