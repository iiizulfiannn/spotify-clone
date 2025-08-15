package com.luckyfriday.spotifycloneapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

fun AppCompatImageView.loadImage(context: Context, url: String) {
    Glide.with(context).load(url).into(this)
}

fun loadImageNotification(context: Context, url: String, onImageReady: (Bitmap) -> Unit) {
    Glide.with(context).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
        override fun onResourceReady(
            resource: Bitmap,
            transition: Transition<in Bitmap>?
        ) {
            onImageReady(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {}

    })
}