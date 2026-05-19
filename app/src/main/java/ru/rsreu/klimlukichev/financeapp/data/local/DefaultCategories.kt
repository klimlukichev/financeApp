package ru.rsreu.klimlukichev.financeapp.data.local

import android.R
import ru.rsreu.klimlukichev.financeapp.domain.model.Category

object DefaultCategories {

    val items: List<Category> = listOf(
        Category(name = "Еда", iconResId = R.drawable.ic_menu_edit, colorInt = 0xFFE57373.toInt(), isDefault = true),
        Category(name = "Транспорт", iconResId = R.drawable.ic_menu_directions, colorInt = 0xFF64B5F6.toInt(), isDefault = true),
        Category(name = "Жильё", iconResId = R.drawable.ic_menu_myplaces, colorInt = 0xFF81C784.toInt(), isDefault = true),
        Category(name = "Развлечения", iconResId = R.drawable.ic_menu_gallery, colorInt = 0xFFFFB74D.toInt(), isDefault = true),
        Category(name = "Здоровье", iconResId = R.drawable.ic_menu_info_details, colorInt = 0xFF4DB6AC.toInt(), isDefault = true),
        Category(name = "Одежда", iconResId = R.drawable.ic_menu_upload, colorInt = 0xFFF06292.toInt(), isDefault = true),
        Category(name = "Маркетплейсы", iconResId = R.drawable.ic_menu_share, colorInt = 0xFF9575CD.toInt(), isDefault = true),
        Category(name = "Связь", iconResId = R.drawable.ic_menu_call, colorInt = 0xFF4FC3F7.toInt(), isDefault = true),
        Category(name = "Подписки", iconResId = R.drawable.ic_menu_recent_history, colorInt = 0xFFFF8A65.toInt(), isDefault = true),
        Category(name = "Путешествия", iconResId = R.drawable.ic_menu_compass, colorInt = 0xFF7986CB.toInt(), isDefault = true),
        Category(name = "Переводы", iconResId = R.drawable.ic_menu_send, colorInt = 0xFFA1887F.toInt(), isDefault = true),
        Category(name = "Образование", iconResId = R.drawable.ic_menu_agenda, colorInt = 0xFFFFD54F.toInt(), isDefault = true),
        Category(name = "Красота", iconResId = R.drawable.ic_menu_manage, colorInt = 0xFFCE93D8.toInt(), isDefault = true),
        Category(name = "Прочее", iconResId = R.drawable.ic_menu_info_details, colorInt = 0xFFBA68C8.toInt(), isDefault = true),
    )
}
