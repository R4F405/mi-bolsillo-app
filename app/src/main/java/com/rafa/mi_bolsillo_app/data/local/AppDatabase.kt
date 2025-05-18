package com.rafa.mi_bolsillo_app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration // Importar Migration
import androidx.sqlite.db.SupportSQLiteDatabase // Importar SupportSQLiteDatabase
import com.rafa.mi_bolsillo_app.data.local.converters.TransactionTypeConverter
import com.rafa.mi_bolsillo_app.data.local.dao.CategoryDao
import com.rafa.mi_bolsillo_app.data.local.dao.TransactionDao
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
// Nuevas importaciones
import com.rafa.mi_bolsillo_app.data.local.entity.RecurringTransaction
import com.rafa.mi_bolsillo_app.data.local.dao.RecurringTransactionDao
import com.rafa.mi_bolsillo_app.data.local.entity.RecurrenceFrequencyConverter

@Database(
    entities = [Category::class, Transaction::class, RecurringTransaction::class], // Añadida RecurringTransaction
    version = 2, // ¡VERSIÓN INCREMENTADA!
    exportSchema = false // Mantenlo en false por ahora para simplificar
)
@TypeConverters(
    TransactionTypeConverter::class,
    RecurrenceFrequencyConverter::class // Añadido el nuevo converter
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao // Nuevo DAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Definición de la Migración de la versión 1 a la 2
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Crear la nueva tabla recurring_transactions
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `recurring_transactions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `description` TEXT,
                        `category_id` INTEGER NOT NULL,
                        `transaction_type` TEXT NOT NULL,
                        `start_date` INTEGER NOT NULL,
                        `frequency` TEXT NOT NULL,
                        `interval` INTEGER NOT NULL DEFAULT 1,
                        `day_of_month` INTEGER,
                        `month_of_year` INTEGER,
                        `end_date` INTEGER,
                        `next_occurrence_date` INTEGER NOT NULL,
                        `last_generated_date` INTEGER,
                        `is_active` INTEGER NOT NULL DEFAULT 1,
                        `creation_date` INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(`category_id`) REFERENCES `categories`(`id`) ON DELETE RESTRICT
                    )
                """.trimIndent())
                // Crear índice para category_id si no existe (opcional pero bueno para rendimiento)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_category_id` ON `recurring_transactions` (`category_id`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mi_bolsillo_database"
                )
                    .addMigrations(MIGRATION_1_2) // ¡AÑADIR LA MIGRACIÓN AQUÍ!
                    // .fallbackToDestructiveMigration() // Quitar esto si usas migraciones reales
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}