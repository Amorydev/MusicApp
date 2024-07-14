plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.amory.musicapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.amory.musicapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders.putIfAbsent("appAuthRedirectScheme", "com.amory.musicapp")

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true

        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.palette:palette-ktx:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.0")

    //SpinKit(Custom progressBar)
    implementation("com.github.ybq:Android-SpinKit:1.4.0")
    //CurvedBottomNavigation
    implementation("com.github.qamarelsafadi:CurvedBottomNavigation:0.1.3")
    //Circle imageView
    implementation("de.hdodenhof:circleimageview:3.1.0")
    //okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    //GSON Converter
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //Glide
    implementation("com.github.bumptech.glide:glide:4.12.0")

    //recyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    //EventBus
    implementation("org.greenrobot:eventbus:3.3.1")
    //AppAuth
    implementation("net.openid:appauth:0.11.1")
    implementation ("com.squareup.okio:okio:3.6.0")

    //For Notification
    implementation("androidx.media:media:1.7.0")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")


}
