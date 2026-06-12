plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.lsposeddarkmode"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lsposeddarkmode"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("debugSign") {
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debugSign")
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
    compileOnly(project(":xposed-api-stubs"))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.11.0")
}
