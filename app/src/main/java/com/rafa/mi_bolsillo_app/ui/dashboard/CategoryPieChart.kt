package com.rafa.mi_bolsillo_app.ui.dashboard

import android.graphics.Color as AndroidColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory

/**
 * Composable para mostrar un gráfico de pastel (Pie Chart) de gastos por categoría.
 *
 */

@Composable
fun CategoryPieChart(
    expensesByCategory: List<ExpenseByCategory>,
    modifier: Modifier = Modifier // Este modifier viene del contextow de DashboardScreen
) {
    val context = LocalContext.current
    // Determina el color del texto de la leyenda basado en el tema actual
    val legendTextColor = MaterialTheme.colorScheme.onBackground.toArgb()
    // Para los valores dentro del gráfico (porcentajes) basado en el tema actual
    val valueTextColor = MaterialTheme.colorScheme.onBackground.toArgb()

    // Composición del gráfico de pastel utilizando AndroidView
    AndroidView(
        factory = { ctx ->
            PieChart(ctx).apply {
                //Configuracion grafico
                description.isEnabled = false
                setUsePercentValues(true)
                isDrawHoleEnabled = true
                holeRadius = 45f
                transparentCircleRadius = 50f
                setEntryLabelTextSize(0f)

                //Configuracion leyenda
                legend.isEnabled = true
                legend.textSize = 10f
                legend.formSize = 8f
                legend.textColor = legendTextColor
                legend.isWordWrapEnabled = true // Habilitar el wrap de palabras
                legend.setDrawInside(false)
                
                // Minimizamos la animación para una experiencia más rápida
                animateY(800, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
                
                // Quitamos padding innecesario
                setExtraOffsets(5f, 5f, 5f, 5f)
                
                // No permitir interacción para evitar problemas de scroll
                isHighlightPerTapEnabled = false
                isDragDecelerationEnabled = false
            }
        },
        update = { pieChart ->
            val entries = ArrayList<PieEntry>()
            val colorsList  = ArrayList<Int>()

            // Si no hay gastos, mostramos un mensaje
            if (expensesByCategory.isEmpty()) {
                pieChart.data = null
                pieChart.centerText = "No hay gastos"
                pieChart.setCenterTextColor(legendTextColor)
                pieChart.setCenterTextSize(12f)
            } else {
                pieChart.centerText = ""
                for (expense in expensesByCategory) {
                    entries.add(PieEntry(expense.totalAmount.toFloat(), expense.categoryName))
                    try {
                        colorsList.add(AndroidColor.parseColor(expense.categoryColorHex))
                    } catch (e: IllegalArgumentException) {
                        // Color por defecto si el parseo falla
                        colorsList.add(ColorTemplate.rgb("#CCCCCC"))
                    }
                }

                // Configuración del conjunto de datos
                val dataSet = PieDataSet(entries, "")
                dataSet.sliceSpace = 2f
                dataSet.selectionShift = 0f
                dataSet.colors = colorsList
                dataSet.valueFormatter = PercentFormatter(pieChart)
                dataSet.valueTextSize = 9f
                dataSet.valueTextColor = valueTextColor // Aplicar color dinámico a los valores de porcentaje

                val data = PieData(dataSet)
                pieChart.data = data
            }
            pieChart.invalidate()
        },
        modifier = modifier
    )
}