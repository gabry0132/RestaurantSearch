plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.restaurantsearch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.restaurantsearch"
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //私が追加したのはここからです。
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.window.size)
    implementation(libs.compose.material3.adaptive)
    implementation(libs.volley)
    implementation(libs.cardview)
    implementation(libs.glide)
    implementation(libs.play.services.location)
    implementation(libs.work.runtime)
    implementation(libs.concurrent.listenablefuture)
}