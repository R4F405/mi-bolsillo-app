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
 * DAO (Data Access Object) para la entidad [Category].
 *
 * Proporciona los métodos que la aplicación utiliza para interactuar con la tabla 'categories'
 * en la base de datos. Todas las operaciones de base de datos deben ejecutarse fuera del hilo principal,
 * por lo que las funciones que realizan operaciones de escritura o lectura única son `suspend`
 * y las que observan cambios en los datos devuelven [Flow].
 */

@Dao
interface CategoryDao {

    //Insertar una nueva categoria en la base de datos.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    //Insertar una lista de categorias en la base de datos.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    //Actualizar una categoria existente en la base de datos.
    @Update
    suspend fun updateCategory(category: Category)

    //Eliminar una categoria existente en la base de datos.
    @Delete
    suspend fun deleteCategory(category: Category)

    //Obtener una categoria por su ID.
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?

    //Obtener todas las categorias.
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    //Obtener todas las categorias definidas por el usuario.
    @Query("SELECT * FROM categories WHERE is_predefined = 0 ORDER BY name ASC")
    fun getUserDefinedCategories(): Flow<List<Category>>

    //Obtener todas las categorias predefinidas.
    @Query("SELECT * FROM categories WHERE is_predefined = 1 ORDER BY name ASC")
    fun getPredefinedCategories(): Flow<List<Category>>
}