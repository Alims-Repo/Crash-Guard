import com.vanniktech.maven.publish.SonatypeHost
import java.util.Base64
import org.gradle.plugins.signing.Sign

plugins {
    alias(libs.plugins.android.library)

    id("com.vanniktech.maven.publish") version "0.30.0"
    id("signing")
}

android {
    namespace = "io.github.alimsrepo.crashguard"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 22

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

mavenPublishing {
    coordinates(
        groupId = "io.github.alims-repo",
        artifactId = "crash-guard",
        version = "1.0.1"
    )

    pom {
        name.set("Crash Guard")
        description.set("Industry-grade Android crash handling library with customizable crash screens, persistent logging, and clean architecture")
        inceptionYear.set("2025")
        url.set("https://github.com/Alims-Repo/crash-guard")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("alim")
                name.set("Alim Sourav")
                email.set("sourav.0.alim@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/Alims-Repo/crash-guard")
            connection.set("scm:git:git://github.com/Alims-Repo/crash-guard.git")
            developerConnection.set("scm:git:ssh://github.com/Alims-Repo/crash-guard.git")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
}

// Read signing credentials from project properties / environment variables.
// In CI, pass the ASCII-armored private key as base64 via ORG_GRADLE_PROJECT_signingInMemoryKeyBase64
// to avoid multi-line environment variable corruption.  Locally you can still use
// signingInMemoryKey (plain armored text) in ~/.gradle/gradle.properties.
val signingKeyId = findProperty("signingInMemoryKeyId") as String?
val signingPassword = findProperty("signingInMemoryKeyPassword") as String? ?: ""

// Prefer the base64-encoded form (CI-safe); fall back to plain armored key (local dev)
val signingKey: String? = run {
    val b64 = findProperty("signingInMemoryKeyBase64") as String?
    if (b64 != null) {
        String(Base64.getDecoder().decode(b64.trim()))
    } else {
        findProperty("signingInMemoryKey") as String?
    }
}

signing {
    isRequired = signingKey != null
    if (signingKey != null) {
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(publishing.publications)
    } else {
        logger.warn("⚠️  signingInMemoryKey / signingInMemoryKeyBase64 not set — publications will NOT be signed.")
    }
}

// Ensure Sign tasks are gracefully skipped when no key is present
// (e.g. during local builds or doc-only runs on CI)
tasks.withType<Sign>().configureEach {
    onlyIf("signing key is configured") { signingKey != null }
}

