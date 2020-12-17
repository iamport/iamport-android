package com.iamport.sdk

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.iamport.sdk.domain.di.appModule
import com.iamport.sdk.domain.di.provideChaiApi
import com.iamport.sdk.domain.di.provideIamportApi
import com.iamport.sdk.domain.di.provideNiceApi
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
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
abstract class AbstractKoin : AutoCloseKoinTest() {

    @Before
    fun initKoin() {
        val mockApiModule by lazy {
            module {
                single { provideIamportApi(get(), null) }
                single { provideChaiApi(get(), null) }
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
        mock(clazz.java)
    }

}