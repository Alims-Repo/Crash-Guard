package io.github.alimsrepo.crashguard.domain.config

import android.content.Context

/**
 * Custom data provider for additional context
 */
interface CustomDataProvider {
    fun provideCustomData(context: Context): Map<String, String>
}