package ru.rsreu.klimlukichev.financeapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.PieValueFormatter
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import ru.rsreu.klimlukichev.financeapp.R
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CategorySpendingChart(
    stats: List<CategoryStat>,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = stringResource(R.string.home_chart_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.home_chart_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (stats.isEmpty()) {
                EmptyChartState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                )
                return@Column
            }

            ChartContent(
                stats = stats,
                currencyFormat = currencyFormat,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ChartContent(
    stats: List<CategoryStat>,
    currencyFormat: NumberFormat,
    modifier: Modifier = Modifier,
) {
    val modelProducer = remember { PieChartModelProducer() }
    val values = stats.map { it.totalAmount.toFloat() }
    val total = remember(stats) { stats.sumOf { it.totalAmount } }

    LaunchedEffect(stats) {
        modelProducer.runTransaction {
            pieSeries { series(values) }
        }
    }

    val labelColor = MaterialTheme.colorScheme.onSurface
    val slices = remember(stats, labelColor) {
        stats.map { stat ->
            PieChart.Slice(
                fill = Fill(Color(stat.colorInt)),
                label = PieChart.SliceLabel.Outside(
                    TextComponent(textStyle = TextStyle(color = labelColor)),
                ),
            )
        }
    }

    if (stats.size == 1) {
        SingleCategoryChart(
            stat = stats.first(),
            amount = "${currencyFormat.format(stats.first().totalAmount)}\n100%",
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp),
        )
    } else {
        PieChartHost(
            chart = rememberPieChart(
                sliceProvider = PieChart.SliceProvider.series(slices),
                valueFormatter = PieValueFormatter { _, value, _ ->
                    val amount = value.toDouble()
                    val percent = amount.percentOf(total)
                    "${currencyFormat.format(amount)}\n$percent"
                },
            ),
            modelProducer = modelProducer,
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp),
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(top = 12.dp),
    ) {
        stats.take(4).forEach { stat ->
            CategoryLegendItem(
                stat = stat,
                amount = "${currencyFormat.format(stat.totalAmount)} (${stat.totalAmount.percentOf(total)})",
            )
        }
    }
}

@Composable
private fun SingleCategoryChart(
    stat: CategoryStat,
    amount: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(172.dp)) {
            drawCircle(color = Color(stat.colorInt))
            drawCircle(
                color = Color.White,
                radius = size.minDimension * 0.28f,
                style = Stroke(width = size.minDimension * 0.22f),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stat.categoryName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CategoryLegendItem(
    stat: CategoryStat,
    amount: String,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(stat.colorInt)),
        )
        Text(
            text = stat.categoryName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f),
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun Double.percentOf(total: Double): String {
    if (total <= 0.0) return "0%"
    return String.format(Locale.forLanguageTag("ru-RU"), "%.1f%%", this / total * 100.0)
}

@Composable
private fun EmptyChartState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(148.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "0",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.home_empty_chart),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
