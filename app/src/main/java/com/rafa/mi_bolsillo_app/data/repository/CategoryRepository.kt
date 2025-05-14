package com.rafa.mi_bolsillo_app.data.repository

import com.rafa.mi_bolsillo_app.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para el repositorio de categorías.
 *
 * Proporciona métodos para interactuar con la tabla de categorías en la base de datos
 *
 */

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>> // Obtener todas las categorías
    fun getUserDefinedCategories(): Flow<List<Category>> // Obtener todas las categorías definidas por el usuario
    fun getPredefinedCategories(): Flow<List<Category>> // Obtener todas las categorías predefinidas
    suspend fun getCategoryById(id: Long): Category? // Obtener una categoría por su ID
    suspend fun insertCategory(category: Category): Long // Insertar una nueva categoría
    suspend fun insertCategories(categories: List<Category>) // Insertar varias categorías
    suspend fun updateCategory(category: Category) // Actualizar una categoría
    suspend fun deleteCategory(category: Category) // Eliminar una categoría
}