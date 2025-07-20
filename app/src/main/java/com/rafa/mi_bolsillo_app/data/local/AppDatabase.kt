package com.rafa.mi_bolsillo_app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rafa.mi_bolsillo_app.data.local.converters.TransactionTypeConverter
import com.rafa.mi_bolsillo_app.data.local.dao.BudgetDao
import com.rafa.mi_bolsillo_app.data.local.dao.CategoryDao
import com.rafa.mi_bolsillo_app.data.local.dao.RecurringTransactionDao
import com.rafa.mi_bolsillo_app.data.local.dao.TransactionDao
import com.rafa.mi_bolsillo_app.data.local.entity.Budget
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.local.entity.RecurringTransaction
import com.rafa.mi_bolsillo_app.data.local.entity.Transaction
import com.rafa.mi_bolsillo_app.data.local.entity.RecurrenceFrequencyConverter

@Database(
    entities = [Category::class, Transaction::class, RecurringTransaction::class, Budget::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(
    TransactionTypeConverter::class,
    RecurrenceFrequencyConverter::class
)

/**
 * Base de datos principal de la aplicación, que utiliza Room para manejar las entidades y sus relaciones.
 * Contiene las definiciones de las tablas y las migraciones entre versiones.
 */

abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Definición de la Migración de la versión 1 a la 2
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
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
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_category_id` ON `recurring_transactions` (`category_id`)")
            }
        }

        // Definición de la Migración de la versión 2 a la 3
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Crear la nueva tabla budgets
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `budgets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `category_id` INTEGER NOT NULL,
                        `amount` REAL NOT NULL,
                        `month` INTEGER NOT NULL,
                        `year` INTEGER NOT NULL,
                        `creation_date` INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY(`category_id`) REFERENCES `categories`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_budgets_category_id` ON `budgets` (`category_id`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_budgets_year_month_category_id` ON `budgets` (`year`, `month`, `category_id`)")
            }
        }

        // Definición de la Migración de la versión 3 a la 4
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `budgets` ADD COLUMN `is_favorite` INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Definición de la Migración de la versión 4 a la 5
        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE `categories_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `color_hex` TEXT NOT NULL,
                        `is_predefined` INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                db.execSQL("CREATE UNIQUE INDEX `index_categories_new_name` ON `categories_new` (`name`)")

                db.execSQL("""
                    INSERT INTO `categories_new` (id, name, color_hex, is_predefined)
                    SELECT id, name, color_hex, is_predefined FROM `categories`
                """.trimIndent())

                db.execSQL("DROP TABLE `categories`")

                db.execSQL("ALTER TABLE `categories_new` RENAME TO `categories`")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mi_bolsillo_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}