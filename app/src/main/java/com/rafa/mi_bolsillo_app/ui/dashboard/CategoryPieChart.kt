package com.rafa.mi_bolsillo_app.ui.dashboard

import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.rafa.mi_bolsillo_app.data.local.dao.ExpenseByCategory
import java.text.NumberFormat
import java.util.Currency

/**
 * Composable que muestra un gráfico de pastel (Pie Chart) con los gastos por categoría.
 * Agrupa las categorías menores al 5% en una porción "Otros".
 *
 */

@Composable
fun CategoryPieChart(
    expensesByCategory: List<ExpenseByCategory>,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    val legendTextColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val valueTextColorPred = MaterialTheme.colorScheme.onBackground.toArgb()
    val currencyFormatter = remember(currency) {
        NumberFormat.getCurrencyInstance().apply {
            this.currency = currency
        }
    }

    // Crear el gráfico de pastel (PieChart) usando AndroidView
    AndroidView(
        factory = { ctx ->
            PieChart(ctx).apply {
                description.isEnabled = false
                setUsePercentValues(true)
                isDrawHoleEnabled = true
                holeRadius = 58f
                transparentCircleRadius = 61f
                setHoleColor(AndroidColor.TRANSPARENT)
                isRotationEnabled = true
                setDrawEntryLabels(false)
                animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)

                legend.apply {
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                    xEntrySpace = 7f
                    yEntrySpace = 5f
                    yOffset = 15f
                    textColor = legendTextColor
                    textSize = 12f
                    form = Legend.LegendForm.SQUARE
                    formSize = 10f
                    formToTextSpace = 6f
                    isWordWrapEnabled = true // Puede ayudar si los nombres de categoría son largos
                }

                setCenterTextTypeface(Typeface.SANS_SERIF)
                setCenterTextColor(legendTextColor)
                setCenterTextSize(16f)

                setExtraOffsets(5f, 10f, 5f, 5f)
            }
        },
        update = { pieChart ->
            // No hay gastos
            if (expensesByCategory.isEmpty()) {
                pieChart.data = null
                pieChart.centerText = "Sin gastos este mes"
                pieChart.invalidate()
                return@AndroidView
            }

            // Calcular el total de gastos para mostrar en el centro del gráfico
            val totalOverallExpenses = expensesByCategory.sumOf { it.totalAmount }
            if (totalOverallExpenses == 0.0) { // Evitar división por cero si todos los gastos son 0
                pieChart.data = null
                pieChart.centerText = "Sin gastos este mes"
                pieChart.invalidate()
                return@AndroidView
            }
            pieChart.centerText = "Gastos\n${currencyFormatter.format(totalOverallExpenses)}"

            val entries = ArrayList<PieEntry>()
            val colorsList = ArrayList<Int>()

            // Agrupar las categorías menores al 5% en "Otros"
            val minPercentageThreshold = 0.05 // 5%
            var othersSliceTotalAmount = 0.0
            val mainCategoriesEntries = mutableListOf<PieEntry>()
            val mainCategoriesColors = mutableListOf<Int>()

            // Primero, identificar y sumar las categorías que irán a "Otros"
            expensesByCategory.forEach { expense ->
                val percentage = if (totalOverallExpenses > 0) expense.totalAmount / totalOverallExpenses else 0.0
                if (percentage < minPercentageThreshold) {
                    othersSliceTotalAmount += expense.totalAmount
                } else {
                    mainCategoriesEntries.add(PieEntry(expense.totalAmount.toFloat(), expense.categoryName))
                    try {
                        mainCategoriesColors.add(AndroidColor.parseColor(expense.categoryColorHex))
                    } catch (e: IllegalArgumentException) {
                        mainCategoriesColors.add(ColorTemplate.rgb("#CCCCCC")) // Color por defecto
                    }
                }
            }

            // Añadir las categorías principales
            entries.addAll(mainCategoriesEntries.sortedByDescending { it.value }) // Ordenar las principales por valor
            colorsList.addAll(mainCategoriesColors) // Los colores ya están asociados, pero si reordenas entradas, necesitarías reordenar colores también o hacerlo después

            // Añadir la porción "Otros" si acumuló algún valor
            if (othersSliceTotalAmount > 0) {
                entries.add(PieEntry(othersSliceTotalAmount.toFloat(), "Otros"))
                colorsList.add(ColorTemplate.rgb("#B0BEC5")) // Color gris estándar para "Otros"
            }

            if (entries.isEmpty()) {
                pieChart.data = null
                pieChart.centerText = "Sin gastos significativos"
                pieChart.invalidate()
                return@AndroidView
            }

            val dataSet = PieDataSet(entries, "").apply {
                sliceSpace = 3f
                iconsOffset = MPPointF(0f, 40f)
                colors = colorsList

                setDrawValues(true)
                valueFormatter = PercentFormatter(pieChart) // Esencial para mostrar porcentajes
                valueTextSize = 10f
                valueTextColor = valueTextColorPred
                valueTypeface = Typeface.DEFAULT_BOLD

                valueLinePart1OffsetPercentage = 85f
                valueLinePart1Length = 0.45f
                valueLinePart2Length = 0.55f
                valueLineWidth = 1.5f
                valueLineColor = legendTextColor
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            }

            val data = PieData(dataSet)
            pieChart.data = data
            pieChart.invalidate()
        },
        modifier = modifier
    )
}