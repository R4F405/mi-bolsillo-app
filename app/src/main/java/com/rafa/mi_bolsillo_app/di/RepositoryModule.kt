package com.rafa.mi_bolsillo_app.di

// Imports necesarios para las interfaces y las implementaciones de los repositorios
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepositoryImpl
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepositoryImpl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Dagger para proveer dependencias relacionadas con los repositorios.
 *
 * Incluye funciones para proveer instancias de CategoryRepository y TransactionRepository.
 */

@Module
@InstallIn(SingletonComponent::class) // Decide el alcance de estas vinculaciones
abstract class RepositoryModule { // Los módulos con @Binds DEBEN ser clases abstractas

    @Binds
    @Singleton // La instancia de CategoryRepository será un singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl // Hilt sabe cómo crear esto
    ): CategoryRepository // Cuando se pida CategoryRepository, Hilt proveerá CategoryRepositoryImpl

    @Binds
    @Singleton // La instancia de TransactionRepository también será un singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl // Hilt sabe cómo crear esto
    ): TransactionRepository // Cuando se pida TransactionRepository, Hilt proveerá TransactionRepositoryImpl
}