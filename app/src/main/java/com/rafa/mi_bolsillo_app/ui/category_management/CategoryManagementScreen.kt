package com.rafa.mi_bolsillo_app.ui.category_management

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rafa.mi_bolsillo_app.data.local.entity.Category
import com.rafa.mi_bolsillo_app.ui.components.ConfirmationDialog
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.rafa.mi_bolsillo_app.ui.theme.LocalIsDarkTheme

/**
 * Pantalla de gestión de categorías.
 * Permite ver, añadir, editar y eliminar categorías.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    navController: NavController,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    val currentDarkTheme = LocalIsDarkTheme.current

    // Efecto para mostrar mensajes de usuario (Snackbar)
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUserMessage()
        }
    }

    // Composición de la pantalla
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val topAppBarContainerColor = if (currentDarkTheme) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primary
            }
            val topAppBarContentColor = if (currentDarkTheme) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onPrimary
            }
            TopAppBar(
                title = { Text("Categorías") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topAppBarContainerColor,
                    titleContentColor = topAppBarContentColor,
                    navigationIconContentColor = topAppBarContentColor
                )
            )
        },
        // Botón flotante para añadir una nueva categoría
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.prepareCategoryForEditing(null)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(Icons.Filled.Add, "Añadir categoría")
            }
        }
        // Textos de carga y vacio
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando categorías...")
                }
            } else if (uiState.categories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay categorías definidas. ¡Añade una!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp) // Espacio al final para el FAB
                ) {
                    items(uiState.categories, key = { it.id }) { category ->
                        CategoryItem(
                            category = category,
                            onEditClick = { cat ->
                                viewModel.prepareCategoryForEditing(cat)
                            },
                            onDeleteClick = { cat ->
                                categoryToDelete = cat
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            }

            // Diálogo de confirmación para eliminar categoría
            ConfirmationDialog(
                showDialog = showDeleteConfirmDialog,
                onConfirm = {
                    categoryToDelete?.let { viewModel.deleteCategory(it.id) }
                    showDeleteConfirmDialog = false
                    categoryToDelete = null
                },
                onDismiss = {
                    showDeleteConfirmDialog = false
                    categoryToDelete = null
                },
                title = "Confirmar Eliminación",
                message = "Si eliminas la categoría '${categoryToDelete?.name ?: ""}', no podrás recuperarla.\n¿Estás seguro?",
                confirmButtonText = "Eliminar",
                icon = Icons.Filled.Warning
            )

            // Dialogo de Añadir/Editar
            if (uiState.showEditDialog) {
                AddEditCategoryDialog(
                    categoryToEdit = uiState.categoryToEdit,
                    onDismiss = {
                        viewModel.dismissEditDialog()
                    },
                    onConfirm = { id, name, colorHex ->
                        if (id == null) { // Nueva categoría
                            viewModel.addCategory(name, colorHex)
                        } else { // Editar categoría existente
                            viewModel.updateCategory(id, name, colorHex)
                        }
                    }
                )
            }
        }
    }
}