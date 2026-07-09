// ============================================================
// App-level build.gradle.kts
// Konfigurasi lengkap modul aplikasi utama.
// ============================================================
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.gempa.id"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gempa.id"
        minSdk = 26          // Android 8.0+ — mendukung 98% perangkat aktif
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BuildConfig fields — URL API dikonfigurasi di sini agar mudah diganti
        buildConfigField("String", "BMKG_BASE_URL", "\"https://data.bmkg.go.id/DataMKG/TEWS/\"")
        buildConfigField("String", "BMKG_BASE_URL_AUTOGEMPA", "\"https://data.bmkg.go.id/DataMKG/TEWS/autogempa.json\"")
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        // Aktifkan context receivers & optin untuk Coroutines
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // ── Core AndroidX ───────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // ── Jetpack Compose (dikelola via BOM) ──────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ── Navigation ──────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ── Dependency Injection — Hilt ─────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // ── Network — Retrofit + OkHttp + KotlinX Serialization ─
    implementation(libs.retrofit)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)

    // ── Local Database — Room ────────────────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ── Coroutines ───────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)

    // ── DataStore — Theme Preference ────────────────────────
    implementation(libs.androidx.datastore.preferences)

    // ── Image Loading ────────────────────────────────────────
    implementation(libs.coil.compose)

    // ── Animation ────────────────────────────────────────────
    implementation(libs.lottie.compose)

    // ── Logging ──────────────────────────────────────────────
    implementation(libs.timber)

    // ── Testing ──────────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
