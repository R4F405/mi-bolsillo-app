package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz que define las operaciones de acceso a datos para las categorías.
 * Proporciona métodos para obtener, insertar, actualizar y eliminar categorías,
 * así como para manejar categorías predefinidas y definidas por el usuario.
 */

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    fun getUserDefinedCategories(): Flow<List<Category>>
    fun getPredefinedCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun insertCategory(category: Category): Long
    suspend fun insertCategories(categories: List<Category>)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
}