package com.rafa.mi_bolsillo_app.ui.dashboard

import android.graphics.Color as AndroidColor // Alias para evitar conflicto con Compose Color
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory // Tu data class
import com.rafa.mi_bolsillo_app.ui.theme.AppTextPrimary // Para el color del texto del gráfico
// Si no lo tienes, usa MaterialTheme.colorScheme.onSurface.toArgb()

@Composable
fun CategoryPieChart(
    expensesByCategory: List<ExpenseByCategory>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Color para el texto de los valores y etiquetas del gráfico (asegúrate de tener AppTextPrimary o usa uno del tema)
    val textColor = try { AndroidColor.parseColor(AppTextPrimary.hexString) } catch (e: Exception) { MaterialTheme.colorScheme.onSurface.toArgb() }


    AndroidView(
        factory = { ctx ->
            PieChart(ctx).apply {
                // Configuraciones iniciales del gráfico (se hacen una vez)
                description.isEnabled = false
                setUsePercentValues(true)
                isDrawHoleEnabled = true // Hueco en el centro
                holeRadius = 58f
                transparentCircleRadius = 61f
                setEntryLabelColor(textColor)
                setEntryLabelTextSize(12f)
                legend.isEnabled = true // O false si prefieres mostrar la leyenda de otra forma
                // legend.textColor = textColor // Configurar color de la leyenda
                // legend.textSize = 10f
                // legend.formSize = 10f
                // legend.formToTextSpace = 5f
                // legend.xEntrySpace = 10f
                // legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                // legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                // legend.orientation = Legend.LegendOrientation.HORIZONTAL
                // legend.setDrawInside(false)

                animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
            }
        },
        update = { pieChart ->
            // Actualizar datos del gráfico (se llama cuando expensesByCategory cambia)
            val entries = ArrayList<PieEntry>()
            val colors = ArrayList<Int>()

            if (expensesByCategory.isEmpty()) {
                // Podrías mostrar un estado vacío en el gráfico o simplemente no dibujar nada
                pieChart.data = null // Limpiar datos si no hay
                pieChart.centerText = "No hay gastos"
                pieChart.setCenterTextColor(textColor)
                pieChart.setCenterTextSize(14f)
            } else {
                pieChart.centerText = "" // Limpiar texto central si hay datos
                for (expense in expensesByCategory) {
                    entries.add(PieEntry(expense.totalAmount.toFloat(), expense.categoryName))
                    try {
                        colors.add(AndroidColor.parseColor(expense.categoryColorHex))
                    } catch (e: IllegalArgumentException) {
                        // Añadir un color por defecto si el parsing falla
                        colors.add(ColorTemplate.rgb("#CCCCCC")) // Gris por defecto
                    }
                }

                val dataSet = PieDataSet(entries, "") // El label del dataset puede ir vacío
                dataSet.sliceSpace = 3f
                dataSet.selectionShift = 5f
                dataSet.colors = colors // Usar los colores de tus categorías
                dataSet.valueFormatter = PercentFormatter(pieChart) // Mostrar valores como porcentaje
                dataSet.valueTextSize = 12f
                dataSet.valueTextColor = textColor // O puedes elegir un color que contraste con los slices

                val data = PieData(dataSet)
                pieChart.data = data
            }
            pieChart.invalidate() // Refrescar el gráfico
        },
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Para que sea cuadrado, o ajusta según necesites
    )
}

// Extensión para convertir Color de Compose a Hex String (si AppTextPrimary es Color de Compose)
val androidx.compose.ui.graphics.Color.hexString: String
    get() = String.format("#%06X", 0xFFFFFF and this.toArgb())
