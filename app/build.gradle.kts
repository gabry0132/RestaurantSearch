import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.restaurantsearch"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.restaurantsearch"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    //APIキーをできるだけ隠すために秘密のlocal.propertiesファイルに入れました。
    //コードを実行しようと思ったら、RestaurantsListActivity.javaの apyKey =
    val localProperties = Properties()
    val localPropertiesFile = File(rootDir, "local.properties")
    if(localPropertiesFile.exists() && localPropertiesFile.isFile){
        localPropertiesFile.inputStream().use {
            localProperties.load(it)
        }
    }

    buildTypes {
        release {
            //リバース・エンジニアリングしても分かりづらいものに切り替えます
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String","API_KEY",localProperties.getProperty("API_KEY"))
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            buildConfigField("String","API_KEY",localProperties.getProperty("API_KEY"))
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