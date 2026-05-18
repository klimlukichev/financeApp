package ru.rsreu.klimlukichev.financeapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface KeywordCategoryRepository {

    fun observeKeywordCategoryMap(): Flow<Map<String, String>>

    suspend fun saveKeywordCategoryMap(keywordCategoryMap: Map<String, String>)
}
