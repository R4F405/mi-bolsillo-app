package com.rafa.mi_bolsillo_app.di

import com.rafa.mi_bolsillo_app.data.repository.BudgetRepository
import com.rafa.mi_bolsillo_app.data.repository.BudgetRepositoryImpl
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepositoryImpl
import com.rafa.mi_bolsillo_app.data.repository.RecurringTransactionRepository
import com.rafa.mi_bolsillo_app.data.repository.RecurringTransactionRepositoryImpl
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepositoryImpl
import com.rafa.mi_bolsillo_app.data.repository.SettingsRepository
import com.rafa.mi_bolsillo_app.data.repository.SettingsRepositoryImpl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger para proporcionar las implementaciones de los repositorios.
 * Este módulo se instala en el componente Singleton, lo que significa que
 * las instancias de los repositorios serán únicas durante toda la vida de la aplicación.
 */

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindRecurringTransactionRepository(
        recurringTransactionRepositoryImpl: RecurringTransactionRepositoryImpl
    ): RecurringTransactionRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        budgetRepositoryImpl: BudgetRepositoryImpl
    ): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository


}