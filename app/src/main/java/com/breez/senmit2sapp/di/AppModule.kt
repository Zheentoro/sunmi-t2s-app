package com.breez.senmit2sapp.di

import com.breez.senmit2sapp.data.repository.PrinterRepository
import com.breez.senmit2sapp.data.repository.ProductRepository
import com.breez.senmit2sapp.data.repository.SunmiPrinterRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Предоставляет синглтон-экземпляр ProductRepository.
     *
     */
    @Provides
    @Singleton
    fun provideProductRepository(): ProductRepository {
        return ProductRepository()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PrinterModule {

    /**
     * Связывает реализацию SunmiPrinterRepository с интерфейсом PrinterRepository.
     */
    @Binds
    @Singleton
    abstract fun bindPrinterRepository(
        sunmiPrinterRepository: SunmiPrinterRepository
    ): PrinterRepository
}