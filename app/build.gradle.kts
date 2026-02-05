plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.laxman.foodgramdelivery"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.laxman.foodgramdelivery"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
// Converter for JSON (Gson)
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}