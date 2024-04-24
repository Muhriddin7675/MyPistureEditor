package com.example.mypistureeditor

import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.example.mypistureeditor.databinding.ActivityMainBinding

import com.example.mypistureeditor.screens.EditScreen
import com.example.mypistureeditor.screens.PhotoScreen
import com.example.mypistureeditor.utils.setOnSingleClickListener

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                supportFragmentManager.beginTransaction()
                    .addToBackStack(EditScreen::class.java.toString())
                    .replace(R.id.container, EditScreen::class.java, bundleOf(Pair("URI", uri.toString())))
                    .commit()
            }
        }

        binding.openCamera.setOnSingleClickListener {
            supportFragmentManager.beginTransaction()
                .addToBackStack(MainActivity::class.java.toString())
                .replace(R.id.container, PhotoScreen::class.java, bundleOf())
                .commit()
        }

        binding.openGallery.setOnSingleClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}

