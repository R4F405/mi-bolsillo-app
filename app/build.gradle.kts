plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
    id("jacoco")

}

android {
    namespace = "com.rafa.mi_bolsillo_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rafa.mi_bolsillo_app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true // Habilita Jetpack Compose
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Dependencias de Room (AÑADE/MODIFICA ESTAS LÍNEAS)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler) // Usa ksp para el procesador de anotaciones de Room
    implementation(libs.androidx.room.ktx)   // Para extensiones Kotlin y soporte de Coroutines

    // DataStore (para preferencias de usuario como la moneda)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.mpandroidchart)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx) // Para WorkManager con Kotlin y Coroutines
    implementation(libs.androidx.hilt.work)


    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler) // Usa ksp para el compilador de Hilt
    ksp(libs.androidx.hilt.compiler)

    // Hilt Navigation Compose (para hiltViewModel())
    implementation(libs.androidx.hilt.navigation.compose)

    // Lifecycle ViewModel KTX (para viewModelScope)
    implementation(libs.androidx.lifecycle.viewmodelktx)

    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)

    // Dependencias de Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Jetpack Compose - BOM (Bill of Materials)
    implementation(platform(libs.androidx.compose.bom)) // Importante: 'platform'
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Jetpack Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Integración de Compose con Activity y ViewModel/Lifecycle
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose) // Para hiltViewModel() y viewModel() en Composables
    implementation(libs.androidx.lifecycle.runtime.compose)   // Para collectAsStateWithLifecycle()

    // Herramientas de Compose (solo para depuración, no para release builds)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // debugImplementation(libs.androidx.compose.ui.test.manifest) // Para tests de UI con Compose

    // MPAndroidChart
    implementation(libs.mpandroidchart)

    // Biometric Authentication
    implementation(libs.androidx.biometric.ktx)
}

// --- Configuración de JaCoCo ---

// Versión de JaCoCo que se usará
jacoco {
    toolVersion = "0.8.12"
}

// Tarea para crear el reporte de cobertura para la variante "debug"
tasks.create("jacocoTestReport", JacocoReport::class) {
    dependsOn("testDebugUnitTest")

    // Fuentes que se incluirán en el reporte
    sourceDirectories.setFrom(files("$projectDir/src/main/java"))
    classDirectories.setFrom(
        fileTree("$buildDir/tmp/kotlin-classes/debug") {
            exclude(
                // Excluir clases generadas por Hilt, Dagger, Room, Compose, etc.
                "**/di/**",
                "**/*_HiltModules**",
                "**/*_Factory.*",
                "**/*_Impl.*",
                "**/databinding/**",
                "**/android/databinding/**",
                "**/BR.*",
                // Excluir UI (Composables) y Navegación
                "**/ui/theme/**",
                "**/ui/components/**",
                "**/navigation/**",
                "**/*Activity.*",
                "**/*Screen.*",
                "**/*Dialog.*",
                "**/*Item.*",
                "**/MiBolsilloApplication.*"
            )
        }
    )

    // Dónde se guardarán los resultados de los tests
    executionData.setFrom(files("$buildDir/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"))

    // Dónde se generarán los reportes
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
