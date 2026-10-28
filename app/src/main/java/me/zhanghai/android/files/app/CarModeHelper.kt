/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 *
 * Car adaptation: force fullscreen helper for the car machine.
 */

package me.zhanghai.android.files.app

import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat

/**
 * 车机模式辅助：当「车机模式 + 强制全屏」开启时隐藏状态栏与导航栏。
 *
 * 使用 [WindowInsetsControllerCompat] 兼容 Android 5.0+，滑动唤出系统栏后会短暂显示并自动
 * 重新隐藏（sticky 沉浸式）；同时监听系统 UI 可见性变化，防止部分车机重新唤起系统栏。
 */
class CarModeHelper(private val activity: AppActivity) {

    private var forceFullscreen = false

    private val insetsController: WindowInsetsControllerCompat by lazy {
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    init {
        Settings.CAR_MODE.observe(activity) { update() }
        Settings.CAR_FORCE_FULLSCREEN.observe(activity) { update() }
        activity.window.decorView.setOnSystemUiVisibilityChangeListener { updateIfNeeded() }
    }

    private fun update() {
        forceFullscreen =
            Settings.CAR_MODE.valueCompat && Settings.CAR_FORCE_FULLSCREEN.valueCompat
        updateIfNeeded()
    }

    private fun updateIfNeeded() {
        if (forceFullscreen) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
