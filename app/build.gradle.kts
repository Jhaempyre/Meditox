plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"

}

android {
    namespace = "com.example.meditox"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.meditox"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",


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
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    //for navigation
    implementation(libs.androidx.navigation.compose)
    // Preferences DataStore (for key-value pairs)
    implementation (libs.androidx.datastore.preferences)
    // Optional: If you also want to use Proto DataStore (for typed objects)
    implementation ("androidx.datastore:datastore:1.1.7")
    // For coroutines support (needed for DataStore's suspend functions)
    implementation (libs.kotlinx.coroutines.android)
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    // For LifecycleScope (if you want to launch coroutines in ViewModel/Activity)
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")

    implementation ("androidx.activity:activity-compose:1.9.0")

    //For calling HTTP server
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")


    implementation("androidx.multidex:multidex:2.0.1")
    implementation ("com.google.accompanist:accompanist-permissions:0.28.0")

    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.33.0-alpha")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation ("com.jakewharton.threetenabp:threetenabp:1.4.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")





    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}