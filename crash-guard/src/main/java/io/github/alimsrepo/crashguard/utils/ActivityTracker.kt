package io.github.alimsrepo.crashguard.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Tracks the live Activity back-stack using [Application.ActivityLifecycleCallbacks].
 *
 * Register once via [Application.registerActivityLifecycleCallbacks] in [CrashGuard.install].
 * The collected stack is read by [CrashExceptionHandler] at crash time, replacing the
 * deprecated and restricted [android.app.ActivityManager.getRunningTasks] API.
 */
object ActivityTracker : Application.ActivityLifecycleCallbacks {

    private val stack = CopyOnWriteArrayList<String>()

    /** Returns a snapshot of the current activity class-name stack (most recent last). */
    fun getStack(): List<String> = stack.toList()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        stack.add(activity.javaClass.name)
    }

    override fun onActivityDestroyed(activity: Activity) {
        stack.remove(activity.javaClass.name)
    }

    // ── Unused lifecycle hooks ──────────────────────────────────────────────
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}

