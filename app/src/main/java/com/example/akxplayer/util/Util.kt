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
import com.example.akxplayer.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.FileNotFoundException

class Util {
    companion object {
        fun fetchDuration(duration: String): String {
            var fetchedDuration = ""
            var unit = duration.toInt() / 1000 // convert from milli to sec

            for (i in 0..1) {
                val numOfUnit = unit / 60
                if (numOfUnit == 0) //has no min or hour or both
                    break

                val time = unit % 60
                fetchedDuration = if (time < 10)
                    ":0$time$fetchedDuration"
                else
                    ":$time$fetchedDuration"
                unit /= 60
            }
            return "$unit$fetchedDuration"
        }

        fun getPic(albumId: Long, contentResolver: ContentResolver): Bitmap? {
            val uriAlbumArt = Uri.parse("content://media/external/audio/albumart");
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
            if(albumId == null || error == null) return
            val uriAlbumArt = Uri.parse("content://media/external/audio/albumart");
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