package ru.rsreu.klimlukichev.financeapp.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.pie.PieChart
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.pieSeries
import com.patrykandpatrick.vico.compose.pie.rememberPieChart
import ru.rsreu.klimlukichev.financeapp.R

@Composable
fun CategorySpendingChart(
    stats: List<CategoryStat>,
    modifier: Modifier = Modifier,
) {
    if (stats.isEmpty()) {
        Text(
            text = stringResource(R.string.home_empty_chart),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }

    val modelProducer = remember { PieChartModelProducer() }
    val values = stats.map { it.totalAmount.toFloat() }

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

    PieChartHost(
        chart = rememberPieChart(
            sliceProvider = PieChart.SliceProvider.series(slices),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
    )
}
