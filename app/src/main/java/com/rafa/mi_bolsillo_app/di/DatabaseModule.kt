package com.rafa.mi_bolsillo_app.di

import android.content.Context
import com.rafa.mi_bolsillo_app.data.local.AppDatabase
import com.rafa.mi_bolsillo_app.data.local.dao.BudgetDao
import com.rafa.mi_bolsillo_app.data.local.dao.CategoryDao
import com.rafa.mi_bolsillo_app.data.local.dao.RecurringTransactionDao
import com.rafa.mi_bolsillo_app.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
/**
 * Módulo Dagger para proveer dependencias relacionadas con la base de datos.
 *
 * Incluye funciones para proveer instancias de AppDatabase, CategoryDao y TransactionDao.
 *
 */

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.transactionDao()
    }

    @Provides
    @Singleton
    fun provideRecurringTransactionDao(appDatabase: AppDatabase): RecurringTransactionDao {
        return appDatabase.recurringTransactionDao()
    }

    @Provides
    @Singleton
    fun provideBudgetDao(appDatabase: AppDatabase): BudgetDao {
        return appDatabase.budgetDao()
    }
}