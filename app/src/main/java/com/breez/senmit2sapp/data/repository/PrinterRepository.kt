package com.breez.senmit2sapp.data.repository

import android.content.Context
import com.breez.senmit2sapp.data.model.Product
import com.breez.senmit2sapp.utils.SunmiPrintHelper
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume


/**
 * Класс для представления результата операции печати.
 */
sealed class PrintResult {
    object Success : PrintResult()
    data class Error(val message: String) : PrintResult()
}

/**
 * Интерфейс для абстрагирования работы с принтером.
 */
interface PrinterRepository {
    fun init(context: Context)
    suspend fun printReceipt(products: List<Product>, total: Double): PrintResult
    fun disconnect(context: Context)
}


/**
 * Реализация репозитория для принтера Sunmi.
 */
class SunmiPrinterRepository @Inject constructor() : PrinterRepository {

    override fun init(context: Context) {
        SunmiPrintHelper.initSunmiPrinterService(context)
    }

    /**
     * Используем suspendCancellableCoroutine для преобразования API на основе колбэков в suspend-функцию.
     * Удобно для использования с корутинами.
     */
    override suspend fun printReceipt(products: List<Product>, total: Double): PrintResult =
        suspendCancellableCoroutine { continuation ->
            SunmiPrintHelper.printReceipt(products, total) { isSuccess, message ->
                if (continuation.isActive) {
                    val result = if (isSuccess) {
                        PrintResult.Success
                    } else {
                        PrintResult.Error(message)
                    }
                    continuation.resume(result)
                }
            }
        }

    override fun disconnect(context: Context) {
        SunmiPrintHelper.disconnectSunmiPrinterService(context)
    }
}

