package com.breez.senmit2sapp.presentation

import com.breez.senmit2sapp.data.model.Product

/**
 * Представляет состояние основного экрана (кассира).
 * @param products Список товаров в корзине.
 * @param total Итоговая сумма.
 * @param isPrintButtonEnabled Доступность кнопки печати.
 */
data class MainUiState(
    val products: List<Product> = emptyList(),
    val total: Double = 0.0,
    val isPrintButtonEnabled: Boolean = false
)

/**
 * Представляет состояние второго экрана (клиента).
 */
sealed class SecondScreenState {
    /**
     * Состояние бездействия, должен отображаться видеоролик.
     */
    object Idle : SecondScreenState()

    /**
     * Активное состояние, отображается список покупок.
     * @param products Список товаров.
     * @param total Итоговая сумма.
     */
    data class Active(val products: List<Product>, val total: Double) : SecondScreenState()
}