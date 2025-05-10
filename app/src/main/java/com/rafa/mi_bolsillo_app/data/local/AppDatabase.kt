package com.rafa.mi_bolsillo_app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rafa.mi_bolsillo_app.data.local.converters.TransactionTypeConverter
import com.rafa.mi_bolsillo_app.data.local.dao.CategoryDao
import com.rafa.mi_bolsillo_app.data.local.dao.TransactionDao
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction

@Database(
    entities = [Category::class, Transaction::class],
    version = 1,
    exportSchema = false // Puedes ponerlo a true si quieres exportar el esquema para migraciones
)
@TypeConverters(TransactionTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mi_bolsillo_database" // Nombre del archivo de la base de datos
                )
                    // .fallbackToDestructiveMigration() // Destruye y crea la base de datos en lugar de migrar.
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}