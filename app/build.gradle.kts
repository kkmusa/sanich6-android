plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}


android {
    namespace = "com.example.sanich"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }


    defaultConfig {
        applicationId = "com.example.sanich"
        minSdk = 24
        targetSdk = 36

        versionCode = 31
        versionName = "2.8.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


    buildFeatures {
        compose = true
    }
}



dependencies {


    // Room база данных
    implementation("androidx.room:room-runtime:2.7.0")

    implementation("androidx.room:room-ktx:2.7.0")

    ksp("androidx.room:room-compiler:2.7.0")



    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.3")



    // Compose
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)

    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.compose.ui)

    implementation(libs.androidx.compose.ui.graphics)

    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.compose.material:material-icons-extended")


    // Core
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.lifecycle.runtime.ktx)



    // Tests
    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    androidTestImplementation(libs.androidx.espresso.core)

    androidTestImplementation(libs.androidx.junit)



    // Debug
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    debugImplementation(libs.androidx.compose.ui.tooling)

}