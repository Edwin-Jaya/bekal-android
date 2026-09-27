package com.edwin.bekal.di

import android.content.Context
import com.edwin.bekal.core.security.DefaultShellCommandExecutor
import com.edwin.bekal.core.security.RootDetector
import com.edwin.bekal.core.security.RootDetectorImpl
import com.edwin.bekal.core.security.ShellCommandExecutor
import com.scottyab.rootbeer.RootBeer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindRootDetector(
        rootDetectorImpl: RootDetectorImpl
    ): RootDetector

    @Binds
    @Singleton
    abstract fun bindShellCommandExecutor(
        defaultShellCommandExecutor: DefaultShellCommandExecutor
    ): ShellCommandExecutor

    companion object {
        @Provides
        @Singleton
        fun provideRootBeer(
            @ApplicationContext context: Context
        ): RootBeer = RootBeer(context)
    }
}
