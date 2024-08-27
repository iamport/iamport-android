// 디펜던시 업데이트 확인 ./gradlew dependencyUpdates

object Versions {
    const val versionCode = 240827000 // yymmdd000
    const val versionName = "1.4.6"  // https://www.notion.so/chaifinance/QA-Process-d1a4be396337493b81c6e85fff2d5cd6

    const val multidex = "2.0.1"
    const val kotlin_stdlib_jdk = "1.9.10"
    const val core_ktx = "1.12.0"
    const val appcompat = "1.6.1"

    const val mokito = "5.6.0"
    const val mokito_inline = "5.2.0"
    const val mokito_kotlin = "5.1.0"

    const val junit = "4.13.1"
    const val testJunit = "1.1.5"
    const val espresso_core = "3.5.1"

    const val androidx_test_core = "1.5.0"
    const val androidx_test_runner = "1.5.2"

    const val constraintlayout = "2.1.4"
    const val lifecycle_extensions = "2.2.0"
    const val lifecycle_common = "2.6.2"

    const val material = "1.10.0"

    const val activity_ktx = "1.8.0"
    const val fragment_ktx = "1.6.1"

    const val koin_version = "3.5.0"
    const val retrofit_version = "2.9.0"
    const val logging_interceptor = "4.9.3"

    // Gson
    const val gson = "2.10.1"

    // Logger
    const val logger = "2.2.0"

    // Rx
    const val rxjava = "2.2.21"
    const val rxandroid = "2.1.1"
    const val rxjava2_debug = "1.4.0"

    const val robolectric = "4.7.3"

    const val serialization = "1.6.0"

    const val workRuntimeKtx = "2.8.1"

    const val lottie = "6.4.1"
}

object Libs {
    const val multidex = "androidx.multidex:multidex:${Versions.multidex}"
    const val kotlin_stdlib_jdk = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin_stdlib_jdk}"
    const val core_ktx = "androidx.core:core-ktx:${Versions.core_ktx}"
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"

    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    const val mokito_core = "org.mockito:mockito-core:${Versions.mokito}"
    const val mokito_inline = "org.mockito:mockito-inline:${Versions.mokito_inline}"
    const val mokito_android = "org.mockito:mockito-android:${Versions.mokito}"
    const val mokito_kotlin = "org.mockito.kotlin:mockito-kotlin:${Versions.mokito_kotlin}"


    const val junit = "junit:junit:${Versions.junit}"
    const val testJunit = "androidx.test.ext:junit:${Versions.testJunit}"
    const val espresso_core = "androidx.test.espresso:espresso-core:${Versions.espresso_core}"

    // Core library
    const val androidx_test = "androidx.test:core:${Versions.androidx_test_core}"
    const val androidx_test_runner = "androidx.test:runner:${Versions.androidx_test_runner}"
    const val androidx_test_rules = "androidx.test:rules:${Versions.androidx_test_runner}"

    // Android Support/Architecture
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}"
    const val lifecycle_extensions = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle_extensions}"
    const val lifecycle_viewmodel_ktx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle_extensions}"
    const val lifecycle_livedata_ktx = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle_extensions}"

    const val lifecycle_common = "androidx.lifecycle:lifecycle-common:${Versions.lifecycle_common}"

    const val material = "com.google.android.material:material:${Versions.material}"


    const val activity_ktx = "androidx.activity:activity-ktx:${Versions.activity_ktx}"
    const val fragment_ktx = "androidx.fragment:fragment-ktx:${Versions.fragment_ktx}"

    // Koin main features for Android (Scope,ViewModel ...)
    const val koin_android = "io.insert-koin:koin-android:${Versions.koin_version}"

    const val koin_core = "io.insert-koin:koin-core:${Versions.koin_version}"
    const val koin_test = "io.insert-koin:koin-test:${Versions.koin_version}"
    const val koin_test_junit4 = "io.insert-koin:koin-test-junit4:${Versions.koin_version}"

    // Koin Java Compatibility
    const val koin_android_compat = "io.insert-koin:koin-android-compat:${Versions.koin_version}"

    // Retorofit
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit_version}"
    const val converter_gson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit_version}"
    const val logging_interceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.logging_interceptor}"

    // Gson
    const val gson = "com.google.code.gson:gson:${Versions.gson}"

    // Logger
    const val logger = "com.orhanobut:logger:${Versions.logger}"

    // Rx
    const val rxjava = "io.reactivex.rxjava2:rxjava:${Versions.rxjava}"
    const val rxandroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxandroid}"
    const val rxjava2_debug = "com.akaita.java:rxjava2-debug:${Versions.rxjava2_debug}"

    const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}"

    // target android 12 pending intent 처리를 위해 추가
    // Targeting S+ (version 31 and above) requires that one of FLAG_IMMUTABLE or FLAG_MUTABLE be specified when creating a PendingIntent.
    const val workRuntimeKtx = "androidx.work:work-runtime-ktx:${Versions.workRuntimeKtx}"

    const val lottie = "com.airbnb.android:lottie:${Versions.lottie}"
}

