package io.github.alimsrepo.crashguard.presentation.ui.base

import io.github.alimsrepo.crashguard.domain.model.CrashData

/**
 * Interface that custom crash activities must implement
 */
interface CrashActivityContract {
    /**
     * Called when crash data is available
     */
    fun onCrashDataReceived(crashData: CrashData)

    /**
     * Called when restart is requested
     */
    fun onRestartRequested()

    /**
     * Called when close is requested
     */
    fun onCloseRequested()
}