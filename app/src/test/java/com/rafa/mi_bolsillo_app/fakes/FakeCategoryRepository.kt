package com.rafa.mi_bolsillo_app.fakes

import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeCategoryRepository : CategoryRepository {
    val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())
    private var nextId = 1L

    override suspend fun insertCategory(category: Category): Long {
        val newCategory = category.copy(id = category.id.takeIf { it != 0L } ?: nextId++)
        categoriesFlow.value += newCategory
        return newCategory.id
    }

    override suspend fun insertCategories(categories: List<Category>) {
        categoriesFlow.value += categories
    }

    override suspend fun updateCategory(category: Category) {
        val currentList = categoriesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == category.id }
        if (index != -1) {
            currentList[index] = category
            categoriesFlow.value = currentList
        }
    }

    override suspend fun deleteCategory(category: Category) {
        categoriesFlow.value = categoriesFlow.value.filterNot { it.id == category.id }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        return categoriesFlow.value.find { it.id == id }
    }

    override fun getAllCategories(): Flow<List<Category>> = categoriesFlow.asStateFlow()

    override fun getUserDefinedCategories(): Flow<List<Category>> {
        return categoriesFlow.map { list -> list.filter { !it.isPredefined } }
    }

    override fun getPredefinedCategories(): Flow<List<Category>> {
        return categoriesFlow.map { list -> list.filter { it.isPredefined } }
    }
}