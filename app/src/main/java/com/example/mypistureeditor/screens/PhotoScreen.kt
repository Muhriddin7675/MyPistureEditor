package com.example.mypistureeditor.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.mypistureeditor.MainActivity
import com.example.mypistureeditor.R
import com.example.mypistureeditor.databinding.ScreenTakePhotoBinding
import com.example.mypistureeditor.utils.logger
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class PhotoScreen : Fragment(R.layout.screen_take_photo) {
    private var _binding: ScreenTakePhotoBinding? = null
    private val binding get() = _binding!!
    private var imageCapture: ImageCapture? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ScreenTakePhotoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var imageCaptureProvider: (() -> ImageCapture)? = null

        PermissionX.init(this)
            .permissions(Manifest.permission.CAMERA)
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    setupCamera(
                        viewLifecycleOwner,
                        requireContext(),
                        binding.cameraPreview
                    ) {
                        imageCaptureProvider = it
                    }

                    binding.photoButton.setOnClickListener {
                        lifecycleScope.launch {
                            it.isEnabled = false
                            delay(2000)
                            it.isEnabled = true
                        }
                        imageCaptureProvider?.let { takeImage2(it) }
                    }
                }
            }
    }

    private fun setupCamera(
        lifecycleOwner: LifecycleOwner,
        context: Context,
        previewView: PreviewView,
        captureProvider: (() -> ImageCapture) -> Unit
    ) {
        val cameraProcessFeature = ProcessCameraProvider.getInstance(context)
        cameraProcessFeature.addListener({
            val processCameraProvider = cameraProcessFeature.get()

            val preview = Preview.Builder()
                .build()

            val imageCapture = ImageCapture.Builder()
                .build()

            this.imageCapture = imageCapture

            captureProvider {
                imageCapture
            }

            preview.setSurfaceProvider(previewView.surfaceProvider)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                processCameraProvider.unbindAll()
                processCameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

//                camera.cameraControl.enableTorch(true)
            } catch (e: Throwable) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(context))
    }

    @SuppressLint("SimpleDateFormat")
    private fun takeImage2(imageCaptureProvider: () -> ImageCapture) {
        val imageCapture = imageCaptureProvider()
        logger("TAKE IMAGE 2")
        val date =
            SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(System.currentTimeMillis())
        val name = "$date.jpg"

        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/CameraX-Image"
            )
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                (requireActivity() as MainActivity).contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                    lifecycleScope.launch {
                        val uri = outputFileResults.savedUri
                        if (uri != null) {
                            parentFragmentManager.beginTransaction()
                                .addToBackStack(PhotoScreen::class.java.toString())
                                .replace(R.id.container, EditScreen::class.java, bundleOf(Pair("URI", uri.toString())))
                                .commit()
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    logger(exception.message.toString())
                }
            }
        )
    }
}