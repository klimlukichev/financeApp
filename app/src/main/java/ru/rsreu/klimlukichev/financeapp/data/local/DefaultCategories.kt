package ru.rsreu.klimlukichev.financeapp.data.local

import android.R
import ru.rsreu.klimlukichev.financeapp.domain.model.Category

object DefaultCategories {

    val items: List<Category> = listOf(
        Category(name = "Еда", iconResId = R.drawable.ic_menu_edit, colorInt = 0xFFE57373.toInt(), isDefault = true),
        Category(name = "Транспорт", iconResId = R.drawable.ic_menu_directions, colorInt = 0xFF64B5F6.toInt(), isDefault = true),
        Category(name = "Жильё", iconResId = R.drawable.ic_menu_myplaces, colorInt = 0xFF81C784.toInt(), isDefault = true),
        Category(name = "Развлечения", iconResId = R.drawable.ic_menu_gallery, colorInt = 0xFFFFB74D.toInt(), isDefault = true),
        Category(name = "Прочее", iconResId = R.drawable.ic_menu_info_details, colorInt = 0xFFBA68C8.toInt(), isDefault = true),
    )
}
