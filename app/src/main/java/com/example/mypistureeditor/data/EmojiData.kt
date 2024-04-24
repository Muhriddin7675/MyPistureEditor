package com.example.mypistureeditor.data

import androidx.annotation.DrawableRes

data class EmojiData(
    override val id: Int,
    @DrawableRes override val res: Int,
) : Emoji

data class TextData(
    override val id: Int,
    override val res: Any
) : Emoji


interface Emoji {
    val id: Int
    val res: Any
}
