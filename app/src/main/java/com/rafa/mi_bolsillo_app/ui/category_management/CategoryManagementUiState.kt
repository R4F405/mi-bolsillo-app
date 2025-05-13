package com.rafa.mi_bolsillo_app.ui.category_management

import com.rafa.mi_bolsillo_app.data.local.entity.Category

data class CategoryManagementUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    val categoryToEdit: Category? = null, // Para precargar el formulario de edición
    val showEditDialog: Boolean = false // Para controlar la visibilidad del diálogo/bottom sheet
)