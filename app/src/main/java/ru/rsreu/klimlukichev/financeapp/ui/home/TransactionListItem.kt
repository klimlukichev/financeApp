package ru.rsreu.klimlukichev.financeapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.rsreu.klimlukichev.financeapp.domain.model.TransactionType
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TransactionListItem(
    item: TransactionItemUi,
    onClick: () -> Unit = {},
    currencyFormat: NumberFormat? = null,
    modifier: Modifier = Modifier,
) {
    val localCurrencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("ru-RU")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    }
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("d MMM, HH:mm")
    }
    val formattedDate = remember(item.date) {
        Instant.ofEpochMilli(item.date)
            .atZone(ZoneId.systemDefault())
            .format(dateFormatter)
    }
    val formattedAmount = remember(item.amount, item.type, currencyFormat) {
        val prefix = if (item.type == TransactionType.INCOME) "+" else "-"
        prefix + (currencyFormat ?: localCurrencyFormat).format(item.amount)
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(item.colorInt).copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(item.colorInt)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.categoryName.take(1).uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                item.note?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            Text(
                text = formattedAmount,
                style = MaterialTheme.typography.titleMedium,
                color = if (item.type == TransactionType.INCOME) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
