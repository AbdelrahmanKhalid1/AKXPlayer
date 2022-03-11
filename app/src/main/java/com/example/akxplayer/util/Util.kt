package com.example.akxplayer.util

import android.content.ContentResolver
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class Util {
    companion object {
        fun fetchDuration(duration: Int): String {
            var fetchedDuration = ""
            var unit = duration / 1000 // convert from milli to sec
            if (unit == 0)
                return "0:00"
            while (unit > 0) {
                val time = unit % 60
                fetchedDuration = if (time < 10)
                    ":0$time$fetchedDuration"
                else
                    ":$time$fetchedDuration"
                unit /= 60
            }
            fetchedDuration = fetchedDuration.substring(1, fetchedDuration.length)
            return if (fetchedDuration.split(':').size == 1) "00:$fetchedDuration" else fetchedDuration
        }

        fun getPic(albumId: Long, contentResolver: ContentResolver): Bitmap? {
            val uriAlbumArt = Uri.parse("content://media/external/audio/albumart")
            val uriCurrentAlbum = ContentUris.withAppendedId(uriAlbumArt, albumId)
            try {
                val pfd = contentResolver.openFileDescriptor(uriCurrentAlbum, "r")
                if (pfd != null) {
                    val fd = pfd.fileDescriptor
                    val options = BitmapFactory.Options()
                    return BitmapFactory.decodeFileDescriptor(
                        fd, null,
                        options
                    )
                }
            } catch (ignore: Exception) {
            }
            return null
        }

        @BindingAdapter("albumId", "error", requireAll = true)
        @JvmStatic
        fun setImage(imageView: ImageView, albumId: Long?, error: Drawable?) {
            if (albumId == null || error == null) return
            val uriAlbumArt = Uri.parse("content://media/external/audio/albumart")
            val uriCurrentAlbum = ContentUris.withAppendedId(uriAlbumArt, albumId)
            Glide.with(imageView)
                .load(uriCurrentAlbum)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(400))
                .error(error)
                .into(imageView)
        }
    }
}
