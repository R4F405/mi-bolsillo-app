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
import com.rafa.mi_bolsillo_app.ui.theme.AppTextPrimary

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
    // Color para el texto de los valores y etiquetas del gráfico
    val textColor = try { AndroidColor.parseColor(AppTextPrimary.hexString) } catch (e: Exception) { MaterialTheme.colorScheme.onSurface.toArgb() }

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
                setEntryLabelColor(textColor)
                setEntryLabelTextSize(0f)

                //Configuracion leyenda
                legend.isEnabled = true
                legend.textSize = 10f
                legend.formSize = 8f
                legend.setDrawInside(false)
                legend.isEnabled = true

                
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
            val colors = ArrayList<Int>()

            // Si no hay gastos, mostramos un mensaje
            if (expensesByCategory.isEmpty()) {
                pieChart.data = null
                pieChart.centerText = "No hay gastos"
                pieChart.setCenterTextColor(textColor)
                pieChart.setCenterTextSize(12f)
            } else {
                pieChart.centerText = ""
                for (expense in expensesByCategory) {
                    entries.add(PieEntry(expense.totalAmount.toFloat(), expense.categoryName))
                    try {
                        colors.add(AndroidColor.parseColor(expense.categoryColorHex))
                    } catch (e: IllegalArgumentException) {
                        colors.add(ColorTemplate.rgb("#CCCCCC"))
                    }
                }

                // Configuración del conjunto de datos
                val dataSet = PieDataSet(entries, "")
                dataSet.sliceSpace = 2f // Menos espacio entre segmentos
                dataSet.selectionShift = 0f // Sin shift al seleccionar
                dataSet.colors = colors
                dataSet.valueFormatter = PercentFormatter(pieChart)
                dataSet.valueTextSize = 9f // Texto de valores más pequeño
                dataSet.valueTextColor = textColor

                val data = PieData(dataSet)
                pieChart.data = data
            }
            pieChart.invalidate()
        },
        modifier = modifier
    )
}

// Extensión para convertir Color de Compose a Hex String
val androidx.compose.ui.graphics.Color.hexString: String
    get() = String.format("#%06X", 0xFFFFFF and this.toArgb())
