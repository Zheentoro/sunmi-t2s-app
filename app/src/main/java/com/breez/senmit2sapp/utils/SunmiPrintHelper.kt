package com.breez.senmit2sapp.utils

import android.content.Context
import android.util.Log
import com.breez.senmit2sapp.data.model.Product
import com.sunmi.peripheral.printer.InnerPrinterCallback
import com.sunmi.peripheral.printer.InnerPrinterException
import com.sunmi.peripheral.printer.InnerPrinterManager
import com.sunmi.peripheral.printer.InnerResultCallback
import com.sunmi.peripheral.printer.SunmiPrinterService

object SunmiPrintHelper {

    private var sunmiPrinterService: SunmiPrinterService? = null
    private const val TAG = "SunmiPrintHelper"

    // Создаем колбэк для отслеживания подключения к сервису
    private val innerPrinterCallback = object : InnerPrinterCallback() {
        override fun onConnected(service: SunmiPrinterService) {
            sunmiPrinterService = service
            Log.d(TAG, "Sunmi Printer Service Connected")
        }

        override fun onDisconnected() {
            sunmiPrinterService = null
            Log.e(TAG, "Sunmi Printer Service Disconnected")
        }
    }

    // Метод для инициализации и подключения
    fun initSunmiPrinterService(context: Context) {
        try {
            InnerPrinterManager.getInstance().bindService(context, innerPrinterCallback)
        } catch (e: InnerPrinterException) {
            Log.e(TAG, "Error binding to Sunmi Printer Service", e)
            e.printStackTrace()
        }
    }

    // Метод для отключения от сервиса
    fun disconnectSunmiPrinterService(context: Context) {
        try {
            InnerPrinterManager.getInstance().unBindService(context, innerPrinterCallback)
        } catch (e: InnerPrinterException) {
            Log.e(TAG, "Error unbinding from Sunmi Printer Service", e)
            e.printStackTrace()
        }
    }

    /**
     * Печатает чек и сообщает о результате через колбэк.
     * @param onResult Лямбда, которая будет вызвана по завершении печати.
     * isSuccess = true при успехе, false при ошибке.
     */
    fun printReceipt(
        products: List<Product>,
        total: Double,
        onResult: (isSuccess: Boolean, message: String) -> Unit
    ) {
        if (sunmiPrinterService == null) {
            Log.e(TAG, "Printer service is not connected. Simulating print.")
            logPrintSimulation(products, total)
            onResult(false, "Принтер не подключен (симуляция)")
            return
        }

        val callback = object : InnerResultCallback() {
            override fun onRunResult(isSuccess: Boolean) {
                Log.d(TAG, "Print job finished. Success: $isSuccess")
                // Этот колбэк вызывается по завершении всей транзакции
            }

            override fun onReturnString(result: String?) {
                // Не используется для печати
            }

            override fun onRaiseException(code: Int, msg: String?) {
                Log.e(TAG, "Print exception. Code: $code, Message: $msg")
                onResult(false, msg ?: "Ошибка принтера (код $code)")
            }

            override fun onPrintResult(code: Int, msg: String?) {
                Log.d(TAG, "Print result callback. Code: $code, Message: $msg")
                if (code == 0) {
                    onResult(true, "Чек напечатан")
                } else {
                    onResult(false, msg ?: "Ошибка печати (код $code)")
                }
            }
        }

        sunmiPrinterService?.run {
            // Код форматирования и печати текста
            // ВАЖНО: Последняя команда печати должна использовать колбэк
            lineWrap(4, callback)
        }
    }

    private fun logPrintSimulation(products: List<Product>, total: Double) {
        Log.i(TAG, "--- СИМУЛЯЦИЯ ПЕЧАТИ -----")
        products.forEach {
            Log.i(TAG, "${it.name} - ${it.quantity} x ${"%.2f".format(it.price)}")
        }
        Log.i(TAG, "ИТОГО: ${"%.2f".format(total)}")
        Log.i(TAG, "-------------------------")
    }
}