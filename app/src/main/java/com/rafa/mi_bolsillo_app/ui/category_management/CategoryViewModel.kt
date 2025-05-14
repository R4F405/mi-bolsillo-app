package com.rafa.mi_bolsillo_app.ui.category_management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.data.repository.CategoryRepository
import com.rafa.mi_bolsillo_app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de gestión de categorías.
 *
 * Permite a los usuarios añadir, editar y eliminar categorías.
 *
 */

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository // Para verificar transacciones antes de eliminar
) : ViewModel() {

    // Estado mutable para la UI
    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    // Cargar categorías desde la base de datos
    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            categoryRepository.getAllCategories()
                .catch { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, userMessage = "Error al cargar categorías: ${throwable.message}")
                    }
                }
                .collect { categories ->
                    _uiState.update {
                        it.copy(isLoading = false, categories = categories)
                    }
                }
        }
    }

    // Agregar una nueva categoría
    fun addCategory(name: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiState.update { it.copy(userMessage = "El nombre de la categoría no puede estar vacío.") }
                return@launch
            }
            // Verificar si ya existe una categoría con el mismo nombre (ignorando mayúsculas/minúsculas)
            val existingCategory = _uiState.value.categories.any { it.name.equals(name, ignoreCase = true) }
            if (existingCategory) {
                _uiState.update { it.copy(userMessage = "Ya existe una categoría con el nombre '$name'.") }
                return@launch
            }

            // Crear la nueva categoría
            val newCategory = Category(
                name = name.trim(),
                colorHex = colorHex.ifBlank { "#CCCCCC" }, // Color por defecto si está vacío
                iconName = iconName.ifBlank { "default_icon" }, // Icono por defecto
                isPredefined = false
            )
            categoryRepository.insertCategory(newCategory)
            _uiState.update { it.copy(userMessage = "Categoría '$name' añadida.", showEditDialog = false) }
            // La lista se actualizará automáticamente gracias al Flow de loadCategories
        }
    }

    // Actualizar una categoría existente
    fun updateCategory(id: Long, name: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _uiState.update { it.copy(userMessage = "El nombre de la categoría no puede estar vacío.") }
                return@launch
            }
            // Verificar si ya existe OTRA categoría con el mismo nombre
            val F = _uiState.value.categories.any { it.id != id && it.name.equals(name, ignoreCase = true) }
            if (F) {
                _uiState.update { it.copy(userMessage = "Ya existe otra categoría con el nombre '$name'.") }
                return@launch
            }

            // Actualizar la categoría
            val categoryToUpdate = _uiState.value.categories.find { it.id == id }
            categoryToUpdate?.let {
                val updatedCategory = it.copy(
                    name = name.trim(),
                    colorHex = colorHex.ifBlank { "#CCCCCC" },
                    iconName = iconName.ifBlank { "default_icon" }
                )
                categoryRepository.updateCategory(updatedCategory)
                _uiState.update { it.copy(userMessage = "Categoría '$name' actualizada.", showEditDialog = false, categoryToEdit = null) }
            } ?: _uiState.update { it.copy(userMessage = "Error: Categoría no encontrada para actualizar.") }
        }
    }

    // Eliminar una categoría
    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            val categoryToDelete = _uiState.value.categories.find { it.id == categoryId }
            if (categoryToDelete == null) {
                _uiState.update { it.copy(userMessage = "Error: Categoría no encontrada.") }
                return@launch
            }

            // Verificar si la categoría es predefinida
            if (categoryToDelete.isPredefined) {
                _uiState.update { it.copy(userMessage = "Las categorías predefinidas no se pueden eliminar.") }
                return@launch
            }

            // Verificar si la categoría tiene transacciones asociadas
            val transactions = transactionRepository.getTransactionsByCategoryId(categoryId).first()
            if (transactions.isNotEmpty()) {
                _uiState.update { it.copy(userMessage = "No se puede eliminar '${categoryToDelete.name}', tiene ${transactions.size} transacciones asociadas.") }
                return@launch
            }

            // Elimina la categoría
            categoryRepository.deleteCategory(categoryToDelete)
            _uiState.update { it.copy(userMessage = "Categoría '${categoryToDelete.name}' eliminada.") }
        }
    }

    // Preparar una categoría para su edición
    fun prepareCategoryForEditing(category: Category?) {
        _uiState.update { it.copy(categoryToEdit = category, showEditDialog = true) }
    }

    // Ocultar el diálogo de edición
    fun dismissEditDialog() {
        _uiState.update { it.copy(showEditDialog = false, categoryToEdit = null) }
    }

    // Limpiar el mensaje de usuario
    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}

