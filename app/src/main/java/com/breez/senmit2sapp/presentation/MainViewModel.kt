package com.breez.senmit2sapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breez.senmit2sapp.data.model.Product
import com.breez.senmit2sapp.data.repository.PrintResult
import com.breez.senmit2sapp.data.repository.PrinterRepository
import com.breez.senmit2sapp.data.repository.ProductRepository
import com.breez.senmit2sapp.domain.usecase.CalculateTotalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val IDLE_DELAY_MS = 15000L // 15 секунд

@HiltViewModel
class MainViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val printerRepository: PrinterRepository,
    private val calculateTotalUseCase: CalculateTotalUseCase
) : ViewModel() {

    private val _mainUiState = MutableStateFlow(MainUiState())
    val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()

    private val _secondScreenState = MutableStateFlow<SecondScreenState>(SecondScreenState.Idle)
    val secondScreenState: StateFlow<SecondScreenState> = _secondScreenState.asStateFlow()

    private var idleTimerJob: Job? = null

    // Канал для отправки событий в UI
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    init {
        // Подписывваемся на изменения в репозитории товаров
        viewModelScope.launch {
            productRepository.products.collect { products ->
                val total = calculateTotalUseCase(products)
                _mainUiState.value = MainUiState(
                    products = products,
                    total = total,
                    isPrintButtonEnabled = products.isNotEmpty()
                )

                // Обновляем состояние второго экрана, если есть товары
                if (products.isNotEmpty()) {
                    _secondScreenState.value = SecondScreenState.Active(products, total)
                }
            }
        }
        // Запускаем таймер при инициализации
        resetIdleTimer()
    }

    /**
     * Добавляет товар в корзину.
     */
    fun addProduct(name: String, priceStr: String, quantityStr: String) {
        val price = priceStr.toDoubleOrNull()
        val quantity = quantityStr.toIntOrNull()

        if (name.isNotBlank() && price != null && quantity != null) {
            val product = Product(name, price, quantity)
            productRepository.addProduct(product)
            resetIdleTimer()
        }
    }

    /**
     * Печатает чек.
     */
    fun printReceipt() {
        viewModelScope.launch {
            val currentState = mainUiState.value
            if (currentState.products.isNotEmpty()) {
                when (val result = printerRepository.printReceipt(currentState.products, currentState.total)) {
                    is PrintResult.Success -> {
                        // Уведомляем об успехе
                         _userMessage.emit("Чек успешно напечатан")

                        // Очищаем корзину после успешной печати
                        productRepository.clearProducts()
                    }
                    is PrintResult.Error -> {
                        // Отправляем сообщение об ошибке
                        _userMessage.emit(result.message)

                        // Очищаем корзину
                        productRepository.clearProducts()
                    }
                }
                resetIdleTimer()
            }
        }
    }

    /**
     * Обрабатывает ошибку загрузки видео со второго экрана.
     */
    fun onVideoError(errorMessage: String) {
        viewModelScope.launch {
            _userMessage.emit("Ошибка видео: $errorMessage")
        }
    }

    /**
     * Сбрасывает таймер бездействия.
     * При каждом действии кассира таймер перезапускается.
     */
    private fun resetIdleTimer() {
        idleTimerJob?.cancel()
        idleTimerJob = viewModelScope.launch {
            delay(IDLE_DELAY_MS)
            // Если за 15 секунд не было действий, и корзина пуста, переходим в режим Idle
            if (_mainUiState.value.products.isEmpty()) {
                _secondScreenState.value = SecondScreenState.Idle
            }
        }
    }
}