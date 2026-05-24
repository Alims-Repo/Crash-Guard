import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

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
    kotlinOptions {
        jvmTarget = "11"
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

// Read signing credentials from ~/.gradle/gradle.properties
// Keys: signingInMemoryKeyId, signingInMemoryKey, signingInMemoryKeyPassword
val signingKeyId = findProperty("signingInMemoryKeyId") as String?
val signingKey = findProperty("signingInMemoryKey") as String?
val signingPassword = findProperty("signingInMemoryKeyPassword") as String? ?: ""

signing {
    if (signingKey != null) {
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(publishing.publications)
    } else {
        logger.warn("⚠️  signingInMemoryKey not set — publications will NOT be signed.")
    }
}