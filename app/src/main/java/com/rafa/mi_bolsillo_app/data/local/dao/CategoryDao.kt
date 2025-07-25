package com.rafa.mi_bolsillo_app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para manejar las operaciones de la tabla de categorias.
 * Contiene métodos para insertar, actualizar, eliminar y consultar categorias.
 */

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE is_predefined = 0 ORDER BY name ASC")
    fun getUserDefinedCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE is_predefined = 1 ORDER BY name ASC")
    fun getPredefinedCategories(): Flow<List<Category>>
}