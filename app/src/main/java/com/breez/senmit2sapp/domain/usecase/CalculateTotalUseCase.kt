package com.breez.senmit2sapp.domain.usecase

import com.breez.senmit2sapp.data.model.Product
import javax.inject.Inject

/**
 * Use case для вычисления итоговой стоимости товаров.
 */
class CalculateTotalUseCase @Inject constructor() {
    operator fun invoke(products: List<Product>): Double {
        return products.sumOf { it.price * it.quantity }
    }
}