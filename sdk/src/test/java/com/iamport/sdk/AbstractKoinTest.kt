package com.iamport.sdk

import androidx.test.core.app.ApplicationProvider
import com.iamport.sdk.data.chai.CHAI_MODE
import com.iamport.sdk.domain.di.appModule
import com.iamport.sdk.domain.di.provideChaiApi
import com.iamport.sdk.domain.di.provideIamportApi
import com.iamport.sdk.domain.di.provideNiceApi
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.mock.MockProviderRule
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23])
abstract class AbstractKoinTest : AutoCloseKoinTest() {

//    @get:Rule
//    val koinTestRule = KoinTestRule.create {
//        printLogger(Level.DEBUG)
//        modules(myModule)
//    }

    @Before
    fun initKoin() {
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            .logStrategy { priority, tag, message -> println(message) }
            .build()
        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))

        val mockApiModule by lazy {
            module {
                single { provideIamportApi(get(), null) }
                single { provideChaiApi(CHAI_MODE.staging.name, get(),null) }
                single { provideNiceApi(get(), null) }
            }
        }

        stopKoin()
        startKoin {
            printLogger(Level.DEBUG)
            androidContext(ApplicationProvider.getApplicationContext())
            modules(mockApiModule, appModule)
        }
    }

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        Mockito.mock(clazz.java)
    }
}