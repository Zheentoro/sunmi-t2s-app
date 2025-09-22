package com.breez.senmit2sapp.data.repository

import com.breez.senmit2sapp.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Репозиторий для управления списком товаров.
 */
class ProductRepository {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    fun addProduct(product: Product) {
        _products.update { currentList -> currentList + product }
    }

    fun clearProducts() {
        _products.value = emptyList()
    }
}