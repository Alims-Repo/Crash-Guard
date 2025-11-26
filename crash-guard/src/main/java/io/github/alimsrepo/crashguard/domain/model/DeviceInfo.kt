package io.github.alimsrepo.crashguard.domain.model

import android.os.Build
import java.io.Serializable


/**
 * Device information data class
 */
data class DeviceInfo(
    val manufacturer: String = Build.MANUFACTURER,
    val model: String = Build.MODEL,
    val androidVersion: String = Build.VERSION.RELEASE,
    val sdkVersion: Int = Build.VERSION.SDK_INT,
    val brand: String = Build.BRAND,
    val device: String = Build.DEVICE,
    val board: String = Build.BOARD,
    val hardware: String = Build.HARDWARE,
    val product: String = Build.PRODUCT,
    val cpuAbi: String = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
    val screenDensity: String = "Unknown",
    val screenResolution: String = "Unknown"
) : Serializable {

    fun toFormattedString(): String = buildString {
        appendLine("Manufacturer: $manufacturer")
        appendLine("Model: $model")
        appendLine("Brand: $brand")
        appendLine("Device: $device")
        appendLine("Android Version: $androidVersion (API $sdkVersion)")
        appendLine("Board: $board")
        appendLine("Hardware: $hardware")
        appendLine("Product: $product")
        appendLine("CPU ABI: $cpuAbi")
        appendLine("Screen Density: $screenDensity")
        appendLine("Screen Resolution: $screenResolution")
    }

    fun toJson(): String = """
        {
            "manufacturer": "$manufacturer",
            "model": "$model",
            "androidVersion": "$androidVersion",
            "sdkVersion": $sdkVersion,
            "brand": "$brand",
            "cpuAbi": "$cpuAbi"
        }
    """.trimIndent()
}