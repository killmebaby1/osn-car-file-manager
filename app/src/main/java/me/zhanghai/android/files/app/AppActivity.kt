/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.theme.custom.CustomThemeHelper
import me.zhanghai.android.files.theme.night.NightModeHelper

abstract class AppActivity : AppCompatActivity() {
    private var isDelegateCreated = false

    override fun getDelegate(): AppCompatDelegate {
        val delegate = super.getDelegate()

        if (!isDelegateCreated) {
            isDelegateCreated = true
            NightModeHelper.apply(this)
        }
        return delegate
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(applyCarFontScale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        CustomThemeHelper.apply(this)

        super.onCreate(savedInstanceState)

        // 车机模式：强制全屏等
        CarModeHelper(this)

        // 字体大小或车机模式变化导致字体比例变化时，重建以应用新比例
        Settings.CAR_MODE.observe(this) { recreateIfFontScaleChanged() }
        Settings.CAR_FONT_SCALE.observe(this) { recreateIfFontScaleChanged() }
    }

    private fun recreateIfFontScaleChanged() {
        if (resources.configuration.fontScale != getCarFontScale()) {
            recreate()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!super.onSupportNavigateUp()) {
            finish()
        }
        return true
    }

    companion object {
        /** 车机模式开启时返回用户设置的字体比例（100~200 / 100），否则 1.0 */
        fun getCarFontScale(): Float =
            try {
                if (Settings.CAR_MODE.valueCompat) {
                    Settings.CAR_FONT_SCALE.valueCompat / 100f
                } else {
                    1.0f
                }
            } catch (e: Exception) {
                1.0f
            }

        /** 用指定字体比例包装 Context（fontScale 会放大所有 sp 文字） */
        fun applyCarFontScale(base: Context): Context {
            val fontScale = getCarFontScale()
            if (fontScale == 1.0f) {
                return base
            }
            val configuration = Configuration(base.resources.configuration)
            configuration.fontScale = fontScale
            return base.createConfigurationContext(configuration)
        }
    }
}
