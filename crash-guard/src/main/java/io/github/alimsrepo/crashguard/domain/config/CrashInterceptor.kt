package io.github.alimsrepo.crashguard.domain.config


/**
 * Interface for intercepting crashes before handling
 */
interface CrashInterceptor {
    fun onCrashIntercepted(throwable: Throwable, thread: Thread): Boolean
}