package com.example.akxplayer.ui.activities

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.akxplayer.R
import com.example.akxplayer.databinding.ActivityLauncherBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val settings = getSharedPreferences("AppSettingsPref", 0)
//        when (settings.getInt("theme", 0)) {
//            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
//            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val binding = DataBindingUtil.setContentView<ActivityLauncherBinding>(
            this,
            R.layout.activity_launcher
        )
        var counter = 1
        Completable.create {
            while (counter > 0) {
                counter--
                Thread.sleep(1000)
            }
            it.onComplete()
        }.subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                counter = 1
            }

        val objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
            binding.imageLogo,
            PropertyValuesHolder.ofFloat("scaleX", 1.2f),
            PropertyValuesHolder.ofFloat("scaleY", 1.2f)
        )
        objectAnimator.apply {
            duration = 500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
    }
}
