package com.breez.senmit2sapp.ui

import android.app.Presentation
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.Display
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.breez.senmit2sapp.R
import com.breez.senmit2sapp.data.model.Product
import com.breez.senmit2sapp.databinding.SecondScreenLayoutBinding

class SecondScreenDisplay(
    context: Context,
    display: Display,
    private val onError: (String) -> Unit
) : Presentation(context, display) {

    private lateinit var binding: SecondScreenLayoutBinding
    private lateinit var productAdapter: ProductAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SecondScreenLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка RecyclerView
        productAdapter = ProductAdapter()
        binding.rvCustomerProducts.layoutManager  = LinearLayoutManager(context)
        binding.rvCustomerProducts.adapter = productAdapter


        setupVideo()
    }

    private fun setupVideo() {
        val videoPath = "android.resource://${context.packageName}/${R.raw.sunmit2s}"
//        val videoPath = "android.resource://${context.packageName}"
        binding.videoView.setVideoURI(Uri.parse(videoPath))
        binding.videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
        }
        // Устанавливаем слушатель ошибок
        binding.videoView.setOnErrorListener { _, what, extra ->
            val errorMsg = when (what) {
                // Обработка разных кодов ошибок
                else -> "Неизвестная ошибка (what=$what, extra=$extra)"
            }
            // Вызываем внешний колбэк
            onError(errorMsg)
            true // Возввращаем true, чтобы остановить дальнейшую обработку
        }
    }


    //Обновляет UI для отображения списка покупок.
    fun showShoppingList(products: List<Product>, total: Double) {
        productAdapter.submitList(products)
        binding.tvCustomerTotal.text = "Итого: ${"%.2f".format(total)}"

        binding.videoView.visibility = View.GONE
        binding.shoppingListLayout.visibility = View.VISIBLE
    }

    // Обновляет UI для отображения видео в режиме ожидания.
    fun showVideo() {
        binding.shoppingListLayout.visibility = View.GONE
        binding.videoView.visibility = View.VISIBLE
        if (!binding.videoView.isPlaying) {
            binding.videoView.start()
        }
    }
}