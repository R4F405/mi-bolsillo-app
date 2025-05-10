package com.rafa.mi_bolsillo_app.di

import android.content.Context
import com.rafa.mi_bolsillo_app.data.local.AppDatabase
import com.rafa.mi_bolsillo_app.data.local.dao.CategoryDao
import com.rafa.mi_bolsillo_app.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Las dependencias vivirán mientras la app viva
object DatabaseModule {

    @Provides
    @Singleton // Asegura que solo haya una instancia de AppDatabase
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton // Los DAOs deben ser singletons si la BD es singleton
    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.transactionDao()
    }
}