package com.breez.senmit2sapp.ui

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.breez.senmit2sapp.data.repository.PrinterRepository
import com.breez.senmit2sapp.databinding.ActivityMainBinding
import com.breez.senmit2sapp.presentation.MainViewModel
import com.breez.senmit2sapp.presentation.SecondScreenState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var productAdapter: ProductAdapter
    private var secondScreenDisplay: SecondScreenDisplay? = null

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var printerRepository: PrinterRepository

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализируем принтер при старте Activity
        printerRepository.init(this)

        setupUI()
        setupSecondScreen()
        observeViewModel()
    }

    private fun setupUI() {
        productAdapter = ProductAdapter()
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = productAdapter

        binding.btnAddProduct.setOnClickListener {
            viewModel.addProduct(
                name = binding.etProductName.text.toString(),
                priceStr = binding.etProductPrice.text.toString(),
                quantityStr = binding.etProductQuantity.text.toString()
            )
        }

        binding.btnPrintReceipt.setOnClickListener {
            viewModel.printReceipt()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.mainUiState.collect { state ->
                // Обновляем список товаров
                productAdapter.submitList(state.products)
                // Обновляем итоговую сумму
                binding.tvTotal.text = "Итого: ${"%.2f".format(state.total)}"
                // Обновляем доступность кнопки печати
                binding.btnPrintReceipt.isEnabled = state.isPrintButtonEnabled

                // Очищаем поля после успешного добавления (если список изменился)
                if (productAdapter.currentList.size != state.products.size) {
                    clearInputFields()
                }
            }
        }

        // Подписывваемся на поток состояний второго экрана
        lifecycleScope.launch {
            viewModel.secondScreenState.collect { state ->
                when (state) {
                    is SecondScreenState.Active -> secondScreenDisplay?.showShoppingList(state.products, state.total)
                    is SecondScreenState.Idle -> secondScreenDisplay?.showVideo()
                }
            }
        }


        // Запускаем сбор событий-сообщений
        lifecycleScope.launch {
            viewModel.userMessage.collect { message ->
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun clearInputFields() {
        binding.etProductName.text.clear()
        binding.etProductPrice.text.clear()
        binding.etProductQuantity.text.clear()
    }


    private fun setupSecondScreen() {
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displays = displayManager.displays

        if (displays.size > 1) {
            // Передаем лямбду, которая ввызывает метод ViewModel
            secondScreenDisplay = SecondScreenDisplay(this, displays[1]) { errorMessage ->
                viewModel.onVideoError(errorMessage)
            }
            secondScreenDisplay?.show()
            Log.d(TAG, "Второй экран подключен.")
        } else {
            Log.d(TAG, "Второй экран не найден.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Отключаем принтер при уничтожении Activity
        printerRepository.disconnect(this)
        secondScreenDisplay?.dismiss()
    }
}