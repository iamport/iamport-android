package com.iamport.sdk.domain.di

import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent

// lib module
object IamportKoinContext {
    var koinApp: KoinApplication? = null
}

interface IamportKoinComponent : KoinComponent {
    override fun getKoin(): Koin = IamportKoinContext.koinApp?.koin!!
}
