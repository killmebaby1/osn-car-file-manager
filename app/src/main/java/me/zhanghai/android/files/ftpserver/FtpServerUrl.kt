/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.RuntimeBroadcastReceiver
import me.zhanghai.android.files.util.getLocalAddresses
import me.zhanghai.android.files.util.valueCompat
import java.net.InetAddress

object FtpServerUrl {
    private fun getUrlForAddress(localAddress: InetAddress, username: String?): String? {
        val host = localAddress.hostAddress ?: return null
        val port = Settings.FTP_SERVER_PORT.valueCompat
        return "ftp://${if (username != null) "$username@" else ""}$host:$port/"
    }

    /**
     * 返回所有网卡对应的 FTP 地址。车机热点(WLAN AP)与蜂窝/WLAN 客户端可能不在同一网段，
     * 全部列出，用户选择与手机同网段的一个即可访问。
     */
    fun getUrls(): List<String> {
        val username = if (!Settings.FTP_SERVER_ANONYMOUS_LOGIN.valueCompat) {
            Settings.FTP_SERVER_USERNAME.valueCompat
        } else {
            null
        }
        return InetAddress::class.getLocalAddresses()
            .mapNotNull { getUrlForAddress(it, username) }
    }

    fun getUrl(): String? = getUrls().firstOrNull()

    fun createChangeReceiver(context: Context, onChange: () -> Unit): RuntimeBroadcastReceiver =
        RuntimeBroadcastReceiver(
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION), object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    onChange()
                }
            }, context
        )
}
