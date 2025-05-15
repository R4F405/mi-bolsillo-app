package com.rafa.mi_bolsillo_app.ui.dashboard

import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.unit.sp // No se usa sp directamente aquí, pero es bueno tenerlo para el proyecto
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
import java.util.Locale

@Composable
fun CategoryPieChart(
    expensesByCategory: List<ExpenseByCategory>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current // context no se usa directamente, pero es bueno tenerlo por si acaso
    val legendTextColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val valueTextColorPred = MaterialTheme.colorScheme.onBackground.toArgb() // Renombrado para claridad

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "ES"))

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
                    yOffset = 15f // Un poco más de espacio para la leyenda
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

                setExtraOffsets(5f, 10f, 5f, 5f) // (left, top, right, bottom) - Ajustado
            }
        },
        update = { pieChart ->
            if (expensesByCategory.isEmpty()) {
                pieChart.data = null
                pieChart.centerText = "Sin gastos este mes"
                pieChart.invalidate()
                return@AndroidView
            }

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

            // --- LÓGICA PARA AGRUPAR CATEGORÍAS MENORES AL 5% EN "OTROS" ---
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
            // --- FIN DE LA LÓGICA DE AGRUPACIÓN ---

            if (entries.isEmpty()) {
                pieChart.data = null
                pieChart.centerText = "Sin gastos significativos" // O un mensaje apropiado
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
                valueLineColor = legendTextColor // Asegúrate que contraste bien
                yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
                xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

                // Opcional: Filtrar valores extremadamente pequeños incluso después de agrupar en "Otros"
                // Si la porción "Otros" es muy pequeña, o alguna otra, podrías ocultar su valor.
                // Por ejemplo, no mostrar el porcentaje si la porción es < 2% del total del gráfico.
                // Esto es adicional a la agrupación. La agrupación ya reduce el número de slices.
                // setValueTextFilter { value, _ -> value >= 2f } // 'value' aquí es el porcentaje de la slice en el gráfico
            }

            val data = PieData(dataSet)
            pieChart.data = data
            // Es importante llamar a notifyDataSetChanged() si la estructura de datos subyacente al adapter cambia
            // pero en este caso, estamos creando un nuevo PieData y asignándolo, por lo que invalidate() es suficiente.
            pieChart.invalidate()
        },
        modifier = modifier
    )
}