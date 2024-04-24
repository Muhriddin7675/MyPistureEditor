package com.example.mypistureeditor.screens

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.mypistureeditor.R
import com.example.mypistureeditor.databinding.ScreenEditBinding


import com.example.mypistureeditor.data.Emoji
import com.example.mypistureeditor.data.EmojiData

import com.example.mypistureeditor.utils.distance
import com.example.mypistureeditor.utils.setOnSingleClickListener
import com.example.mypistureeditor.utils.showSoftKeyboard
import java.text.SimpleDateFormat
import kotlin.math.PI
import kotlin.math.atan

class EditScreen() : Fragment(R.layout.screen_edit), Parcelable {

    private var _binding: ScreenEditBinding? = null
    private val binding get() = _binding!!
    private var emojiID = 0
    private val drawableList = listOf(
        R.drawable.emoje_1,
        R.drawable.emoje_2,
        R.drawable.emoje_3,
        R.drawable.emoje_4,
        R.drawable.emoje_5,
        R.drawable.emoje_6,
        R.drawable.emoje_7,
        R.drawable.emoje_8,
        R.drawable.emoje_9,
        R.drawable.emoje_10,
        R.drawable.emoje_11,
        R.drawable.image_app,
    )
    private var selectedEmoji: Emoji = EmojiData(emojiID, R.drawable.swimming_glasses)
    private var emojis = arrayListOf<ViewGroup>()
    private var isGray = false

    constructor(parcel: Parcel) : this() {
        emojiID = parcel.readInt()
        isGray = parcel.readByte() != 0.toByte()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(requireContext())
                    .setTitle("Reminder")
                    .setMessage("If you go back, changes will not be saved . Do you want to exit?")
                    .setNegativeButton("Cancel"){dialog,_,->
                        dialog.dismiss()

                    }
                    .setPositiveButton("Exit") { dialog, _ ->
                        dialog.dismiss()
                        parentFragmentManager.popBackStack()
                    }.show()
            }
        })

        _binding = ScreenEditBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility", "NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uriString = arguments?.getString("URI")

        if (uriString != null) {
            binding.image.setImageURI(Uri.parse(uriString))
        }

        binding.backIcon.setOnSingleClickListener{
            AlertDialog.Builder(requireContext())
                .setMessage("If you go back, changes will not be saved. Do you want to exit ?")
                .setPositiveButton("Exit") { dialog, _ ->
                    parentFragmentManager.popBackStack()
                    dialog.dismiss()
                }.show()
        }

        (binding.bottomContainer[0] as ViewGroup).forEachIndexed { index, image ->
            if (index == 0) (image as ImageView).isSelected = true

            (image as ImageView).setOnClickListener {
                clearSelectedBottomItems()
                image.isSelected = true
                selectedEmoji = EmojiData(++emojiID, drawableList[index])
            }
        }

        binding.imgContainer.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) addEmoji(event.x, event.y)
            return@setOnTouchListener true
        }

        binding.btnSave.setOnClickListener {
            binding.btnSave.isVisible = false
            clearSelectedState()

            val bitmap = catchBitmapFromView(binding.imgContainer)
            saveBitmapToExternalStorage(bitmap)

            binding.btnSave.isVisible = true
            Toast.makeText(requireContext(), "Image saved to gallery successfully", Toast.LENGTH_SHORT).show()
        }

        binding.gray.setOnClickListener {
            if (isGray) {
                antiMakeGray(binding.image)
                binding.gray.text = "Make gray"
            } else {
                makeGray(binding.image)
                binding.gray.text = "Original"
            }

            isGray = !isGray
        }

        binding.addText.setOnClickListener {
            addText(binding.root.x, binding.root.y)
        }
    }

    private fun makeGray(view: ImageView) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        val filter = ColorMatrixColorFilter(matrix)
        view.colorFilter = filter
    }

    private fun antiMakeGray(view: ImageView) {
        view.colorFilter = null
    }

    private fun addEmoji(x: Float, y: Float) {
        clearSelectedState()
        val emojiContainer =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.emoji_container, binding.imgContainer, false) as ViewGroup

        (emojiContainer[1] as ImageView).setImageResource(selectedEmoji.res as Int)
        emojiContainer[0].isSelected = true
        emojiContainer[2].setOnClickListener {
            emojis.remove(emojiContainer)
            binding.imgContainer.removeView(emojiContainer)
        }

        emojiContainer.x = x
        emojiContainer.y = y

        binding.imgContainer.addView(emojiContainer)
        emojis.add(emojiContainer)
        attachTouchEvents(emojiContainer)
    }

    private fun addText(x: Float, y: Float) {
        clearSelectedState()
        val emojiContainer =
            LayoutInflater.from(requireContext())
                .inflate(R.layout.text_container, binding.imgContainer, false) as ViewGroup

        (emojiContainer[1] as EditText).setText("")

        emojiContainer[0].isSelected = true
        emojiContainer[2].setOnClickListener {
            emojis.remove(emojiContainer)
            binding.imgContainer.removeView(emojiContainer)
        }





        emojiContainer.x = x
        emojiContainer.y = y

        val editText = (emojiContainer[1] as EditText)
        editText.showSoftKeyboard()

        binding.imgContainer.addView(emojiContainer)
        emojis.add(emojiContainer)
        attachTouchEvents(emojiContainer)
    }


    private fun clearSelectedState() {
        emojis.forEach {
            it[0].isSelected = false
            it[2].isVisible = false
        }

        emojis.forEach {
            if (it[1] is EditText) {
                (it[1] as EditText).clearFocus()
            }
        }
    }

    private fun clearSelectedBottomItems() {
        (binding.bottomContainer[0] as ViewGroup).forEach {
            it.isSelected = false
            // TODO -> it.isEnabled = false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attachTouchEvents(viewGroup: ViewGroup) {
        var initTouchX = 0f
        var initTouchY = 0f
        var isInitState = false
        var initDistance = 0f
        var initAngle = 0f

        viewGroup.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    clearSelectedState()
                    viewGroup[0].isSelected = true
                    viewGroup[2].visibility = View.VISIBLE

                    initTouchX = event.x
                    initTouchY = event.y
                }

                MotionEvent.ACTION_MOVE -> {
                    clearSelectedState()
                    viewGroup[0].isSelected = true
                    viewGroup[2].visibility = View.VISIBLE

                    if (event.pointerCount == 1) {
                        viewGroup.x += event.x - initTouchX
                        viewGroup.y += event.y - initTouchY
                    }

                    if (event.pointerCount == 2) {
                        if (!isInitState) {
                            val firstPointF = PointF(event.getX(0), event.getY(0))
                            val secondPointF = PointF(event.getX(1), event.getY(1))

                            initDistance = firstPointF distance secondPointF
                            isInitState = true
                            initAngle = atan((firstPointF.y - secondPointF.y) / (firstPointF.x - secondPointF.x))
                        }

                        val newFirstPointF = PointF(event.getX(0), event.getY(0))
                        val newSecondPointF = PointF(event.getX(1), event.getY(1))

                        val newDistance = newFirstPointF distance newSecondPointF

                        val scale = newDistance / initDistance
                        if (!scale.isNaN()) {
                            viewGroup.scaleX *= scale
                            viewGroup.scaleY *= scale
                        }

                        // rotation
                        val newAngle =
                            atan((newFirstPointF.y - newSecondPointF.y) / (newFirstPointF.x - newSecondPointF.x))
                        viewGroup.rotation += ((newAngle - initAngle) * 180 / PI).toFloat()

                    } else isInitState = false
                }

                MotionEvent.ACTION_UP -> {

                }
            }
            return@setOnTouchListener true
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SimpleDateFormat")
    private fun saveBitmapToExternalStorage(bitmap: Bitmap) {
        val name = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())
        val displayName = "$name.jpg"

        val resolver = requireContext().contentResolver
        val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
        }

        val imageUri = resolver.insert(imageCollection, imageDetails)

        try {
            imageUri?.let { uri ->
                val outputStream = resolver.openOutputStream(uri)
                outputStream?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Logging the image details
        imageUri?.let { uri ->

        }
    }

    fun catchBitmapFromView(view: View): Bitmap {
        view.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        view.isDrawingCacheEnabled = false
        return bitmap
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(emojiID)
        parcel.writeByte(if (isGray) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EditScreen> {
        override fun createFromParcel(parcel: Parcel): EditScreen {
            return EditScreen(parcel)
        }

        override fun newArray(size: Int): Array<EditScreen?> {
            return arrayOfNulls(size)
        }
    }

}
